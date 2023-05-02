-- Introduce field to Krankheit
ALTER TABLE Krankheit ADD COLUMN IF NOT EXISTS kantonaleBerechtigung VARCHAR(50) NOT NULL;
ALTER TABLE Krankheit_AUD ADD COLUMN IF NOT EXISTS kantonaleBerechtigung VARCHAR(50) NULL;

UPDATE Krankheit SET kantonaleBerechtigung = 'KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG' WHERE identifier = 'COVID';
UPDATE Krankheit SET kantonaleBerechtigung = 'KANTONALE_IMPFKAMPAGNE' WHERE identifier = 'AFFENPOCKEN';
UPDATE Krankheit SET kantonaleBerechtigung = 'LEISTUNGSERBRINGER' WHERE identifier = 'FSME';

# Default version
ALTER TABLE Impfung ADD COLUMN IF NOT EXISTS kantonaleBerechtigung VARCHAR(50) NOT NULL DEFAULT 'KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG';
ALTER TABLE Impfung_AUD ADD COLUMN IF NOT EXISTS kantonaleBerechtigung varchar(50) NULL;

ALTER TABLE Impfung ALTER COLUMN kantonaleBerechtigung DROP DEFAULT;

UPDATE Impfung
SET kantonaleBerechtigung = 'KANTONALE_IMPFKAMPAGNE'
WHERE id IN (SELECT I.id
					FROM Impfung I
						 INNER JOIN Impfstoff I2 on I.impfstoff_id = I2.id
						 INNER JOIN Impfstoff_Krankheit IK on I2.id = IK.impfstoff_id
						 INNER JOIN Krankheit K on IK.krankheit_id = K.id
					WHERE K.identifier = 'AFFENPOCKEN');


/*
-- UNDO
ALTER TABLE Krankheit DROP COLUMN IF EXISTS kantonaleBerechtigung;
ALTER TABLE Krankheit_AUD DROP COLUMN IF EXISTS kantonaleBerechtigung;
ALTER TABLE Impfung DROP COLUMN IF EXISTS kantonaleBerechtigung;
ALTER TABLE Impfung_AUD DROP COLUMN IF EXISTS kantonaleBerechtigung;
DELETE from flyway_schema_history where script = 'db/migration/V19.3.8__kantonaleBerechtigung.sql';
*/