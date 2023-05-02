ALTER TABLE OrtDerImpfung
    MODIFY organisationsverantwortungKeyCloakId VARCHAR(255) NULL;

ALTER TABLE OrtDerImpfung
    MODIFY identifier VARCHAR(255) NOT NULL default 'unknown';
