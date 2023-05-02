# Move identification to Registrierung
ALTER TABLE Registrierung
	ADD COLUMN IF NOT EXISTS identifikationsnummer VARCHAR(255) NULL;

ALTER TABLE Registrierung_AUD
	ADD COLUMN IF NOT EXISTS identifikationsnummer VARCHAR(255) NULL;

UPDATE Registrierung REG
SET identifikationsnummer =
		(SELECT P.identifikationsnummer
		 FROM Fragebogen F
			  INNER JOIN Personenkontrolle P ON F.personenkontrolle_id = P.id
		 WHERE F.registrierung_id = REG.id)
WHERE REG.identifikationsnummer IS NULL;


/*
-- UNDO
ALTER TABLE Registrierung DROP COLUMN identifikationsnummer;
ALTER TABLE Registrierung_AUD DROP COLUMN identifikationsnummer;

DELETE from flyway_schema_history where script = 'db/migration/V19.3.3__moveIdentifikationsnummer.sql';
*/