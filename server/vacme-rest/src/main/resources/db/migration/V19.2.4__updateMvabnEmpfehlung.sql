/*
 *
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

# MVA-BN®
UPDATE Impfstoff set zulassungsStatusBooster = 'EMPFOHLEN', zulassungsStatus = 'EMPFOHLEN' where id = 'adea588d-edfd-4955-9794-d120cbddbdf2';

/*
-- UNDO
DELETE from flyway_schema_history where script = 'db/migration/V19.2.4__updateMvabnEmpfehlung.sql';
*/