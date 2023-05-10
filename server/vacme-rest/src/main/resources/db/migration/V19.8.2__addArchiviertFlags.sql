ALTER TABLE Impfung ADD COLUMN IF NOT EXISTS archiviertAm DATETIME(6) NULL;
ALTER TABLE Impfung_AUD ADD COLUMN IF NOT EXISTS archiviertAm DATETIME(6) NULL;

ALTER TABLE Impfung ADD COLUMN IF NOT EXISTS sollArchiviertWerden BIT NULL;
UPDATE Impfung SET sollArchiviertWerden = FALSE WHERE sollArchiviertWerden IS NULL;
ALTER TABLE Impfung MODIFY sollArchiviertWerden BIT NOT NULL;
ALTER TABLE Impfung_AUD ADD COLUMN IF NOT EXISTS sollArchiviertWerden BIT NULL;



/*
-- UNDO
ALTER TABLE Impfung DROP COLUMN archiviertAm;
ALTER TABLE Impfung_AUD DROP COLUMN archiviertAm;

ALTER TABLE Impfung DROP COLUMN sollArchiviertWerden;
ALTER TABLE Impfung_AUD DROP COLUMN sollArchiviertWerden;

DELETE from flyway_schema_history where script = 'db/migration/V19.8.2__addArchiviertFlags.sql';
*/
