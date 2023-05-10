ALTER TABLE OrtDerImpfung ADD IF NOT EXISTS deaktiviert BIT NOT NULL DEFAULT FALSE;
ALTER TABLE OrtDerImpfung_AUD ADD IF NOT EXISTS deaktiviert BIT NULL;

/**
Undo:

ALTER TABLE OrtDerImpfung DROP IF EXISTS deaktiviert;
ALTER TABLE OrtDerImpfung_AUD DROP IF EXISTS deaktiviert;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V16.0.1__odiDeaktivieren.sql';

 **/

