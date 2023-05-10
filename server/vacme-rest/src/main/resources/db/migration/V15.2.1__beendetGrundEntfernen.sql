ALTER TABLE Registrierung DROP COLUMN IF EXISTS beendetGrund;
ALTER TABLE Registrierung_AUD DROP COLUMN IF EXISTS beendetGrund;

/**
Undo:

ALTER TABLE Registrierung ADD beendetGrund VARCHAR(50) NULL;
ALTER TABLE Registrierung_AUD ADD beendetGrund VARCHAR(50) NULL;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.2.1__beendetGrundEntfernen.sql';

 **/