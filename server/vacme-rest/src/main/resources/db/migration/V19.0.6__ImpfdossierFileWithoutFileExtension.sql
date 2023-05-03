ALTER TABLE ImpfdossierFile DROP IF EXISTS fileExtension;

/*
-- UNDO:

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.0.6__ImpfdossierFileWithoutFileExtension';
*/