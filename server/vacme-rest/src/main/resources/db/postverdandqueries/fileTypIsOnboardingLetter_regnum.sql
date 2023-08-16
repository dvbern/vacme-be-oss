SELECT DISTINCT registrierungsnummer
FROM Registrierung
	 INNER JOIN RegistrierungFile RF ON Registrierung.id = RF.registrierung_id
WHERE RF.fileTyp = 'ONBOARDING_LETTER' AND abgeholt = FALSE AND RF.timestampErstellt < '${DATE}' AND
	RegistrierungsEingang != 'ONLINE_REGISTRATION';