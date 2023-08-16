CREATE INDEX IF NOT EXISTS IX_RegistrierungFile_registrierungId ON RegistrierungFile(registrierung_id, id);
CREATE INDEX IF NOT EXISTS IX_RegistrierungFile_registrierungId_filetyp ON RegistrierungFile(registrierung_id, fileTyp, id);

CREATE INDEX IF NOT EXISTS IX_ImpfdossierFile_impfdossierId ON ImpfdossierFile(impfdossier_id, id);
CREATE INDEX IF NOT EXISTS IX_ImpfdossierFile_impfdossierId_filetyp ON ImpfdossierFile(impfdossier_id, fileTyp, id);
