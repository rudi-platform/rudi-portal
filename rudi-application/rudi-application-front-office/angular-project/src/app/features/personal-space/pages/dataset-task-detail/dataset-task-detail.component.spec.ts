import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DatasetTaskDetailComponent} from './dataset-task-detail.component';

describe('RequestDetailComponent', () => {
    let component: DatasetTaskDetailComponent;
    let fixture: ComponentFixture<DatasetTaskDetailComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [DatasetTaskDetailComponent]
        })
            .compileComponents();
    });

    beforeEach(async () => {
        fixture = TestBed.createComponent(DatasetTaskDetailComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
