ALTER TABLE Impfung ADD IF NOT EXISTS selbstzahlende BIT NOT NULL DEFAULT false;
ALTER TABLE Impfung_AUD ADD IF NOT EXISTS selbstzahlende BIT NULL;

ALTER TABLE ImpfungkontrolleTermin ADD IF NOT EXISTS selbstzahlende BIT NULL;
ALTER TABLE ImpfungkontrolleTermin_AUD ADD IF NOT EXISTS selbstzahlende BIT NULL;


/*
-- UNDO:
ALTER TABLE Impfung DROP IF EXISTS selbstzahlende;
ALTER TABLE Impfung_AUD DROP IF EXISTS selbstzahlende;

ALTER TABLE ImpfungkontrolleTermin DROP IF EXISTS selbstzahlende;
ALTER TABLE ImpfungkontrolleTermin_AUD DROP IF EXISTS selbstzahlende;
*/
