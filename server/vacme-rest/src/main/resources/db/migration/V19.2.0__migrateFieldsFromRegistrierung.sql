# 1. Neue Spalten erstellen (nullable)

ALTER TABLE Impfdossier ADD IF NOT EXISTS nichtVerwalteterOdiSelected BIT NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS selbstzahler BIT NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS timestampZuletztAbgeschlossen TIMESTAMP NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS vollstaendigerImpfschutzTyp VARCHAR(50) NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS genesen BIT NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS positivGetestetDatum DATE NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS zweiteImpfungVerzichtetGrund VARCHAR(2000) NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS zweiteImpfungVerzichtetZeit TIMESTAMP NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS abgesagteTermine_id VARCHAR(36) NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS gewuenschterOdi_id VARCHAR(36) NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS impftermin1_id VARCHAR(36) NULL;
ALTER TABLE Impfdossier ADD IF NOT EXISTS impftermin2_id VARCHAR(36) NULL;

ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS nichtVerwalteterOdiSelected BIT NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS selbstzahler BIT NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS timestampZuletztAbgeschlossen TIMESTAMP NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS vollstaendigerImpfschutzTyp VARCHAR(50) NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS genesen BIT NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS positivGetestetDatum DATE NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS zweiteImpfungVerzichtetGrund VARCHAR(2000) NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS zweiteImpfungVerzichtetZeit TIMESTAMP NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS abgesagteTermine_id VARCHAR(36) NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS gewuenschterOdi_id VARCHAR(36) NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS impftermin1_id VARCHAR(36) NULL;
ALTER TABLE Impfdossier_AUD ADD IF NOT EXISTS impftermin2_id VARCHAR(36) NULL;

# Alte Indexe und FKs entfernen



ALTER TABLE Registrierung DROP FOREIGN KEY IF EXISTS FK_registrierung_abgesagtetermine_id;
ALTER TABLE Registrierung DROP FOREIGN KEY IF EXISTS FK_registrierung_impfzentrum_id;
ALTER TABLE Registrierung DROP FOREIGN KEY IF EXISTS FK_registrierung_impftermin1_id;
ALTER TABLE Registrierung DROP FOREIGN KEY IF EXISTS FK_registrierung_impftermin2_id;

ALTER TABLE Registrierung DROP INDEX IF EXISTS FK_registrierung_abgesagtetermine_id;
ALTER TABLE Registrierung DROP INDEX IF EXISTS FK_registrierung_impfzentrum_id;
ALTER TABLE Registrierung DROP INDEX IF EXISTS IX_Registrierung_impftermin1;
ALTER TABLE Registrierung DROP INDEX IF EXISTS IX_Registrierung_impftermin2;

ALTER TABLE Registrierung DROP CONSTRAINT IF EXISTS UC_Registrierung_impftermin1;
ALTER TABLE Registrierung DROP CONSTRAINT IF EXISTS UC_Registrierung_impftermin2;

# Migration der Daten
# Wir migrieren die werte nur fuer Covid weil wir aktuell annehmen dass sie fuer Affenpocken noch nicht relevant sind
UPDATE Impfdossier DOS SET nichtVerwalteterOdiSelected =
	(SELECT R.nichtVerwalteterOdiSelected FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.nichtVerwalteterOdiSelected is null;

UPDATE Impfdossier DOS SET selbstzahler =
	(SELECT R.selbstzahler FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.selbstzahler is null;

UPDATE Impfdossier DOS SET timestampZuletztAbgeschlossen =
	(SELECT R.timestampZuletztAbgeschlossen FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.timestampZuletztAbgeschlossen is null;

UPDATE Impfdossier DOS SET vollstaendigerImpfschutzTyp =
	(SELECT R.vollstaendigerImpfschutzTyp FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.vollstaendigerImpfschutzTyp is null;

UPDATE Impfdossier DOS SET genesen =
	(SELECT R.genesen FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.genesen is null;

UPDATE Impfdossier DOS SET positivGetestetDatum =
	(SELECT R.positivGetestetDatum FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.positivGetestetDatum is null;

UPDATE Impfdossier DOS SET zweiteImpfungVerzichtetGrund =
	(SELECT R.zweiteImpfungVerzichtetGrund FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.zweiteImpfungVerzichtetGrund is null;

UPDATE Impfdossier DOS SET zweiteImpfungVerzichtetZeit =
	(SELECT R.zweiteImpfungVerzichtetZeit FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.zweiteImpfungVerzichtetZeit is null;

UPDATE Impfdossier DOS SET abgesagteTermine_id =
	(SELECT R.abgesagteTermine_id FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.abgesagteTermine_id is null;

UPDATE Impfdossier DOS SET gewuenschterOdi_id =
	(SELECT R.gewuenschterOdi_id FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.gewuenschterOdi_id is null;

UPDATE Impfdossier DOS SET impftermin1_id =
	(SELECT R.impftermin1_id FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.impftermin1_id is null;

UPDATE Impfdossier DOS SET impftermin2_id =
	(SELECT R.impftermin2_id FROM Impfdossier D INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.impftermin2_id is null;

# Jetzt sind die Daten fuer COVID migriert. Defaults setzen fuer AFFENPOCKEN

UPDATE Impfdossier set nichtVerwalteterOdiSelected = FALSE WHERE nichtVerwalteterOdiSelected IS NULL AND krankheitIdentifier = 'AFFENPOCKEN';
UPDATE Impfdossier set selbstzahler = FALSE WHERE selbstzahler IS NULL AND krankheitIdentifier = 'AFFENPOCKEN';
UPDATE Impfdossier set genesen = FALSE WHERE genesen IS NULL AND krankheitIdentifier = 'AFFENPOCKEN';

# Die zwingenden Felder NotNull machen

ALTER TABLE Impfdossier MODIFY nichtVerwalteterOdiSelected BIT NOT NULL;
ALTER TABLE Impfdossier MODIFY selbstzahler BIT NOT NULL;
ALTER TABLE Impfdossier MODIFY genesen BIT NOT NULL;

# Neue Indexe und FKs erstellen

CREATE INDEX IF NOT EXISTS IX_Impfdossier_impftermin1  ON Impfdossier(impftermin1_id, id);
CREATE INDEX IF NOT EXISTS  IX_Impfdossier_impftermin2 ON Impfdossier(impftermin2_id, id);

ALTER TABLE Impfdossier ADD CONSTRAINT UC_Impfdossier_impftermin1 UNIQUE (impftermin1_id);
ALTER TABLE Impfdossier ADD CONSTRAINT UC_Impfdossier_impftermin2 UNIQUE (impftermin2_id);

ALTER TABLE Impfdossier ADD CONSTRAINT FK_impfdossier_abgesagtetermine_id FOREIGN KEY IF NOT EXISTS (abgesagteTermine_id) REFERENCES AbgesagteTermine(id);
ALTER TABLE Impfdossier ADD CONSTRAINT FK_impfdossier_impfzentrum_id FOREIGN KEY IF NOT EXISTS  (gewuenschterOdi_id) REFERENCES OrtDerImpfung(id);
ALTER TABLE Impfdossier ADD CONSTRAINT FK_impfdossier_impftermin1_id FOREIGN KEY IF NOT EXISTS  (impftermin1_id) REFERENCES Impftermin(id);
ALTER TABLE Impfdossier ADD CONSTRAINT FK_impfdossier_impftermin2_id FOREIGN KEY IF NOT EXISTS  (impftermin2_id) REFERENCES Impftermin(id);



/*
-- UNDO

ALTER TABLE Impfdossier DROP IF EXISTS nichtVerwalteterOdiSelected;
ALTER TABLE Impfdossier DROP IF EXISTS selbstzahler;
ALTER TABLE Impfdossier DROP IF EXISTS timestampZuletztAbgeschlossen;
ALTER TABLE Impfdossier DROP IF EXISTS vollstaendigerImpfschutzTyp;
ALTER TABLE Impfdossier DROP IF EXISTS genesen;
ALTER TABLE Impfdossier DROP IF EXISTS positivGetestetDatum;
ALTER TABLE Impfdossier DROP IF EXISTS zweiteImpfungVerzichtetGrund;
ALTER TABLE Impfdossier DROP IF EXISTS zweiteImpfungVerzichtetZeit;
ALTER TABLE Impfdossier DROP IF EXISTS abgesagteTermine_id;
ALTER TABLE Impfdossier DROP IF EXISTS gewuenschterOdi_id;
ALTER TABLE Impfdossier DROP IF EXISTS impftermin1_id;
ALTER TABLE Impfdossier DROP IF EXISTS impftermin2_id;

ALTER TABLE Impfdossier_AUD DROP IF EXISTS nichtVerwalteterOdiSelected;
ALTER TABLE Impfdossier_AUD DROP IF EXISTS selbstzahler;
ALTER TABLE Impfdossier_AUD DROP IF EXISTS timestampZuletztAbgeschlossen;
ALTER TABLE Impfdossier_AUD DROP IF EXISTS vollstaendigerImpfschutzTyp;
ALTER TABLE Impfdossier_AUD DROP IF EXISTS genesen;
ALTER TABLE Impfdossier_AUD DROP IF EXISTS positivGetestetDatum;
ALTER TABLE Impfdossier_AUD DROP IF EXISTS zweiteImpfungVerzichtetGrund;
ALTER TABLE Impfdossier_AUD DROP IF EXISTS zweiteImpfungVerzichtetZeit;
ALTER TABLE Impfdossier_AUD DROP IF EXISTS abgesagteTermine_id;
ALTER TABLE Impfdossier_AUD DROP IF EXISTS gewuenschterOdi_id;
ALTER TABLE Impfdossier_AUD DROP IF EXISTS impftermin1_id;
ALTER TABLE Impfdossier_AUD DROP IF EXISTS impftermin2_id;

DELETE from flyway_schema_history where script = 'db/migration/V19.2.0__migrateFieldsFromRegistrierung.sql';

*/


