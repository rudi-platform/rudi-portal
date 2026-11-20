import {ComponentFixture, TestBed} from '@angular/core/testing';
import {KonsultMetierService} from '@core/services/konsult-metier.service';
import {of} from 'rxjs';

import {DatasetListComponent} from '@shared/business/dataset/common/dataset-list/dataset-list.component';

describe('DatasetListComponent', () => {
    let component: DatasetListComponent;
    let fixture: ComponentFixture<DatasetListComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [DatasetListComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DatasetListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should emit pageLoaded only after a page change (RGAA 12.8)', () => {
        const konsult = TestBed.inject(KonsultMetierService);
        spyOn(konsult, 'searchMetadatas').and.returnValue(of({total: 40, items: []}));
        spyOn(window, 'scroll');
        const emitted = jasmine.createSpy('pageLoaded');
        component.pageLoaded.subscribe(emitted);

        component.searchMetadatas();
        expect(emitted).not.toHaveBeenCalled();

        component.handlePageChange(2);
        component.searchMetadatas();
        expect(emitted).toHaveBeenCalledTimes(1);

        component.searchMetadatas();
        expect(emitted).toHaveBeenCalledTimes(1);
    });
});
