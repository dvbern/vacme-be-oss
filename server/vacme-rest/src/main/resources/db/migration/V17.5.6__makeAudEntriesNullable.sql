ALTER TABLE ImpfempfehlungChGrundimmunisierung_AUD MODIFY anzahlVerabreicht INT NULL;
ALTER TABLE ImpfempfehlungChGrundimmunisierung_AUD MODIFY notwendigFuerChGrundimmunisierung INT NULL;
ALTER TABLE ImpfempfehlungChGrundimmunisierung_AUD MODIFY impfstoff_id VARCHAR(36) NULL;

ALTER TABLE Impfstoff_AUD CHANGE IF EXISTS myCOVIDvacCode covidCertProdCode VARCHAR(255) NULL;
ALTER TABLE Impfstoff_AUD MODIFY covidCertProdCode VARCHAR(255) NULL;


/*
-- UNDO:
ALTER TABLE ImpfempfehlungChGrundimmunisierung_AUD MODIFY anzahlVerabreicht INT NOT NULL;
ALTER TABLE ImpfempfehlungChGrundimmunisierung_AUD MODIFY notwendigFuerChGrundimmunisierung INT NOT NULL;
ALTER TABLE ImpfempfehlungChGrundimmunisierung_AUD MODIFY impfstoff_id VARCHAR(36) NOT NULL;

ALTER TABLE Impfstoff_AUD CHANGE IF EXISTS covidCertProdCode myCOVIDvacCode VARCHAR(255) NOT NULL;

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V17.5.6__makeAudEntriesNullable.sql';
*/
