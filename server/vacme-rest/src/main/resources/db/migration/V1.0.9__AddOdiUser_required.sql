ALTER TABLE OrtDerImpfung
	MODIFY organisationsverantwortungKeyCloakId VARCHAR(255) NOT NULL default 'dbeddee0-a50d-423d-804c-fe727f3fcb7f';

ALTER TABLE OrtDerImpfung
	MODIFY fachverantwortungbabKeyCloakId VARCHAR(255) NOT NULL default 'd373c90d-f75a-4d60-be14-ec88a72e1202';
