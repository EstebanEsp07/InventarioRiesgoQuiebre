package com.restaurante.quiebre.recomendaciones;

import com.restaurante.quiebre.auditoria.AuditoriaService;
import com.restaurante.quiebre.config.AppProperties;
import com.restaurante.quiebre.inventario.ExistenciaVista;
import com.restaurante.quiebre.inventario.InventarioRepository;
import com.restaurante.quiebre.pronostico.Demanda;
import com.restaurante.quiebre.pronostico.PronosticoService;
import com.restaurante.quiebre.recomendaciones.RiesgoCalculator.Nivel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class RecomendacionService {
    private static final Logger log = LoggerFactory.getLogger(RecomendacionService.class);

    private final InventarioRepository inventario;
    private final PronosticoService pronostico;
    private final RecomendacionRepository repo;
    private final AuditoriaService auditoria;
    private final AppProperties props;

    public RecomendacionService(InventarioRepository inventario, PronosticoService pronostico,
                                RecomendacionRepository repo, AuditoriaService auditoria, AppProperties props) {
        this.inventario = inventario;
        this.pronostico = pronostico;
        this.repo = repo;
        this.auditoria = auditoria;
        this.props = props;
    }

    /** Evalúa todas las existencias y genera recomendaciones PENDIENTES. Devuelve cuántas creó. */
    public int evaluar() {
        int umbral = props.riesgo().umbralDias();
        double objetivoDias = umbral * 2.0;
        List<ExistenciaVista> todas = inventario.listarExistencias();

        Map<String, Demanda> demandas = new HashMap<>();
        for (var e : todas) {
            demandas.put(clave(e), pronostico.estimar(e.bodegaId(), e.productoId()));
        }

        int creadas = 0;
        for (var e : todas) {
            Demanda d = demandas.get(clave(e));
            double dias = RiesgoCalculator.diasCobertura(e.cantidad(), d.diaria());
            Nivel nivel = RiesgoCalculator.nivel(dias, umbral);
            if (nivel == Nivel.BAJO || repo.existePendiente(e.bodegaId(), e.productoId())) continue;

            double faltante = Math.ceil(d.diaria() * objetivoDias - e.cantidad());
            if (faltante <= 0) continue;

            // Mejor origen: la otra bodega con más excedente sobre su propia necesidad
            ExistenciaVista origen = null;
            double mejorExcedente = 0;
            for (var o : todas) {
                if (o.productoId() != e.productoId() || o.bodegaId() == e.bodegaId()) continue;
                double excedente = o.cantidad() - demandas.get(clave(o)).diaria() * objetivoDias;
                if (excedente > mejorExcedente) {
                    mejorExcedente = excedente;
                    origen = o;
                }
            }

            boolean transferir = origen != null && mejorExcedente >= faltante;
            boolean degradada = d.degradada() || (transferir && demandas.get(clave(origen)).degradada());
            var nueva = new NuevaRecomendacion(
                    transferir ? "TRANSFERENCIA" : "COMPRA",
                    e.productoId(), e.producto(), e.bodegaId(), e.bodega(),
                    transferir ? origen.bodegaId() : null, transferir ? origen.bodega() : null,
                    faltante, Math.round(dias * 100.0) / 100.0, nivel.name(), degradada);
            try {
                long id = repo.crear(nueva);
                auditoria.registrar("sistema", "RECOMENDACION_CREADA", "recomendacion", id,
                        "%s de %.0f %s de %s, cobertura %.1f días, modelo %s".formatted(
                                nueva.tipo(), faltante, e.unidad(), e.producto(), dias, d.modelo()));
                creadas++;
            } catch (DuplicateKeyException ignorada) {
                log.debug("Ya existe una recomendación pendiente para {}", clave(e));
            }
        }
        return creadas;
    }

    public int expirarVencidas() {
        int n = repo.expirarVencidas(props.vigencia().expiraHoras());
        if (n > 0) auditoria.registrar("sistema", "RECOMENDACIONES_EXPIRADAS", "recomendacion", "-", n + " expiradas");
        return n;
    }

    private static String clave(ExistenciaVista e) {
        return e.bodegaId() + ":" + e.productoId();
    }
}
