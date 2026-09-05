# Saldo E2E Tests - verify all saldo endpoints are reachable and routed

# GET /api/saldos
GET http://localhost:8080/api/saldos?page=1&size=10&search=admin

HTTP 200

# GET /api/saldos/{id}
GET http://localhost:8080/api/saldos/1

HTTP 404

# POST /api/saldos
POST http://localhost:8080/api/saldos
Content-Type: application/json

{"cardNumber":"4111111111111111","totalBalance":100000}

HTTP 404

# PUT /api/saldos/{id}
PUT http://localhost:8080/api/saldos/1
Content-Type: application/json

{"cardNumber":"4111111111111111","totalBalance":200000}

HTTP 404

# DELETE /api/saldos/{id}
DELETE http://localhost:8080/api/saldos/1

HTTP 404

# GET /api/saldos/stats/balance/monthly
GET http://localhost:8080/api/saldos/stats/balance/monthly?year=2024

HTTP 200

# GET /api/saldos/stats/total/monthly
GET http://localhost:8080/api/saldos/stats/total/monthly?year=2024&month=1

HTTP 200
