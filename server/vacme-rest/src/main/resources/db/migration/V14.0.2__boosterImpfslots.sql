ALTER TABLE Impfslot ADD kapazitaetBoosterImpfung INT NOT NULL DEFAULT 0;
ALTER TABLE Impfslot_AUD ADD kapazitaetBoosterImpfung INT;



/*
ALTER TABLE Impfslot drop COLUMN kapazitaetBoosterImpfung;
ALTER TABLE Impfslot_AUD drop COLUMN kapazitaetBoosterImpfung;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V14.0.2__boosterImpfslots.sql';
 */