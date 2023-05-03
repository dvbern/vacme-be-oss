ALTER TABLE Registrierung
	ADD abgeschlossenZeit 			 DATETIME(6)   NULL,
	ADD zweiteImpfungVerzichtetGrund VARCHAR(2000) NULL,
	ADD zweiteImpfungVerzichtetZeit  DATETIME(6)   NULL;
ALTER TABLE Registrierung_AUD
	ADD abgeschlossenZeit    		 DATETIME(6)   NULL,
	ADD zweiteImpfungVerzichtetGrund VARCHAR(2000) NULL,
	ADD zweiteImpfungVerzichtetZeit  DATETIME(6)   NULL;
UPDATE Registrierung reg
	SET reg.abgeschlossenZeit = (
		SELECT regAud.timestampMutiert FROM Registrierung_AUD regAud
			WHERE reg.id = regAud.id AND (regAud.registrierungStatus = 'ABGESCHLOSSEN' OR regAud.registrierungStatus = 'ARCHIVIERT')
			ORDER BY regAud.timestampMutiert
			LIMIT 1
);
