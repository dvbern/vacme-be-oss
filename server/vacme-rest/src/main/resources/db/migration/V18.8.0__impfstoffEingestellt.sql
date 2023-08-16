ALTER TABLE Impfstoff ADD IF NOT EXISTS eingestellt BIT NOT NULL DEFAULT FALSE;
ALTER TABLE Impfstoff_AUD ADD IF NOT EXISTS eingestellt BIT NULL;


/*
-- UNDO

ALTER TABLE Impfstoff DROP COLUMN eingestellt;
ALTER TABLE Impfstoff_AUD DROP COLUMN eingestellt;

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V18.8.0__impfstoffEingestellt.sql';
*/