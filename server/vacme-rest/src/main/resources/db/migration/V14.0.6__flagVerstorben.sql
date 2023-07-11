ALTER TABLE Registrierung
	ADD COLUMN IF NOT EXISTS verstorben BIT NULL;
ALTER TABLE Registrierung_AUD
	ADD COLUMN IF NOT EXISTS verstorben BIT NULL;

/*
-- UNDO:
ALTER TABLE Registrierung
	DROP COLUMN verstorben;
ALTER TABLE Registrierung_AUD
	DROP COLUMN verstorben;

DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V14.0.6__flagVerstorben.sql';
*/