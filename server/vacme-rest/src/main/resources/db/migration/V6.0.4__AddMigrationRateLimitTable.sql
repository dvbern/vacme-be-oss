/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

CREATE TABLE MigrationRateLimit (
       id                                VARCHAR(36)   NOT NULL PRIMARY KEY,
       timestampErstellt                 DATETIME(6)   NOT NULL,
       timestampMutiert                  DATETIME(6)   NOT NULL,
       userErstellt                      VARCHAR(255)  NOT NULL,
       userMutiert                       VARCHAR(255)  NOT NULL,
       version                           BIGINT        NOT NULL,
       timestampLastRequest              DATETIME(6)   NOT NULL
);
