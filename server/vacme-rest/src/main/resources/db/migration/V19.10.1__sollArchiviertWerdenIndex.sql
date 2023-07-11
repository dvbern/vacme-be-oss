CREATE INDEX IF NOT EXISTS IX_Impfung_sollArchiviertWerden_archiviertAm ON Impfung(sollArchiviertWerden, archiviertAm, id);
/*
-- UNDO
DROP INDEX IX_Impfung_sollArchiviertWerden_archiviertAm on Impfung;

DELETE from flyway_schema_history where script = 'db/migration/V19.10.1__sollArchiviertWerdenIndex.sql';
*/
