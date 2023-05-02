ALTER TABLE Impfung ADD COLUMN IF NOT EXISTS schnellschemaGesetztFuerImpfung BIT NULL;
UPDATE Impfung SET schnellschemaGesetztFuerImpfung = FALSE WHERE schnellschemaGesetztFuerImpfung IS NULL;
ALTER TABLE Impfung MODIFY COLUMN schnellschemaGesetztFuerImpfung BIT NOT NULL;

ALTER TABLE Impfung_AUD ADD COLUMN IF NOT EXISTS schnellschemaGesetztFuerImpfung BIT NULL;

/*
-- UNDO
ALTER TABLE Impfung DROP COLUMN schnellschemaGesetztFuerImpfung;
ALTER TABLE Impfung_AUD DROP COLUMN schnellschemaGesetztFuerImpfung;

DELETE from flyway_schema_history where script = 'db/migration/V19.5.4__addSchnellschemaInImpfung.sql';
*/
