export interface Account {
  id: number
  name: string
  type: string
  balance: number
}

export interface Category {
  id: number
  name: string
  type: 'INCOME' | 'EXPENSE'
}

export interface Transaction {
  id: number
  account: Account
  category: Category
  amount: number
  date: string
  description: string
  type: 'INCOME' | 'EXPENSE'
}

export interface Budget {
  id: number
  category: Category
  month: number
  year: number
  amount: number
}

export interface SavingsGoal {
  id: number
  name: string
  targetAmount: number
  savedAmount: number
  deadline: string | null
}

export interface Holding {
  id: number
  symbol: string
  name: string
  shares: number
  purchasePrice: number
  currentPrice: number
}

export interface Summary {
  year: number
  month: number
  totalIncome: number
  totalExpenses: number
  net: number
  expensesByCategory: { category: string; spent: number }[]
  budgetUsage: {
    category: string
    budget: number
    spent: number
    remaining: number
    percentUsed: number
  }[]
  goals: {
    id: number
    name: string
    targetAmount: number
    savedAmount: number
    percent: number
  }[]
  portfolioCost: number
  portfolioValue: number
  portfolioProfitLoss: number
  portfolioProfitLossPercent: number
}
