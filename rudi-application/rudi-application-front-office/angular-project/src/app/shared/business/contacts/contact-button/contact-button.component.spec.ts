import {LiveAnnouncer} from '@angular/cdk/a11y';
import {Clipboard} from '@angular/cdk/clipboard';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideTranslateService, TranslateService} from '@ngx-translate/core';

import {ContactButtonComponent} from '@shared/business/contacts/contact-button/contact-button.component';

describe('ContactButtonComponent', () => {
    let component: ContactButtonComponent;
    let fixture: ComponentFixture<ContactButtonComponent>;
    let liveAnnouncer: jasmine.SpyObj<LiveAnnouncer>;
    let clipboard: jasmine.SpyObj<Clipboard>;

    beforeEach(async () => {
        liveAnnouncer = jasmine.createSpyObj('LiveAnnouncer', ['announce']);
        clipboard = jasmine.createSpyObj('Clipboard', ['copy']);
        await TestBed.configureTestingModule({
            imports: [ContactButtonComponent],
            providers: [
                provideTranslateService(),
                {provide: LiveAnnouncer, useValue: liveAnnouncer},
                {provide: Clipboard, useValue: clipboard}
            ]
        })
            .compileComponents();
        TestBed.inject(TranslateService).setTranslation('fr', {
            metaData: {contact: 'Contacter'},
            common: {copiedButton: {copy: 'Copier', copied: 'Copié dans le presse-papier'}}
        });
        TestBed.inject(TranslateService).use('fr');
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ContactButtonComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('email', 'contact@example.org');
        document.body.appendChild(fixture.nativeElement);
        fixture.detectChanges();
    });

    afterEach(() => fixture.nativeElement.remove());

    function button(): HTMLButtonElement {
        return fixture.nativeElement.querySelector('button');
    }

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should not submit a surrounding form', () => {
        expect(button().type).toBe('button');
        button().click();
        fixture.detectChanges();
        expect(button().type).toBe('button');
    });

    it('should move the focus to the email button that replaces the contact button (RGAA 12.8)', () => {
        button().focus();
        button().click();
        fixture.detectChanges();
        expect(button().classList).toContain('email');
        expect(document.activeElement).toBe(button());
    });

    it('should name the email button by its copy action and hide the icon (RGAA 7.1)', () => {
        button().click();
        fixture.detectChanges();
        expect(button().querySelector('.visually-hidden').textContent).toBe('Copier');
        expect(button().textContent).toContain('contact@example.org');
        expect(button().querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
        expect(button().querySelector('mat-icon').textContent.trim()).toBe('');
    });

    it('should copy the email and announce it (RGAA 7.5)', () => {
        button().click();
        fixture.detectChanges();
        button().click();
        expect(clipboard.copy).toHaveBeenCalledWith('contact@example.org');
        expect(liveAnnouncer.announce).toHaveBeenCalledWith('Copié dans le presse-papier');
    });

    it('should not steal the focus when the email is displayed directly', () => {
        fixture.componentRef.setInput('showEmailDirectly', true);
        fixture.detectChanges();
        expect(button().classList).toContain('email');
        expect(document.activeElement).not.toBe(button());
    });
});
