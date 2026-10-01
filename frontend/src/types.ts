export interface Existencia {
  bodegaId: number
  bodega: string
  productoId: number
  sku: string
  producto: string
  unidad: string
  cantidad: number
  leadTimeDias: number
}

export type Estado = 'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'EXPIRADA' | 'EJECUTADA'

export interface Recomendacion {
  id: number
  tipo: 'TRANSFERENCIA' | 'COMPRA'
  productoId: number
  productoNombre: string
  bodegaDestinoId: number
  bodegaDestinoNombre: string
  bodegaOrigenId: number | null
  bodegaOrigenNombre: string | null
  cantidad: number
  diasCobertura: number
  riesgo: 'ALTO' | 'MEDIO'
  estimacionDegradada: boolean
  estado: Estado
  creadaEn: string
  decididaEn: string | null
  decididaPor: string | null
  motivo: string | null
  ordenExternaId: string | null
}

export interface FilaConteo {
  producto: string
  bodegas: number
}

export interface Metricas {
  quiebresPorProducto: FilaConteo[]
  riesgoAltoPendientePorProducto: FilaConteo[]
  tiempoMedioAprobacionMinutos: number
  comprasUrgentes: number
  recomendacionesAceptadas: number
  recomendacionesDecididas: number
  tasaAceptacion: number
  recomendacionesPendientes: number
}

export interface EventoAuditoria {
  id: number
  ocurridoEn: string
  actor: string
  accion: string
  entidad: string
  entidadId: string
  detalle: string | null
}
