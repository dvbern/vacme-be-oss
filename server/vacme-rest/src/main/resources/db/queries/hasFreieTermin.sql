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

select slot.id
from Impfslot slot
	inner join Impftermin termin on (termin.impfslot_id=slot.id)
	inner join OrtDerImpfung odi on slot.ortDerImpfung_id=odi.id
where termin.gebucht = false
	and odi.terminverwaltung = true
	and odi.oeffentlich = true
	and (termin.impffolge = 'ERSTE_IMPFUNG' OR termin.impffolge = 'BOOSTER_IMPFUNG')
	and slot.bis > :bisDate
	and slot.krankheitIdentifier = :krankheitIdentifier
	limit :minTermin, 1;
