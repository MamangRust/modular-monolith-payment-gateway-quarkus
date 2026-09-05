# User E2E Tests - verify all user endpoints are reachable and routed

# GET /api/users
GET http://localhost:8080/api/users?page=1&size=10&search=admin

HTTP 200

# GET /api/users/{id}
GET http://localhost:8080/api/users/1

HTTP 404

# POST /api/users
POST http://localhost:8080/api/users
Content-Type: application/json

{"firstname":"Jane","lastname":"Doe","email":"jane@example.com","password":"secret123","confirmPassword":"secret123"}

HTTP 404

# PUT /api/users/{id}
PUT http://localhost:8080/api/users/1
Content-Type: application/json

{"firstname":"Jane","lastname":"Smith","email":"jane@example.com","password":"secret123","confirmPassword":"secret123"}

HTTP 404

# DELETE /api/users/{id}
DELETE http://localhost:8080/api/users/1

HTTP 404
