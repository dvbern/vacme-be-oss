ALTER TABLE Registrierung DROP COLUMN IF EXISTS generateZertifikat;
ALTER TABLE Registrierung_AUD DROP COLUMN IF EXISTS generateZertifikat;

/**
Undo:

ALTER TABLE Registrierung ADD generateZertifikat BIT NOT NULL;
ALTER TABLE Registrierung_AUD ADD generateZertifikat BIT;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.2.2__registrierungGenerateZertifikatEntfernen.sql';

 **/