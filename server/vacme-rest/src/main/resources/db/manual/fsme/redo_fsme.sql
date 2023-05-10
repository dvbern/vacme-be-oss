/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

# impfzentrum_fsme
# Remove COVID Krankheit, Add FSME
DELETE FROM OrtDerImpfung_Krankheit WHERE ortDerImpfung_id =
    (SELECT id FROM OrtDerImpfung WHERE identifier = 'impfzentrum_fsme');
INSERT INTO OrtDerImpfung_Krankheit (ortDerImpfung_id, krankheit_id) VALUE (
	(SELECT id
	 FROM OrtDerImpfung
	 WHERE identifier = 'impfzentrum_fsme'),
	'61bd68be-9808-4247-9f3c-cd569dec7bc2');
# impfzentrum_covid_fsme
# Add FSME, Covid added by default
INSERT INTO OrtDerImpfung_Krankheit (ortDerImpfung_id, krankheit_id) VALUE (
	(SELECT id
	 FROM OrtDerImpfung
	 WHERE identifier = 'impfzentrum_covid_fsme'),
	'61bd68be-9808-4247-9f3c-cd569dec7bc2');

# impfzentrum_all
# Add FSME and AFFENPOCKEN, Covid added by default
INSERT INTO OrtDerImpfung_Krankheit (ortDerImpfung_id, krankheit_id) VALUE (
	(SELECT id
	 FROM OrtDerImpfung
	 WHERE identifier = 'impfzentrum_all'),
	'61bd68be-9808-4247-9f3c-cd569dec7bc2');
INSERT INTO OrtDerImpfung_Krankheit (ortDerImpfung_id, krankheit_id) VALUE (
	(SELECT id
	 FROM OrtDerImpfung
	 WHERE identifier = 'impfzentrum_all'),
	'4b99ace7-443c-4efc-98a5-388ba12cd1ba');