import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {provideTranslateService} from '@ngx-translate/core';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';

import {Step2ProjectComponent} from './step2-project.component';

describe('Step2ProjectComponent', () => {
    let fixture: ComponentFixture<Step2ProjectComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [Step2ProjectComponent],
            providers: [
                provideTranslateService(),
                {provide: OrganizationService, useValue: {}}
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(Step2ProjectComponent);
        fixture.componentRef.setInput('step2FormGroup', new FormGroup({
            ownerType: new FormControl('ORGANIZATION'),
            organizationUuid: new FormControl(null, Validators.required),
            lastname: new FormControl(''),
            firstname: new FormControl(''),
            contactEmail: new FormControl('', Validators.required),
            relatedOrganizationUuids: new FormControl([])
        }));
        fixture.componentRef.setInput('organizationItems', [{uuid: 'org-1', name: 'Organization 1'}]);
        fixture.detectChanges();
    });

    it('should name the organization select by its visible label (RGAA 11.1)', () => {
        const select: HTMLElement = fixture.nativeElement.querySelector('mat-select#organization-name');
        const label: HTMLLabelElement = fixture.nativeElement.querySelector(`label[for="${select.id}"]`);
        expect(label.textContent).toContain('project.stepper.submission.step2.ownerType.organization.name.label');
        expect(select.getAttribute('aria-labelledby').trim().split(/\s+/)).toContain(label.id);
    });
});
