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

import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {ErrorMessageService} from '../../service/error-message.service';

import {DvErrorMessagesComponent} from './dv-error-messages.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';

describe('DvErrorMessagesComponent', () => {
    let component: DvErrorMessagesComponent;
    let fixture: ComponentFixture<DvErrorMessagesComponent>;
    let errorService: ErrorMessageService;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [NgbModal],
            providers: [ErrorMessageService],
            declarations: [DvErrorMessagesComponent],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(DvErrorMessagesComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        errorService = TestBed.inject(ErrorMessageService); // actually inject
    });

    it('should create', () => {
        expect(component).toBeTruthy();
        expect(errorService).toBeTruthy();

    });

});
