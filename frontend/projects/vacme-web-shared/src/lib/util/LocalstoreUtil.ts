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

export default class LocalstoreUtil {
    public static setWithExpiry(key: string, value: any, ttl: number): void {
        const now = new Date();

        // `item` is an object which contains the original value
        // as well as the time when it's supposed to expire
        const item = {
            // eslint-disable-next-line object-shorthand
            value: value,
            expiry: now.getTime() + ttl,
        };
        localStorage.setItem(key, JSON.stringify(item));
    }

    public static getWithExpiry(key: string): any | null {
        const itemStr = localStorage.getItem(key);
        // if the item doesn't exist, return null
        if (!itemStr) {
            return null;
        }
        const item = JSON.parse(itemStr);
        const now = new Date();
        // compare the expiry time of the item with the current time
        if (now.getTime() > item.expiry) {
            // If the item is expired, delete the item from storage
            // and return null
            localStorage.removeItem(key);
            return null;
        }
        return item.value;
    }

    public static delete(key: string): void {
        localStorage.removeItem(key);
    }
}
