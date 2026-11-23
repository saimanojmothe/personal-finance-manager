# Personal Finance Manager

A small, working demo app for managing one person's money: spending, budgets, savings goals, and mock investments. It was built as a portfolio project to show a full-stack workflow — Spring Boot on the back end, React + TypeScript on the front end — with real validation, tests, and a clean, readable codebase.

**All data is synthetic demo data.** There are no real people, accounts, or market prices in this project, and nothing here is connected to any employer or real financial system.

## Features

- **Dashboard** — monthly income, expenses, and net; spending by category; budget usage; savings-goal progress; portfolio totals.
- **Transactions** — list, add (with validation), and delete income/expense transactions. Account balances move with each transaction.
- **Budgets** — a monthly budget per spending category, with spent vs. budget and remaining amounts.
- **Savings goals** — track progress toward a target (capped at 100%) and add contributions.
- **Investments** — mock holdings with shares, purchase price, and a fixed mock current price, so profit/loss is computed, never fetched live.
- **Login** — demo JWT login (one demo user). Every `/api/**` endpoint except login requires a token.

## Tech stack

- **Backend:** Java 21, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security (JWT), Bean Validation, H2 in-memory database, JUnit 5 + Mockito.
- **Frontend:** Vite, React 18, TypeScript. No paid libraries. API calls are proxied to the backend in development.
- **Data:** `schema.sql` + `data.sql` seed the demo month (November 2026) on startup. The database resets when the backend restarts.

## How to run

### Backend (port 8080)

```bash
cd backend
mvn spring-boot:run
```

### Frontend (port 5173)

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 and sign in with the demo login below. The frontend proxies `/api` calls to `http://localhost:8080`.

### Demo login

- Email: `demo@finance.local`
- Password: `demo1234`

### Run the tests / production build

```bash
cd backend && mvn test
cd frontend && npm run build
```

## Example API calls

```bash
# 1. Log in and save the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@finance.local","password":"demo1234"}' | python3 -c 'import sys,json; print(json.load(sys.stdin)["token"])')

# 2. Monthly summary (defaults to November 2026, the seeded demo month)
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/summary?month=11&year=2026"

# 3. Add a transaction
curl -s -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"accountId":1,"categoryId":3,"amount":25.50,"date":"2026-11-20","description":"Groceries (demo)","type":"EXPENSE"}'
```

Other endpoints: `GET /api/accounts`, `GET /api/categories`, `GET /api/transactions`, `GET /api/budgets`, `GET /api/goals`, `POST /api/goals/{id}/contribute`, `GET /api/holdings`.

## Architecture notes

```
frontend (React + TypeScript)
    |  REST / JSON, JWT in the Authorization header
backend (Spring Boot)
    |  FinanceService — summary, budget usage, goal progress, portfolio P&L
    |  JPA repositories — Account, Category, Transaction, Budget, SavingsGoal, Holding
    v
H2 in-memory database (seeded by schema.sql / data.sql)
```

- Controllers are thin: they validate input and delegate to `FinanceService`.
- The calculations that matter (budget usage, goal progress, portfolio profit/loss, monthly totals) live in `FinanceService` and are covered by unit tests in `FinanceServiceTest`.
- Security is deliberately simple for a demo: one in-memory demo user, stateless JWT, no sessions. Do not reuse the demo secret or credentials in a real app.

## Screenshots

_Screenshots to be added after the first run — the dashboard, transactions, and goals views._

## License

MIT — see [LICENSE](LICENSE).
