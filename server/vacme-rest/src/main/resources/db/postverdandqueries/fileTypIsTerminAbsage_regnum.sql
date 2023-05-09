SELECT registrierungsnummer
FROM Registrierung
	 INNER JOIN Impfdossier I on Registrierung.id = I.registrierung_id
	 INNER JOIN ImpfdossierFile F on I.id = F.impfdossier_id
WHERE I.krankheitIdentifier = 'COVID' AND F.fileTyp LIKE 'TERMIN_ABSAGE' AND abgeholt = FALSE AND
	  F.timestampErstellt < '${DATE}';