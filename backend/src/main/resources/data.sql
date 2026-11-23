-- Synthetic demo data for one fictional person. No real people, accounts, or prices.
INSERT INTO categories (id, name, type) VALUES
  (1, 'Salary', 'INCOME'),
  (2, 'Freelance', 'INCOME'),
  (3, 'Groceries', 'EXPENSE'),
  (4, 'Rent', 'EXPENSE'),
  (5, 'Transport', 'EXPENSE'),
  (6, 'Dining Out', 'EXPENSE'),
  (7, 'Utilities', 'EXPENSE'),
  (8, 'Entertainment', 'EXPENSE');

INSERT INTO accounts (id, name, type, balance) VALUES
  (1, 'Everyday Checking', 'CHECKING', 2450.00),
  (2, 'Emergency Savings', 'SAVINGS', 8200.00),
  (3, 'Everyday Card', 'CREDIT_CARD', -320.00);

-- November 2026 demo month (the default month for /api/summary).
INSERT INTO transactions (id, account_id, category_id, amount, txn_date, description, type) VALUES
  (1, 1, 1, 4200.00, DATE '2026-11-01', 'Monthly salary (demo)', 'INCOME'),
  (2, 1, 2, 650.00, DATE '2026-11-12', 'Weekend freelance project (demo)', 'INCOME'),
  (3, 1, 4, 1450.00, DATE '2026-11-01', 'Monthly rent (demo)', 'EXPENSE'),
  (4, 1, 3, 96.40, DATE '2026-11-03', 'Weekly groceries (demo)', 'EXPENSE'),
  (5, 1, 3, 82.15, DATE '2026-11-10', 'Weekly groceries (demo)', 'EXPENSE'),
  (6, 1, 3, 74.90, DATE '2026-11-17', 'Weekly groceries (demo)', 'EXPENSE'),
  (7, 1, 5, 58.00, DATE '2026-11-05', 'Transit pass (demo)', 'EXPENSE'),
  (8, 1, 5, 34.50, DATE '2026-11-14', 'Ride share (demo)', 'EXPENSE'),
  (9, 3, 6, 46.20, DATE '2026-11-07', 'Dinner with friends (demo)', 'EXPENSE'),
  (10, 3, 6, 28.75, DATE '2026-11-15', 'Lunch (demo)', 'EXPENSE'),
  (11, 1, 7, 132.00, DATE '2026-11-09', 'Electricity and internet (demo)', 'EXPENSE'),
  (12, 3, 8, 19.99, DATE '2026-11-02', 'Streaming subscription (demo)', 'EXPENSE'),
  (13, 3, 8, 42.00, DATE '2026-11-19', 'Movie night (demo)', 'EXPENSE');

INSERT INTO budgets (id, category_id, budget_month, budget_year, amount) VALUES
  (1, 3, 11, 2026, 400.00),
  (2, 5, 11, 2026, 120.00),
  (3, 6, 11, 2026, 150.00),
  (4, 7, 11, 2026, 180.00),
  (5, 8, 11, 2026, 80.00);

INSERT INTO savings_goals (id, name, target_amount, saved_amount, deadline) VALUES
  (1, 'Emergency fund', 10000.00, 8200.00, DATE '2027-06-30'),
  (2, 'Trip to the coast', 1800.00, 640.00, DATE '2027-05-01'),
  (3, 'New laptop', 1400.00, 1400.00, DATE '2026-12-31');

-- Mock holdings: current prices are fixed demo values, NOT live market prices.
INSERT INTO holdings (id, symbol, name, shares, purchase_price, current_price) VALUES
  (1, 'DEMO', 'Demo Index Fund', 12.0000, 100.00, 118.50),
  (2, 'ACME', 'Acme Widgets (demo)', 20.0000, 45.00, 41.25),
  (3, 'BETA', 'Beta Energy (demo)', 8.0000, 60.00, 66.00);
