
DROP TABLE IF EXISTS ExterneImpfinfo_AUD;
DROP TABLE IF EXISTS ExternerImpfinfoEintrag;
DROP TABLE IF EXISTS ExterneImpfinfo;
DROP TABLE IF EXISTS ExternerImpfinfoEintrag_AUD;

CREATE TABLE ExternesZertifikat (
	id                    VARCHAR(36)  NOT NULL PRIMARY KEY,
	timestampErstellt     DATETIME(6)  NOT NULL,
	timestampMutiert      DATETIME(6)  NOT NULL,
	userErstellt          VARCHAR(255) NOT NULL,
	userMutiert           VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,

	registrierung_id      VARCHAR(36)  NOT NULL,
	letzteImpfungDate     DATE         NOT NULL,
	impfstoff_id          VARCHAR(36)  NOT NULL,
	anzahlImpfungen       INT          NOT NULL,
	genesen               BIT          NOT NULL DEFAULT FALSE,
	kontrollePersonUUID   VARCHAR(36)  NULL,
	kontrolliertTimestamp DATETIME(6)  NULL,

	CONSTRAINT FK_ExternesZertifikat_impfstoff_id
		FOREIGN KEY (impfstoff_id) REFERENCES Impfstoff(id),
	CONSTRAINT UC_ExternesZertifikat_registrierung
		UNIQUE (registrierung_id),
	CONSTRAINT FK_ExternesZertifikat_registrierung
		FOREIGN KEY (registrierung_id) REFERENCES Registrierung(id)
);
CREATE INDEX IX_ExternesZertifikat_registrierung
	ON ExternesZertifikat(registrierung_id, id);


CREATE TABLE ExternesZertifikat_AUD (
	id                VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	timestampErstellt DATETIME(6)  NULL,
	timestampMutiert  DATETIME(6)  NULL,
	userErstellt      VARCHAR(255) NULL,
	userMutiert       VARCHAR(255) NULL,

	registrierung_id      VARCHAR(36)  NULL,
	letzteImpfungDate     DATE         NULL,
	impfstoff_id          VARCHAR(36)  NULL,
	anzahlImpfungen       INT          NULL,
	genesen               BIT          NULL,
	kontrollePersonUUID   VARCHAR(36)  NULL,
	kontrolliertTimestamp DATETIME(6)  NULL,

	PRIMARY KEY (id, REV),
	CONSTRAINT FK_ExternesZertifikat_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);


/*
-- UNDO:
 DROP TABLE IF EXISTS ExternesZertifikat_AUD;
 DROP TABLE IF EXISTS ExternesZertifikat;
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.5__externesZertifikat.sql';
*/


