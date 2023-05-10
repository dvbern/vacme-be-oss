ALTER TABLE Impfstoff_AUD CHANGE myCOVIDvacCode covidCertProdCode VARCHAR(255) NOT NULL;

CREATE INDEX IF NOT EXISTS IX_ZertifikatQueue_timestamp_prio on ZertifikatQueue (timestampErstellt, prioritaet, id);