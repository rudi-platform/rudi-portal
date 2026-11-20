import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';

import { CmsOrderComponent } from './order.component';

describe('CmsOrderComponent', () => {
  let component: CmsOrderComponent;
  let fixture: ComponentFixture<CmsOrderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CmsOrderComponent],
      providers: [provideTranslateService()]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CmsOrderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
