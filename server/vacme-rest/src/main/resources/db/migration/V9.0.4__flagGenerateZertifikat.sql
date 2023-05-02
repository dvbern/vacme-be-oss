ALTER TABLE Registrierung ADD generateZertifikat BIT NOT NULL default false;
ALTER TABLE Registrierung_AUD ADD generateZertifikat BIT NULL;

UPDATE Registrierung SET generateZertifikat = true WHERE abgleichElektronischerImpfausweis = true AND vollstaendigerImpfschutz = true;