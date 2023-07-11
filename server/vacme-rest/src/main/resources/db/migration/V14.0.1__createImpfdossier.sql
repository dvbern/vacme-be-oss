#Impfschutz
CREATE TABLE Impfschutz (
	id                           VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt            DATETIME(6)  NOT NULL,
	timestampMutiert             DATETIME(6)  NOT NULL,
	userErstellt                 VARCHAR(255) NOT NULL,
	userMutiert                  VARCHAR(255) NOT NULL,
	version                      BIGINT       NOT NULL,
	freigegebenNaechsteImpfungAb DATETIME(6)  NULL,
	immunisiertBis               DATETIME(6)  NULL
);

CREATE TABLE Impfschutz_AUD (
	id                           VARCHAR(36)  NOT NULL,
	REV                          INT          NOT NULL,
	REVTYPE                      TINYINT      NULL,
	timestampErstellt            DATETIME(6)  NULL,
	timestampMutiert             DATETIME(6)  NULL,
	userErstellt                 VARCHAR(255) NULL,
	userMutiert                  VARCHAR(255) NULL,
	freigegebenNaechsteImpfungAb DATETIME(6)  NULL,
	immunisiertBis               DATETIME(6)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK_Impfschutz_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

#Impfdossier
CREATE TABLE Impfdossier (
	id                VARCHAR(36)  NOT NULL
		PRIMARY KEY,
	timestampErstellt DATETIME(6)  NOT NULL,
	timestampMutiert  DATETIME(6)  NOT NULL,
	userErstellt      VARCHAR(255) NOT NULL,
	userMutiert       VARCHAR(255) NOT NULL,
	version           BIGINT       NOT NULL,
	impfschutz_id     VARCHAR(36)  NULL,
	registrierung_id  VARCHAR(36)  NOT NULL,
	CONSTRAINT UC_Impfdossier_impfschutz UNIQUE (impfschutz_id),
	CONSTRAINT UC_Impfdossier_registrierung UNIQUE (registrierung_id),
	CONSTRAINT FK_Impfdossier_impfschutz FOREIGN KEY (impfschutz_id) REFERENCES Impfschutz(id),
	CONSTRAINT FK_Impfdossier_registrierung FOREIGN KEY (registrierung_id) REFERENCES Registrierung(id)
);

CREATE INDEX IX_Impfdossier_impfschutz 	ON Impfdossier(impfschutz_id, id);

CREATE INDEX IX_Impfdossier_registrierung ON Impfdossier(registrierung_id, id);


CREATE TABLE Impfdossier_AUD (
	id                VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	timestampErstellt DATETIME(6)  NULL,
	timestampMutiert  DATETIME(6)  NULL,
	userErstellt      VARCHAR(255) NULL,
	userMutiert       VARCHAR(255) NULL,
	impfschutz_id     VARCHAR(36)  NULL,
	registrierung_id  VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK_impfdossier_aud_rev FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);


# Dossiereintrag
CREATE TABLE Impfdossiereintrag (
	id                        VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt         DATETIME(6)  NOT NULL,
	timestampMutiert          DATETIME(6)  NOT NULL,
	userErstellt              VARCHAR(255) NOT NULL,
	userMutiert               VARCHAR(255) NOT NULL,
	version                   BIGINT       NOT NULL,
	impffolgeNr               INT          NOT NULL,
	impfdossier_id            VARCHAR(36)  NOT NULL,
	impftermin_id             VARCHAR(36)  NULL,
	impfungkontrolleTermin_id VARCHAR(36)  NULL,
	CONSTRAINT UC_Impfdossiereintrag_impftermin UNIQUE (impftermin_id),
	CONSTRAINT UC_Impfdossiereintrag_kontrolle UNIQUE (impfungkontrolleTermin_id),
	CONSTRAINT FK_Impfdossiereintrag_impfdossier FOREIGN KEY (impfdossier_id) REFERENCES Impfdossier(id),
	CONSTRAINT FK_Impfdossiereintrag_impftermin_id FOREIGN KEY (impftermin_id) REFERENCES Impftermin(id),
	CONSTRAINT FK_Impfdossiereintrag_impfungkontrolleTermin_id FOREIGN KEY (impfungkontrolleTermin_id) REFERENCES ImpfungkontrolleTermin(id)
);

CREATE INDEX IX_Impfdossiereintrag_impfdossier ON Impfdossiereintrag(impfdossier_id, id);

CREATE INDEX IX_Impfdossiereintrag_impftermin ON Impfdossiereintrag(impftermin_id, id);

CREATE INDEX IX_Impfdossiereintrag_kontrolle ON Impfdossiereintrag(impfungkontrolleTermin_id, id);


CREATE TABLE Impfdossiereintrag_AUD (
	id                        VARCHAR(36)  NOT NULL,
	REV                       INT          NOT NULL,
	REVTYPE                   TINYINT      NULL,
	timestampErstellt         DATETIME(6)  NULL,
	timestampMutiert          DATETIME(6)  NULL,
	userErstellt              VARCHAR(255) NULL,
	userMutiert               VARCHAR(255) NULL,
	impffolgeNr               INT          NULL,
	impfdossier_id            VARCHAR(36)  NULL,
	impftermin_id             VARCHAR(36)  NULL,
	impfungkontrolleTermin_id VARCHAR(36)  NULL,
	PRIMARY KEY (id, REV),
	CONSTRAINT FK_Impfdossiereintrag_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

# to revert
/*
DROP TABLE Impfdossiereintrag;
DROP TABLE Impfdossiereintrag_AUD;
DROP TABLE Impfdossier_AUD;
DROP TABLE Impfdossier;
DROP TABLE Impfschutz_AUD;
DROP TABLE Impfschutz;


DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V14.0.1__createImpfdossier.sql'
  */

