
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

import {isSeverityEnabled} from './logging-helpers';
import {LogSeverity} from './LogSeverity';

describe('logging-helpers', () => {
  describe('isSeverityEnabled', () => {
    type TestData = [boolean, LogSeverity, LogSeverity];

    ([
      [false, LogSeverity.TRACE, LogSeverity.ERROR],
      [false, LogSeverity.DEBUG, LogSeverity.ERROR],
      [false, LogSeverity.INFO, LogSeverity.ERROR],
      [false, LogSeverity.WARN, LogSeverity.ERROR],
      [true, LogSeverity.ERROR, LogSeverity.ERROR],

      [false, LogSeverity.TRACE, LogSeverity.WARN],
      [false, LogSeverity.DEBUG, LogSeverity.WARN],
      [false, LogSeverity.INFO, LogSeverity.WARN],
      [true, LogSeverity.WARN, LogSeverity.WARN],
      [true, LogSeverity.ERROR, LogSeverity.WARN],

      [false, LogSeverity.TRACE, LogSeverity.INFO],
      [false, LogSeverity.DEBUG, LogSeverity.INFO],
      [true, LogSeverity.INFO, LogSeverity.INFO],
      [true, LogSeverity.WARN, LogSeverity.INFO],
      [true, LogSeverity.ERROR, LogSeverity.INFO],

      [false, LogSeverity.TRACE, LogSeverity.DEBUG],
      [true, LogSeverity.DEBUG, LogSeverity.DEBUG],
      [true, LogSeverity.INFO, LogSeverity.DEBUG],
      [true, LogSeverity.WARN, LogSeverity.DEBUG],
      [true, LogSeverity.ERROR, LogSeverity.DEBUG],

      [true, LogSeverity.TRACE, LogSeverity.TRACE],
      [true, LogSeverity.DEBUG, LogSeverity.TRACE],
      [true, LogSeverity.INFO, LogSeverity.TRACE],
      [true, LogSeverity.WARN, LogSeverity.TRACE],
      [true, LogSeverity.ERROR, LogSeverity.TRACE],
    ] as TestData[]).forEach(([expected, severity, minLevel]) =>
      it(`should return ${expected} for LogLevel: ${severity}, minLevel: ${minLevel}`, () => {
        const actual = isSeverityEnabled(severity, minLevel);

        expect(actual)
          .toEqual(expected);
      }));
  });
});
