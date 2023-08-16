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

-- USER de340daa-3ff7-40e3-b084-efff6b2fca0a (muss als FachverantwortlicherBAB gesetzt werden, damit die Abrechnung funktioniert)
REPLACE INTO Benutzer (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
					  benutzername, deaktiviert, email,
					  geloescht, geloeschtAm, glnNummer, mobiltelefon, mobiltelefonValidiert, name, vorname, issuer, benutzernameGesendetTimestamp)
VALUES ('de340daa-3ff7-40e3-b084-efff6b2fca0a', '2021-04-01 00:00:00.000000', '2021-04-01 00:00:00.000000', 'flyway', 'flyway', 0,
		'MIGRATION-USER', true, 'dev@vacme.ch', false, null, null, 'invalid', false,
		'MIGRATION-USER', 'MIGRATION-USER', 'invalid', null);



-- IMPFZENTRUM: 146f7dfd-cc94-4383-842a-b92e4060928a, GLN=ZSR=111111
REPLACE INTO OrtDerImpfung (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						   adresse1, adresse2, ort, plz,
						   glnNummer, identifier, kommentar, name, oeffentlich, terminverwaltung, mobilerOrtDerImpfung, typ, zsrNummer,
						   organisationsverantwortungKeyCloakId, fachverantwortungbabKeyCloakId, booster)
VALUES ('146f7dfd-cc94-4383-842a-b92e4060928a', '2021-04-01 00:00:00.000000', '2021-04-01 00:00:00.000000', 'flyway', 'flyway', 0,
		'Teststrasse 1', NULL, 'Zürich', '8000',
		'111111', 'MIGRATION-IMPFZENTRUM', 'Dies ist ein Dummy-ODI für die Datenmigration', 'MIGRATION-IMPFZENTRUM', false, false, false, 'IMPFZENTRUM', '111111',
		null, 'de340daa-3ff7-40e3-b084-efff6b2fca0a', TRUE);
-- HAUSARZT: 25fa876e-cb24-4154-8e10-b34cb8fa88d7, GLN=ZSR=222222
REPLACE INTO OrtDerImpfung (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						   adresse1, adresse2, ort, plz,
						   glnNummer, identifier, kommentar, name, oeffentlich, terminverwaltung, mobilerOrtDerImpfung, typ, zsrNummer,
						   organisationsverantwortungKeyCloakId, fachverantwortungbabKeyCloakId, booster)
VALUES ('25fa876e-cb24-4154-8e10-b34cb8fa88d7', '2021-04-01 00:00:00.000000', '2021-04-01 00:00:00.000000', 'flyway', 'flyway', 0,
		'Teststrasse 1', NULL, 'Zürich', '8000',
		'222222', 'MIGRATION-HAUSARZT', 'Dies ist ein Dummy-ODI für die Datenmigration', 'MIGRATION-HAUSARZT', false, false, false, 'HAUSARZT', '222222',
		null, 'de340daa-3ff7-40e3-b084-efff6b2fca0a', TRUE);
-- ALTERSHEIM: d1d7c061-6a33-47eb-a032-ffd802bd7289, GLN=ZSR=333333
REPLACE INTO OrtDerImpfung (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						   adresse1, adresse2, ort, plz,
						   glnNummer, identifier, kommentar, name, oeffentlich, terminverwaltung, mobilerOrtDerImpfung, typ, zsrNummer,
						   organisationsverantwortungKeyCloakId, fachverantwortungbabKeyCloakId, booster)
VALUES ('d1d7c061-6a33-47eb-a032-ffd802bd7289', '2021-04-01 00:00:00.000000', '2021-04-01 00:00:00.000000', 'flyway', 'flyway', 0,
		'Teststrasse 1', NULL, 'Zürich', '8000',
		'333333', 'MIGRATION-ALTERSHEIM', 'Dies ist ein Dummy-ODI für die Datenmigration', 'MIGRATION-ALTERSHEIM', false, false, false, 'ALTERSHEIM', '333333',
		null, 'de340daa-3ff7-40e3-b084-efff6b2fca0a', TRUE);
-- APOTHEKE: 20cb512b-39c2-4f97-af06-051365ceeda5, GLN=ZSR=444444
REPLACE INTO OrtDerImpfung (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						   adresse1, adresse2, ort, plz,
						   glnNummer, identifier, kommentar, name, oeffentlich, terminverwaltung, mobilerOrtDerImpfung, typ, zsrNummer,
						   organisationsverantwortungKeyCloakId, fachverantwortungbabKeyCloakId, booster)
VALUES ('20cb512b-39c2-4f97-af06-051365ceeda5', '2021-04-01 00:00:00.000000', '2021-04-01 00:00:00.000000', 'flyway', 'flyway', 0,
		'Teststrasse 1', NULL, 'Zürich', '8000',
		'444444', 'MIGRATION-APOTHEKE', 'Dies ist ein Dummy-ODI für die Datenmigration', 'MIGRATION-APOTHEKE', false, false, false, 'APOTHEKE', '444444',
		null, 'de340daa-3ff7-40e3-b084-efff6b2fca0a', TRUE);
-- MOBIL: ce96e6f5-f2f5-4cc8-a462-86123f672e8a, GLN=ZSR=555555
REPLACE INTO OrtDerImpfung (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						   adresse1, adresse2, ort, plz,
						   glnNummer, identifier, kommentar, name, oeffentlich, terminverwaltung, mobilerOrtDerImpfung, typ, zsrNummer,
						   organisationsverantwortungKeyCloakId, fachverantwortungbabKeyCloakId)
VALUES ('ce96e6f5-f2f5-4cc8-a462-86123f672e8a', '2021-04-01 00:00:00.000000', '2021-04-01 00:00:00.000000', 'flyway', 'flyway', 0,
		'Teststrasse 1', NULL, 'Zürich', '8000',
		'555555', 'MIGRATION-MOBIL', 'Dies ist ein Dummy-ODI für die Datenmigration', 'MIGRATION-MOBIL', false, false, false, 'MOBIL', '555555',
		null, 'de340daa-3ff7-40e3-b084-efff6b2fca0a');
-- SPITAL: 98c498ec-f5ed-497c-aa8f-1c9fd51c748a, GLN=ZSR=666666
REPLACE INTO OrtDerImpfung (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						   adresse1, adresse2, ort, plz,
						   glnNummer, identifier, kommentar, name, oeffentlich, terminverwaltung, mobilerOrtDerImpfung, typ, zsrNummer,
						   organisationsverantwortungKeyCloakId, fachverantwortungbabKeyCloakId, booster)
VALUES ('98c498ec-f5ed-497c-aa8f-1c9fd51c748a', '2021-04-01 00:00:00.000000', '2021-04-01 00:00:00.000000', 'flyway', 'flyway', 0,
		'Teststrasse 1', NULL, 'Zürich', '8000',
		'666666', 'MIGRATION-SPITAL', 'Dies ist ein Dummy-ODI für die Datenmigration', 'MIGRATION-SPITAL', false, false, false, 'SPITAL', '666666',
		null, 'de340daa-3ff7-40e3-b084-efff6b2fca0a', TRUE);
-- ANDERE: de20940e-bcd8-4123-bb7e-8d695ad7d594, GLN=ZSR=777777
REPLACE INTO OrtDerImpfung (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
						   adresse1, adresse2, ort, plz,
						   glnNummer, identifier, kommentar, name, oeffentlich, terminverwaltung, mobilerOrtDerImpfung, typ, zsrNummer,
						   organisationsverantwortungKeyCloakId, fachverantwortungbabKeyCloakId, booster)
VALUES ('de20940e-bcd8-4123-bb7e-8d695ad7d594', '2021-04-01 00:00:00.000000', '2021-04-01 00:00:00.000000', 'flyway', 'flyway', 0,
		'Teststrasse 1', NULL, 'Zürich', '8000',
		'777777', 'MIGRATION-ANDERE', 'Dies ist ein Dummy-ODI für die Datenmigration', 'MIGRATION-ANDERE', false, false, false, 'ANDERE', '777777',
		null, 'de340daa-3ff7-40e3-b084-efff6b2fca0a', TRUE);


