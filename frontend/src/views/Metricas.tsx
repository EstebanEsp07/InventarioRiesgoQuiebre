import { api } from '../api'
import type { FilaConteo } from '../types'
import { useFetch } from '../useFetch'

function Lista({ titulo, filas }: { titulo: string; filas: FilaConteo[] }) {
  return (
    <div className="card">
      <h3>{titulo}</h3>
      {filas.length === 0 ? (
        <p className="vacio">Sin casos</p>
      ) : (
        <ul>
          {filas.map((f) => (
            <li key={f.producto}>
              {f.producto}: <strong>{f.bodegas}</strong> bodega(s)
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default function Metricas() {
  const { data: m, error, reload } = useFetch(api.metricas)
  return (
    <section>
      <div className="toolbar">
        <button onClick={() => void reload()}>Actualizar</button>
      </div>
      {error && <p className="error">{error}</p>}
      {m && (
        <>
          <div className="cards">
            <div className="card">
              <h3>Tiempo medio de aprobación</h3>
              <p className="cifra">{m.tiempoMedioAprobacionMinutos.toFixed(1)} min</p>
            </div>
            <div className="card">
              <h3>Compras urgentes</h3>
              <p className="cifra">{m.comprasUrgentes}</p>
            </div>
            <div className="card">
              <h3>Recomendaciones aceptadas</h3>
              <p className="cifra">
                {m.recomendacionesAceptadas}/{m.recomendacionesDecididas} ({Math.round(m.tasaAceptacion * 100)}%)
              </p>
            </div>
            <div className="card">
              <h3>Pendientes</h3>
              <p className="cifra">{m.recomendacionesPendientes}</p>
            </div>
          </div>
          <div className="cards">
            <Lista titulo="Quiebres actuales por producto" filas={m.quiebresPorProducto} />
            <Lista titulo="Riesgo alto pendiente por producto" filas={m.riesgoAltoPendientePorProducto} />
          </div>
        </>
      )}
    </section>
  )
}
