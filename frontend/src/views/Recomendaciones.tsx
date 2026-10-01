import { useState } from 'react'
import { api } from '../api'
import type { Estado } from '../types'
import { useFetch } from '../useFetch'

const ESTADOS: (Estado | '')[] = ['PENDIENTE', '', 'APROBADA', 'EJECUTADA', 'RECHAZADA', 'EXPIRADA']

export default function Recomendaciones({ usuario }: { usuario: string }) {
  const [estado, setEstado] = useState<Estado | ''>('PENDIENTE')
  const [msg, setMsg] = useState('')
  const [ocupado, setOcupado] = useState(false)
  const { data, error, reload } = useFetch(() => api.recomendaciones(estado), [estado])

  async function evaluar() {
    setOcupado(true)
    try {
      const r = await api.evaluar()
      setMsg(`Evaluación completa: ${r.creadas} recomendaciones nuevas`)
      await reload()
    } catch (e) {
      setMsg((e as Error).message)
    } finally {
      setOcupado(false)
    }
  }

  async function decidir(id: number, accion: 'aprobar' | 'rechazar') {
    if (!usuario.trim()) {
      setMsg('Ingresa tu nombre arriba para poder decidir')
      return
    }
    let motivo: string | undefined
    if (accion === 'rechazar') {
      motivo = window.prompt('Motivo del rechazo (opcional)') ?? undefined
    }
    try {
      await api.decidir(id, accion, usuario.trim(), motivo)
      setMsg(`Recomendación #${id} ${accion === 'aprobar' ? 'aprobada' : 'rechazada'}`)
    } catch (e) {
      setMsg((e as Error).message)
    }
    await reload()
  }

  return (
    <section>
      <div className="toolbar">
        <label>
          Estado{' '}
          <select value={estado} onChange={(e) => setEstado(e.target.value as Estado | '')}>
            {ESTADOS.map((s) => (
              <option key={s} value={s}>
                {s || 'TODOS'}
              </option>
            ))}
          </select>
        </label>
        <button onClick={evaluar} disabled={ocupado}>
          {ocupado ? 'Evaluando…' : 'Evaluar ahora'}
        </button>
      </div>
      {msg && <p className="aviso">{msg}</p>}
      {error && <p className="error">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Acción recomendada</th>
            <th>Producto</th>
            <th>Cantidad</th>
            <th>Cobertura</th>
            <th>Riesgo</th>
            <th>Estado</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {data?.map((r) => (
            <tr key={r.id}>
              <td>{r.id}</td>
              <td>
                {r.tipo === 'TRANSFERENCIA'
                  ? `Transferir ${r.bodegaOrigenNombre} → ${r.bodegaDestinoNombre}`
                  : `Comprar para ${r.bodegaDestinoNombre}`}
                {r.estimacionDegradada && (
                  <span className="badge warn" title="El servicio de pronóstico no respondió; se usó promedio móvil">
                    estimación degradada
                  </span>
                )}
              </td>
              <td>{r.productoNombre}</td>
              <td>{r.cantidad}</td>
              <td>{r.diasCobertura} días</td>
              <td>
                <span className={`badge ${r.riesgo === 'ALTO' ? 'alto' : 'medio'}`}>{r.riesgo}</span>
              </td>
              <td>
                {r.estado}
                {r.decididaPor && <small> · {r.decididaPor}</small>}
                {r.ordenExternaId && <small> · {r.ordenExternaId}</small>}
              </td>
              <td className="acciones">
                {r.estado === 'PENDIENTE' && (
                  <>
                    <button className="ok" onClick={() => decidir(r.id, 'aprobar')}>
                      Aprobar
                    </button>
                    <button className="no" onClick={() => decidir(r.id, 'rechazar')}>
                      Rechazar
                    </button>
                  </>
                )}
              </td>
            </tr>
          ))}
          {data?.length === 0 && (
            <tr>
              <td colSpan={8} className="vacio">
                Sin recomendaciones. Pulsa «Evaluar ahora».
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </section>
  )
}
