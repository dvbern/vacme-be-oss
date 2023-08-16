ALTER TABLE Impfung
	ADD COLUMN kontaktMitPersonAusRisikogruppe BIT NULL;

ALTER TABLE Impfung_AUD
	ADD COLUMN kontaktMitPersonAusRisikogruppe BIT NULL;


/*
--UNDO
ALTER TABLE Impfung
	DROP COLUMN kontaktMitPersonAusRisikogruppe;
ALTER TABLE Impfung_AUD
	DROP COLUMN kontaktMitPersonAusRisikogruppe;


DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.2.3__kontaktMitPersonAusRisikogruppe.sql';

*/