ALTER TABLE Benutzer ADD COLUMN  IF NOT EXISTS timestampLastUnlocked  DATETIME(6) NULL;
ALTER TABLE Benutzer_AUD ADD COLUMN  IF NOT EXISTS timestampLastUnlocked  DATETIME(6) NULL;

/*
ALTER TABLE Benutzer DROP COLUMN timestampLastUnlocked;
ALTER TABLE Benutzer_AUD DROP COLUMN timestampLastUnlocked;
*/