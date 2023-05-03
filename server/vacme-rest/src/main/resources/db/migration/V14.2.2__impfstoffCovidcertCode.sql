ALTER TABLE Impfstoff CHANGE myCOVIDvacCode covidCertProdCode VARCHAR(255) NOT NULL;


#JANSSEN_UUID
UPDATE Impfstoff set covidCertProdCode = 'EU/1/20/1525' where id = '12c5d49e-ce77-464a-a951-3c840e5a1d1b';
#PFIZER_BIONTECH_UUID
UPDATE Impfstoff set covidCertProdCode = 'EU/1/20/1528' where id = '141fca55-ab78-4c0e-a2fd-edf2fe4e9b30';
#ASTRA_ZENECA_UUID
UPDATE Impfstoff set covidCertProdCode = 'EU/1/21/1529' where id = '7ff61fb9-0993-11ec-b1f1-0242ac140003';
#MODERNA_UUID
UPDATE Impfstoff set covidCertProdCode = 'EU/1/20/1507' where id = 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643';

/* UNDO
ALTER TABLE Impfstoff CHANGE covidCertProdCode myCOVIDvacCode VARCHAR(255) NOT NULL;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V14.2.2__impfstoffCovidcertCode.sql';
 */
