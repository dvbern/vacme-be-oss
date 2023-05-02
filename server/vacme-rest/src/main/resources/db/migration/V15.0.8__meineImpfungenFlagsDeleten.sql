ALTER TABLE Impfung DROP IF EXISTS timestampMyCOVIDvac;
ALTER TABLE Impfung DROP IF EXISTS myCOVIDvacDocId;
ALTER TABLE Impfung_AUD DROP IF EXISTS timestampMyCOVIDvac;
ALTER TABLE Impfung_AUD DROP IF EXISTS myCOVIDvacDocId;


/*
ALTER TABLE Impfung ADD IF NOT EXISTS myCOVIDvacDocId VARCHAR(36) NULL;
ALTER TABLE Impfung ADD IF NOT EXISTS timestampMyCOVIDvac DATETIME(6) NULL;
ALTER TABLE Impfung_AUD ADD IF NOT EXISTS myCOVIDvacDocId VARCHAR(36) NULL;
ALTER TABLE Impfung_AUD ADD IF NOT EXISTS timestampMyCOVIDvac DATETIME(6) NULL;

DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.8__meineImpfungenFlagsDeleten.sql';
*/
