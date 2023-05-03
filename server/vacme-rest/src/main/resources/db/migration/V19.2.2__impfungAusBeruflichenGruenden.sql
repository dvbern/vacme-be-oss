ALTER TABLE Impfung
	ADD COLUMN impfungAusBeruflichenGruenden BIT NULL;

ALTER TABLE Impfung_AUD
	ADD COLUMN impfungAusBeruflichenGruenden BIT NULL;


/*
--UNDO
ALTER TABLE Impfung
	DROP COLUMN impfungAusBeruflichenGruenden;
ALTER TABLE Impfung_AUD
	DROP COLUMN impfungAusBeruflichenGruenden;


DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.2.2__impfungAusBeruflichenGruenden.sql';

*/