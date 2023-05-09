SELECT ImpfdossierFile.id
FROM ImpfdossierFile
	 INNER JOIN Impfdossier I on ImpfdossierFile.impfdossier_id = I.id
	 INNER JOIN Registrierung R on I.registrierung_id = R.id
WHERE R.registrierungsnummer = '${d}' AND fileTyp = 'TERMIN_ZERTIFIKAT_STORNIERUNG' AND abgeholt = FALSE AND
	  I.krankheitIdentifier = 'COVID';