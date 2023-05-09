# Impfdossier:

DROP INDEX IF EXISTS IX_Impfdossier_impfschutz ON Impfdossier;
DROP INDEX IF EXISTS IX_Impfdossier_impftermin1 ON Impfdossier;
DROP INDEX IF EXISTS IX_Impfdossier_impftermin2 ON Impfdossier;

CREATE INDEX IF NOT EXISTS IX_Impfdossier_dossierstatus_krankheit_reg
    ON Impfdossier (dossierStatus, krankheitIdentifier, registrierung_id, id);
CREATE INDEX IF NOT EXISTS IX_Impfdossier_krankheit
    ON Impfdossier (krankheitIdentifier, id);
CREATE INDEX IF NOT EXISTS IX_Impfdossier_impfschutz_krankheit
    ON Impfdossier (impfschutz_id, krankheitIdentifier, id);
CREATE INDEX IF NOT EXISTS IX_Impfdossier_registrierung_impfschutz_krankheit
    ON Impfdossier (registrierung_id, impfschutz_id, krankheitIdentifier, id);
CREATE INDEX IF NOT EXISTS IX_Impfdossier_impftermin1_krankheit_registrierung
    ON Impfdossier (impftermin1_id, registrierung_id, krankheitIdentifier, id);
CREATE INDEX IF NOT EXISTS IX_Impfdossier_impftermin2_krankheit_registrierung
    ON Impfdossier (impftermin2_id, registrierung_id, krankheitIdentifier, id);

# Impfdossiereintrag
CREATE INDEX IF NOT EXISTS IX_Impfdossiereintrag_impfdossier_impftermin
	ON Impfdossiereintrag (impfdossier_id, impftermin_id, id);

DROP INDEX IF EXISTS IX_Impfdossiereintrag_impfdossier ON Impfdossiereintrag;
DROP INDEX IF EXISTS IX_Impfdossiereintrag_impftermin ON Impfdossiereintrag;


# Impfschutz

CREATE INDEX IF NOT EXISTS IX_Impfschutz_freigegebenNaechsteImpfungAb
	ON Impfschutz (freigegebenNaechsteImpfungAb, id);

# Registrierung

DROP INDEX IF EXISTS IX_registrierungsnummer ON Registrierung;

CREATE INDEX IF NOT EXISTS IX_registrierungsnummer_id
	ON Registrierung (registrierungsnummer, id);