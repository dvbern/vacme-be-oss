SELECT ImpfdossierFile.id
FROM ImpfdossierFile
     INNER JOIN Impfdossier I on ImpfdossierFile.impfdossier_id = I.id
	 INNER JOIN Registrierung R on I.registrierung_id = R.id
WHERE R.registrierungsnummer = '$d' AND ImpfdossierFile.fileTyp = 'FREIGABE_BOOSTER_INFO' AND
	ImpfdossierFile.abgeholt = FALSE AND I.krankheitIdentifier = 'COVID';