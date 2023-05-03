CREATE TABLE IF NOT EXISTS Impfstoff_Krankheit (
    impfstoff_id				VARCHAR(36)	 NOT NULL,
    krankheit_id				VARCHAR(36)	 NOT NULL,
    PRIMARY KEY (impfstoff_id, krankheit_id),
    CONSTRAINT impfstoff_krankheit_krankheit_fk
        FOREIGN KEY (krankheit_id) REFERENCES Krankheit(id),
    CONSTRAINT impfstoff_krankheit_impfstoff_fk
		FOREIGN KEY (impfstoff_id) REFERENCES Impfstoff(id)
);

CREATE TABLE IF NOT EXISTS Impfstoff_Krankheit_AUD (
	impfstoff_id      VARCHAR(36)  NOT NULL,
	krankheit_id  	  VARCHAR(36)  NOT NULL,
	REV               INT          NOT NULL,
	REVTYPE           TINYINT      NULL,
	PRIMARY KEY (impfstoff_id, krankheit_id, REV),
	CONSTRAINT impfstoff_krankheit_aud_rev
		FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

INSERT IGNORE INTO Impfstoff_Krankheit (impfstoff_id, krankheit_id)
SELECT id, (SELECT id FROM Krankheit WHERE identifier = 'COVID')
FROM Impfstoff;

/*
-- UNDO:

DROP TABLE Impfstoff_Krankheit;
DROP TABLE Impfstoff_Krankheit_AUD;

DELETE from flyway_schema_history where flyway_schema_history.script = 'db/migration/V19.0.1__Impfstoff_Krankheit_migration.sql';
*/

