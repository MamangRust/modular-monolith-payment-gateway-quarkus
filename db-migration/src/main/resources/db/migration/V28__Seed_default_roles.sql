-- Seed the roles used by registration, JWT claims, and gateway authorization.
INSERT INTO "roles" ("role_name")
VALUES ('ROLE_ADMIN'), ('ROLE_STAFF'), ('ROLE_USER')
ON CONFLICT ("role_name") DO NOTHING;
