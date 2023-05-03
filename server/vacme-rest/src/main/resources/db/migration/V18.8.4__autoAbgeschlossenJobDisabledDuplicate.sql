-- Duplicate of 19.0.12__autoAbgeschlossenJobDisabled, für rückvollziehbarkeitshalber
INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('2d265cfc-a7e7-4441-bd9f-c559890ba70b', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
		'VACME_REGISTRIERUNG_AUTO_ABSCHLIESSEN_JOB_DISABLED', 'false');



/*
-- UNDO

DELETE FROM ApplicationProperty WHERE name = 'VACME_REGISTRIERUNG_AUTO_ABSCHLIESSEN_JOB_DISABLED';

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V18.8.4__autoAbgeschlossenJobDisabledDuplicate.sql';
*/