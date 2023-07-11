ALTER TABLE OrtDerImpfung ADD COLUMN IF NOT EXISTS kundengruppe VARCHAR(50) NULL;
ALTER TABLE OrtDerImpfung_AUD ADD COLUMN IF NOT EXISTS kundengruppe VARCHAR(50) NULL;

UPDATE OrtDerImpfung SET kundengruppe = 'UNBEKANNT' WHERE kundengruppe IS NULL;

ALTER TABLE OrtDerImpfung MODIFY kundengruppe VARCHAR(50) NOT NULL;

/*
--UNDO
ALTER TABLE OrtDerImpfung DROP COLUMN kundengruppe;
ALTER TABLE OrtDerImpfung_AUD DROP COLUMN kundengruppe;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.5.6__addKundengruppe.sql';
*/