# Auth E2E Tests - verify all auth endpoints are reachable and routed
# Auth status codes depend on state (lockout, missing user/role)
# We verify: gateway routes correctly, no 500/503/502

# POST /api/auth/register
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{"firstname":"John","lastname":"Doe","email":"john_e2e_888@test.com","password":"secret123","confirmPassword":"secret123"}

HTTP *

[Asserts]
status < 500

# POST /api/auth/login
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{"email":"john_e2e_888@test.com","password":"secret123"}

HTTP *

[Asserts]
status < 500

# POST /api/auth/verify
POST http://localhost:8080/api/auth/verify
Content-Type: application/json

{"code":"ABC123"}

HTTP *

[Asserts]
status < 500

# POST /api/auth/forgot-password
POST http://localhost:8080/api/auth/forgot-password
Content-Type: application/json

{"email":"john_e2e_888@test.com"}

HTTP *

[Asserts]
status < 500

# POST /api/auth/reset-password
POST http://localhost:8080/api/auth/reset-password
Content-Type: application/json

{"resetToken":"reset_token_123","password":"newsecret","confirmPassword":"newsecret"}

HTTP *

[Asserts]
status < 500

# POST /api/auth/refresh
POST http://localhost:8080/api/auth/refresh
Content-Type: application/json

{"refreshToken":"refresh_token_123"}

HTTP *

[Asserts]
status < 500

# GET /api/auth/me
GET http://localhost:8080/api/auth/me?userId=1

HTTP *

[Asserts]
status < 500
