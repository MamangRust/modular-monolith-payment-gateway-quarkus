# Withdraw E2E Tests - verify all withdraw endpoints are reachable and routed

# GET /api/withdraws
GET http://localhost:8080/api/withdraws?page=1&size=10&search=admin

HTTP 200

# GET /api/withdraws/active
GET http://localhost:8080/api/withdraws/active?page=1&size=10&search=admin

HTTP 200

# GET /api/withdraws/trashed
GET http://localhost:8080/api/withdraws/trashed?page=1&size=10&search=admin

HTTP 200

# GET /api/withdraws/by-card/{cardNumber}
GET http://localhost:8080/api/withdraws/by-card/4111111111111111

HTTP 200

# GET /api/withdraws/{id}
GET http://localhost:8080/api/withdraws/1

HTTP 404

# POST /api/withdraws
POST http://localhost:8080/api/withdraws
Content-Type: application/json

{"cardNumber":"4111111111111111","withdrawAmount":10000}

HTTP 400

# GET /api/withdraws/stats/amount/monthly
GET http://localhost:8080/api/withdraws/stats/amount/monthly?year=2024

HTTP 200

# GET /api/withdraws/stats/amount/yearly
GET http://localhost:8080/api/withdraws/stats/amount/yearly?year=2024

HTTP 200

# GET /api/withdraws/stats/amount/monthly/by-card
GET http://localhost:8080/api/withdraws/stats/amount/monthly/by-card?year=2024&cardNumber=4111111111111111

HTTP 200

# GET /api/withdraws/stats/amount/yearly/by-card
GET http://localhost:8080/api/withdraws/stats/amount/yearly/by-card?year=2024&cardNumber=4111111111111111

HTTP 200
