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

import * as moment from 'moment';

import {ISerializable} from './ISerializable';
import DateUtil from '../util/DateUtil';

export default class TSAbstractEntity {
    public id?: string;
    public timestampErstellt?: moment.Moment;
    public version?: number;

    public isNew(): boolean {
        return !this.timestampErstellt;
    }

    public deserialize(restObject: any): TSAbstractEntity {
        this.id = restObject.id;
        this.version = restObject.version;
        this.timestampErstellt = DateUtil.localDateTimeToMoment(restObject.timestampErstellt);

        return this;
    }

    public serialize(restObject: any): any {
        restObject.id = this.id;
        restObject.version = this.version;

        return restObject;
    }

    public serializeOrNull(object: ISerializable<any> | undefined): any {
        if (object) {
            return object.serialize({});
        }
        return undefined;
    }

    public arrayToRestObject(array?: Array<ISerializable<any>>): Array<any> {
        const list: any[] = [];
        if (array) {
            for (let i = 0; i < array.length; i++) {
                list[i] = this.serializeOrNull(array[i]);
            }
        }
        return list;
    }

    public isEmptyString(str: string | undefined): boolean {
        return (!str || 0 === str.length);
    }

    public getOptionalString(str: string | undefined): string | undefined {
        if (this.isEmptyString(str)) {
            return undefined;
        }
        return str;
    }
}
