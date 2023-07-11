ALTER TABLE Impfung
ADD COLUMN IF NOT EXISTS zeckenstich BIT NULL;

ALTER TABLE Impfung_AUD
ADD COLUMN IF NOT EXISTS zeckenstich BIT NULL;


/*
--UNDO
ALTER TABLE Impfung
	DROP COLUMN zeckenstich;
ALTER TABLE Impfung_AUD
	DROP COLUMN zeckenstich;


DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.5.5__addZeckenstichFlag.sql';

*/