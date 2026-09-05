# Card E2E Tests - verify all card endpoints are reachable and routed

# GET /api/cards
GET http://localhost:8080/api/cards?page=1&size=10&search=admin

HTTP 200

# GET /api/cards/active
GET http://localhost:8080/api/cards/active?page=1&size=10&search=admin

HTTP 200

# GET /api/cards/trashed
GET http://localhost:8080/api/cards/trashed?page=1&size=10&search=admin

HTTP 200

# GET /api/cards/{id}
GET http://localhost:8080/api/cards/1

HTTP 404

# GET /api/cards/by-user/{user_id}
GET http://localhost:8080/api/cards/by-user/1

HTTP 404

# GET /api/cards/by-number/{card_number}
GET http://localhost:8080/api/cards/by-number/4111111111111111

HTTP 404

# POST /api/cards
POST http://localhost:8080/api/cards
Content-Type: application/json

{"userId":1,"cardType":"credit","expireDate":"12/25","cvv":"123","cardProvider":"Visa"}

HTTP 400

# PUT /api/cards/{id}
PUT http://localhost:8080/api/cards/1
Content-Type: application/json

{"userId":1,"cardType":"debit","expireDate":"12/26","cvv":"456","cardProvider":"Mastercard"}

HTTP 400

# DELETE /api/cards/{id}
DELETE http://localhost:8080/api/cards/1

HTTP 400

# DELETE /api/cards/{id}/permanent
DELETE http://localhost:8080/api/cards/1/permanent

HTTP 400

# POST /api/cards/restore/{id}
POST http://localhost:8080/api/cards/restore/1

HTTP 404

# POST /api/cards/restore-all
POST http://localhost:8080/api/cards/restore-all

HTTP 400

# POST /api/cards/delete-all
POST http://localhost:8080/api/cards/delete-all

HTTP 400

# GET /api/cards/stats/balance/monthly
GET http://localhost:8080/api/cards/stats/balance/monthly?year=2024

HTTP 200

# GET /api/cards/stats/balance/yearly
GET http://localhost:8080/api/cards/stats/balance/yearly?year=2024

HTTP 200

# GET /api/cards/stats/topup/monthly
GET http://localhost:8080/api/cards/stats/topup/monthly?year=2024

HTTP 200

# GET /api/cards/stats/topup/yearly
GET http://localhost:8080/api/cards/stats/topup/yearly?year=2024

HTTP 200

# GET /api/cards/stats/transaction/monthly
GET http://localhost:8080/api/cards/stats/transaction/monthly?year=2024

HTTP 200

# GET /api/cards/stats/transaction/yearly
GET http://localhost:8080/api/cards/stats/transaction/yearly?year=2024

HTTP 200

# GET /api/cards/stats/withdraw/monthly
GET http://localhost:8080/api/cards/stats/withdraw/monthly?year=2024

HTTP 200

# GET /api/cards/stats/withdraw/yearly
GET http://localhost:8080/api/cards/stats/withdraw/yearly?year=2024

HTTP 200

# GET /api/cards/dashboard
GET http://localhost:8080/api/cards/dashboard

HTTP 200

# GET /api/cards/dashboard/{card_number}
GET http://localhost:8080/api/cards/dashboard/4111111111111111

HTTP 200
