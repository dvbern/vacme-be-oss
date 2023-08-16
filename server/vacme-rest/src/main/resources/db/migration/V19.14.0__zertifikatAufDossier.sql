/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
ALTER TABLE Zertifikat DROP CONSTRAINT IF EXISTS FK_Zertifikat_registrierung;
ALTER TABLE Zertifikat ADD IF NOT EXISTS impfdossier_id VARCHAR(36);
ALTER TABLE Zertifikat ADD CONSTRAINT FK_Zertifikat_impfdossier FOREIGN KEY (impfdossier_id) REFERENCES Impfdossier(id);

# Migration
UPDATE Zertifikat Z
SET Z.impfdossier_id = (
	SELECT I.id FROM Impfdossier I
	            WHERE I.registrierung_id = Z.registrierung_id and I.krankheitIdentifier = 'COVID')
WHERE Z.impfdossier_id IS NULL;

# Wir entfernen die Spalte erst spaeter, wenn wir sicher sind, dass alles erfolgreich durchgelaufen ist
# ALTER TABLE Zertifikat DROP COLUMN IF EXISTS registrierung_id;