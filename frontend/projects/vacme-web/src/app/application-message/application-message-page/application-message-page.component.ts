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

import {DatePipe} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {Moment} from 'moment';
import {ApplicationMessageJaxTS, DateTimeRangeJaxTS, PagerJaxApplicationMessageJaxTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {MessagesService} from 'vacme-web-generated';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import {DATE_TIME_FORMAT} from '../../../../../vacme-web-shared/src/lib/constants';
import {PagerEvent, PagerState} from '../../../../../vacme-web-shared/src/lib/simple-pager/pager-enum';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';

const LOG = LogFactory.createLog('ApplicationMessagePageComponent');

@Component({
    selector: 'app-application-message-page',
    templateUrl: './application-message-page.component.html',
    styleUrls: ['./application-message-page.component.scss'],
})
export class ApplicationMessagePageComponent extends BaseDestroyableComponent implements OnInit {

    public page: PagerJaxApplicationMessageJaxTS | undefined;
    private pageIndex: number | undefined;
    private readonly PAGE_SIZE = 20;
    loading = true;
    pagerState = PagerState.NONE;

    constructor(
        private translationService: TranslateService,
        private messagesService: MessagesService,
        private router: Router,
        private datePipe: DatePipe,
    ) {
        super();
    }

    ngOnInit(): void {
        this.retrieveTable(0);
    }

    private retrieveTable(page: number): void {
        this.loading = true;
        this.messagesService.applicationMessageResourceGetApplicationMessageAll(page, this.PAGE_SIZE).subscribe(pager => {
            if (pager != null) {
                this.page = pager;
            } else {
                this.page = undefined;
            }
            this.updatePagerState();
            this.loading = false;
        }, (error) => {
            LOG.error(error);
        });
    }

    public formatZeitFenster(zeitFenster: DateTimeRangeJaxTS | undefined): string {
        if (!zeitFenster) {
            return '';
        }
        return this.translationService.instant('APPMESSAGE.ADMINISTRATION.DATE', {
            bisZeit: this.datePipe.transform(zeitFenster.bis, 'dd.MM.YYYY, HH:mm'),
            vonZeit: this.datePipe.transform(zeitFenster.von, 'dd.MM.YYYY, HH:mm'),
        });
    }

    public getMessages(): ApplicationMessageJaxTS[] {
        if (this.page?.pageElements) {
            return this.page.pageElements;
        }
        return [];
    }

    public messageBearbeiten(messageId: string | undefined): void {
        if (messageId) {
            void this.router.navigate(['appmessage', 'bearbeiten', messageId]);
        }
    }

    public pageChange(event: PagerEvent): void {
        const pageIndex = this.pageIndex ? this.pageIndex : 0;
        switch (event) {
            case PagerEvent.NEXT:
                this.retrieveTable(pageIndex + 1);
                break;
            case PagerEvent.PREVIOUS:
                this.retrieveTable(pageIndex - 1);
                break;
        }
    }

    private updatePagerState(): void {
        if (this.page) {
            this.pageIndex = this.page.currentPage;
            const pageAmount = this.page.pageAmount;
            if (pageAmount && pageAmount !== 1) {
                if (!this.pageIndex) {
                    this.pagerState = PagerState.DISPLAY_NEXT;
                } else if (this.pageIndex === pageAmount - 1) {
                    this.pagerState = PagerState.DISPLAY_PREVIOUS;
                } else {
                    this.pagerState = PagerState.DISPLAY_NEXT_AND_PREVIOUS;
                }
            } else {
                this.pagerState = PagerState.NONE;
            }
        } else {
            this.pagerState = PagerState.NONE;
        }
    }

    public showPager(): boolean {
        return this.pagerState !== PagerState.NONE;
    }

    public getZeitStatus(message: ApplicationMessageJaxTS): string {
        if (!message.zeitfenster) {
            return '';
        }
        const now: Moment = DateUtil.now();
        const von: Moment | undefined = DateUtil.localDateTimeToMomentWithFormat(
            DateUtil.dateAsLocalDateTimeString(message.zeitfenster.von, DATE_TIME_FORMAT), DATE_TIME_FORMAT);
        const bis: Moment | undefined = DateUtil.localDateTimeToMomentWithFormat(
            DateUtil.dateAsLocalDateTimeString(message.zeitfenster.bis, DATE_TIME_FORMAT), DATE_TIME_FORMAT);
        if (von?.isAfter(now)) {
            return this.translationService.instant('APPMESSAGE.ADMINISTRATION.ZEIT_FUTURE');
        } else if (bis?.isBefore(now)) {
            return this.translationService.instant('APPMESSAGE.ADMINISTRATION.ZEIT_PAST');
        }
        return this.translationService.instant('APPMESSAGE.ADMINISTRATION.ZEIT_PRESENT');
    }
}
