CREATE INDEX IX_Registrierung_impftermin1 ON Registrierung(impftermin1_id);
CREATE INDEX IX_Registrierung_impftermin2 ON Registrierung(impftermin2_id);

CREATE INDEX IX_Impfslot_odi ON Impfslot(ortDerImpfung_id);

CREATE INDEX IX_Impftermin_impfslot ON Impftermin(impfslot_id);