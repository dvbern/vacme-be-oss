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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ApplicationMessageJaxTS} from '../../../../vacme-web-generated/src/lib/model/application-message-jax';
import {ApplicationMessageStatusTS} from '../../../../vacme-web-generated/src/lib/model/application-message-status';

@Component({
  selector: 'lib-application-message-window',
  templateUrl: './application-message-window.component.html',
  styleUrls: ['./application-message-window.component.scss']
})
export class ApplicationMessageWindowComponent implements OnInit {

  @Input() applicationMessage: ApplicationMessageJaxTS | undefined;
  @Input() isOpen = false;
  @Output() closeEvent: EventEmitter<void> = new EventEmitter<void>();
  @Output() openEvent: EventEmitter<void> = new EventEmitter<void>();

  constructor() { }

  ngOnInit(): void {
  }

  public getStatusClass(): string {
      switch (this.applicationMessage?.status) {
          case ApplicationMessageStatusTS.ERROR:
              return 'error';
          case ApplicationMessageStatusTS.INFO:
              return 'info';
          case ApplicationMessageStatusTS.WARNING:
              return 'warning';
      }
      return '';
  }

  public close(): void {
      this.closeEvent.emit();
  }

  public open(): void {
      this.openEvent.emit();
  }

}
