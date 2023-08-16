ALTER TABLE ImpfungkontrolleTermin ADD timestampKontrolle DATETIME(6)  NULL;
ALTER TABLE ImpfungkontrolleTermin_AUD ADD timestampKontrolle DATETIME(6) NULL;

UPDATE ImpfungkontrolleTermin SET timestampKontrolle = timestampMutiert where timestampKontrolle is null;

ALTER TABLE ImpfungkontrolleTermin MODIFY timestampKontrolle DATETIME(6) NOT NULL;
