# Old MVA-BN®, Affenpocken Impfstoff
UPDATE Impfstoff set code = '00350632001020', name = 'Jynneos®' where id = 'adea588d-edfd-4955-9794-d120cbddbdf2';

/*
-- UNDO
DELETE from flyway_schema_history where script = 'db/migration/V19.2.9__affenpockenImpfstoffDetails.sql';
*/