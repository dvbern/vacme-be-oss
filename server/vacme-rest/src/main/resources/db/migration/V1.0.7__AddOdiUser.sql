ALTER TABLE OrtDerImpfung
	ADD COLUMN organisationsverantwortungKeyCloakId VARCHAR(255) NULL;

ALTER TABLE OrtDerImpfung
	ADD COLUMN fachverantwortungbabKeyCloakId VARCHAR(255) NULL;

ALTER TABLE OrtDerImpfung_AUD
	ADD COLUMN organisationsverantwortungKeyCloakId VARCHAR(255) NULL;

ALTER TABLE OrtDerImpfung_AUD
	ADD COLUMN fachverantwortungbabKeyCloakId VARCHAR(255) NULL;