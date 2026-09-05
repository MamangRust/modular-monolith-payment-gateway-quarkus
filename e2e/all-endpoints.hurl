# ═══════════════════════════════════════════════════════════════════
# E2E Smoke Test - ALL endpoints reachable and routed correctly
# Pure GET-only test + auth POST (state-independent)
# Verifies: routing, security bypass, backend responds
# ═══════════════════════════════════════════════════════════════════

# ── Auth (status depends on state: lockout, missing role/user) ───
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{"firstname":"John","lastname":"Doe","email":"smoke_e2e@test.com","password":"secret123","confirmPassword":"secret123"}

HTTP *

[Asserts]
status < 500

POST http://localhost:8080/api/auth/login
Content-Type: application/json

{"email":"smoke_e2e@test.com","password":"secret123"}

HTTP *

[Asserts]
status < 500

POST http://localhost:8080/api/auth/verify
Content-Type: application/json

{"code":"ABC123"}

HTTP *

[Asserts]
status < 500

POST http://localhost:8080/api/auth/forgot-password
Content-Type: application/json

{"email":"smoke_e2e@test.com"}

HTTP *

[Asserts]
status < 500

POST http://localhost:8080/api/auth/reset-password
Content-Type: application/json

{"resetToken":"tk","password":"newsecret","confirmPassword":"newsecret"}

HTTP *

[Asserts]
status < 500

POST http://localhost:8080/api/auth/refresh
Content-Type: application/json

{"refreshToken":"rt"}

HTTP *

[Asserts]
status < 500

GET http://localhost:8080/api/auth/me?userId=1

HTTP *

[Asserts]
status < 500

# ── Users ─────────────────────────────────────────────────────────
GET http://localhost:8080/api/users?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/users/1

HTTP 404

# ── Roles ─────────────────────────────────────────────────────────
GET http://localhost:8080/api/roles?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/roles/1

HTTP 200

# ── Cards ─────────────────────────────────────────────────────────
GET http://localhost:8080/api/cards?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/cards/active?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/cards/trashed?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/cards/1

HTTP 404

GET http://localhost:8080/api/cards/by-user/1

HTTP 404

GET http://localhost:8080/api/cards/by-number/4111111111111111

HTTP 404

GET http://localhost:8080/api/cards/dashboard

HTTP 200

GET http://localhost:8080/api/cards/stats/balance/monthly?year=2024

HTTP 200

GET http://localhost:8080/api/cards/stats/balance/yearly?year=2024

HTTP 200

GET http://localhost:8080/api/cards/stats/topup/monthly?year=2024

HTTP 200

GET http://localhost:8080/api/cards/stats/transaction/monthly?year=2024

HTTP 200

GET http://localhost:8080/api/cards/stats/withdraw/monthly?year=2024

HTTP 200

# ── Merchants ─────────────────────────────────────────────────────
GET http://localhost:8080/api/merchants?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/merchants/1

HTTP 404

GET http://localhost:8080/api/merchants/transactions?page=1&size=10&search=admin&merchantId=1

HTTP 400

GET http://localhost:8080/api/merchants/stats/amount/monthly?year=2024

HTTP 200

GET http://localhost:8080/api/merchants/stats/method/monthly?year=2024

HTTP 200

GET http://localhost:8080/api/merchants/stats/total-amount/monthly?year=2024

HTTP 200

# ── Merchant Documents ────────────────────────────────────────────
GET http://localhost:8080/api/merchant-documents?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/merchant-documents/active?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/merchant-documents/trashed?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/merchant-documents/1

HTTP 404

# ── Saldos ────────────────────────────────────────────────────────
GET http://localhost:8080/api/saldos?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/saldos/1

HTTP 404

GET http://localhost:8080/api/saldos/stats/balance/monthly?year=2024

HTTP 200

GET http://localhost:8080/api/saldos/stats/total/monthly?year=2024&month=1

HTTP 200

# ── Topups ────────────────────────────────────────────────────────
GET http://localhost:8080/api/topups?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/topups/by-card?cardNumber=4111111111111111&page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/topups/active?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/topups/trashed?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/topups/1

HTTP 404

GET http://localhost:8080/api/topups/card/4111111111111111?year=2024

HTTP 200

GET http://localhost:8080/api/topups/stats/amount/monthly?year=2024

HTTP 200

# ── Transactions ──────────────────────────────────────────────────
GET http://localhost:8080/api/transactions?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/transactions/by-card?cardNumber=4111111111111111&page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/transactions/active?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/transactions/trashed?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/transactions/1

HTTP 404

GET http://localhost:8080/api/transactions/merchant/1

HTTP 200

GET http://localhost:8080/api/transactions/stats/amount/monthly?year=2024

HTTP 200

# ── Transfers ─────────────────────────────────────────────────────
GET http://localhost:8080/api/transfers?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/transfers/active?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/transfers/trashed?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/transfers/1

HTTP 404

GET http://localhost:8080/api/transfers/from/4111111111111111

HTTP 200

GET http://localhost:8080/api/transfers/to/4222222222222222

HTTP 200

GET http://localhost:8080/api/transfers/stats/amount/monthly?year=2024

HTTP 200

# ── Withdraws ─────────────────────────────────────────────────────
GET http://localhost:8080/api/withdraws?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/withdraws/active?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/withdraws/trashed?page=1&size=10&search=admin

HTTP 200

GET http://localhost:8080/api/withdraws/by-card/4111111111111111

HTTP 200

GET http://localhost:8080/api/withdraws/1

HTTP 404

GET http://localhost:8080/api/withdraws/stats/amount/monthly?year=2024

HTTP 200
