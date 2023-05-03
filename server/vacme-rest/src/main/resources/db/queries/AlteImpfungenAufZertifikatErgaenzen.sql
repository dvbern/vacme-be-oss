/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

# Wieviele sind betroffen? Nur nicht-revozierte
# select count(*) from Zertifikat where revoked is false and impfung_id is null;

# Betroffene Regs:
# select distinct(registrierung_id) from Zertifikat where revoked is false and impfung_id is null;

# IDEE
# Alle Impfungen der betroffenen Regs sammeln
# Aus diesen Impfungen die neuste ermitteln
# Fuer alle Zerfitikate, die keine Impfung gesetzt haben und nicht revoziert sind die neuste Impfung nehmen
CREATE OR REPLACE TEMPORARY TABLE AllImpfungenOfConcernedRegs (
	impfung_id         		VARCHAR(36) NOT NULL,
	registrierung_id        VARCHAR(36) NOT NULL,
	timestampLastImpfung   	DATETIME(6) NOT NULL,
	timestampErstellt   	DATETIME(6) NOT NULL,
	impffolge 				VARCHAR(20) NOT NULL,
	generateZertifikat      BIT NOT NULL,
	registrierungsEingang 	VARCHAR(50) NOT NULL,
	registrierungNummer 	VARCHAR(50) NOT NULL
);


CREATE INDEX IF NOT EXISTS IX_AllImpfungenOfConcernedRegs_registrierungNummer
	ON AllImpfungenOfConcernedRegs(registrierungNummer, timestampLastImpfung);

TRUNCATE AllImpfungenOfConcernedRegs;
INSERT INTO AllImpfungenOfConcernedRegs (
	SELECT I.id as impfung_id, R.id as registrierung_id, I.timestampImpfung, I.timestampErstellt, T.impffolge, R.generateZertifikat, R.registrierungsEingang,R
	    .registrierungsnummer
	FROM Impfung I
			 INNER JOIN Impftermin T ON I.termin_id = T.id
			 INNER JOIN Impfdossiereintrag E ON T.id = E.impftermin_id
			 INNER JOIN Impfdossier D ON E.impfdossier_id = D.id
			 INNER JOIN Registrierung R ON D.registrierung_id = R.id
	WHERE R.id in (select distinct(registrierung_id) from Zertifikat where revoked is false and impfung_id is null)
);
INSERT INTO AllImpfungenOfConcernedRegs (
	SELECT I.id as impfung_id, R.id as registrierung_id, I.timestampImpfung, I.timestampErstellt, T.impffolge, R.generateZertifikat, R.registrierungsEingang, R.registrierungsnummer
	FROM Impfung I
		 INNER JOIN Impftermin T ON I.termin_id = T.id
		 INNER JOIN Registrierung R ON T.id = R.impftermin2_id
	WHERE R.id in (select distinct(registrierung_id) from Zertifikat where revoked is false and impfung_id is null)
);
INSERT INTO AllImpfungenOfConcernedRegs (
	SELECT I.id as impfung_id, R.id as registrierung_id, I.timestampImpfung, I.timestampErstellt, T.impffolge, R.generateZertifikat, R.registrierungsEingang, R.registrierungsnummer
	FROM Impfung I
		 INNER JOIN Impftermin T ON I.termin_id = T.id
		 INNER JOIN Registrierung R ON T.id = R.impftermin1_id
	WHERE R.id in (select distinct(registrierung_id) from Zertifikat where revoked is false and impfung_id is null)
);

# select count(*) from AllImpfungenOfConcernedRegs;

CREATE OR REPLACE TEMPORARY TABLE NewestImpfungenOfReg (
	impfung_id         		VARCHAR(36) NOT NULL,
	registrierung_id        VARCHAR(36) NOT NULL,
	timestampLastImpfung   	DATETIME(6) NOT NULL,
	timestampErstellt   	DATETIME(6) NOT NULL,
	impffolge 				VARCHAR(20) NOT NULL,
	generateZertifikat      BIT NOT NULL,
	registrierungsEingang 	VARCHAR(50) NOT NULL,
	registrierungNummer 	VARCHAR(50) NOT NULL
);


TRUNCATE NewestImpfungenOfReg;
INSERT INTO NewestImpfungenOfReg (
	SELECT impfung_id, registrierung_id, timestampLastImpfung, timestampErstellt, impffolge, generateZertifikat, registrierungsEingang,
		registrierungNummer
	FROM AllImpfungenOfConcernedRegs AI1
	WHERE NOT EXISTS(
			SELECT 1
			FROM AllImpfungenOfConcernedRegs AI2
			WHERE AI2.registrierungNummer = AI1.registrierungNummer AND
				(AI2.timestampLastImpfung > AI1.timestampLastImpfung OR
				 (AI2.timestampLastImpfung = AI1.timestampLastImpfung AND
				  AI2.timestampErstellt > AI1.timestampErstellt)))
);

# Soll gleich viel geben wie oben die distinct Regs
# select * from NewestImpfungenOfReg;

CREATE INDEX IF NOT EXISTS IX_NewestImpfungenOfReg_regID ON NewestImpfungenOfReg(registrierung_id);

update Zertifikat Z set impfung_id = (select impfung_id from NewestImpfungenOfReg where Z.registrierung_id = registrierung_id)
	where impfung_id is null and revoked is false;

DROP TABLE NewestImpfungenOfReg;
DROP TABLE AllImpfungenOfConcernedRegs;



/**

DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.0.12__alteImpfungenAufZertifikatErgaenzen.sql';
 */