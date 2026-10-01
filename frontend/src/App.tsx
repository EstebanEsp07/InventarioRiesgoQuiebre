import { useState } from 'react'
import Auditoria from './components/Auditoria'
import Existencias from './components/Existencias'
import Metricas from './components/Metricas'
import Recomendaciones from './components/Recomendaciones'

type Tab = 'recomendaciones' | 'existencias' | 'metricas' | 'auditoria'

const TABS: { id: Tab; titulo: string }[] = [
  { id: 'recomendaciones', titulo: 'Recomendaciones' },
  { id: 'existencias', titulo: 'Existencias' },
  { id: 'metricas', titulo: 'Métricas' },
  { id: 'auditoria', titulo: 'Auditoría' },
]

export default function App() {
  const [tab, setTab] = useState<Tab>('recomendaciones')
  const [usuario, setUsuario] = useState(() => localStorage.getItem('usuario') ?? '')

  function cambiarUsuario(v: string) {
    setUsuario(v)
    localStorage.setItem('usuario', v)
  }

  return (
    <div className="app">
      <header>
        <h1>Inventario y riesgo de quiebre</h1>
        <label>
          Aprobador{' '}
          <input value={usuario} onChange={(e) => cambiarUsuario(e.target.value)} placeholder="Tu nombre" />
        </label>
      </header>
      <nav>
        {TABS.map((t) => (
          <button key={t.id} className={t.id === tab ? 'activa' : ''} onClick={() => setTab(t.id)}>
            {t.titulo}
          </button>
        ))}
      </nav>
      <main>
        {tab === 'recomendaciones' && <Recomendaciones usuario={usuario} />}
        {tab === 'existencias' && <Existencias />}
        {tab === 'metricas' && <Metricas />}
        {tab === 'auditoria' && <Auditoria />}
      </main>
    </div>
  )
}
