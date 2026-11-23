import { useCallback, useEffect, useState } from 'react'
import { api, clearToken, getToken, setToken } from './api'
import type { Account, Budget, Category, Holding, SavingsGoal, Summary, Transaction } from './types'

type View = 'dashboard' | 'transactions' | 'budgets' | 'goals' | 'investments'

function money(n: number): string {
  return n.toLocaleString('en-US', { style: 'currency', currency: 'USD' })
}

function Bar({ percent, tone = 'green' }: { percent: number; tone?: 'green' | 'red' | 'blue' }) {
  const capped = Math.min(100, Math.max(0, percent))
  return (
    <div className="bar">
      <div className={`bar-fill ${tone}`} style={{ width: `${capped}%` }} />
    </div>
  )
}

function Login({ onLoggedIn }: { onLoggedIn: () => void }) {
  const [email, setEmail] = useState('demo@finance.local')
  const [password, setPassword] = useState('demo1234')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const res = await api.login(email, password)
      setToken(res.token)
      onLoggedIn()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="login-wrap">
      <form className="card login-card" onSubmit={submit}>
        <h1>Personal Finance Manager</h1>
        <p className="muted">Track spending, budgets, savings goals and mock investments — for one person, with demo data.</p>
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          Password
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
        <p className="muted small">Demo login: demo@finance.local / demo1234</p>
      </form>
    </main>
  )
}

export default function App() {
  const [authed, setAuthed] = useState(() => getToken() !== null)
  const [view, setView] = useState<View>('dashboard')
  const [summary, setSummary] = useState<Summary | null>(null)
  const [accounts, setAccounts] = useState<Account[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [budgets, setBudgets] = useState<Budget[]>([])
  const [goals, setGoals] = useState<SavingsGoal[]>([])
  const [holdings, setHoldings] = useState<Holding[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [s, a, c, t, b, g, h] = await Promise.all([
        api.summary(),
        api.accounts(),
        api.categories(),
        api.transactions(),
        api.budgets(),
        api.goals(),
        api.holdings()
      ])
      setSummary(s)
      setAccounts(a)
      setCategories(c)
      setTransactions(t)
      setBudgets(b)
      setGoals(g)
      setHoldings(h)
    } catch (err) {
      if (err instanceof Error && /401|403/.test(err.message)) {
        clearToken()
        setAuthed(false)
      }
      setError(err instanceof Error ? err.message : 'Failed to load data')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (authed) void load()
  }, [authed, load])

  if (!authed) return <Login onLoggedIn={() => setAuthed(true)} />

  const maxSpend = Math.max(1, ...(summary?.expensesByCategory.map((c) => c.spent) ?? [1]))

  return (
    <div className="layout">
      <header>
        <div>
          <h1>Personal Finance Manager</h1>
          <p className="muted">November 2026 · demo data · all amounts in USD</p>
        </div>
        <button className="secondary" onClick={() => { clearToken(); setAuthed(false) }}>
          Sign out
        </button>
      </header>

      <nav>
        {(['dashboard', 'transactions', 'budgets', 'goals', 'investments'] as View[]).map((v) => (
          <button key={v} className={view === v ? 'active' : ''} onClick={() => setView(v)}>
            {v[0].toUpperCase() + v.slice(1)}
          </button>
        ))}
      </nav>

      {error && <p className="error">{error}</p>}
      {loading && <p className="muted">Loading…</p>}

      {view === 'dashboard' && summary && (
        <section className="grid">
          <div className="card stat">
            <span>Income</span>
            <strong className="positive">{money(summary.totalIncome)}</strong>
          </div>
          <div className="card stat">
            <span>Expenses</span>
            <strong className="negative">{money(summary.totalExpenses)}</strong>
          </div>
          <div className="card stat">
            <span>Net this month</span>
            <strong className={summary.net >= 0 ? 'positive' : 'negative'}>{money(summary.net)}</strong>
          </div>
          <div className="card stat">
            <span>Portfolio value (mock prices)</span>
            <strong>
              {money(summary.portfolioValue)}{' '}
              <small className={summary.portfolioProfitLoss >= 0 ? 'positive' : 'negative'}>
                {summary.portfolioProfitLoss >= 0 ? '+' : ''}
                {money(summary.portfolioProfitLoss)} ({summary.portfolioProfitLossPercent.toFixed(1)}%)
              </small>
            </strong>
          </div>

          <div className="card wide">
            <h2>Spending by category</h2>
            {summary.expensesByCategory.map((c) => (
              <div key={c.category} className="row">
                <span>{c.category}</span>
                <Bar percent={(c.spent / maxSpend) * 100} tone="blue" />
                <strong>{money(c.spent)}</strong>
              </div>
            ))}
            {summary.expensesByCategory.length === 0 && <p className="muted">No expenses this month.</p>}
          </div>

          <div className="card wide">
            <h2>Budget usage</h2>
            {summary.budgetUsage.map((b) => (
              <div key={b.category} className="row">
                <span>{b.category}</span>
                <Bar percent={b.percentUsed} tone={b.percentUsed > 100 ? 'red' : 'green'} />
                <strong>
                  {money(b.spent)} / {money(b.budget)} ({b.percentUsed.toFixed(0)}%)
                </strong>
              </div>
            ))}
          </div>

          <div className="card wide">
            <h2>Savings goals</h2>
            {summary.goals.map((g) => (
              <div key={g.id} className="row">
                <span>{g.name}</span>
                <Bar percent={g.percent} />
                <strong>
                  {money(g.savedAmount)} / {money(g.targetAmount)}
                </strong>
              </div>
            ))}
          </div>

          <div className="card wide">
            <h2>Accounts</h2>
            {accounts.map((a) => (
              <div key={a.id} className="row">
                <span>{a.name} · {a.type}</span>
                <strong className={a.balance < 0 ? 'negative' : ''}>{money(a.balance)}</strong>
              </div>
            ))}
          </div>
        </section>
      )}

      {view === 'transactions' && (
        <TransactionsView
          accounts={accounts}
          categories={categories}
          transactions={transactions}
          onChanged={load}
        />
      )}

      {view === 'budgets' && (
        <section className="card">
          <h2>Budgets — November 2026</h2>
          <table>
            <thead>
              <tr><th>Category</th><th>Budget</th></tr>
            </thead>
            <tbody>
              {budgets.map((b) => (
                <tr key={b.id}><td>{b.category.name}</td><td>{money(b.amount)}</td></tr>
              ))}
            </tbody>
          </table>
          <p className="muted small">
            Budget usage for the month is shown on the Dashboard. Budgets are per category, per month.
          </p>
        </section>
      )}

      {view === 'goals' && (
        <GoalsView goals={goals} onChanged={load} />
      )}

      {view === 'investments' && (
        <section className="card">
          <h2>Investments</h2>
          <p className="muted">Prices are fixed mock values for this demo — not live market prices.</p>
          <table>
            <thead>
              <tr><th>Symbol</th><th>Name</th><th>Shares</th><th>Buy price</th><th>Current price</th><th>Value</th><th>P&amp;L</th></tr>
            </thead>
            <tbody>
              {holdings.map((h) => {
                const value = h.shares * h.currentPrice
                const pnl = value - h.shares * h.purchasePrice
                return (
                  <tr key={h.id}>
                    <td>{h.symbol}</td><td>{h.name}</td><td>{h.shares}</td>
                    <td>{money(h.purchasePrice)}</td><td>{money(h.currentPrice)}</td>
                    <td>{money(value)}</td>
                    <td className={pnl >= 0 ? 'positive' : 'negative'}>{money(pnl)}</td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </section>
      )}
    </div>
  )
}

function TransactionsView({
  accounts,
  categories,
  transactions,
  onChanged
}: {
  accounts: Account[]
  categories: Category[]
  transactions: Transaction[]
  onChanged: () => void
}) {
  const [accountId, setAccountId] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [amount, setAmount] = useState('')
  const [date, setDate] = useState('2026-11-20')
  const [description, setDescription] = useState('')
  const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE')
  const [formError, setFormError] = useState('')

  const filteredCategories = categories.filter((c) => c.type === type)

  useEffect(() => {
    if (
      filteredCategories.length > 0 &&
      !filteredCategories.some((c) => String(c.id) === categoryId)
    ) {
      setCategoryId(String(filteredCategories[0].id))
    }
  }, [type, categories, categoryId, filteredCategories])

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setFormError('')
    const parsed = Number(amount)
    if (!accountId || !categoryId || !parsed || parsed <= 0 || !description.trim()) {
      setFormError('Fill in every field. Amount must be greater than zero.')
      return
    }
    try {
      await api.addTransaction({
        accountId: Number(accountId),
        categoryId: Number(categoryId),
        amount: parsed,
        date,
        description: description.trim(),
        type
      })
      setAmount('')
      setDescription('')
      await onChanged()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Could not add transaction')
    }
  }

  return (
    <div className="grid">
      <form className="card" onSubmit={submit}>
        <h2>Add transaction</h2>
        <label>
          Type
          <select value={type} onChange={(e) => setType(e.target.value as 'INCOME' | 'EXPENSE')}>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>
        </label>
        <label>
          Account
          <select value={accountId} onChange={(e) => setAccountId(e.target.value)} required>
            <option value="">Choose an account</option>
            {accounts.map((a) => (
              <option key={a.id} value={a.id}>{a.name}</option>
            ))}
          </select>
        </label>
        <label>
          Category
          <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
            {filteredCategories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        </label>
        <label>
          Amount (USD)
          <input type="number" min="0.01" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required />
        </label>
        <label>
          Date
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
        </label>
        <label>
          Description
          <input value={description} onChange={(e) => setDescription(e.target.value)} required />
        </label>
        {formError && <p className="error">{formError}</p>}
        <button type="submit">Add transaction</button>
      </form>

      <section className="card wide">
        <h2>Transactions — November 2026</h2>
        <table>
          <thead>
            <tr><th>Date</th><th>Description</th><th>Category</th><th>Account</th><th>Amount</th><th></th></tr>
          </thead>
          <tbody>
            {transactions.map((t) => (
              <tr key={t.id}>
                <td>{t.date}</td>
                <td>{t.description}</td>
                <td>{t.category.name}</td>
                <td>{t.account.name}</td>
                <td className={t.type === 'INCOME' ? 'positive' : 'negative'}>
                  {t.type === 'INCOME' ? '+' : '−'}{money(t.amount)}
                </td>
                <td>
                  <button
                    className="secondary small-btn"
                    onClick={async () => {
                      await api.deleteTransaction(t.id)
                      await onChanged()
                    }}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  )
}

function GoalsView({ goals, onChanged }: { goals: SavingsGoal[]; onChanged: () => void }) {
  const [amounts, setAmounts] = useState<Record<number, string>>({})
  const [error, setError] = useState('')

  return (
    <section className="card">
      <h2>Savings goals</h2>
      {goals.map((g) => {
        const pct = Math.min(100, (g.savedAmount / g.targetAmount) * 100)
        return (
          <div key={g.id} className="goal">
            <div className="row">
              <span>
                <strong>{g.name}</strong>
                {g.deadline ? <span className="muted"> · by {g.deadline}</span> : null}
              </span>
              <strong>{money(g.savedAmount)} / {money(g.targetAmount)} ({pct.toFixed(0)}%)</strong>
            </div>
            <Bar percent={pct} />
            <div className="contribute">
              <input
                type="number"
                min="0.01"
                step="0.01"
                placeholder="Amount"
                value={amounts[g.id] ?? ''}
                onChange={(e) => setAmounts({ ...amounts, [g.id]: e.target.value })}
              />
              <button
                onClick={async () => {
                  const value = Number(amounts[g.id])
                  if (!value || value <= 0) {
                    setError('Enter an amount greater than zero.')
                    return
                  }
                  setError('')
                  await api.contribute(g.id, value)
                  setAmounts({ ...amounts, [g.id]: '' })
                  await onChanged()
                }}
              >
                Add savings
              </button>
            </div>
          </div>
        )
      })}
      {error && <p className="error">{error}</p>}
    </section>
  )
}
