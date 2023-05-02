ALTER TABLE Registrierung ADD IF NOT EXISTS vollstaendigerImpfschutzTyp VARCHAR(50) NULL;
ALTER TABLE Registrierung_AUD ADD IF NOT EXISTS vollstaendigerImpfschutzTyp VARCHAR(50) NULL;

UPDATE Registrierung reg
SET reg.vollstaendigerImpfschutzTyp = 'VOLLSTAENDIG_VACME' WHERE reg.vollstaendigerImpfschutz = true && (reg.genesen = false || reg.genesen IS NULL);

UPDATE Registrierung reg
SET reg.vollstaendigerImpfschutzTyp = 'VOLLSTAENDIG_VACME_GENESEN' WHERE reg.vollstaendigerImpfschutz = true && (reg.genesen = true);



/*
-- UNDO:
ALTER TABLE Registrierung DROP COLUMN IF EXISTS vollstaendigerImpfschutzTyp;
ALTER TABLE Registrierung_AUD DROP COLUMN IF EXISTS vollstaendigerImpfschutzTyp;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.7__vollstaendigerImpfschutzTyp.sql';
*/


