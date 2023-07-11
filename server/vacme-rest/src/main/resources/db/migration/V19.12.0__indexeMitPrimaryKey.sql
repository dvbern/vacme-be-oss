ALTER TABLE Benutzer DROP INDEX IF EXISTS IX_Benutzer_version_geloeschtam;

ALTER TABLE Benutzer DROP INDEX IF EXISTS benutzerberechtigung_benutzer_fk1_ix;
CREATE INDEX IF NOT EXISTS benutzerberechtigung_benutzer_fk1_ix ON BenutzerBerechtigung(benutzer_id, id);

ALTER TABLE Impfslot DROP INDEX IF EXISTS IX_Impfslot_odi;
ALTER TABLE Impfslot DROP INDEX IF EXISTS IX_Impfslot_von;
ALTER TABLE Impfslot DROP INDEX IF EXISTS IX_Impfslot_bis;
ALTER TABLE Impfslot DROP INDEX IF EXISTS IX_Impfslot_odi_bis_krankheitIdentifier;
CREATE INDEX IF NOT EXISTS IX_Impfslot_odi ON Impfslot(ortDerImpfung_id, id);
CREATE INDEX IF NOT EXISTS IX_Impfslot_von ON Impfslot(von, id);
CREATE INDEX IF NOT EXISTS IX_Impfslot_bis ON Impfslot(bis, id);
CREATE INDEX IF NOT EXISTS IX_Impfslot_odi_bis_krankheitIdentifier ON Impfslot(ortDerImpfung_id, bis, krankheitIdentifier, id);

ALTER TABLE Impftermin DROP INDEX IF EXISTS IX_Impftermin_impfslot;
ALTER TABLE Impftermin DROP INDEX IF EXISTS IX_Impftermin_impffolge;
ALTER TABLE Impftermin DROP INDEX IF EXISTS IX_Impftermin_gebucht;
ALTER TABLE Impftermin DROP INDEX IF EXISTS IX_Impftermin_Impffolge_Gebucht;
ALTER TABLE Impftermin DROP FOREIGN KEY FK_impftermin_impfslot_id;
ALTER TABLE Impftermin DROP INDEX IF EXISTS IX_Impftermin_gebucht_folge;
CREATE INDEX IF NOT EXISTS IX_Impftermin_impfslot ON Impftermin(impfslot_id, id);
CREATE INDEX IF NOT EXISTS IX_Impftermin_impffolge ON Impftermin(impffolge, id);
CREATE INDEX IF NOT EXISTS IX_Impftermin_gebucht ON Impftermin(gebucht, id);
CREATE INDEX IF NOT EXISTS IX_Impftermin_Impffolge_Gebucht ON Impftermin(impffolge, gebucht, id);
CREATE INDEX IF NOT EXISTS IX_Impftermin_gebucht_folge ON Impftermin(impfslot_id, gebucht, impffolge, id);
ALTER TABLE Impftermin ADD CONSTRAINT FK_impftermin_impfslot_id FOREIGN KEY (impfslot_id) REFERENCES Impfslot(id);

ALTER TABLE Impfung DROP INDEX IF EXISTS IX_Impfung_timestamp_impfung;
CREATE INDEX IF NOT EXISTS IX_Impfung_timestamp_impfung ON Impfung(timestampImpfung, id);

ALTER TABLE PLZData DROP INDEX IF EXISTS IX_plz;
CREATE INDEX IF NOT EXISTS IX_plz ON PLZData(plz, id);

ALTER TABLE PLZMedstat DROP INDEX IF EXISTS IX_plz_medstat;
CREATE INDEX IF NOT EXISTS IX_plz_medstat ON PLZMedstat(plz, id);

ALTER TABLE Registrierung DROP INDEX IF EXISTS IX_Registrierung_externalId;
CREATE INDEX IF NOT EXISTS IX_Registrierung_externalId ON Registrierung(externalId, id);