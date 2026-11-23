const TOKEN_KEY = 'pfm-token'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken()
  const res = await fetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers ?? {})
    }
  })
  if (res.status === 204) return undefined as T
  const body = await res.json().catch(() => ({}))
  if (!res.ok) {
    const message =
      typeof body?.error === 'string'
        ? body.error
        : body?.details
          ? Object.values(body.details).join(', ')
          : `Request failed (${res.status})`
    throw new Error(message)
  }
  return body as T
}

export const api = {
  login: (email: string, password: string) =>
    request<{ token: string; email: string }>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    }),
  summary: (month = 11, year = 2026) =>
    request<import('./types').Summary>(`/api/summary?month=${month}&year=${year}`),
  accounts: () => request<import('./types').Account[]>('/api/accounts'),
  categories: () => request<import('./types').Category[]>('/api/categories'),
  transactions: () =>
    request<import('./types').Transaction[]>('/api/transactions?start=2026-11-01&end=2026-11-30'),
  addTransaction: (payload: Record<string, unknown>) =>
    request<import('./types').Transaction>('/api/transactions', {
      method: 'POST',
      body: JSON.stringify(payload)
    }),
  deleteTransaction: (id: number) =>
    request<void>(`/api/transactions/${id}`, { method: 'DELETE' }),
  budgets: () => request<import('./types').Budget[]>('/api/budgets?month=11&year=2026'),
  goals: () => request<import('./types').SavingsGoal[]>('/api/goals'),
  contribute: (id: number, amount: number) =>
    request<import('./types').SavingsGoal>(`/api/goals/${id}/contribute`, {
      method: 'POST',
      body: JSON.stringify({ amount })
    }),
  holdings: () => request<import('./types').Holding[]>('/api/holdings')
}
