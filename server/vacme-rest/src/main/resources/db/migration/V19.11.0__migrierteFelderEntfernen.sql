ALTER TABLE Fragebogen DROP IF EXISTS personenkontrolle_id;
ALTER TABLE Fragebogen_AUD DROP IF EXISTS personenkontrolle_id;

ALTER TABLE ExternesZertifikat DROP IF EXISTS registrierung_id;
ALTER TABLE ExternesZertifikat_AUD DROP IF EXISTS registrierung_id;

ALTER TABLE Personenkontrolle DROP IF EXISTS identifikationsnummer;
ALTER TABLE Personenkontrolle_AUD DROP IF EXISTS identifikationsnummer;

ALTER TABLE Registrierung DROP IF EXISTS registrierungStatus;
ALTER TABLE Registrierung DROP IF EXISTS impftermin1_id;
ALTER TABLE Registrierung DROP IF EXISTS impftermin2_id;
ALTER TABLE Registrierung DROP IF EXISTS gewuenschterOdi_id;
ALTER TABLE Registrierung DROP IF EXISTS nichtVerwalteterOdiSelected;
ALTER TABLE Registrierung DROP IF EXISTS timestampZuletztAbgeschlossen;
ALTER TABLE Registrierung DROP IF EXISTS zweiteImpfungVerzichtetGrund;
ALTER TABLE Registrierung DROP IF EXISTS zweiteImpfungVerzichtetZeit;
ALTER TABLE Registrierung DROP IF EXISTS vollstaendigerImpfschutz;
ALTER TABLE Registrierung DROP IF EXISTS abgesagteTermine_id;
ALTER TABLE Registrierung DROP IF EXISTS positivGetestetDatum;
ALTER TABLE Registrierung DROP IF EXISTS genesen;
ALTER TABLE Registrierung DROP IF EXISTS vollstaendigerImpfschutzTyp;
ALTER TABLE Registrierung DROP IF EXISTS selbstzahler;
ALTER TABLE Registrierung_AUD DROP IF EXISTS registrierungStatus;
ALTER TABLE Registrierung_AUD DROP IF EXISTS impftermin1_id;
ALTER TABLE Registrierung_AUD DROP IF EXISTS impftermin2_id;
ALTER TABLE Registrierung_AUD DROP IF EXISTS gewuenschterOdi_id;
ALTER TABLE Registrierung_AUD DROP IF EXISTS nichtVerwalteterOdiSelected;
ALTER TABLE Registrierung_AUD DROP IF EXISTS timestampZuletztAbgeschlossen;
ALTER TABLE Registrierung_AUD DROP IF EXISTS zweiteImpfungVerzichtetGrund;
ALTER TABLE Registrierung_AUD DROP IF EXISTS zweiteImpfungVerzichtetZeit;
ALTER TABLE Registrierung_AUD DROP IF EXISTS vollstaendigerImpfschutz;
ALTER TABLE Registrierung_AUD DROP IF EXISTS abgesagteTermine_id;
ALTER TABLE Registrierung_AUD DROP IF EXISTS positivGetestetDatum;
ALTER TABLE Registrierung_AUD DROP IF EXISTS genesen;
ALTER TABLE Registrierung_AUD DROP IF EXISTS vollstaendigerImpfschutzTyp;
ALTER TABLE Registrierung_AUD DROP IF EXISTS selbstzahler;