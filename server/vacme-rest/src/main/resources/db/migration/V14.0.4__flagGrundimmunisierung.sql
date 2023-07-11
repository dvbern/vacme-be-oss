ALTER TABLE Impfung ADD COLUMN IF NOT EXISTS grundimmunisierung  BOOLEAN;

UPDATE Impfung 	SET grundimmunisierung = true where Impfung.grundimmunisierung is not true;
# dauert für 1Mio ca 1 Minute
ALTER TABLE Impfung MODIFY grundimmunisierung BOOLEAN NOT NULL;
ALTER TABLE Impfung_AUD ADD COLUMN IF NOT EXISTS grundimmunisierung BOOLEAN;

/*
--UNDO
ALTER TABLE Impfung DROP COLUMN grundimmunisierung;
ALTER TABLE Impfung_AUD DROP COLUMN grundimmunisierung;


DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V14.0.4__flagGrundimmunisierung.sql';

*/