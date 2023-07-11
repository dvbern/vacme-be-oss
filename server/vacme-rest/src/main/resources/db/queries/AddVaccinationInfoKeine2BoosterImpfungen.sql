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
VALUES ('ec4e4440-6457-49dd-962b-ea700c4069d8', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 1,
        '2022-06-22 00:00:00.000000', '2022-01-01 00:00:00.000000', 'INFO',
        'Über VacMe sind keine 2. Booster-Impfungen für Selbstzahlerinnen und Selbstzahler buchbar.<br>Bitte informieren Sie sich dazu auf folgenden Seiten:<li><a target=\'_blank\' href=\'https://www.gsi.be.ch/de/start/themen/coronavirus/impfen/boostercheck.html\'>Booster-Check</a></li><li><a target=\'_blank\' href=\'https://covid-kennzahlen.apps.be.ch/#/de/search\'>Impfortwebseite</a></li>',
        'GENERAL_INFOTEXT_DE');

INSERT INTO ApplicationMessage (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, bis, von,
                                status, htmlContent, title)
VALUES ('c63a76b1-9cae-44b7-aafd-96f692c3cd46', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 'flyway', 'flyway', 1,
        '2022-06-22 00:00:00.000000', '2022-01-01 00:00:00.000000', 'INFO',
        'VacMe ne permet pas de prendre rendez-vous pour une deuxième dose de rappel aux frais de la patientèle.<br>Des informations sont disponibles sur les pages suivantes :<li><a target=\'_blank\' href=\'https://www.gsi.be.ch/fr/start/themen/coronavirus/impfen/boostercheck.html\'>Vaccination de rappel dans le canton de Berne</a></li><li><a target=\'_blank\' href=\'https://covid-kennzahlen.apps.be.ch/#/fr/search\'>Lieux de vaccination</a></li>',
        'GENERAL_INFOTEXT_FR');