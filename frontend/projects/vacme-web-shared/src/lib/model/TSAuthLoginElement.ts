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

import {ISerializable} from './ISerializable';

export default class TSAuthLoginElement implements ISerializable<TSAuthLoginElement> {

    public username?: string;  // entspricht email
    public password?: string;

    constructor(username?: string, password?: string) {
        this.username = username;
        this.password = password;
    }

    public deserialize(restObject: any): TSAuthLoginElement {
        this.username = restObject.username;
        this.password = restObject.password;

        return this;
    }

    public serialize(restObject: any): any {
        restObject.username = this.username;
        restObject.password = this.password;

        return restObject;
    }
}
