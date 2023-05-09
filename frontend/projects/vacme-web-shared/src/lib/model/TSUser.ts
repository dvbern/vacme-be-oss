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


import {OrtDerImpfungJaxTS} from 'vacme-web-generated';
import {ISerializable} from './ISerializable';
import TSAbstractEntity from './TSAbstractEntity';
import {rolePrefix, TSRole} from './TSRole';

/**
 * Entspricht ungefaehr den Felder die ueber JaxAuthAccessElement in das Cookie gesetzt werden.
 * Zusaetzliche Felder werden aus DB geladen.
 */
export default class TSUser extends TSAbstractEntity implements ISerializable<TSUser> {
    public username?: string;
    public nachname?: string;
    public vorname?: string;
    public telefon?: string;
    public password?: string;
    public email?: string;
    public roles: TSRole[] = [];
    public orteDerImpfung: OrtDerImpfungJaxTS[] = []; // orte der Impfung

    constructor(username?: string, vorname?: string, nachname?: string, password?: string, email?: string,
                role?: TSRole[], groups?: OrtDerImpfungJaxTS[], telefon?: string) {
        super();
        this.username = username;
        this.vorname = vorname;
        this.nachname = nachname;
        this.password = password;
        this.email = email;
        this.telefon = telefon;
        this.roles = role === undefined ? [] : role;
        this.orteDerImpfung = groups === undefined ? [] : groups;
    }

    public getFullName(): string {
        return `${this.vorname ? this.vorname : ''} ${this.nachname ? this.nachname : ''}`;
    }

    public getRoleKey(): string {
        return `${rolePrefix()}${this.roles}`;
    }

    public hasRole(role: TSRole): boolean {
        return this.roles.indexOf(role) > -1;
    }

    public hasGroupWithName(group: string): boolean {
        return this.orteDerImpfung
            .map(value => value.name)
            .indexOf(group) > -1;
    }

    public deserialize(restObject: any): TSUser {
        super.deserialize(restObject);

        // TODO: do we use??
        this.vorname = restObject.vorname;
        this.nachname = restObject.nachname;
        this.password = restObject.password;
        this.email = restObject.email;
        this.roles = restObject.roles;

        return this;
    }

    public serialize(restObject: any): any {
        super.serialize(restObject);
        // don't use!

        return restObject;
    }
}
