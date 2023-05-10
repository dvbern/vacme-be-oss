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
 * Interface to serialize and deserialize from and to JSon - Typescript object
 *
 * Example use:
 *
 * class Member implements Serializable<Member> {
 *    id: number;
 *
 *    deserialize(restObject: any) : Member {
 *        super.deserialize(restObject);
 *        this.id = input.id;
 *        return this;
 *    }
 * }
 *
 * class ExampleClass extends TSAbstractEntity implements Serializable<ExampleClass> {
 *    firstMember: Member;
 *    secondMember: Member;
 *
 *    deserialize(restObject: any) : ExampleClass {
 *        super.deserialize(restObject);
 *
 *        this.firstMember = new Member().deserialize(input.firstMember);
 *        this.secondMember = new Member().deserialize(input.secondMember);
 *
 *        return this;
 *    }
 * }
 *
 *
 *
 */
export interface ISerializable<T> {

    // rest-Object from Server to Typescript-Object
    deserialize(restObject: any): T;

    // Typescript-Object to Server rest-Object
    serialize(restObject: any): any;

}
