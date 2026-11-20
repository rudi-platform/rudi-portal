import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ProjectKey} from 'micro_service_modules/acl/acl-model';
import {ProjektService} from 'micro_service_modules/projekt/projekt-api';
import {Project} from 'micro_service_modules/projekt/projekt-model';
import {of, throwError} from 'rxjs';

import {GenerateKeysDialogComponent} from './generate-keys-dialog.component';

describe('GenerateKeysDialogComponent', () => {
    let component: GenerateKeysDialogComponent;
    let fixture: ComponentFixture<GenerateKeysDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [GenerateKeysDialogComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(GenerateKeysDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should name the dialog by its visible title (RGAA 7.1)', () => {
        const title: HTMLElement = fixture.nativeElement.querySelector('.dialog-title');
        expect(title.classList).toContain('mat-mdc-dialog-title');
        expect(title.id).toMatch(/^mat-mdc-dialog-title-/);
    });

    it('should link visible labels and hints to the step 1 inputs (RGAA 11.1)', () => {
        const name: HTMLInputElement = fixture.nativeElement.querySelector('input[formcontrolname="name"]');
        const password: HTMLInputElement = fixture.nativeElement.querySelector('#generate-key-password');
        expect(name.getAttribute('aria-labelledby')).toBe('generate-key-name-label');
        expect(name.getAttribute('aria-describedby')).toContain('generate-key-name-hint');
        expect(password.getAttribute('aria-labelledby')).toBe('generate-key-password-label');
        expect(password.getAttribute('aria-describedby')).toContain('generate-key-password-hint');
        expect(fixture.nativeElement.querySelector('#generate-key-name-label')).toBeTruthy();
        expect(fixture.nativeElement.querySelector('#generate-key-password-hint')).toBeTruthy();
    });

    it('should expose a localized password toggle button (RGAA 7.1)', () => {
        const toggle: HTMLButtonElement = fixture.nativeElement.querySelector('button[matsuffix]');
        expect(toggle.type).toBe('button');
        expect(toggle.getAttribute('aria-label')).toBe('common.showPassword');
        expect(toggle.hasAttribute('aria-pressed')).toBeFalse();
        const icon: HTMLElement = toggle.querySelector('mat-icon');
        expect(icon.getAttribute('aria-hidden')).toBe('true');
        expect(icon.textContent.trim()).toBe('');

        toggle.click();
        fixture.detectChanges();
        expect(toggle.getAttribute('aria-label')).toBe('common.hidePassword');
    });

    it('should link the password error and focus the password field on failure (RGAA 11.10)', () => {
        spyOn(TestBed.inject(ProjektService), 'createProjectKey').and.returnValue(throwError(() => new Error('401')));
        component.project = {uuid: 'project-uuid'} as Project;

        component.confirmForm1();

        const password: HTMLInputElement = fixture.nativeElement.querySelector('#generate-key-password');
        expect(fixture.nativeElement.querySelector('#generate-key-password-error')).toBeTruthy();
        expect(password.getAttribute('aria-describedby')).toContain('generate-key-password-error');
        expect(document.activeElement).toBe(password);
    });

    it('should move focus to the generated keys step on success', () => {
        const projectKey = {name: 'key', client_id: 'id', client_secret: 'secret'} as ProjectKey;
        spyOn(TestBed.inject(ProjektService), 'createProjectKey').and.returnValue(of(projectKey) as never);
        component.project = {uuid: 'project-uuid'} as Project;

        component.confirmForm1();

        expect(document.activeElement).toBe(fixture.nativeElement.querySelector('#generate-key-result'));
    });

    it('should open the documentation links of the keys step in a new window and say so (RGAA 13.2)', () => {
        const projectKey = {name: 'key', client_id: 'id', client_secret: 'secret'} as ProjectKey;
        spyOn(TestBed.inject(ProjektService), 'createProjectKey').and.returnValue(of(projectKey) as never);
        component.project = {uuid: 'project-uuid'} as Project;

        component.confirmForm1();

        const links = Array.from(fixture.nativeElement.querySelectorAll('.consumer-field-wrapper a') as NodeListOf<HTMLAnchorElement>);
        expect(links.length).toBe(2);
        links.forEach(link => {
            expect(link.target).toBe('_blank');
            expect(link.rel).toBe('noopener noreferrer');
            expect(link.title).toBe('personalSpace.projectApi.clickHere footer.newWindow');
            expect(link.querySelector('.visually-hidden').textContent.trim()).toBe('footer.newWindow');
            expect(link.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
        });
    });

    it('should tell that the password is required once the field is left empty (RGAA 11.10)', () => {
        const password: HTMLInputElement = fixture.nativeElement.querySelector('#generate-key-password');
        expect(password.required).toBeTrue();

        component.generateKeyForm1.get('password').markAsTouched();
        fixture.detectChanges();

        const error: HTMLElement = fixture.nativeElement.querySelector('mat-form-field mat-error');
        expect(error.textContent.trim()).toBe('personalSpace.projectApi.generateKeyDialog.form.passwordRequiredError');
        expect(password.getAttribute('aria-describedby')).toContain(error.id);
    });

    it('should not offer the calendar button for the read-only expiration date', () => {
        expect(fixture.nativeElement.querySelector('mat-datepicker-toggle')).toBeNull();
        expect(fixture.nativeElement.querySelector('#datepicker-expirationDate').getAttribute('aria-label'))
            .toBe('personalSpace.projectApi.generateKeyDialog.form.expirationDateLabel');
    });
});
