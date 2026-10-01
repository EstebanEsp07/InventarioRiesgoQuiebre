import type { EventoAuditoria, Existencia, Metricas, Recomendacion } from './types'

const BASE = import.meta.env.VITE_API_URL ?? '/api'

async function req<T>(path: string, init?: RequestInit): Promise<T> {
  const r = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  })
  if (!r.ok) {
    let mensaje = `${r.status} ${r.statusText}`
    try {
      const body = await r.json()
      mensaje = body.mensaje ?? body.message ?? mensaje
    } catch {
      /* cuerpo no JSON */
    }
    throw new Error(mensaje)
  }
  return r.json() as Promise<T>
}

export const api = {
  existencias: () => req<Existencia[]>('/existencias'),
  recomendaciones: (estado?: string) =>
    req<Recomendacion[]>('/recomendaciones' + (estado ? `?estado=${estado}` : '')),
  evaluar: () => req<{ creadas: number }>('/recomendaciones/evaluar', { method: 'POST' }),
  decidir: (id: number, accion: 'aprobar' | 'rechazar', usuario: string, motivo?: string) =>
    req<Recomendacion>(`/recomendaciones/${id}/${accion}`, {
      method: 'POST',
      body: JSON.stringify({ usuario, motivo }),
    }),
  metricas: () => req<Metricas>('/metricas'),
  auditoria: () => req<EventoAuditoria[]>('/auditoria?limite=100'),
}
