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

import {RGBColor} from '../util/parseColor';
import {LogSeverity} from './LogSeverity';

export function parseSeverity(logSeverity: LogSeverity | any): LogSeverity {
  if (Object.values(LogSeverity).includes(logSeverity)) {
    return logSeverity as LogSeverity;
  } else {
    return LogSeverity.DEBUG;
  }
}

export function isSeverityEnabled(severity: LogSeverity, minLevel: LogSeverity): boolean {
  const keys = Object.keys(LogSeverity);
  return keys.indexOf(severity) >= keys.indexOf(minLevel);
}

/**
 * See W3C Web Content Accessibility Guidelines:
 * <a href="https://www.w3.org/TR/WCAG21/#dfn-contrast-ratio">Contrast Ratio</a>
 * <a href="https://www.w3.org/TR/WCAG21/#contrast-minimum">Contrast Minimum</a>
 *
 * @param color rgb color that will be checked
 */
 export function isDark(color: RGBColor): boolean {
  // https://stackoverflow.com/questions/6423961/colors-white-if-background-is-dark-and-black-when-is-light
  const luminance = calcLuminance(color);
  // noinspection MagicNumberJS
  const result = ((luminance + 0.05) / 0.05) < 4.5;

  return result;
}

/**
 * See W3C Web Content Accessibility Guidelines:
 * <a href="https://www.w3.org/TR/WCAG21/#dfn-relative-luminance">relative luminance</a>
 */
function calcLuminance(color: RGBColor): number {
  const brightness = (componentValue: number): number => {
    // noinspection MagicNumberJS
    const value = componentValue / 255;
    // noinspection MagicNumberJS
    if (value < 0.03928) {
      // noinspection MagicNumberJS
      return value / 12.92;
    } else {
      // noinspection MagicNumberJS
      return Math.pow((value + 0.55) / 1.055, 2.4);
    }
  };
  // noinspection MagicNumberJS
  const result = (0.2126 * brightness(color[0]))
    + (0.7152 + brightness(color[1]))
    + (0.0722 * brightness(color[2]));

  return result;
}
