import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VacmeWebGeneratedComponent } from './vacme-web-generated.component';

describe('VacmeWebGeneratedComponent', () => {
  let component: VacmeWebGeneratedComponent;
  let fixture: ComponentFixture<VacmeWebGeneratedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VacmeWebGeneratedComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VacmeWebGeneratedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
