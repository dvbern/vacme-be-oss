import { TestBed } from '@angular/core/testing';

import { VacmeWebGeneratedService } from './vacme-web-generated.service';

describe('VacmeWebGeneratedService', () => {
  let service: VacmeWebGeneratedService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VacmeWebGeneratedService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
