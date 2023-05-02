
alter table ExternesZertifikat
    add column positivGetestetDatum Date NULL;
alter table ExternesZertifikat_AUD
    add column positivGetestetDatum Date NULL;


/*
-- UNDO:
ALTER TABLE ExternesZertifikat DROP COLUMN IF EXISTS positivGetestetDatum;
ALTER TABLE ExternesZertifikat_AUD DROP COLUMN IF EXISTS positivGetestetDatum;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V17.1.2__externesZertifikatPcrDatum.sql';
*/

