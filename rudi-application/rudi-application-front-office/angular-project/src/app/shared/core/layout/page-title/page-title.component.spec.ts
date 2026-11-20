import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PageTitleComponent} from '@shared/core/layout/page-title/page-title.component';

describe('PageTitleComponent', () => {
    let component: PageTitleComponent;
    let fixture: ComponentFixture<PageTitleComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [PageTitleComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PageTitleComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should keep the visible link text in the accessible name (RGAA 6.1, WCAG 2.5.3)', () => {
        fixture.componentRef.setInput('title4', 'Cliquez ici pour en savoir plus.');
        fixture.componentRef.setInput('urlToDoc', 'https://doc.example.org');
        fixture.detectChanges();
        const link: HTMLAnchorElement = fixture.nativeElement.querySelector('a.nav-link');

        expect(link.hasAttribute('aria-label')).toBeFalse();
        expect(link.textContent).toContain('Cliquez ici pour en savoir plus.');
        expect(link.querySelector('.visually-hidden')).not.toBeNull();
        expect(link.getAttribute('rel')).toBe('noopener noreferrer');
    });
});
