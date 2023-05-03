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

export class GeplanteImpfungen  {

    private readonly min: number = 0;
    private readonly max: number = 0;

    public constructor(min: number, max: number) {
        if (min) {
            this.min = min;
        }
        if (max) {
            this.max = max;
        }
    }

    public get getMin(): number {
        return this.min;
    }

    public get getMax(): number {
        return this.max;
    }

    public static add(summand1: GeplanteImpfungen, summand2: GeplanteImpfungen, summand3: GeplanteImpfungen): GeplanteImpfungen {
        return new GeplanteImpfungen(summand1.min + summand2.min + summand3.min, summand1.max + summand2.max + summand3.max);
    }

    public get showHint(): boolean {
        return this.min !== this.max;
    }

    public print(): string {
        if (this.min === this.max) {
            return String(this.min);
        }
        return this.min + ' - ' + this.max;
    }
}

