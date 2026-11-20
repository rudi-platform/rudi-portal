import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SearchCountComponent} from '@shared/core/search/search-count/search-count.component';

describe('SearchCountComponent', () => {
    let component: SearchCountComponent;
    let fixture: ComponentFixture<SearchCountComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SearchCountComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SearchCountComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should render the no result link as a real link (RGAA 7.1)', () => {
        fixture.componentRef.setInput('searchIsRunning', false);
        fixture.componentRef.setInput('count', 0);
        fixture.componentRef.setInput('linkUrl', '/projets');
        fixture.componentRef.setInput('hyperLink', 'ici');
        fixture.detectChanges();

        const link: HTMLAnchorElement = fixture.nativeElement.querySelector('a');
        expect(link.getAttribute('href')).toBe('/projets');
        expect(link.textContent).toBe('ici');
    });

    it('should render the count as plain text even when a link url is given', () => {
        fixture.componentRef.setInput('searchIsRunning', false);
        fixture.componentRef.setInput('count', 3);
        fixture.componentRef.setInput('resultMessage', 'reuses');
        fixture.componentRef.setInput('linkUrl', '/projets');
        fixture.detectChanges();

        expect(fixture.nativeElement.querySelector('a')).toBeNull();
        expect(fixture.nativeElement.textContent.trim()).toBe('3 reuses');
    });

    it('should expose the count as a heading only when a heading level is given (RGAA 9.1)', () => {
        const host: HTMLElement = fixture.nativeElement;
        expect(host.hasAttribute('role')).toBeFalse();
        expect(host.hasAttribute('aria-level')).toBeFalse();

        fixture.componentRef.setInput('headingLevel', 2);
        fixture.detectChanges();

        expect(host.getAttribute('role')).toBe('heading');
        expect(host.getAttribute('aria-level')).toBe('2');
    });
});
