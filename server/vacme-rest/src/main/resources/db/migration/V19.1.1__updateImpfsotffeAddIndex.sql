CREATE INDEX IF NOT EXISTS IX_Impfung_generateZertifikat ON Impfung(generateZertifikat, id);
/*
-- UNDO
DROP INDEX IX_Impfung_generateZertifikat on Impfung;

DELETE from flyway_schema_history where script = 'db/migration/V19.0.16__updateImpfsotffeAddIndex.sql';
*/
