-- src/main/resources/db/migration/V2__seed_roles_permissions.sql

-- =========================
-- PERMISSIONS
-- =========================
INSERT INTO permissions (name, description) VALUES
('project:read',   'Permiso para leer proyectos'),
('project:create', 'Permiso para crear proyectos'),
('project:update', 'Permiso para modificar proyectos'),
('project:delete', 'Permiso para eliminar proyectos'),
('invest:create',  'Permiso para invertir'),
('user:read',      'Permiso para leer usuarios'),
('user:update',    'Permiso para modificar usuarios'),
('user:delete',    'Permiso para eliminar usuarios');

-- =========================
-- ROLES
-- =========================
INSERT INTO roles (name, description, active, created_at)
VALUES
('BASIC',     'Usuario básico',        true, now()),
('INVESTOR',  'Usuario inversor',      true, now()),
('DEVELOPER', 'Usuario desarrollador', true, now()),
('ADMIN',      'Administrador',        true, now());

-- =========================
-- RELACIONES ROLE-PERMISSION
-- =========================

-- BASIC → PROJECT_READ
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'BASIC' AND p.name = 'PROJECT_READ';

-- INVESTOR → PROJECT_READ, INVEST_CREATE
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'INVESTOR'
AND p.name IN ('PROJECT_READ', 'INVEST_CREATE');

-- DEVELOPER → PROJECT_READ, CREATE, UPDATE
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'DEVELOPER'
AND p.name IN ('PROJECT_READ', 'PROJECT_CREATE', 'PROJECT_UPDATE');

-- ADMIN → TODOS
INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN';