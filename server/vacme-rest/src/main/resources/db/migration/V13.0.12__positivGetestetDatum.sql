ALTER TABLE Registrierung
	ADD COLUMN positivGetestetDatum DATE;
ALTER TABLE Registrierung_AUD
	ADD COLUMN positivGetestetDatum DATE;

/*
-- UNDO:
ALTER TABLE Registrierung
	DROP COLUMN positivGetestetDatum;
ALTER TABLE Registrierung_AUD
	DROP COLUMN positivGetestetDatum;
*/