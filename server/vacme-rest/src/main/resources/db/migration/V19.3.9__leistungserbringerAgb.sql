ALTER TABLE Impfdossier ADD COLUMN IF NOT EXISTS leistungerbringerAgbConfirmationNeeded BIT NOT NULL DEFAULT TRUE;
ALTER TABLE Impfdossier_AUD ADD COLUMN IF NOT EXISTS leistungerbringerAgbConfirmationNeeded BIT NULL DEFAULT TRUE;

/*
-- UNDO
ALTER TABLE Impfdossier DROP COLUMN IF EXISTS leistungerbringerAgbConfirmationNeeded;
ALTER TABLE Impfdossier_AUD DROP COLUMN IF EXISTS leistungerbringerAgbConfirmationNeeded;

DELETE from flyway_schema_history where script = 'db/migration/V19.3.9__leistungserbringerAgb.sql';
*/