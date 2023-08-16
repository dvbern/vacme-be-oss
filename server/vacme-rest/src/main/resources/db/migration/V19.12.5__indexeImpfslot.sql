/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

DROP INDEX IF EXISTS IX_Impfslot_odi ON Impfslot;
DROP INDEX IF EXISTS IX_Impfslot_von ON Impfslot;
DROP INDEX IF EXISTS IX_Impfslot_bis ON Impfslot;
DROP INDEX IF EXISTS IX_Impfslot_odi_bis_krankheitIdentifier ON Impfslot;

CREATE INDEX IF NOT EXISTS IX_Impfslot_odi_bis ON Impfslot(ortDerImpfung_id, bis, id);
CREATE INDEX IF NOT EXISTS IX_Impfslot_krankheit_odi_bis_von ON Impfslot(krankheitIdentifier, ortDerImpfung_id, bis, von, id);