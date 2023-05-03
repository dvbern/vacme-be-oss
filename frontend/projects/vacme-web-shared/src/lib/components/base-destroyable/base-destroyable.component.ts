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

import {Injectable, OnDestroy} from '@angular/core';
import {MonoTypeOperatorFunction, Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';

// Actually, @Injectable() is not required for abstract classes since these do not get injected ever.
// But: the abstract keyword ist lost somewhere in the angular compiler and thus omitting the decorator
// emits a warning message on ng serve/build
// => include the decorator until this is fixed.
@Injectable()
export abstract class BaseDestroyableComponent implements OnDestroy {

    protected readonly destroyedEmitter$ = new Subject<void>();

    constructor() {
    }

    ngOnDestroy(): void {
        this.destroyedEmitter$.next();
        this.destroyedEmitter$.complete();
    }

    /**
     * Unsubscribe from the Observable when {@link ngOnDestroy} is triggered by angular.
     * <p>
     *   <strong>Must be the last entry in a pipe()</strong>.
     * <p>
     * Usage: someObservable$.pipe(this.takeUntilDestroyed())
     */
    protected takeUntilDestroyed<T>(): MonoTypeOperatorFunction<T> {
        return takeUntil(this.destroyedEmitter$);
    }

}
