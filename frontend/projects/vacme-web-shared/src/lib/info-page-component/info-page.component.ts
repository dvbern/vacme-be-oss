/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {BaseDestroyableComponent} from '../components/base-destroyable/base-destroyable.component';
import {LogFactory} from '../logging';

const LOG = LogFactory.createLog('InfoPageComponent');

@Component({
    selector: 'lib-info-page-component',
    templateUrl: './info-page.component.html',
    styleUrls: ['./info-page.component.scss'],
})
export class InfoPageComponent  extends BaseDestroyableComponent implements OnInit {

    @Input()
    public htmlstr = 'Welcome to the Infopage';

    constructor(
        private activeRoute: ActivatedRoute,
        private router: Router,
    ) {
        super();
    }
    ngOnInit(): void {

        this.activeRoute.queryParams
            .pipe(this.takeUntilDestroyed())
            .subscribe(params => {
                if (params.htmlstr !== undefined) {
                    this.htmlstr = params.htmlstr;
                }

            }, error => LOG.error(error));
    }
}
