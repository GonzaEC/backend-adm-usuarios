-- src/main/resources/db/migration/V5__add_soft_delete_to_all_tables.sql

-- Añadir campos de auditoría a permissions ya que ahora hereda de Auditable
ALTER TABLE permissions
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now(),
ADD COLUMN updated_at TIMESTAMP;

-- Añadir el flag de soft delete a todas las tablas
ALTER TABLE users ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE roles ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE permissions ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE projects ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE user_projects ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE investments ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE wallets ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
