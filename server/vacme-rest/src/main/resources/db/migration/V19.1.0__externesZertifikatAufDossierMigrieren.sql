# Neue Spalten nullable erstellen
ALTER TABLE ExternesZertifikat ADD IF NOT EXISTS impfdossier_id VARCHAR(36) NULL;
ALTER TABLE ExternesZertifikat_AUD ADD IF NOT EXISTS impfdossier_id VARCHAR(36) NULL;

# Die Daten migrieren
UPDATE ExternesZertifikat EZ
SET impfdossier_id =
	(SELECT D1.id
	 FROM Impfdossier D1
		  INNER JOIN Registrierung R1 ON D1.registrierung_id = R1.id
	 WHERE EZ.registrierung_id = R1.id and D1.krankheitIdentifier = 'COVID')
WHERE EZ.impfdossier_id is null;

# Alte Indexe und Constraints entfernen
DROP INDEX IF EXISTS IX_ExternesZertifikat_registrierung ON ExternesZertifikat;
ALTER TABLE ExternesZertifikat DROP FOREIGN KEY FK_ExternesZertifikat_registrierung;
DROP INDEX IF EXISTS UC_ExternesZertifikat_registrierung ON ExternesZertifikat;

# Indexe und Constraints erstellen
CREATE INDEX IF NOT EXISTS IX_ExternesZertifikat_impfdossier ON ExternesZertifikat (impfdossier_id, id);

ALTER TABLE ExternesZertifikat
	ADD CONSTRAINT UC_ExternesZertifikat_impfdossier UNIQUE (impfdossier_id);

ALTER TABLE ExternesZertifikat
	ADD FOREIGN KEY FK_ExternesZertifikat_impfdossier(impfdossier_id)
	REFERENCES Impfdossier(id);

# DossierId darf jetzt nicht mehr nullable sein
ALTER TABLE ExternesZertifikat MODIFY impfdossier_id VARCHAR(36) NOT NULL;

# Dafuer muss jetzt die RegistrierungsId nullable sein
ALTER TABLE ExternesZertifikat MODIFY registrierung_id VARCHAR(36) NULL;