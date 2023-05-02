ALTER TABLE Impfslot ADD COLUMN IF NOT EXISTS krankheitIdentifier VARCHAR(50) NULL;
ALTER TABLE Impfslot_AUD ADD COLUMN IF NOT EXISTS krankheitIdentifier VARCHAR(50) NULL;

UPDATE Impfslot SET krankheitIdentifier = 'COVID' where krankheitIdentifier is null;

ALTER TABLE Impfslot MODIFY krankheitIdentifier VARCHAR(50) NOT NULL;


/*
-- UNDO:

ALTER TABLE Impfslot DROP COLUMN krankheitIdentifier;
ALTER TABLE Impfslot_AUD DROP COLUMN krankheitIdentifier;

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.0.2__addImpfslotKrankheitAndMigrate.sql';
*/
