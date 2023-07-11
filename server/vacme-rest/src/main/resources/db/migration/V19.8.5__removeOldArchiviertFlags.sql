ALTER TABLE Registrierung DROP COLUMN timestampArchiviert;
ALTER TABLE Registrierung_AUD DROP COLUMN timestampArchiviert;

/*
-- UNDO
ALTER TABLE Registrierung ADD COLUMN IF NOT EXISTS timestampArchiviert DATETIME(6) NULL;
ALTER TABLE Registrierung_AUD ADD COLUMN IF NOT EXISTS timestampArchiviert DATETIME(6) NULL;

DELETE from flyway_schema_history where script = 'db/migration/V19.8.5__removeOldArchiviertFlags.sql';
*/
