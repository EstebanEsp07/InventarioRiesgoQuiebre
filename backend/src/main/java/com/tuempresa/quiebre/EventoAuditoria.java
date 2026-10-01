package com.restaurante.quiebre.auditoria;

import java.time.OffsetDateTime;

public record EventoAuditoria(long id, OffsetDateTime ocurridoEn, String actor, String accion,
                              String entidad, String entidadId, String detalle) {}
