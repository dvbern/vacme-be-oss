ALTER TABLE Impfung
	ADD COLUMN risikoreichesSexualleben BIT NULL;

ALTER TABLE Impfung_AUD
	ADD COLUMN risikoreichesSexualleben BIT NULL;


/*
--UNDO
ALTER TABLE Impfung
	DROP COLUMN risikoreichesSexualleben;
ALTER TABLE Impfung_AUD
	DROP COLUMN risikoreichesSexualleben;


DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.2.1__risikoreichesSexualleben.sql';

*/