# War urspruenglich falsch weil dossierStatus Bedignung gerade verkehrt war, korrigiert mit v19.0.13
UPDATE Impfdossier DOS
SET dossierStatus =
	(SELECT R.registrierungStatus
	 FROM Impfdossier D
		  INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.dossierStatus = 'NOCH_NICHT_MIGRIERT';


UPDATE Impfdossier DOS set dossierStatus = 'NEU' where dossierStatus = 'REGISTRIERT';

ALTER TABLE Registrierung MODIFY registrierungStatus VARCHAR(50) NULL;