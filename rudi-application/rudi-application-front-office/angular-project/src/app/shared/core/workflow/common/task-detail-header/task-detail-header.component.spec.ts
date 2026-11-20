import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TaskDetailHeaderComponent} from '@shared/core/workflow/common/task-detail-header/task-detail-header.component';

describe('RequestDetailHeaderComponent', () => {
    let component: TaskDetailHeaderComponent;
    let fixture: ComponentFixture<TaskDetailHeaderComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TaskDetailHeaderComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TaskDetailHeaderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should expose the title and subtitle as the page h1 with a decorative icon (RGAA 9.1, 1.2)', () => {
        component.headingLoading = false;
        component.pageTitle = 'Demande d\'accès';
        component.pageSubTitle = 'My dataset';
        component.icon = 'restricted-dataset';
        fixture.changeDetectorRef.markForCheck();
        fixture.detectChanges();

        const element: HTMLElement = fixture.nativeElement;
        const headings = element.querySelectorAll('h1');
        expect(headings.length).toBe(1);
        expect(headings[0].textContent).toContain('Demande d\'accès');
        expect(headings[0].textContent).toContain('My dataset');

        const icon = headings[0].querySelector('mat-icon');
        expect(icon.getAttribute('aria-hidden')).toBe('true');
        expect(icon.hasAttribute('aria-label')).toBeFalse();
    });
});
