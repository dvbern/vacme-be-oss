-- Introduce field to Krankheit
ALTER TABLE Krankheit ADD COLUMN IF NOT EXISTS hasAtleastOneImpfungViewableByKanton BIT NOT NULL DEFAULT FALSE;
ALTER TABLE Krankheit_AUD ADD COLUMN IF NOT EXISTS hasAtleastOneImpfungViewableByKanton BIT NULL;

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('64e8230b-b44f-4190-a405-8a09049e5f21', '2023-01-17 00:00:0', '2023-01-17 00:00:0', 'flyway', 'flyway', 0,
		'VACME_CRON_HAS_IMPFUNG_FOR_KANTON_DISABLED', 'false');


/*
-- UNDO
ALTER TABLE Krankheit DROP COLUMN IF EXISTS hasAtleastOneImpfungViewableByKanton;
ALTER TABLE Krankheit_AUD DROP COLUMN IF EXISTS hasAtleastOneImpfungViewableByKanton;

DELETE FROM ApplicationProperty where name = 'VACME_CRON_HAS_IMPFUNG_FOR_KANTON_DISABLED';
*/