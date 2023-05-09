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

import {Component, OnInit} from '@angular/core';
import {VacmeWebSharedService} from '../vacme-web-shared.service';
import {LogFactory} from '../logging';



const LOG = LogFactory.createLog('GreetingComponentComponent');

@Component({
  selector: 'lib-greeting-component',
  templateUrl: './greeting-component.component.html',
  styleUrls: ['./greeting-component.component.scss']
})
export class GreetingComponentComponent implements OnInit {

  private greeting = '';

  constructor(
    private service: VacmeWebSharedService
  ) { }

  ngOnInit(): void {
    LOG.info('ok this component is iniitalized');
    this.service.getGreeting$().subscribe(value => {
      return this.greeting = value;
    }, error => LOG.error(error));
  }

  public getGreeting(): string {
    return this.greeting;
  }

}
