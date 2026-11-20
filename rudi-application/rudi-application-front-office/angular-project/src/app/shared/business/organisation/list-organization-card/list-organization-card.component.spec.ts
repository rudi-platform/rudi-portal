import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {OrganizationBean} from 'micro_service_modules/strukture/strukture-model';
import {PaginationControlsComponent} from 'ngx-pagination';
import {ListOrganizationCardComponent} from './list-organization-card.component';

describe('ListOrganizationCardComponent', () => {
    let component: ListOrganizationCardComponent;
    let fixture: ComponentFixture<ListOrganizationCardComponent>;
    const mediaSize = {isDeviceMobile: false} as MediaSize;

    beforeEach(async () => {
        mediaSize.isDeviceMobile = false;
        await TestBed.configureTestingModule({
            imports: [ListOrganizationCardComponent],
            providers: [
                URIComponentCodec,
                {
                    provide: BreakpointObserverService,
                    useValue: {getMediaSize: () => mediaSize, getNgClassFromMediaSize: () => ({})}
                }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ListOrganizationCardComponent);
        component = fixture.componentInstance;
        component.organizations = [
            {uuid: 'a', name: 'Org A'} as OrganizationBean,
            {uuid: 'b', name: 'Org B'} as OrganizationBean
        ];
        component.itemsPerPage = 1;
        component.totalItems = 30;
        fixture.detectChanges();
    });

    it('should render the organizations as a list (RGAA 9.3)', () => {
        const items = fixture.nativeElement.querySelectorAll('ul.card-container > li.card-organization');

        expect(items.length).toBe(2);
        expect(items[0].querySelector('app-organization-card')).not.toBeNull();
    });

    it('should render level 2 card titles by default and level 3 when requested (RGAA 9.1)', () => {
        const element: HTMLElement = fixture.nativeElement;
        expect(element.querySelectorAll('h2.resource-title').length).toBe(2);

        fixture.componentRef.setInput('cardTitleLevel', 3);
        fixture.detectChanges();

        expect(element.querySelectorAll('h3.resource-title').length).toBe(2);
        expect(element.querySelector('h2')).toBeNull();
    });

    it('should emit the page and scroll to the top after a page change, like the catalogue', () => {
        const emitted: number[] = [];
        component.pageChangeEvent.subscribe((page: number) => emitted.push(page));
        const scroll = spyOn(globalThis, 'scroll');

        component.onPageChange(2);

        expect(emitted).toEqual([2]);
        expect(scroll).toHaveBeenCalledWith(0, 0);
    });

    it('should limit the number of page links on desktop (RGAA 10.11)', () => {
        const pagination = fixture.debugElement.query(By.directive(PaginationControlsComponent)).componentInstance;

        expect(pagination.maxSize).toBe(component.maxPageDesktop);
    });

    it('should limit the number of page links on mobile (RGAA 10.11)', () => {
        mediaSize.isDeviceMobile = true;
        fixture.detectChanges();
        const pagination = fixture.debugElement.query(By.directive(PaginationControlsComponent)).componentInstance;

        expect(pagination.maxSize).toBe(component.maxPageMobile);
    });
});
