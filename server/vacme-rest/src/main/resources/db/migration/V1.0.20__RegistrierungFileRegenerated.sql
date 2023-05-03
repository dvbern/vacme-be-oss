ALTER TABLE RegistrierungFile ADD regenerated BIT NOT NULL DEFAULT FALSE;
ALTER TABLE RegistrierungFile_AUD ADD regenerated BIT NULL;