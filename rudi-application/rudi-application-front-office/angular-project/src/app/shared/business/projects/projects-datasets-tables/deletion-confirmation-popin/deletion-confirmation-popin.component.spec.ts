import {ComponentFixture, TestBed} from '@angular/core/testing';

import {
    DeletionConfirmationPopinComponent
} from '@shared/business/projects/projects-datasets-tables/deletion-confirmation-popin/deletion-confirmation-popin.component';

describe('DeletionConfirmationPopinComponent', () => {
    let component: DeletionConfirmationPopinComponent;
    let fixture: ComponentFixture<DeletionConfirmationPopinComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DeletionConfirmationPopinComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DeletionConfirmationPopinComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
