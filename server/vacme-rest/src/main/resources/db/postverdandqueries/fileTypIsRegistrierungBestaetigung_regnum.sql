SELECT registrierungsnummer
FROM Registrierung
	 INNER JOIN RegistrierungFile RF ON Registrierung.id = RF.registrierung_id
WHERE RF.fileTyp = 'REGISTRIERUNG_BESTAETIGUNG' AND RF.abgeholt = FALSE AND RF.timestampErstellt < '${DATE}';