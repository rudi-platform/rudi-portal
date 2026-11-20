import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MapTextAlternativeService} from '@core/services/data-set/map-text-alternative.service';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';

import {DetailComponent} from './detail.component';

describe('MetaDataComponent', () => {
    let component: DetailComponent;
    let fixture: ComponentFixture<DetailComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [DetailComponent]
        })
            .compileComponents();
    });

    beforeEach(async () => {
        fixture = TestBed.createComponent(DetailComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should select the Tableau tab and focus it when using the map alternative (RGAA 4.8)', () => {
        const tabButton = document.createElement('button');
        tabButton.id = 'spreadsheet-tab-test-id';
        document.body.appendChild(tabButton);
        const spreadsheetTab = {} as TabComponent;
        const detailTabs = jasmine.createSpyObj<TabsComponent>('TabsComponent', ['selectTab', 'getSelectedTabId']);
        detailTabs.getSelectedTabId.and.returnValue(tabButton.id);
        component.detailTabs = detailTabs;
        component.spreadsheetTab = spreadsheetTab;

        component.showSpreadsheetTab();

        expect(detailTabs.selectTab).toHaveBeenCalledOnceWith(spreadsheetTab);
        expect(document.activeElement).toBe(tabButton);
        tabButton.remove();
    });

    it('should request the focus of the map text alternative before selecting the Informations tab (RGAA 4.8)', () => {
        const mapTextAlternativeService = TestBed.inject(MapTextAlternativeService);
        const informationsTab = {} as TabComponent;
        const detailTabs = jasmine.createSpyObj<TabsComponent>('TabsComponent', ['selectTab']);
        detailTabs.selectTab.and.callFake(() => expect(mapTextAlternativeService.consumeFocusRequest()).toBeTrue());
        component.detailTabs = detailTabs;
        component.informationsTab = informationsTab;

        component.showMapTextAlternative();

        expect(detailTabs.selectTab).toHaveBeenCalledOnceWith(informationsTab);
    });
});
