ALTER TABLE ImpfdossierFile CHANGE COLUMN regenerated abgeholt BIT NOT NULL DEFAULT FALSE;
ALTER TABLE RegistrierungFile CHANGE COLUMN regenerated abgeholt BIT NOT NULL DEFAULT FALSE;

/*
-- UNDO

ALTER TABLE ImpfdossierFile CHANGE COLUMN abgeholt regenerated BIT NOT NULL DEFAULT FALSE;
ALTER TABLE RegistrierungFile CHANGE COLUMN abgeholt regenerated BIT NOT NULL DEFAULT FALSE;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.2.1__impfdossierFileRenameRegenerate.sql';
*/