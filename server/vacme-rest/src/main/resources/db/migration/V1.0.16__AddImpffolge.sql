CREATE INDEX IX_Impftermin_impffolge ON Impftermin(impffolge);

ALTER TABLE Impftermin MODIFY impffolge VARCHAR(20) NOT NULL;
ALTER TABLE Impftermin_AUD MODIFY impffolge VARCHAR(20) NULL;