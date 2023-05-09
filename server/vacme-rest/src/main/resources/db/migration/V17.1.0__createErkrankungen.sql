
CREATE TABLE IF NOT EXISTS Erkrankung (
	id                        VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt         DATETIME(6)  NOT NULL,
	timestampMutiert          DATETIME(6)  NOT NULL,
	userErstellt              VARCHAR(255) NOT NULL,
	userMutiert               VARCHAR(255) NOT NULL,
	version                   BIGINT       NOT NULL,

	date               		  DATE         NOT NULL,
	impfdossier_id            VARCHAR(36)  NOT NULL,

	CONSTRAINT FK_Erkrankung_impfdossier FOREIGN KEY (impfdossier_id) REFERENCES Impfdossier(id)
);

CREATE INDEX IF NOT EXISTS IX_Erkrankung_impfdossier ON Erkrankung(impfdossier_id, id);


CREATE TABLE IF NOT EXISTS Erkrankung_AUD (
	id                        VARCHAR(36)  NOT NULL,
	REV                       INT          NOT NULL,
	REVTYPE                   TINYINT      NULL,
	timestampErstellt         DATETIME(6)  NULL,
	timestampMutiert          DATETIME(6)  NULL,
	userErstellt              VARCHAR(255) NULL,
	userMutiert               VARCHAR(255) NULL,

	date                      DATE          NULL,
	impfdossier_id            VARCHAR(36)  NULL,

	PRIMARY KEY (id, REV),
	CONSTRAINT FK_Erkrankung_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);


/*
DROP TABLE Erkrankung_AUD;
DROP TABLE Erkrankung;

DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V17.1.0__createErkrankungen.sql'
  */

