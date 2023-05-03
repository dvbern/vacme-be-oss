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

/**
 * parse CSS style color value.
 *
 * See <a
 * href="https://stackoverflow.com/questions/11068240/what-is-the-most-efficient-way-to-parse-a-css-color-in-javascript">Stackoverflow</a>
 */
export type RGBColor = [number, number, number];

export function parseColor(input: string): RGBColor {
  const div: HTMLDivElement = document.createElement('div');
  // need to add div to document or else getComputedStyle() returns all empties.
  // noinspection XHTMLIncompatabilitiesJS
  document.body.appendChild(div);

  div.style.color = input.toLowerCase();
  const colorStyle = getComputedStyle(div).color;

  // noinspection XHTMLIncompatabilitiesJS
  document.body.removeChild(div);

  const m = colorStyle.match(/^rgb\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)$/i);
  if (m) {
    return [parseInt(m[1], 10), parseInt(m[2], 10), parseInt(m[3], 10)];
  } else {
    throw new Error('Colour ' + input + ' could not be parsed.');
  }
}
