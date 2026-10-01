package com.restaurante.quiebre.auditoria;

import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {
    private final JdbcClient jdbc;

    public AuditoriaService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void registrar(String actor, String accion, String entidad, Object entidadId, String detalle) {
        jdbc.sql("""
                INSERT INTO auditoria.evento (actor, accion, entidad, entidad_id, detalle)
                VALUES (:actor, :accion, :entidad, :entidadId, :detalle)""")
                .param("actor", actor).param("accion", accion).param("entidad", entidad)
                .param("entidadId", String.valueOf(entidadId)).param("detalle", detalle)
                .update();
    }

    public List<EventoAuditoria> ultimos(int limite) {
        return jdbc.sql("SELECT * FROM auditoria.evento ORDER BY id DESC LIMIT :n")
                .param("n", limite).query(EventoAuditoria.class).list();
    }
}
