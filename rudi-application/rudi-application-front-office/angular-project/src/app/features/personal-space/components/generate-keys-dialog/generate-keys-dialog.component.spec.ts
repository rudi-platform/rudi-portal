import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenerateKeysDialogComponent } from './generate-keys-dialog.component';

describe('GenerateKeysDialogComponent', () => {
  let component: GenerateKeysDialogComponent;
  let fixture: ComponentFixture<GenerateKeysDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GenerateKeysDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(GenerateKeysDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
