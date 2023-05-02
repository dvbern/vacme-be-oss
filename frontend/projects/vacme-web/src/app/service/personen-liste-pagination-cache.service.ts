import {Injectable} from '@angular/core';
import {Observable, of, Subject} from 'rxjs';
import {filter, map} from 'rxjs/operators';
import {OdibenutzerService, OdiUserJaxTS} from 'vacme-web-generated';
import {
    BaseDestroyableComponent
} from '../../../../vacme-web-shared';
import {TSAppEventTyp} from '../../../../vacme-web-shared/src/lib/model';
import {LogFactory} from 'vacme-web-shared';
import OdiUserPaging from '../../../../vacme-web-shared/src/lib/model/OdiUserPaging';
import {ApplicationEventService} from '../../../../vacme-web-shared/src/lib/service/application-event.service';
import {AppEvent} from '../../../../vacme-web-shared/src/lib/model';
import UniqueElementUtil from '../../../../vacme-web-shared/src/lib/util/UniqueElementUtil';


const LOG = LogFactory.createLog('PersonenListePaginationCacheService');

@Injectable({
    providedIn: 'root',
})
export class PersonenListePaginationCacheService extends BaseDestroyableComponent {

    private BATCH_SIZE = 100;

    // map of odi Identifier to List of Persons
    private odiToPersonenMap: Map<string, OdiUserPaging> = new Map<string, OdiUserPaging>();

    private _personListForOdi$ = new Subject<OdiUserPaging>();

    constructor(
        private odibenutzerService: OdibenutzerService,
        private applicationEventService: ApplicationEventService,
    ) {
        super();
        this.applicationEventService.appEventStream$
            .pipe(
                filter<AppEvent>(value => value.appEventTyp === TSAppEventTyp.USER_CHANGED
                    || value.appEventTyp === TSAppEventTyp.USER_CREATED),
                this.takeUntilDestroyed()
            ).subscribe(event => {this.ensureUserLoaded(event.data.group, event.data.username);},
                error => LOG.error(error));
    }

    public loadNextBatch$(odiIdentifier: string): Observable<OdiUserPaging> {
        if (this.odiToPersonenMap.has(odiIdentifier)) {
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            const odiUserCachedPages = this.odiToPersonenMap.get(odiIdentifier)!;
            if (odiUserCachedPages.foundAll) {
                return of(odiUserCachedPages);
            } else {
                const firstElement = odiUserCachedPages.page * this.BATCH_SIZE;
                const maxElementsToLoad = this.BATCH_SIZE;

                return this.odibenutzerService.odiBenutzerResourceGetUsersFromGroup(
                    odiIdentifier,
                    firstElement,
                    maxElementsToLoad
                ).pipe(map(odiPersonen => {
                    odiUserCachedPages.page++; // store next page to load

                    if (odiPersonen.length < this.BATCH_SIZE) {
                        odiUserCachedPages.foundAll = true;
                    }

                    odiUserCachedPages.list = odiUserCachedPages.list.concat(odiPersonen);
                    // make sure each user only is displayed once (needed because we insert new users manually)
                    odiUserCachedPages.list  = UniqueElementUtil.uniqueObjectsById(odiUserCachedPages.list);
                    return odiUserCachedPages;
                }));
            }
        } else {
            return this.odibenutzerService.odiBenutzerResourceGetUsersFromGroup(
                odiIdentifier,
                0,
                this.BATCH_SIZE
            ).pipe(map(odiPersonen => {
                const tablePageToEmit: OdiUserPaging = {
                    page: 1,
                    foundAll: odiPersonen.length < this.BATCH_SIZE,
                    list: odiPersonen,
                    odiIdentifier
                };
                this.odiToPersonenMap.set(odiIdentifier, tablePageToEmit);
                // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                return this.odiToPersonenMap.get(odiIdentifier)!;
            }));
        }
    }


    private ensureUserLoaded( odiIdentifier: string, username: string ): void {
        LOG.info('Reloading User in pagination for username ' + username);
        this.odibenutzerService.odiBenutzerResourceGetUserFromGroup(odiIdentifier, username) // laod single user
            .subscribe(loadedUsr => {
                const odiUserPaging = this.odiToPersonenMap.get(odiIdentifier);
                if (odiUserPaging) {

                    if (odiUserPaging.list.some(value => value.id === loadedUsr.id)) {
                        // iterate through array and if the id of the loaded element matches the one in the array then replace it
                        odiUserPaging.list = odiUserPaging.list.map(element => element.id === loadedUsr.id ? loadedUsr : element);
                    } else {
                        odiUserPaging.list = [loadedUsr].concat(odiUserPaging.list); // insert new user in front
                    }
                    this._personListForOdi$.next(odiUserPaging);
                }
            }, error => LOG.error(error));
    }

    get personListForOdi$(): Subject<OdiUserPaging> {
        return this._personListForOdi$;
    }
}
