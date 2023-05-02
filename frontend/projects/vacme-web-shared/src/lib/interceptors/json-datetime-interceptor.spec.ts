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

import {HttpClient, HttpHeaders} from '@angular/common/http';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {bodyToBackend, bodyToFrontend, Configurator} from './json-datetime-interceptor';


describe('HttpJsonDateInterceptor', () => {
  describe('bodyToFrontend', () => {
    let fixture: any;

    beforeEach(() => {
      fixture = {
        foo: 'asdf',
        bar: 123,
        myDate: '2020-12-17',
        nested: {
          schnitzel: 'mit pommes',
          doener: 321,
          otherDate: '1976-11-19',
        },
      };
    });

    it('returns a deep clone', () => {
      const actual = bodyToFrontend(fixture, new Configurator({missingTimezoneConversion: 'treat-as-utc'}));

      expect(typeof actual)
        .toEqual('object');

      expect(actual)
        .not.toBe(fixture);

      fixture.nested.schnitzel = 'mit ketchup';
      expect(actual.nested.schnitzel)
        .toEqual('mit pommes');
    });

    it('keeps non-date fields the same', () => {
      const actual = bodyToFrontend(fixture, new Configurator({missingTimezoneConversion: 'treat-as-utc'}));

      const joc = jasmine.objectContaining;
      expect(actual)
        .toEqual(joc({
          foo: 'asdf',
          bar: 123,
          nested: joc({
            schnitzel: 'mit pommes',
            doener: 321,
          }),
        }));
    });

    describe('treat-as-utc', () => {
      it('converts only date fields recursively returning UTC', () => {
        const actual = bodyToFrontend(fixture, new Configurator({missingTimezoneConversion: 'treat-as-utc'}));

        expect(actual.myDate)
          .toEqual(new Date(Date.UTC(2020, 11, 17)));

        expect(actual.nested.otherDate)
          .toEqual(new Date(Date.UTC(1976, 10, 19)));
      });
    });

    describe('treat-as-local', () => {

      it('converts only date fields recursively returning local timezone', () => {
        const actual = bodyToFrontend(fixture, new Configurator({missingTimezoneConversion: 'treat-as-local'}));

        expect(actual.myDate)
          .toEqual(new Date(2020, 11, 17));

        expect(actual.nested.otherDate)
          .toEqual(new Date(1976, 10, 19));
      });
    });

  });

  describe('bodyToBackend', () => {
    let fixture: any;

    beforeEach(() => {
      fixture = {
        foo: 'asdf',
        bar: 123,
        myDate: null,
        nested: {
          schnitzel: 'mit pommes',
          doener: 321,
          otherDate: null,
        },
      };
    });

    it('returns a deep clone', () => {
      const actual = bodyToBackend(fixture, new Configurator({missingTimezoneConversion: 'treat-as-utc'}));

      expect(typeof actual)
        .toEqual('object');

      expect(actual)
        .not.toBe(fixture);

      fixture.nested.schnitzel = 'mit ketchup';
      expect(actual.nested.schnitzel)
        .toEqual('mit pommes');
    });

    it('keeps non-date fields the same', () => {
      const actual = bodyToBackend(fixture, new Configurator({missingTimezoneConversion: 'treat-as-utc'}));

      const joc = jasmine.objectContaining;
      expect(actual)
        .toEqual(joc({
          foo: 'asdf',
          bar: 123,
          nested: joc({
            schnitzel: 'mit pommes',
            doener: 321,
          }),
        }));
    });

    describe('treat-as-utc', () => {
      it('converts frontend UTC date fields, stripping timezone', () => {
        fixture.myDate = new Date(Date.UTC(2020, 11, 17));
        fixture.nested.otherDate = new Date(Date.UTC(1976, 10, 19));

        const actual = bodyToBackend(fixture, new Configurator({missingTimezoneConversion: 'treat-as-utc'}));

        expect(actual.myDate)
          .toEqual('2020-12-17T00:00:00.000');

        expect(actual.nested.otherDate)
          .toEqual('1976-11-19T00:00:00.000');
      });
    });

    describe('treat-as-local', () => {
      it('converts frontend local-date fields, stripping timezone', () => {
        fixture.myDate = new Date(2020, 11, 17);
        fixture.nested.otherDate = new Date(1976, 10, 19);
        const actual = bodyToBackend(fixture, new Configurator({missingTimezoneConversion: 'treat-as-local'}));

        expect(actual.myDate)
          .toEqual('2020-12-17T00:00:00.000');

        expect(actual.nested.otherDate)
          .toEqual('1976-11-19T00:00:00.000');
      });
    });
  });

  describe('integrated in compoment', () => {
    let httpMock: HttpTestingController;
    let httpClient: HttpClient;

    interface MockData {
      asdf: string;
      someDate: Date | string;
      nested: {
        banana: string;
        otherDate: Date | string;
      };
    }

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [
          HttpClientTestingModule,

        ],
      });

      httpMock = TestBed.inject(HttpTestingController);
      httpClient = TestBed.inject(HttpClient);
    });

    it('transforms from backend', () => {
      httpClient.get<MockData>('/foo')
        .subscribe(
          resp => expect(resp)
            .toEqual({
              asdf: 'fdsa',
              someDate: new Date(2020, 11, 17),
              nested: {
                banana: 'yellow',
                otherDate: new Date(1976, 10, 19),
              },
            }),
          error => fail(error),
        );

      httpMock.expectOne('/foo')
        .flush({
            asdf: 'fdsa',
            someDate: '2020-12-17',
            nested: {
              banana: 'yellow',
              otherDate: '1976-11-19',
            },
          },
          {
            headers: new HttpHeaders({
              'Content-Type': 'application/json',
            }),
          });

    });

    it('transforms to backend', () => {
      httpClient.post<MockData>('/foo', {
        asdf: 'fdsa',
        someDate: '2020-12-17',
        nested: {
          banana: 'yellow',
          otherDate: '1976-11-19',
        },
      })
        .subscribe(
          ignored => {},
          error => fail(error),
        );

      const mockRequest = httpMock.expectOne('/foo');

      expect(mockRequest.request.body)
        .toEqual({
          asdf: 'fdsa',
          someDate: '2020-12-17',
          nested: {
            banana: 'yellow',
            otherDate: '1976-11-19',
          },
        });

    });

  });
});
