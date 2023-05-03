ALTER TABLE Impfung
	ADD COLUMN schwanger BIT NULL;

ALTER TABLE Impfung_AUD
	ADD COLUMN schwanger BIT NULL;


/*
--UNDO
ALTER TABLE Impfung
	DROP COLUMN schwanger;
ALTER TABLE Impfung_AUD
	DROP COLUMN schwanger;


DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V14.0.5__schwanger.sql';

*/