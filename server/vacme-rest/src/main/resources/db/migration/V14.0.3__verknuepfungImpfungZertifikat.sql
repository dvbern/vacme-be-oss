ALTER TABLE Zertifikat ADD COLUMN IF NOT EXISTS impfung_id VARCHAR(36) NULL;
ALTER TABLE Zertifikat ADD CONSTRAINT FK_Zertifikat_impfung FOREIGN KEY IF NOT EXISTS (impfung_id) REFERENCES Impfung(id);
/*
ALTER TABLE Zertifikat DROP CONSTRAINT FK_Zertifikat_impfung;
ALTER TABLE Zertifikat DROP COLUMN impfung_id;
DELETE FROM flyway_schema_history WHERE script = 'db/migration/V14.0.3__verknuepfungImpfungZertifikat.sql';
 */