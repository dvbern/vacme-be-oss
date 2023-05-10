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

import {Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class ApplicationMessageCacheService {

    private closedApplicationMessages: string[] | undefined;
    private readonly storageKey: string = 'closedApplicationMessages';

    constructor() {
    }

    public cacheClosedApplicationMessages(messageId: string): void {
        const cached: string[] = this.getClosedApplicationMessages();
        if (!this.isCached(messageId)) {
            cached.push(messageId);
            localStorage.setItem(this.storageKey, JSON.stringify(cached));
            this.closedApplicationMessages = cached;
        }
    }

    public isCached(messageId: string): boolean {
        const cache: string[] = this.getClosedApplicationMessages();
        return cache.indexOf(messageId) !== -1;
    }

    private getClosedApplicationMessages(): string[] {
        if (!this.closedApplicationMessages) {
            const cached = localStorage.getItem(this.storageKey);
            if (cached) {
                this.closedApplicationMessages = JSON.parse(cached) as string[];
            }
        }
        return this.closedApplicationMessages ? this.closedApplicationMessages : [];
    }
}
