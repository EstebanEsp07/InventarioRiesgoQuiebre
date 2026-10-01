package com.restaurante.quiebre.integracion;

import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class OutboxRepository {
    private final JdbcClient jdbc;

    public OutboxRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void insertar(String tipo, String payload) {
        jdbc.sql("INSERT INTO integracion.outbox (tipo, payload) VALUES (:t, :p)")
                .param("t", tipo).param("p", payload).update();
    }

    public List<OutboxMensaje> pendientes(int limite) {
        return jdbc.sql("SELECT id, tipo, payload FROM integracion.outbox WHERE publicado_en IS NULL ORDER BY id LIMIT :n")
                .param("n", limite).query(OutboxMensaje.class).list();
    }

    public void marcarPublicado(long id) {
        jdbc.sql("UPDATE integracion.outbox SET publicado_en = now() WHERE id = :id").param("id", id).update();
    }
}
