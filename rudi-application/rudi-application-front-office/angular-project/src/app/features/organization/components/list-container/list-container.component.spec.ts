import {FiltersService} from '@core/services/filters.service';
import {SearchOrganizationsService} from '@shared/business/organisation/list-organization-card/search-organizations.service';
import {BehaviorSubject} from 'rxjs';
import {ListContainerComponent} from './list-container.component';

describe('ListContainerComponent (organization)', () => {
    let component: ListContainerComponent;
    let isLoading$: BehaviorSubject<boolean>;
    let total$: BehaviorSubject<number>;
    let searchService: jasmine.SpyObj<SearchOrganizationsService>;

    beforeEach(() => {
        isLoading$ = new BehaviorSubject<boolean>(true);
        total$ = new BehaviorSubject<number>(0);
        searchService = jasmine.createSpyObj('SearchOrganizationsService', ['initSubscriptions', 'complete', 'onFreeTextChange'], {
            isLoadingCatalogue$: isLoading$,
            totalOrganizations$: total$,
            organizations$: new BehaviorSubject([]),
            datasetCountLoading$: new BehaviorSubject(false),
            projectsCountLoading$: new BehaviorSubject(false),
            currentPage$: new BehaviorSubject(1),
        });
        const filtersService = {searchFilter: jasmine.createSpyObj('searchFilter', ['clear'])} as unknown as FiltersService;
        component = new ListContainerComponent(searchService, filtersService);
        component.ngOnInit();
    });

    it('should not expose a new search term before its results are received (RGAA 7.5)', () => {
        total$.next(449);
        isLoading$.next(false);

        component.onChangesSearchTerms('eau');
        isLoading$.next(true);

        expect(component.isLoading).toBeTrue();
        expect(component.displayedTotal).toBe(449);
        expect(component.searchedTerm).toBe('');

        total$.next(12);
        isLoading$.next(false);

        expect(component.isLoading).toBeFalse();
        expect(component.displayedTotal).toBe(12);
        expect(component.searchedTerm).toBe('eau');
    });

    it('should move the focus to the results count once a new page is loaded (RGAA 12.8)', () => {
        const output = document.createElement('output');
        output.tabIndex = -1;
        document.body.appendChild(output);
        component.resultsCount = {nativeElement: output};
        isLoading$.next(false);

        expect(document.activeElement).not.toBe(output);

        component.onPageChange(2);
        isLoading$.next(true);
        expect(document.activeElement).not.toBe(output);
        isLoading$.next(false);

        expect(document.activeElement).toBe(output);
        output.remove();
    });

    it('should stop listening to the loading state on destroy', () => {
        component.ngOnDestroy();
        isLoading$.next(false);

        expect(searchService.complete).toHaveBeenCalled();
        expect(component.isLoading).toBeTrue();
    });
});
