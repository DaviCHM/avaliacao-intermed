// Chamada generica autenticada. Na prova, so muda o path e o body.
const API = import.meta.env.VITE_API_URL

export async function apiFetch(getToken, path, options = {}) {
  const token = await getToken()
  const res = await fetch(`${API}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
      ...(options.headers || {}),
    },
  })
  if (!res.ok) {
    const texto = await res.text()
    throw new Error(`${res.status} ${texto}`)
  }
  return res.status === 204 ? null : res.json()
}
