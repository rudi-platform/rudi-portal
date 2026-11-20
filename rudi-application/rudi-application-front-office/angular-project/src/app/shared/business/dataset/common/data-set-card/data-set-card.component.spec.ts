import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {ThemeCacheService} from '@core/services/theme-cache.service';
import {provideTranslateService} from '@ngx-translate/core';
import {DataSetCardComponent} from '@shared/business/dataset/common/data-set-card/data-set-card.component';
import {OrganizationLogoComponent} from '@shared/business/organisation/organization-logo/organization-logo.component';
import {Metadata} from 'micro_service_modules/api-kaccess';

describe('DataSetCardComponent', () => {
    let component: DataSetCardComponent;
    let fixture: ComponentFixture<DataSetCardComponent>;

    const metadata = {
        global_id: 'uuid-1',
        resource_title: 'Qualité de l\'air',
        theme: 'environment',
        keywords: ['air'],
        synopsis: [],
        producer: {organization_id: 'org-1', organization_name: 'Rennes Métropole'},
        access_condition: {confidentiality: {restricted_access: false, gdpr_sensitive: false}},
    } as unknown as Metadata;

    beforeEach(async () => {
        const themeCacheService = jasmine.createSpyObj<ThemeCacheService>('ThemeCacheService', ['init', 'getThemeLabelFor']);
        themeCacheService.getThemeLabelFor.and.returnValue('Environnement');

        await TestBed.configureTestingModule({
            imports: [DataSetCardComponent],
            providers: [
                URIComponentCodec,
                provideHttpClient(),
                provideHttpClientTesting(),
                provideRouter([]),
                provideTranslateService(),
                {provide: ThemeCacheService, useValue: themeCacheService}
            ]
        })
            .overrideComponent(DataSetCardComponent, {
                remove: {imports: [OrganizationLogoComponent]},
                add: {schemas: [CUSTOM_ELEMENTS_SCHEMA]}
            })
            .compileComponents();

        fixture = TestBed.createComponent(DataSetCardComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('metadata', metadata);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should give a text alternative to the theme picto (RGAA 1.1)', () => {
        const theme: HTMLElement = fixture.nativeElement.querySelector('.rudi-btn-theme');

        expect(theme.querySelector('mat-icon')?.getAttribute('aria-hidden')).toBe('true');
        expect(theme.querySelector('.visually-hidden')?.textContent?.trim()).toBe('metaData.thematiqueLibelle');
    });

    it('should render theme and keywords as non-interactive labels (RGAA 7.1)', () => {
        const keywordsRow: HTMLElement = fixture.nativeElement.querySelector('.keywords-row');

        expect(keywordsRow.querySelectorAll('button').length).toBe(0);
        expect(keywordsRow.querySelectorAll('[tabindex]').length).toBe(0);
        expect(keywordsRow.textContent).toContain('air');
    });

    it('should announce the keywords list to screen readers', () => {
        const firstKeyword: HTMLElement = fixture.nativeElement.querySelectorAll('.keywords-row > li')[1];

        expect(firstKeyword.querySelector('.visually-hidden')?.textContent?.trim()).toBe('metaData.motsClesLibelle');
        expect(firstKeyword.textContent).toContain('air');
    });

    it('should not announce a keywords label when there is no keyword', () => {
        component.metadata = {...metadata, keywords: []} as unknown as Metadata;
        fixture.detectChanges();

        const hiddenTexts = Array.from<HTMLElement>(fixture.nativeElement.querySelectorAll('.keywords-row .visually-hidden'));
        expect(hiddenTexts.map(el => el.textContent?.trim())).not.toContain('metaData.motsClesLibelle');
    });

    it('should structure theme and keywords as a list (RGAA 9.3)', () => {
        const keywordsRow: HTMLElement = fixture.nativeElement.querySelector('.keywords-row');

        expect(keywordsRow.tagName).toBe('UL');
        expect(keywordsRow.querySelectorAll(':scope > li').length).toBe(2);
        expect(keywordsRow.querySelectorAll(':scope > :not(li)').length).toBe(0);
    });

    it('should expose the dataset title as a heading (RGAA 9.1)', () => {
        expect(fixture.nativeElement.querySelector('h2.data-set-card-title')).not.toBeNull();
    });

    it('should expose the dataset title as a level 3 heading under a section heading (RGAA 9.1)', () => {
        fixture.componentRef.setInput('titleLevel', 3);
        fixture.detectChanges();

        const heading: HTMLElement = fixture.nativeElement.querySelector('h3.data-set-card-title');
        expect(heading?.textContent).toContain('Qualité de l\'air');
        expect(fixture.nativeElement.querySelector('h2')).toBeNull();
    });

    it('should expose keywords hidden behind the tooltip to screen readers (RGAA 10.13)', () => {
        component.metadata = {...metadata, keywords: ['air', 'pollution', 'climat']} as Metadata;
        fixture.detectChanges();

        const hiddenTexts: NodeListOf<HTMLElement> = fixture.nativeElement.querySelectorAll('.keywords-row .visually-hidden');
        const hiddenKeywords = hiddenTexts[hiddenTexts.length - 1];

        expect(hiddenKeywords?.textContent?.trim()).toBe(': pollution, climat');
    });

    describe('access status (RGAA 1.1 / 6.1)', () => {
        const withConfidentiality = (restricted_access: boolean, gdpr_sensitive: boolean): Metadata =>
            ({...metadata, access_condition: {confidentiality: {restricted_access, gdpr_sensitive}}}) as unknown as Metadata;

        it('should give a text alternative to the restricted access icon', () => {
            fixture.componentRef.setInput('metadata', withConfidentiality(true, false));
            fixture.detectChanges();

            const icon: HTMLElement = fixture.nativeElement.querySelector('h2 .key-icon');
            expect(icon.getAttribute('role')).toBe('img');
            expect(icon.getAttribute('aria-hidden')).toBe('false');
            expect(icon.getAttribute('aria-label')).toBe('metaData.accesRestreintLibelle');
            expect(icon.getAttribute('title')).toBe('metaData.accesRestreintLibelle');
            expect(fixture.nativeElement.querySelector('h2 .self-data-icon')).toBeNull();
        });

        it('should give a text alternative to the personal data icon', () => {
            fixture.componentRef.setInput('metadata', withConfidentiality(true, true));
            fixture.detectChanges();

            const icon: HTMLElement = fixture.nativeElement.querySelector('h2 .self-data-icon');
            expect(icon.getAttribute('role')).toBe('img');
            expect(icon.getAttribute('aria-hidden')).toBe('false');
            expect(icon.getAttribute('aria-label')).toBe('metaData.donneesPersonnellesLibelle');
            expect(icon.getAttribute('title')).toBe('metaData.donneesPersonnellesLibelle');
            expect(fixture.nativeElement.querySelector('h2 .key-icon')).toBeNull();
        });

        it('should keep the corner triangle decorative', () => {
            fixture.componentRef.setInput('metadata', withConfidentiality(true, false));
            fixture.detectChanges();

            const triangle: HTMLElement = fixture.nativeElement.querySelector('.triangle-restricted');
            expect(triangle.getAttribute('aria-hidden')).toBe('true');
        });

        it('should include the access status in the link name, after the visible title (WCAG 2.5.3)', () => {
            fixture.componentRef.setInput('metadata', withConfidentiality(true, false));
            fixture.detectChanges();

            const link: HTMLElement = fixture.nativeElement.querySelector('a.stretched-link');
            expect(link.getAttribute('aria-label')).toBe('Qualité de l\'air (metaData.accesRestreintLibelle)');
        });

        it('should include the access status in the name of a selectable card', () => {
            fixture.componentRef.setInput('isSelectable', true);
            fixture.componentRef.setInput('metadata', withConfidentiality(true, true));
            fixture.detectChanges();

            const button: HTMLElement = fixture.nativeElement.querySelector('button.stretched-link');
            expect(button.getAttribute('aria-label')).toBe('Qualité de l\'air (metaData.donneesPersonnellesLibelle)');
        });

        it('should keep the title as the only link name for open data', () => {
            const link: HTMLElement = fixture.nativeElement.querySelector('a.stretched-link');

            expect(link.getAttribute('aria-label')).toBe('Qualité de l\'air');
            expect(fixture.nativeElement.querySelector('h2 mat-icon')).toBeNull();
        });
    });

    describe('selectable card (RGAA 7.1 / 7.3)', () => {
        let card: HTMLElement;
        let button: HTMLButtonElement;
        let selected: jasmine.Spy;

        beforeEach(() => {
            fixture.componentRef.setInput('isSelectable', true);
            fixture.detectChanges();
            card = fixture.nativeElement.querySelector('mat-card');
            button = card.querySelector('button.stretched-link');
            selected = jasmine.createSpy('selectMetadata');
            component.selectMetadata.subscribe(selected);
        });

        it('should expose a native button named by the title only, not a role="button" card (WCAG 2.5.3)', () => {
            expect(button.type).toBe('button');
            expect(button.getAttribute('aria-label')).toBe('Qualité de l\'air');
            expect(button.textContent.trim()).toBe('');
            expect(card.hasAttribute('role')).toBeFalse();
            expect(card.hasAttribute('tabindex')).toBeFalse();
            expect(card.querySelector('a.stretched-link')).toBeNull();
        });

        it('should not select the card when it only receives the focus', () => {
            button.focus();
            fixture.detectChanges();

            expect(selected).not.toHaveBeenCalled();
            expect(button.getAttribute('aria-pressed')).toBe('false');
        });

        it('should select the card at once on a keyboard click (Enter / Space on the button) and expose its state', () => {
            button.dispatchEvent(new MouseEvent('click', {bubbles: true, detail: 0}));
            fixture.detectChanges();

            expect(selected).toHaveBeenCalledOnceWith(metadata);
            expect(button.getAttribute('aria-pressed')).toBe('true');
            expect(card.classList).toContain('selected');
        });

        it('should wait for a possible double click on a mouse click', fakeAsync(() => {
            card.dispatchEvent(new MouseEvent('click', {bubbles: true, detail: 1}));
            expect(selected).not.toHaveBeenCalled();

            tick(250);
            expect(selected).toHaveBeenCalledOnceWith(metadata);
        }));

        it('should keep the state given by the list after the focus leaves the card', () => {
            fixture.componentRef.setInput('isSelected', true);
            fixture.detectChanges();
            button.dispatchEvent(new FocusEvent('focusout', {bubbles: true}));
            fixture.detectChanges();

            expect(button.getAttribute('aria-pressed')).toBe('true');
            expect(card.classList).toContain('selected');
        });

        it('should not expose a pressed state on a navigation card', () => {
            fixture.componentRef.setInput('isSelectable', false);
            fixture.detectChanges();

            expect(card.querySelector('[aria-pressed]')).toBeNull();
            expect(card.classList).not.toContain('selected');
        });
    });
});
