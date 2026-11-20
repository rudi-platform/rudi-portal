import {FiltersService} from '@core/services/filters.service';
import {ListContainerComponent} from '@shared/business/dataset/filters/list-container/list-container.component';

describe('ListContainerComponent', () => {
    let component: ListContainerComponent;
    const searchFilter = {value: 'eau'};

    beforeEach(() => {
        searchFilter.value = 'eau';
        const filtersService = {searchFilter} as unknown as FiltersService;
        component = new ListContainerComponent(null, null, null, filtersService, null, null, null);
    });

    it('should not expose a new search term before its results are received (RGAA 7.5)', () => {
        component.getMetadataListTotal(449);
        searchFilter.value = 'bus';

        expect(component.resultsSearchTerm).toBe('eau');
        expect(component.metadataListTotal).toBe(449);
    });

    it('should update the search term together with the new total', () => {
        component.getMetadataListTotal(449);
        searchFilter.value = 'bus';
        component.getMetadataListTotal(12);

        expect(component.resultsSearchTerm).toBe('bus');
        expect(component.metadataListTotal).toBe(12);
    });
});
