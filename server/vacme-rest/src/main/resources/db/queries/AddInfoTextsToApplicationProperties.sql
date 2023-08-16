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

INSERT INTO ApplicationMessage (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, bis, von,
								status, htmlContent, title)
VALUES ('2fbb5fc8-45cc-4df5-86f9-b975c49bea11', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 1,
		'1900-12-31 00:00:00.000000', '1900-01-01 00:00:00.000000', 'INFO',
		'Derzeit werden von der Gesundheitsdirektion des Kantons Zürich Reminder für die Booster-Impfung via SMS verschickt. Falls Sie eine solche SMS erhalten haben, jedoch bereits eine Booster-Impfung erhalten haben, können Sie die SMS ignorieren. In diesem Fall ist keine Aktion Ihrerseits erforderlich.',
		'GENERAL_INFOTEXT_DE');

INSERT INTO ApplicationMessage (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, bis, von,
								status, htmlContent, title)
VALUES ('7bacaece-4225-4b16-b5a5-413fd9119275', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 1,
		'1900-12-31 00:00:00.000000', '1900-01-01 00:00:00.000000', 'INFO',
		'Actuellement, la direction de la santé du canton de Zurich envoie des rappels pour la vaccination de rappel par SMS. Si vous avez reçu un tel SMS mais que vous avez déjà reçu une vaccination de rappel, vous pouvez l''ignorer. Dans ce cas, aucune action de votre part n''est nécessaire.',
		'GENERAL_INFOTEXT_FR');
INSERT INTO ApplicationMessage (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, bis, von,
								status, htmlContent, title)
VALUES ('69232e32-c148-4015-bbb7-34f9adf3a0ed', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 1,
		'1900-12-31 00:00:00.000000', '1900-01-01 00:00:00.000000', 'INFO',
		'Currently, reminders for booster vaccinations are sent via SMS by the Health Department of the Canton of Zurich. If you have received such an SMS but have already received a booster vaccination, you can ignore the SMS. In this case, no action is required on your part.',
		'GENERAL_INFOTEXT_EN');

