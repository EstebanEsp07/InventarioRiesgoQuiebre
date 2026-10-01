package com.restaurante.quiebre.inventario;

public record ExistenciaVista(long bodegaId, String bodega, long productoId, String sku,
                              String producto, String unidad, double cantidad, int leadTimeDias) {}
