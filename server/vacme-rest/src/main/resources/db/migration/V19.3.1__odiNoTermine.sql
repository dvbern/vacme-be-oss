CREATE TABLE OdiNoFreieTermine (
	id                  VARCHAR(36)  NOT NULL,
	timestampErstellt   DATETIME(6)  NOT NULL,
	timestampMutiert    DATETIME(6)  NOT NULL,
	userErstellt        VARCHAR(255) NOT NULL,
	userMutiert         VARCHAR(255) NOT NULL,
	version             BIGINT       NOT NULL,
	krankheitIdentifier VARCHAR(50)  NOT NULL,
	noFreieTermine1  	BIT NOT NULL,
	noFreieTermine2   	BIT NOT NULL,
	noFreieTermineN   	BIT NOT NULL,
	ortDerImpfung_id    VARCHAR(36)  NOT NULL,
	PRIMARY KEY (id)
);

ALTER TABLE OdiNoFreieTermine
ADD CONSTRAINT UC_OdiNoFreieTermine_odi_krankheit UNIQUE (ortDerImpfung_id, krankheitIdentifier);

ALTER TABLE OdiNoFreieTermine
ADD CONSTRAINT FK_odiNoFreieTermine_ortDerImpfung_id
	FOREIGN KEY (ortDerImpfung_id)
		REFERENCES OrtDerImpfung(id);

ALTER TABLE OrtDerImpfung DROP IF EXISTS noFreieTermine1;
ALTER TABLE OrtDerImpfung DROP IF EXISTS noFreieTermine2;
ALTER TABLE OrtDerImpfung DROP IF EXISTS noFreieTermineN;

ALTER TABLE OrtDerImpfung_AUD DROP IF EXISTS noFreieTermine1;
ALTER TABLE OrtDerImpfung_AUD DROP IF EXISTS noFreieTermine2;
ALTER TABLE OrtDerImpfung_AUD DROP IF EXISTS noFreieTermineN;

/**
UNDO:

DROP TABLE IF EXISTS OdiNoFreieTermine;
ALTER TABLE OrtDerImpfung ADD noFreieTermine1 BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE OrtDerImpfung_AUD ADD noFreieTermine1 BOOLEAN NULL;
ALTER TABLE OrtDerImpfung ADD noFreieTermine2 BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE OrtDerImpfung_AUD ADD noFreieTermine2 BOOLEAN NULL;
ALTER TABLE OrtDerImpfung ADD noFreieTermineN BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE OrtDerImpfung_AUD ADD noFreieTermineN BOOLEAN NULL;
DELETE FROM flyway_schema_history where version = '19.2.11';
 */
