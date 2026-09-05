# Transfer E2E Tests - verify all transfer endpoints are reachable and routed

# GET /api/transfers
GET http://localhost:8080/api/transfers?page=1&size=10&search=admin

HTTP 200

# GET /api/transfers/active
GET http://localhost:8080/api/transfers/active?page=1&size=10&search=admin

HTTP 200

# GET /api/transfers/trashed
GET http://localhost:8080/api/transfers/trashed?page=1&size=10&search=admin

HTTP 200

# GET /api/transfers/{id}
GET http://localhost:8080/api/transfers/1

HTTP 404

# GET /api/transfers/from/{cardNumber}
GET http://localhost:8080/api/transfers/from/4111111111111111

HTTP 200

# GET /api/transfers/to/{cardNumber}
GET http://localhost:8080/api/transfers/to/4222222222222222

HTTP 200

# POST /api/transfers
POST http://localhost:8080/api/transfers
Content-Type: application/json

{"transferFrom":"4111111111111111","transferTo":"4222222222222222","transferAmount":30000}

HTTP 400

# GET /api/transfers/stats/amount/monthly
GET http://localhost:8080/api/transfers/stats/amount/monthly?year=2024

HTTP 200

# GET /api/transfers/stats/amount/yearly
GET http://localhost:8080/api/transfers/stats/amount/yearly?year=2024

HTTP 200

# GET /api/transfers/stats/amount/monthly/from-card
GET http://localhost:8080/api/transfers/stats/amount/monthly/from-card?year=2024&cardNumber=4111111111111111

HTTP 200

# GET /api/transfers/stats/amount/monthly/to-card
GET http://localhost:8080/api/transfers/stats/amount/monthly/to-card?year=2024&cardNumber=4222222222222222

HTTP 200

# GET /api/transfers/stats/amount/yearly/from-card
GET http://localhost:8080/api/transfers/stats/amount/yearly/from-card?year=2024&cardNumber=4111111111111111

HTTP 200

# GET /api/transfers/stats/amount/yearly/to-card
GET http://localhost:8080/api/transfers/stats/amount/yearly/to-card?year=2024&cardNumber=4222222222222222

HTTP 200
