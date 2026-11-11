import { useEffect, useState } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { apiFetch } from './api'

export default function App() {
  const { isAuthenticated, isLoading, user, loginWithRedirect, logout, getAccessTokenSilently } = useAuth0()

  if (isLoading) return <p className="container">Carregando...</p>

  if (!isAuthenticated) {
    return (
      <div className="container">
        <h1>Tarefas</h1>
        <button onClick={() => loginWithRedirect()}>Login</button>
      </div>
    )
  }

  return (
    <div className="container">
      <header>
        <h1>Tarefas</h1>
        <div>
          <span>{user.email}</span>
          <button onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>Logout</button>
        </div>
      </header>
      <Tarefas getToken={getAccessTokenSilently} />
    </div>
  )
}

const FORM_VAZIO = { projeto: '', titulo: '', descricao: '', prioridade: '' }

function Tarefas({ getToken }) {
  const [lista, setLista] = useState([])
  const [erro, setErro] = useState('')
  const [form, setForm] = useState(FORM_VAZIO)
  const [enviando, setEnviando] = useState(false)

  async function carregar() {
    try {
      setErro('')
      setLista(await apiFetch(getToken, '/tarefa'))
    } catch (e) {
      setErro(e.message)
    }
  }

  async function cadastrar(e) {
    e.preventDefault()
    setEnviando(true)
    try {
      setErro('')
      await apiFetch(getToken, '/tarefa', { method: 'POST', body: JSON.stringify({ ...form, prioridade: Number(form.prioridade) }) })
      setForm(FORM_VAZIO)
      await carregar()
    } catch (e) {
      setErro(e.message)
    } finally {
      setEnviando(false)
    }
  }

  async function deletar(id) {
    if (!confirm('Deletar esta tarefa?')) return
    try {
      setErro('')
      await apiFetch(getToken, `/tarefa/${id}`, { method: 'DELETE' })
      await carregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  useEffect(() => { carregar() }, [])

  const mudar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  return (
    <>
      {erro && <p className="erro">{erro}</p>}

      <form onSubmit={cadastrar}>
        <h2>Nova tarefa</h2>
        <input placeholder="Projeto" value={form.projeto} onChange={mudar('projeto')} required />
        <input placeholder="Titulo" value={form.titulo} onChange={mudar('titulo')} required />
        <textarea placeholder="Descricao" value={form.descricao} onChange={mudar('descricao')} />
        <input type="number" min="0" max="10" placeholder="Prioridade (0 a 10)" value={form.prioridade} onChange={mudar('prioridade')} required />
        <button type="submit" disabled={enviando}>{enviando ? 'Enviando...' : 'Criar'}</button>
      </form>

      <h2>Tarefas</h2>
      <table>
        <thead>
          <tr><th>ID</th><th>Projeto</th><th>Titulo</th><th>Descricao</th><th>Prioridade</th><th>Criada por</th><th>Data</th><th></th></tr>
        </thead>
        <tbody>
          {lista.map((t) => (
            <tr key={t.id}>
              <td>{t.id}</td>
              <td>{t.projeto}</td>
              <td>{t.titulo}</td>
              <td>{t.descricao}</td>
              <td>{t.prioridade}</td>
              <td>{t.emailCriador}</td>
              <td>{t.dataCriacao ? new Date(t.dataCriacao).toLocaleString('pt-BR') : ''}</td>
              <td><button onClick={() => deletar(t.id)}>Deletar</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  )
}
