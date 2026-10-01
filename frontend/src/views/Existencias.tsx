import { api } from '../api'
import { useFetch } from '../useFetch'

export default function Existencias() {
  const { data, error, reload } = useFetch(api.existencias)
  return (
    <section>
      <div className="toolbar">
        <button onClick={() => void reload()}>Actualizar</button>
      </div>
      {error && <p className="error">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Bodega</th>
            <th>SKU</th>
            <th>Producto</th>
            <th>Existencia</th>
            <th>Lead time</th>
          </tr>
        </thead>
        <tbody>
          {data?.map((e) => (
            <tr key={`${e.bodegaId}-${e.productoId}`}>
              <td>{e.bodega}</td>
              <td>{e.sku}</td>
              <td>{e.producto}</td>
              <td>
                {e.cantidad} {e.unidad}
              </td>
              <td>{e.leadTimeDias} d</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}
