ALTER TABLE Registrierung
	ADD COLUMN auslandArt VARCHAR(50);
ALTER TABLE Registrierung_AUD
	ADD COLUMN auslandArt VARCHAR(50);

/*
ALTER TABLE Registrierung
	DROP COLUMN auslandArt;
ALTER TABLE Registrierung_AUD
	DROP COLUMN auslandArt;

*/