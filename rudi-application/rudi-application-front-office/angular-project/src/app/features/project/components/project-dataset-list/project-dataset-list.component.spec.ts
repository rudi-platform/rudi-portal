import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {OrganizationLogoComponent} from '@shared/business/organisation/organization-logo/organization-logo.component';
import {ProjectDatasetItem} from '../../model/project-dataset-item';
import {ProjectDatasetPictoType} from '../../model/project-dataset-picto-type';
import {ProjectDatasetListComponent} from './project-dataset-list.component';

describe('ProjectDatasetListComponent', () => {
    let fixture: ComponentFixture<ProjectDatasetListComponent>;
    let component: ProjectDatasetListComponent;

    const restricted = {title: 'Restricted', titleIcon: 'key_icon_88_secondary-color', editable: true, pictoType: ProjectDatasetPictoType.LOGO} as ProjectDatasetItem;
    const open = {title: 'Open', pictoType: ProjectDatasetPictoType.LOGO} as ProjectDatasetItem;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ProjectDatasetListComponent]
        })
            .overrideComponent(ProjectDatasetListComponent, {
                remove: {imports: [OrganizationLogoComponent]},
                add: {schemas: [CUSTOM_ELEMENTS_SCHEMA]}
            })
            .compileComponents();

        fixture = TestBed.createComponent(ProjectDatasetListComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('items', [restricted, open]);
        fixture.detectChanges();
    });

    it('should expose the edit and remove actions as named buttons (RGAA 7.1, 7.3)', () => {
        const rows: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('.dataset-card'));
        const restrictedButtons: HTMLButtonElement[] = Array.from(rows[0].querySelectorAll('.dataset-actions button'));
        const openButtons: HTMLButtonElement[] = Array.from(rows[1].querySelectorAll('.dataset-actions button'));

        expect(restrictedButtons.map(button => button.getAttribute('aria-label'))).toEqual([
            'project.stepper.submission.step3.editItem',
            'project.stepper.submission.step3.removeItem'
        ]);
        expect(openButtons.length).toBe(1);
        [...restrictedButtons, ...openButtons].forEach(button => {
            expect(button.getAttribute('type')).toBe('button');
            expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
        });
    });

    it('should emit the edited and removed items', () => {
        const edited = jasmine.createSpy('edit');
        const removed = jasmine.createSpy('delete');
        component.edit.subscribe(edited);
        component.delete.subscribe(removed);
        const buttons: HTMLButtonElement[] = Array.from(fixture.nativeElement.querySelectorAll('.dataset-actions button'));

        buttons[0].click();
        buttons[2].click();

        expect(edited).toHaveBeenCalledOnceWith(restricted);
        expect(removed).toHaveBeenCalledOnceWith(open);
    });

    it('should move the focus to the next remove button after a removal', () => {
        component.delete.subscribe(item => component.items.splice(component.items.indexOf(item), 1));
        const removeFirst: HTMLButtonElement = fixture.nativeElement.querySelectorAll('.dataset-actions button')[1];

        removeFirst.click();
        fixture.detectChanges();

        const remaining: HTMLButtonElement = fixture.nativeElement.querySelector('.dataset-actions button');
        expect(fixture.nativeElement.ownerDocument.activeElement).toBe(remaining);
    });

    it('should give a text alternative to the access status icon only (RGAA 1.1)', () => {
        const icons: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('.dataset-info-title-icon'));

        expect(icons.length).toBe(1);
        expect(icons[0].getAttribute('aria-hidden')).toBe('false');
        expect(icons[0].getAttribute('aria-label')).toBe('metaData.accesRestreintLibelle');
    });

    it('should hide the producer logo, redundant with the displayed producer name (RGAA 1.2)', () => {
        const logos: (HTMLElement & {decorative?: boolean})[] = Array.from(fixture.nativeElement.querySelectorAll('app-organization-logo'));

        expect(logos.length).toBe(2);
        logos.forEach(logo => expect(logo.decorative).toBeTrue());
    });
});
