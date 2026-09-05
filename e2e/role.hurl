# Role E2E Tests - verify all role endpoints are reachable and routed

# GET /api/roles
GET http://localhost:8080/api/roles?page=1&size=10&search=admin

HTTP 200

# GET /api/roles/{id}
GET http://localhost:8080/api/roles/1

HTTP 200
