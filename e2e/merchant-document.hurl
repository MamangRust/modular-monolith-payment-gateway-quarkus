# Merchant Document E2E Tests - verify all merchant document endpoints are reachable

# GET /api/merchant-documents
GET http://localhost:8080/api/merchant-documents?page=1&size=10&search=admin

HTTP 200

# GET /api/merchant-documents/active
GET http://localhost:8080/api/merchant-documents/active?page=1&size=10&search=admin

HTTP 200

# GET /api/merchant-documents/trashed
GET http://localhost:8080/api/merchant-documents/trashed?page=1&size=10&search=admin

HTTP 200

# GET /api/merchant-documents/{id}
GET http://localhost:8080/api/merchant-documents/1

HTTP 404

# POST /api/merchant-documents
POST http://localhost:8080/api/merchant-documents
Content-Type: application/json

{"merchantId":1,"documentType":"license","documentUrl":"http://example.com/doc.pdf"}

HTTP 404

# PUT /api/merchant-documents/{id}
PUT http://localhost:8080/api/merchant-documents/1
Content-Type: application/json

{"merchantId":1,"documentType":"license","documentUrl":"http://example.com/doc.pdf","note":"Updated","status":"approved"}

HTTP 404

# DELETE /api/merchant-documents/{id}
DELETE http://localhost:8080/api/merchant-documents/1

HTTP 404

# POST /api/merchant-documents/trash/{id}
POST http://localhost:8080/api/merchant-documents/trash/1

HTTP 404

# POST /api/merchant-documents/restore/{id}
POST http://localhost:8080/api/merchant-documents/restore/1

HTTP 404
