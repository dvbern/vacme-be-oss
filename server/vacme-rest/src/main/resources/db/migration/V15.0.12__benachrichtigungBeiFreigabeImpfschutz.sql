ALTER TABLE Impfschutz ADD IF NOT EXISTS benachrichtigungBeiFreigabe BIT NULL;
ALTER TABLE Impfschutz_AUD ADD IF NOT EXISTS benachrichtigungBeiFreigabe BIT NULL;
UPDATE Impfschutz SET benachrichtigungBeiFreigabe = TRUE WHERE benachrichtigungBeiFreigabe IS NULL;
ALTER TABLE Impfschutz CHANGE benachrichtigungBeiFreigabe benachrichtigungBeiFreigabe BIT NOT NULL;

/*
-- UNDO:
ALTER TABLE Impfschutz DROP benachrichtigungBeiFreigabe;
ALTER TABLE Impfschutz_AUD DROP benachrichtigungBeiFreigabe;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.12__benachrichtigungBeiFreigabeImpfschutz.sql';
*/
