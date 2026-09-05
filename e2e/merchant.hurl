# Merchant E2E Tests - verify all merchant endpoints are reachable and routed

# GET /api/merchants
GET http://localhost:8080/api/merchants?page=1&size=10&search=admin

HTTP 200

# GET /api/merchants/{id}
GET http://localhost:8080/api/merchants/1

HTTP 404

# POST /api/merchants
POST http://localhost:8080/api/merchants
Content-Type: application/json

{"name":"Merchant Test","userId":1}

HTTP 404

# PUT /api/merchants/{id}
PUT http://localhost:8080/api/merchants/1
Content-Type: application/json

{"name":"Merchant Updated","userId":1,"status":"active"}

HTTP 404

# DELETE /api/merchants/{id}
DELETE http://localhost:8080/api/merchants/1

HTTP 404

# GET /api/merchants/transactions
GET http://localhost:8080/api/merchants/transactions?page=1&size=10&search=admin&merchantId=1

HTTP 400

# GET /api/merchants/transactions/by-id
GET http://localhost:8080/api/merchants/transactions/by-id?page=1&size=10&search=admin&id=1

HTTP 400

# GET /api/merchants/transactions/by-api-key
GET http://localhost:8080/api/merchants/transactions/by-api-key?page=1&size=10&search=admin&apiKey=ak_123

HTTP 400

# GET /api/merchants/stats/amount/monthly
GET http://localhost:8080/api/merchants/stats/amount/monthly?year=2024

HTTP 200

# GET /api/merchants/stats/amount/yearly
GET http://localhost:8080/api/merchants/stats/amount/yearly?year=2024

HTTP 200

# GET /api/merchants/stats/amount/by-id
GET http://localhost:8080/api/merchants/stats/amount/by-id?year=2024&merchantId=1

HTTP 200

# GET /api/merchants/stats/amount/yearly/by-id
GET http://localhost:8080/api/merchants/stats/amount/yearly/by-id?year=2024&merchantId=1

HTTP 200

# GET /api/merchants/stats/amount/by-api-key
GET http://localhost:8080/api/merchants/stats/amount/by-api-key?year=2024&apiKey=ak_123

HTTP 200

# GET /api/merchants/stats/amount/yearly/by-api-key
GET http://localhost:8080/api/merchants/stats/amount/yearly/by-api-key?year=2024&apiKey=ak_123

HTTP 200

# GET /api/merchants/stats/method/monthly
GET http://localhost:8080/api/merchants/stats/method/monthly?year=2024

HTTP 200

# GET /api/merchants/stats/method/yearly
GET http://localhost:8080/api/merchants/stats/method/yearly?year=2024

HTTP 200

# GET /api/merchants/stats/method/monthly/by-id
GET http://localhost:8080/api/merchants/stats/method/monthly/by-id?year=2024&merchantId=1

HTTP 200

# GET /api/merchants/stats/method/yearly/by-id
GET http://localhost:8080/api/merchants/stats/method/yearly/by-id?year=2024&merchantId=1

HTTP 200

# GET /api/merchants/stats/method/monthly/by-api-key
GET http://localhost:8080/api/merchants/stats/method/monthly/by-api-key?year=2024&apiKey=ak_123

HTTP 200

# GET /api/merchants/stats/method/yearly/by-api-key
GET http://localhost:8080/api/merchants/stats/method/yearly/by-api-key?year=2024&apiKey=ak_123

HTTP 200

# GET /api/merchants/stats/total-amount/monthly
GET http://localhost:8080/api/merchants/stats/total-amount/monthly?year=2024

HTTP 200

# GET /api/merchants/stats/total-amount/monthly/by-id
GET http://localhost:8080/api/merchants/stats/total-amount/monthly/by-id?year=2024&merchantId=1

HTTP 200

# GET /api/merchants/stats/total-amount/yearly/by-id
GET http://localhost:8080/api/merchants/stats/total-amount/yearly/by-id?year=2024&merchantId=1

HTTP 200

# GET /api/merchants/stats/total-amount/monthly/by-api-key
GET http://localhost:8080/api/merchants/stats/total-amount/monthly/by-api-key?year=2024&apiKey=ak_123

HTTP 200

# GET /api/merchants/stats/total-amount/yearly/by-api-key
GET http://localhost:8080/api/merchants/stats/total-amount/yearly/by-api-key?year=2024&apiKey=ak_123

HTTP 200
