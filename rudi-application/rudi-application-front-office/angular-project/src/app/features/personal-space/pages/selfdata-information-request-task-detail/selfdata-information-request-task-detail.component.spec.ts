import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';

import {SelfdataInformationRequestTaskDetailComponent} from './selfdata-information-request-task-detail.component';

describe('SelfdataDetailComponent', () => {
    let component: SelfdataInformationRequestTaskDetailComponent;
    let fixture: ComponentFixture<SelfdataInformationRequestTaskDetailComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SelfdataInformationRequestTaskDetailComponent],
            providers: [{provide: ActivatedRoute, useValue: {params: of({taskId: 'task-uuid'})}}]
        })
            .compileComponents();
    });

    beforeEach(async () => {
        fixture = TestBed.createComponent(SelfdataInformationRequestTaskDetailComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
