ALTER TABLE Registrierung ADD generateOnboardingLetter BIT NOT NULL default false;
ALTER TABLE Registrierung_AUD ADD generateOnboardingLetter BIT NULL;


#Regs mit min. einer Impfung(alle diesewurden mind. einmal kontrolliert)und dem EingangODI, Massenuploadoder Migration.
UPDATE
	Registrierung R
		INNER JOIN Impftermin T ON T.id = R.impftermin1_id
		INNER JOIN Impfslot S ON T.impfslot_id = S.id
		INNER JOIN Impfung I ON T.id = I.termin_id

SET R.generateOnboardingLetter = TRUE
WHERE registrierungsEingang IN ('ORT_DER_IMPFUNG', 'MASSENUPLOAD', 'DATA_MIGRATION');