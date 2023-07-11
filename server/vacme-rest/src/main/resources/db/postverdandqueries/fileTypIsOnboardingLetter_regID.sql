SELECT RegistrierungFile.id
FROM RegistrierungFile
	 INNER JOIN Registrierung REG ON RegistrierungFile.registrierung_id = REG.id
WHERE REG.registrierungsnummer = '$d' AND fileTyp = 'ONBOARDING_LETTER' AND abgeholt = FALSE;
