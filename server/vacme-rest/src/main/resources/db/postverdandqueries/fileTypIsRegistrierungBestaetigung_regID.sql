SELECT Registrierung.id
FROM Registrierung
	 INNER JOIN RegistrierungFile RF ON Registrierung.id = RF.registrierung_id
WHERE RF.fileTyp LIKE 'REGISTRIERUNG_BESTAETIGUNG' AND registrierungsnummer = '${d}';