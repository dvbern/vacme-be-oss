ALTER TABLE Impfschutz ADD IF NOT EXISTS erlaubteImpfstoffe VARCHAR(720) NULL;
ALTER TABLE Impfschutz_AUD ADD IF NOT EXISTS erlaubteImpfstoffe VARCHAR(720) NULL;

/*
-- UNDO:
ALTER TABLE Impfschutz DROP COLUMN IF EXISTS erlaubteImpfstoffe;
ALTER TABLE Impfschutz_AUD DROP COLUMN IF EXISTS vollstaendigerImpfschutzTyp;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.10__updateImpfschutzAddImpfstoffe.sql';
*/



