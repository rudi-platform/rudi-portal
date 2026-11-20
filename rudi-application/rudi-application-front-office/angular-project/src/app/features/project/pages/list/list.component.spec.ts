import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ProjectCatalogItemPage} from '../../model/project-catalog-item';

import {ListComponent} from './list.component';

describe('ListComponent', () => {
    let component: ListComponent;
    let fixture: ComponentFixture<ListComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ListComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('results count live region', () => {
        const liveRegion = (): HTMLOutputElement => fixture.nativeElement.querySelector('output[aria-live="polite"]');

        beforeEach(() => {
            component.onProjectListChange({total: 5, items: []} as ProjectCatalogItemPage);
            fixture.detectChanges();
        });

        it('should keep the previous count and term while a new search is running', () => {
            component.freeText = 'rennes';
            fixture.detectChanges();

            expect(liveRegion().textContent).toContain('5');
            expect(liveRegion().textContent).not.toContain('rennes');
        });

        it('should update the count and the term together when the results arrive', () => {
            component.freeText = 'rennes';
            component.onProjectListChange({total: 2, items: []} as ProjectCatalogItemPage);
            fixture.detectChanges();

            expect(component.displayedTotal).toBe(2);
            expect(component.resultsForParams).toEqual({term: 'rennes'});
            expect(liveRegion().textContent).toContain('2');
        });

        it('should announce no result', () => {
            component.onProjectListChange({total: 0, items: []} as ProjectCatalogItemPage);
            fixture.detectChanges();

            expect(liveRegion().textContent).toContain('searchbox.noResult');
        });

        it('should move the focus to the results count once a new page is displayed', () => {
            fixture.debugElement.query(By.css('app-project-list')).triggerEventHandler('pageLoaded');

            expect(document.activeElement).toBe(liveRegion());
        });
    });
});
