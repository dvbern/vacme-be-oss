ALTER TABLE OrtDerImpfung ADD IF NOT EXISTS impfungGegenBezahlung BIT NOT NULL DEFAULT FALSE;
ALTER TABLE OrtDerImpfung_AUD ADD IF NOT EXISTS impfungGegenBezahlung BIT NULL;

ALTER TABLE Impfschutz ADD IF NOT EXISTS freigegebenAbSelbstzahler DATETIME(6) NULL;
ALTER TABLE Impfschutz_AUD ADD IF NOT EXISTS freigegebenAbSelbstzahler DATETIME(6) NULL;

ALTER TABLE Registrierung ADD IF NOT EXISTS selbstzahler BIT NOT NULL DEFAULT FALSE;
ALTER TABLE Registrierung_AUD ADD IF NOT EXISTS selbstzahler BIT NULL;

/*
-- UNDO:
ALTER TABLE OrtDerImpfung DROP IF EXISTS impfungGegenBezahlung;
ALTER TABLE OrtDerImpfung_AUD DROP IF EXISTS impfungGegenBezahlung;

ALTER TABLE Impfschutz DROP IF EXISTS freigegebenAbSelbstzahler;
ALTER TABLE Impfschutz_AUD DROP IF EXISTS freigegebenAbSelbstzahler;

ALTER TABLE Registrierung DROP IF EXISTS selbstzahler;
ALTER TABLE Registrierung_AUD DROP IF EXISTS selbstzahler;
*/