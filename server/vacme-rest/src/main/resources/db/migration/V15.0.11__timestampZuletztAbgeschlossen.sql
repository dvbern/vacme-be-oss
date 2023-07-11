ALTER TABLE Registrierung CHANGE abgeschlossenZeit timestampZuletztAbgeschlossen DATETIME(6) NULL;
ALTER TABLE Registrierung_AUD CHANGE abgeschlossenZeit timestampZuletztAbgeschlossen DATETIME(6) NULL;

ALTER TABLE RegistrierungSnapshot CHANGE abgeschlossenZeit timestampZuletztAbgeschlossen DATETIME(6) NULL;

/*
-- UNDO:
ALTER TABLE Registrierung CHANGE timestampZuletztAbgeschlossen abgeschlossenZeit DATETIME(6) NULL;
ALTER TABLE Registrierung_AUD CHANGE timestampZuletztAbgeschlossen abgeschlossenZeit DATETIME(6) NULL;
ALTER TABLE RegistrierungSnapshot CHANGE timestampZuletztAbgeschlossen abgeschlossenZeit DATETIME(6) NULL;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.11__timestampZuletztAbgeschlossen.sql';
*/



