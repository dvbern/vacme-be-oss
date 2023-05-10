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
VALUES ('9ded0628-f09a-480c-b0e5-f817cbc31633', '2021-07-23 00:00:0', '2021-07-23 00:00:0', 'flyway', 'flyway', 0,
		'VACME_ONBOARDING_BRIEF_DISABLED', 'true');

INSERT IGNORE INTO ApplicationProperty (
	id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version,
	name, value)
VALUES ('8e23c28a-2f78-427f-b949-d44bf72436c8', '2021-07-23 00:00:0', '2021-07-23 00:00:0', 'flyway', 'flyway', 0,
		'VACME_ONBOARDING_BRIEF_BATCHSIZE', '1');
