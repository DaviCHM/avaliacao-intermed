import { useEffect, useState } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { apiFetch } from './api'

const ROLES_CLAIM = 'https://avaliacoes-api/roles'

export default function App() {
  const { isAuthenticated, isLoading, user, loginWithRedirect, logout, getAccessTokenSilently } = useAuth0()

  if (isLoading) return <p className="container">Carregando...</p>

  if (!isAuthenticated) {
    return (
      <div className="container">
        <h1>Avaliacoes</h1>
        <button onClick={() => loginWithRedirect()}>Login</button>
      </div>
    )
  }

  const roles = user?.[ROLES_CLAIM] || []
  const isAdmin = roles.includes('ADMIN')

  return (
    <div className="container">
      <header>
        <h1>Avaliacoes</h1>
        <div>
          <span>{user.email} ({roles.join(', ') || 'sem role'})</span>
          <button onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>Logout</button>
        </div>
      </header>
      <Avaliacoes getToken={getAccessTokenSilently} isAdmin={isAdmin} />
    </div>
  )
}

function Avaliacoes({ getToken, isAdmin }) {
  const [lista, setLista] = useState([])
  const [erro, setErro] = useState('')
  const [form, setForm] = useState({ emailAvaliado: '', comentarios: '', nota: '' })

  async function carregar() {
    try {
      setErro('')
      setLista(await apiFetch(getToken, '/avaliacoes'))
    } catch (e) {
      setErro(e.message)
    }
  }

  async function cadastrar(e) {
    e.preventDefault()
    try {
      setErro('')
      await apiFetch(getToken, '/avaliacoes', {
        method: 'POST',
        body: JSON.stringify({ ...form, nota: Number(form.nota) }),
      })
      setForm({ emailAvaliado: '', comentarios: '', nota: '' })
      carregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  useEffect(() => { carregar() }, [])

  const mudar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  return (
    <>
      {erro && <p className="erro">{erro}</p>}

      {isAdmin && (
        <form onSubmit={cadastrar}>
          <h2>Nova avaliacao</h2>
          <input type="email" placeholder="Email do avaliado" value={form.emailAvaliado} onChange={mudar('emailAvaliado')} required />
          <textarea placeholder="Comentarios" value={form.comentarios} onChange={mudar('comentarios')} />
          <input type="number" min="0" max="10" placeholder="Nota (0 a 10)" value={form.nota} onChange={mudar('nota')} required />
          <button type="submit">Cadastrar</button>
        </form>
      )}

      <h2>Avaliacoes</h2>
      <table>
        <thead>
          <tr><th>ID</th><th>Avaliador</th><th>Avaliado</th><th>Comentarios</th><th>Nota</th><th>Data</th></tr>
        </thead>
        <tbody>
          {lista.map((a) => (
            <tr key={a.id}>
              <td>{a.id}</td>
              <td>{a.emailAvaliador}</td>
              <td>{a.emailAvaliado}</td>
              <td>{a.comentarios}</td>
              <td>{a.nota}</td>
              <td>{a.dataAvaliacao ? new Date(a.dataAvaliacao).toLocaleString('pt-BR') : ''}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  )
}
