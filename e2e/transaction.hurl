# Transaction E2E Tests - verify all transaction endpoints are reachable and routed

# GET /api/transactions
GET http://localhost:8080/api/transactions?page=1&size=10&search=admin

HTTP 200

# GET /api/transactions/by-card
GET http://localhost:8080/api/transactions/by-card?cardNumber=4111111111111111&page=1&size=10&search=admin

HTTP 200

# GET /api/transactions/active
GET http://localhost:8080/api/transactions/active?page=1&size=10&search=admin

HTTP 200

# GET /api/transactions/trashed
GET http://localhost:8080/api/transactions/trashed?page=1&size=10&search=admin

HTTP 200

# GET /api/transactions/{id}
GET http://localhost:8080/api/transactions/1

HTTP 404

# GET /api/transactions/merchant/{merchantId}
GET http://localhost:8080/api/transactions/merchant/1

HTTP 200

# POST /api/transactions
POST http://localhost:8080/api/transactions
Content-Type: application/json

{"apiKey":"ak_123","cardNumber":"4111111111111111","amount":25000,"paymentMethod":"credit_card","merchantId":1}

HTTP 400

# GET /api/transactions/stats/amount/monthly
GET http://localhost:8080/api/transactions/stats/amount/monthly?year=2024

HTTP 200

# GET /api/transactions/stats/amount/yearly
GET http://localhost:8080/api/transactions/stats/amount/yearly?year=2024

HTTP 200

# GET /api/transactions/stats/amount/monthly/by-card
GET http://localhost:8080/api/transactions/stats/amount/monthly/by-card?year=2024&cardNumber=4111111111111111

HTTP 200

# GET /api/transactions/stats/amount/yearly/by-card
GET http://localhost:8080/api/transactions/stats/amount/yearly/by-card?year=2024&cardNumber=4111111111111111

HTTP 200

# GET /api/transactions/stats/method/monthly
GET http://localhost:8080/api/transactions/stats/method/monthly?year=2024

HTTP 200

# GET /api/transactions/stats/method/yearly
GET http://localhost:8080/api/transactions/stats/method/yearly?year=2024

HTTP 200

# GET /api/transactions/stats/method/monthly/by-card
GET http://localhost:8080/api/transactions/stats/method/monthly/by-card?year=2024&cardNumber=4111111111111111

HTTP 200

# GET /api/transactions/stats/method/yearly/by-card
GET http://localhost:8080/api/transactions/stats/method/yearly/by-card?year=2024&cardNumber=4111111111111111

HTTP 200
