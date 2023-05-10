ALTER TABLE Impfung ADD IF NOT EXISTS generateZertifikat BIT NOT NULL DEFAULT FALSE;
ALTER TABLE Impfung_AUD ADD IF NOT EXISTS generateZertifikat BIT NULL;

ALTER TABLE RegistrierungSnapshot DROP IF EXISTS generateZertifikat;

# Ausser es war auf der Registrierung TRUE, dann auf der letzten Impfung TRUE setzen


# IDEE
# ALLE Impfungen einer Registrierung die das Flag hat per SQL suchen
# Aus diesen Impfungen die neuste ermitteln
# Fuer alle Registrierungen die ein True flag haben die neuste Impfung nehmen und auf dieser das flag setzen

# SELECT count(id) from Registrierung where generateZertifikat is TRUE;

CREATE OR REPLACE TEMPORARY TABLE AllImpfungenOfGenerateTrueRegs (
	id                		VARCHAR(36) NOT NULL,
	timestampLastImpfung   	DATETIME(6) NOT NULL,
	timestampErstellt   	DATETIME(6) NOT NULL,
	impffolge 				VARCHAR(20) NOT NULL,
	generateZertifikat      BIT NOT NULL,
	registrierungsEingang 	VARCHAR(50) NOT NULL,
	registrierungNummer 	VARCHAR(50) NOT NULL
);

CREATE INDEX IF NOT EXISTS IX_AllImpfungenOfGenerateTrueRegs_registrierungNummer
	ON AllImpfungenOfGenerateTrueRegs(registrierungNummer, id);

TRUNCATE AllImpfungenOfGenerateTrueRegs;

INSERT INTO AllImpfungenOfGenerateTrueRegs (
	SELECT I.id, I.timestampImpfung, I.timestampErstellt, T.impffolge, R.generateZertifikat, R.registrierungsEingang,R.registrierungsnummer
	FROM Impfung I
			 INNER JOIN Impftermin T ON I.termin_id = T.id
			 INNER JOIN Impfdossiereintrag E ON T.id = E.impftermin_id
			 INNER JOIN Impfdossier D ON E.impfdossier_id = D.id
			 INNER JOIN Registrierung R ON D.registrierung_id = R.id
	WHERE R.generateZertifikat IS TRUE
);
INSERT INTO AllImpfungenOfGenerateTrueRegs (
	SELECT I.id, I.timestampImpfung, I.timestampErstellt, T.impffolge, R.generateZertifikat, R.registrierungsEingang, R.registrierungsnummer
	FROM Impfung I
		 INNER JOIN Impftermin T ON I.termin_id = T.id
		 INNER JOIN Registrierung R ON T.id = R.impftermin2_id
	WHERE R.generateZertifikat IS TRUE
);
INSERT INTO AllImpfungenOfGenerateTrueRegs (
	SELECT I.id, I.timestampImpfung, I.timestampErstellt, T.impffolge, R.generateZertifikat, R.registrierungsEingang, R.registrierungsnummer
	FROM Impfung I
		 INNER JOIN Impftermin T ON I.termin_id = T.id
		 INNER JOIN Registrierung R ON T.id = R.impftermin1_id
	WHERE R.generateZertifikat IS TRUE
);

CREATE OR REPLACE TEMPORARY TABLE NewestImpfungenOfReg (

	id                		VARCHAR(36) NOT NULL PRIMARY KEY ,
	timestampLastImpfung   	DATETIME(6) NOT NULL,
	timestampErstellt   	DATETIME(6) NOT NULL,
	impffolge 				VARCHAR(20) NOT NULL,
	generateZertifikat      BIT NOT NULL,
	registrierungsEingang 	VARCHAR(50) NOT NULL,
	registrierungNummer 	VARCHAR(50) NOT NULL
);




# Soll gleich viel geben wie oben der Count der Registrierungen

#SELECT count(*), registrierungNummer FROM AllImpfungenOfGenerateTrueRegs GROUP BY registrierungNummer;

# jeweils neuste Impfung pro Reg. Muss gleich viel sein wie oben
# SELECT count(*) FROM AllImpfungenOfGenerateTrueRegs AI1 WHERE NOT EXISTS(
# 		SELECT 1
# 		FROM AllImpfungenOfGenerateTrueRegs AI2
# 		WHERE AI2.registrierungNummer = AI1.registrierungNummer AND
# 			(AI2.timestampLastImpfung > AI1.timestampLastImpfung OR
# 			 (AI2.timestampLastImpfung = AI1.timestampLastImpfung AND AI2.timestampErstellt > AI1.timestampErstellt)));

TRUNCATE NewestImpfungenOfReg;
INSERT INTO NewestImpfungenOfReg (
	SELECT id, timestampLastImpfung, timestampErstellt, impffolge, generateZertifikat, registrierungsEingang,
		registrierungNummer
	FROM AllImpfungenOfGenerateTrueRegs AI1
	WHERE NOT EXISTS(
			SELECT 1
			FROM AllImpfungenOfGenerateTrueRegs AI2
			WHERE AI2.registrierungNummer = AI1.registrierungNummer AND
				(AI2.timestampLastImpfung > AI1.timestampLastImpfung OR
				 (AI2.timestampLastImpfung = AI1.timestampLastImpfung AND
				  AI2.timestampErstellt > AI1.timestampErstellt)))
);


# select id, hex(generateZertifikat) from Impfung where id in (select id from NewestImpfungenOfReg);

UPDATE Impfung set generateZertifikat = false;
UPDATE Impfung set generateZertifikat = true where id in (select id from NewestImpfungenOfReg);

DROP TABLE NewestImpfungenOfReg;
DROP TABLE AllImpfungenOfGenerateTrueRegs;



/**

ALTER TABLE Impfung DROP COLUMN IF EXISTS generateZertifikat;
ALTER TABLE Impfung_AUD DROP COLUMN IF EXISTS generateZertifikat;

ALTER TABLE RegistrierungSnapshot ADD IF NOT EXISTS generateZertifikat BIT NOT NULL,;

DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.9__generateZertifikatAufImpfung.sql';
 */