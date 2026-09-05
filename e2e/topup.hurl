# Topup E2E Tests - verify all topup endpoints are reachable and routed

# GET /api/topups
GET http://localhost:8080/api/topups?page=1&size=10&search=admin

HTTP 200

# GET /api/topups/by-card
GET http://localhost:8080/api/topups/by-card?cardNumber=4111111111111111&page=1&size=10&search=admin

HTTP 200

# GET /api/topups/active
GET http://localhost:8080/api/topups/active?page=1&size=10&search=admin

HTTP 200

# GET /api/topups/trashed
GET http://localhost:8080/api/topups/trashed?page=1&size=10&search=admin

HTTP 200

# GET /api/topups/{id}
GET http://localhost:8080/api/topups/1

HTTP 404

# GET /api/topups/card/{cardNumber}
GET http://localhost:8080/api/topups/card/4111111111111111?year=2024

HTTP 200

# POST /api/topups
POST http://localhost:8080/api/topups
Content-Type: application/json

{"cardNumber":"4111111111111111","topupAmount":50000,"topupMethod":"bank_transfer"}

HTTP 404

# POST /api/topups/trash/{id}
POST http://localhost:8080/api/topups/trash/1

HTTP 404

# POST /api/topups/restore/{id}
POST http://localhost:8080/api/topups/restore/1

HTTP 404

# GET /api/topups/stats/amount/monthly
GET http://localhost:8080/api/topups/stats/amount/monthly?year=2024

HTTP 200

# GET /api/topups/stats/amount/yearly
GET http://localhost:8080/api/topups/stats/amount/yearly?year=2024

HTTP 200

# GET /api/topups/stats/amount/monthly/by-card
GET http://localhost:8080/api/topups/stats/amount/monthly/by-card?year=2024&cardNumber=4111111111111111

HTTP 200

# GET /api/topups/stats/amount/yearly/by-card
GET http://localhost:8080/api/topups/stats/amount/yearly/by-card?year=2024&cardNumber=4111111111111111

HTTP 200

# GET /api/topups/stats/method/monthly
GET http://localhost:8080/api/topups/stats/method/monthly?year=2024

HTTP 200

# GET /api/topups/stats/method/yearly
GET http://localhost:8080/api/topups/stats/method/yearly?year=2024

HTTP 200

# GET /api/topups/stats/method/monthly/by-card
GET http://localhost:8080/api/topups/stats/method/monthly/by-card?year=2024&cardNumber=4111111111111111

HTTP 200

# GET /api/topups/stats/method/yearly/by-card
GET http://localhost:8080/api/topups/stats/method/yearly/by-card?year=2024&cardNumber=4111111111111111

HTTP 200

# GET /api/topups/stats/status/monthly/success
GET http://localhost:8080/api/topups/stats/status/monthly/success?year=2024&month=1

HTTP 200

# GET /api/topups/stats/status/yearly/success
GET http://localhost:8080/api/topups/stats/status/yearly/success?year=2024

HTTP 200

# GET /api/topups/stats/status/monthly/failed
GET http://localhost:8080/api/topups/stats/status/monthly/failed?year=2024&month=1

HTTP 200

# GET /api/topups/stats/status/yearly/failed
GET http://localhost:8080/api/topups/stats/status/yearly/failed?year=2024

HTTP 200
