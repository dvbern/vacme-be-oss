UPDATE Registrierung SET generateZertifikat = true WHERE vollstaendigerImpfschutz = true;

UPDATE Registrierung SET generateZertifikat = false WHERE  vollstaendigerImpfschutz = false;