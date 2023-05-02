UPDATE Impfdossier DOS
SET dossierStatus =
	(SELECT R.registrierungStatus
	 FROM Impfdossier D
		  INNER JOIN Registrierung R ON D.registrierung_id = R.id
	 WHERE D.krankheitIdentifier = 'COVID' AND DOS.id = D.id)
WHERE DOS.krankheitIdentifier = 'COVID' AND DOS.dossierStatus = 'NOCH_NICHT_MIGRIERT';