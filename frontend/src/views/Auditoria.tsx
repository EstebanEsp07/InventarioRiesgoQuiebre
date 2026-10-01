import { api } from '../api'
import { useFetch } from '../useFetch'

export default function Auditoria() {
  const { data, error, reload } = useFetch(api.auditoria)
  return (
    <section>
      <div className="toolbar">
        <button onClick={() => void reload()}>Actualizar</button>
      </div>
      {error && <p className="error">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Fecha</th>
            <th>Actor</th>
            <th>Acción</th>
            <th>Entidad</th>
            <th>Detalle</th>
          </tr>
        </thead>
        <tbody>
          {data?.map((e) => (
            <tr key={e.id}>
              <td>{new Date(e.ocurridoEn).toLocaleString()}</td>
              <td>{e.actor}</td>
              <td>{e.accion}</td>
              <td>
                {e.entidad} #{e.entidadId}
              </td>
              <td>{e.detalle}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}
