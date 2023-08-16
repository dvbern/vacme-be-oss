INSERT IGNORE INTO Impfstoff (
   id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
   anzahlDosenBenoetigt, code, name, hersteller, covidCertProdCode, hexFarbe, zulassungsStatus,
   informationsLink, impfstofftyp, zulassungsStatusBooster)
VALUES('adea588d-edfd-4955-9794-d120cbddbdf2', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 0,
	   2, '112233445566', 'IMVANEX /JYNNEOS', 'Bavarian Nordic', '??todo prodcode??' ,'#39ff14', 'ZUGELASSEN', null, 'ANDERE', 'ZUGELASSEN');

INSERT IGNORE INTO Impfstoff_Krankheit (impfstoff_id, krankheit_id)
SELECT 'adea588d-edfd-4955-9794-d120cbddbdf2', (SELECT id FROM Krankheit WHERE identifier = 'AFFENPOCKEN')


/*
-- UNDO:

DELETE FROM Impfstoff_Krankheit where impfstoff_id = 'adea588d-edfd-4955-9794-d120cbddbdf2';
DELETE FROM Impfstoff where id = 'adea588d-edfd-4955-9794-d120cbddbdf2';

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.0.7__impfstoffAffenpocken.sql';

*/