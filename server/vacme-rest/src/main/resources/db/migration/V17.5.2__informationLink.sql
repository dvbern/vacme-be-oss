ALTER TABLE Impfstoff ADD informationsLink VARCHAR(255);
ALTER TABLE Impfstoff_AUD ADD informationsLink VARCHAR(255);


/*
-- UNDO:
ALTER TABLE Impfstoff DROP COLUMN IF EXISTS informationsLink;
ALTER TABLE Impfstoff_AUD DROP COLUMN IF EXISTS informationsLink;
DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V17.5.2__informationLink.sql';
*/
