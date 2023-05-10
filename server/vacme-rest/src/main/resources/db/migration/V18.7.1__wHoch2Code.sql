ALTER TABLE Impfstoff ADD COLUMN IF NOT EXISTS wHoch2Code VARCHAR(255) NULL;
ALTER TABLE Impfstoff_AUD ADD COLUMN IF NOT EXISTS wHoch2Code VARCHAR(255) NULL;

UPDATE Impfstoff SET wHoch2Code = 'Spikevax®' WHERE id = 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643'; # Moderna
UPDATE Impfstoff SET wHoch2Code = 'Comirnaty®' WHERE id = '141fca55-ab78-4c0e-a2fd-edf2fe4e9b30'; # Pfitzer
UPDATE Impfstoff SET wHoch2Code = 'COVID-19 Vaccine Janssen' WHERE id = '12c5d49e-ce77-464a-a951-3c840e5a1d1b'; # Janssen
UPDATE Impfstoff SET wHoch2Code = 'Comirnaty® 10 Mikrogramm/Dosis (Kinderimpfung)' WHERE id = '4ebb48c1-cc96-4a8e-9832-77092bb968db'; # Pfizer Kinder


/*
-- UNDO:

ALTER TABLE Impfstoff DROP COLUMN wHoch2Code;
ALTER TABLE Impfstoff_AUD DROP COLUMN wHoch2Code;

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V18.7.1__wHoch2Code.sql';

*/