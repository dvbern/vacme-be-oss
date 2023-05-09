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

-- Das Folgende Skript ist falsch aber hat gluecklicherweise keine Auswirkungen. Fixed in V16.2.5__flywayFixes.sql
CREATE INDEX IF NOT EXISTS IX_Impfung_timestampvmdl_extern ON Impfung (timestampErstellt, prioritaet, id);


/*
-- UNDO:
ALTER TABLE  Impfung DROP INDEX IX_Impfung_timestampvmdl_extern
DELETE  from flyway_schema_history where flyway_schema_history.script = 'db/migration/V15.2.3__zertifikatQueueIndex.sql';
*/