CREATE TABLE IF NOT EXISTS OrtDerImpfung_Krankheit (
	ortDerImpfung_id			VARCHAR(36)	 NOT NULL,
	krankheit_id				VARCHAR(36)	 NOT NULL,
	PRIMARY KEY (ortDerImpfung_id, krankheit_id),
	CONSTRAINT ortderimpfung_krankheit_krankheit_fk
		FOREIGN KEY (krankheit_id) REFERENCES Krankheit(id),
	CONSTRAINT ortderimpfung_krankheit_ortderimpfung_fk
		FOREIGN KEY (ortDerImpfung_id) REFERENCES OrtDerImpfung(id)
);

CREATE TABLE IF NOT EXISTS OrtDerImpfung_Krankheit_AUD (
	ortDerImpfung_id  VARCHAR(36)  NOT NULL,
	krankheit_id  	  VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	PRIMARY KEY (ortDerImpfung_id, krankheit_id, REV),
	CONSTRAINT ortderimpfung_krankheit_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

CREATE INDEX IF NOT EXISTS ortderimpfung_krankheit_ortderimpfung_fk_ix
	ON OrtDerImpfung_Krankheit(ortDerImpfung_id);

CREATE INDEX IF NOT EXISTS ortderimpfung_krankheit_krankheit_fk_ix
	ON OrtDerImpfung_Krankheit(krankheit_id);


INSERT IGNORE INTO  OrtDerImpfung_Krankheit (ortDerImpfung_id, krankheit_id)
SELECT id, (SELECT id FROM Krankheit WHERE identifier = 'COVID')
FROM OrtDerImpfung;

/*
-- UNDO:

DROP TABLE OrtDerImpfung_Krankheit;
DROP TABLE OrtDerImpfung_Krankheit_AUD;

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.0.3__OrtDerImpfung_KrankheitAndMigrate.sql';
*/
