UPDATE Impfstoff set zulassungsStatus = 'EMPFOHLEN'
WHERE id in ('141fca55-ab78-4c0e-a2fd-edf2fe4e9b30', '58f34c3a-b07b-48c8-a6a4-ae4e1305ba8d', 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643', '313769d0-a3e1-4c0f-92e2-264e32dd9b15');
UPDATE Impfstoff set zulassungsStatusBooster = 'EMPFOHLEN'
WHERE id in ('141fca55-ab78-4c0e-a2fd-edf2fe4e9b30', '58f34c3a-b07b-48c8-a6a4-ae4e1305ba8d', 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643', '313769d0-a3e1-4c0f-92e2-264e32dd9b15');
/*
-- UNDO
UPDATE Impfstoff set zulassungsStatus = 'ZUGELASSEN'
WHERE id in ('141fca55-ab78-4c0e-a2fd-edf2fe4e9b30', '58f34c3a-b07b-48c8-a6a4-ae4e1305ba8d', 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643', '313769d0-a3e1-4c0f-92e2-264e32dd9b15');
UPDATE Impfstoff set zulassungsStatusBooster = 'ZUGELASSEN'
WHERE id in ('141fca55-ab78-4c0e-a2fd-edf2fe4e9b30', '58f34c3a-b07b-48c8-a6a4-ae4e1305ba8d', 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643', '313769d0-a3e1-4c0f-92e2-264e32dd9b15');
DELETE from flyway_schema_history where script = 'db/migration/V19.0.15__updateEmpfohleneImpfstoffe.sql';
*/