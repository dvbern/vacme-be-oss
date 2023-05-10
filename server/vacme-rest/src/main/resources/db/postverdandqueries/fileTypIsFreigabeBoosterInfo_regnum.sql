SELECT DISTINCT registrierungsnummer
FROM Registrierung
	 INNER JOIN Impfdossier I on Registrierung.id = I.registrierung_id
	 INNER JOIN ImpfdossierFile F on I.id = F.impfdossier_id
WHERE I.krankheitIdentifier = 'COVID' AND F.fileTyp = 'FREIGABE_BOOSTER_INFO' AND F.abgeholt = FALSE AND
	  F.timestampErstellt < '${DATE}'
ORDER BY F.timestampErstellt
LIMIT 5000;