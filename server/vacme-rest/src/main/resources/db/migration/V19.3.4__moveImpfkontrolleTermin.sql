# Move PersonenKontrolle to Impfdossier
ALTER TABLE Fragebogen
	DROP FOREIGN KEY IF EXISTS FK_fragebogen_personenkontrolle_id;
ALTER TABLE Fragebogen
	DROP INDEX IF EXISTS UC_Fragebogen_personenkontrolle;
# Da wir die Kolumne noch nicht droppen, muss sie nullable sein
ALTER TABLE Fragebogen MODIFY personenkontrolle_id VARCHAR(36) NULL;

ALTER TABLE Impfdossier
	ADD COLUMN IF NOT EXISTS personenkontrolle_id VARCHAR(36) NULL;
ALTER TABLE Impfdossier_AUD
	ADD COLUMN IF NOT EXISTS personenkontrolle_id VARCHAR(36) NULL;

ALTER TABLE Impfdossier
	ADD CONSTRAINT FK_impfdossier_personenkontrolle_id FOREIGN KEY IF NOT EXISTS (personenkontrolle_id) references Personenkontrolle(id);

ALTER TABLE Impfdossier
ADD CONSTRAINT UC_Impfdossier_personenkontrolle UNIQUE (personenkontrolle_id);

UPDATE Impfdossier DOS
SET personenkontrolle_id =
		(SELECT P.id
		 FROM Impfdossier D
			  INNER JOIN Registrierung R on D.registrierung_id = R.id
			  INNER JOIN Fragebogen F on R.id = F.registrierung_id
			  INNER JOIN Personenkontrolle P on F.personenkontrolle_id = P.id
		 WHERE D.id = DOS.id AND D.krankheitIdentifier = 'COVID')
WHERE DOS.krankheitIdentifier = 'COVID' AND personenkontrolle_id IS NULL;


/*
-- UNDO
ALTER TABLE Fragebogen MODIFY personenkontrolle_id VARCHAR(36) NOT NULL;
ALTER TABLE Fragebogen ADD CONSTRAINT UC_Fragebogen_personenkontrolle UNIQUE (personenkontrolle_id);
ALTER TABLE Fragebogen ADD CONSTRAINT FK_fragebogen_personenkontrolle_id FOREIGN KEY (personenkontrolle_id) references Personenkontrolle(id);
ALTER TABLE Impfdossier DROP FOREIGN KEY IF EXISTS FK_impfdossier_personenkontrolle_id;
ALTER TABLE Impfdossier DROP INDEX IF EXISTS UC_Impfdossier_personenkontrolle;
ALTER TABLE Impfdossier DROP COLUMN personenkontrolle_id;
ALTER TABLE Impfdossier_AUD DROP COLUMN personenkontrolle_id;

DELETE from flyway_schema_history where script = 'db/migration/V19.3.4__moveImpfkontrolleTermin.sql';
*/