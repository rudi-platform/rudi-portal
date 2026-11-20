import {ComponentFixture, TestBed} from '@angular/core/testing';

import {
    WorkflowFormSubmitSuccessComponent
} from '@shared/core/workflow/forms/workflow-form-submit-success/workflow-form-submit-success.component';

describe('WorkflowFormSubmitSuccessComponent', () => {
    let component: WorkflowFormSubmitSuccessComponent;
    let fixture: ComponentFixture<WorkflowFormSubmitSuccessComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFormSubmitSuccessComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFormSubmitSuccessComponent);
        component = fixture.componentInstance;
        for (const key of ['title1Key', 'title2Key', 'subTitleKey', 'descriptionBeforeLinkKey', 'descriptionLinkTextKey',
            'descriptionAfterLinkKey', 'buttonMessageKey']) {
            fixture.componentRef.setInput(key, key);
        }
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
