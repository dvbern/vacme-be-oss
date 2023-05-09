/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('33ac4807-481a-43cb-b6ac-ab2fa8cb10c2', '2021-06-10 00:00:0', '2021-06-10 00:00:0', 'flyway', 'flyway', 0,
		'DISTANCE_BETWEEN_IMPFUNGEN_DISIRED', '28');

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('3cf97c0a-2b15-40bb-88bd-881c6e907ade', '2021-06-10 00:00:0', '2021-06-10 00:00:0', 'flyway', 'flyway', 0,
		'DISTANCE_BETWEEN_IMPFUNGEN_TOLERANCE_BEFORE', '0');

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('88d4b5d2-f354-4c84-a8ea-d70de94ff8f7', '2021-06-10 00:00:0', '2021-06-10 00:00:0', 'flyway', 'flyway', 0,
		'DISTANCE_BETWEEN_IMPFUNGEN_TOLERANCE_AFTER', '7');

ALTER TABLE Impfstoff DROP COLUMN anzahlTageZwischenImpfungen;
ALTER TABLE Impfstoff DROP COLUMN anzahlTageZwischenImpfungenOffsetBefore;
ALTER TABLE Impfstoff DROP COLUMN anzahlTageZwischenImpfungenOffsetAfter;

ALTER TABLE Impfstoff_AUD DROP COLUMN anzahlTageZwischenImpfungen;
ALTER TABLE Impfstoff_AUD DROP COLUMN anzahlTageZwischenImpfungenOffsetBefore;
ALTER TABLE Impfstoff_AUD DROP COLUMN anzahlTageZwischenImpfungenOffsetAfter;