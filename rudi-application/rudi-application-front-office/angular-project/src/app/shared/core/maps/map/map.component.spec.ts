import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateService} from '@ngx-translate/core';
import {MapComponent} from '@shared/core/maps/map/map.component';
import {Metadata} from 'micro_service_modules/api-kaccess';

describe('MapComponent', () => {
    let component: MapComponent;
    let fixture: ComponentFixture<MapComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [MapComponent]
        }).compileComponents();

        spyOn(TestBed.inject(TranslateService), 'instant').and.callFake(
            (key: string | string[], params?: { title?: string }) => params?.title ? `${key}:${params.title}` : `${key}`
        );

        fixture = TestBed.createComponent(MapComponent);
        component = fixture.componentInstance;
        component.mapId = 'test-map';
    });

    afterEach(() => {
        component.map?.setTarget(undefined);
    });

    describe('map rendering label (RGAA 1.1, 4.13)', () => {
        it('should expose the layers rendering as a named image without hiding the map controls', () => {
            fixture.detectChanges();

            const layersElement: HTMLElement = fixture.nativeElement.querySelector('.ol-layers');
            expect(layersElement.getAttribute('role')).toBe('img');
            expect(layersElement.getAttribute('aria-label')).toBe('metaData.mapDataTab.locationMapLabel');
            expect(layersElement.querySelector('.ol-attribution')).toBeNull();
            expect(fixture.nativeElement.querySelector('.ol-attribution')).not.toBeNull();
        });

        it('should name the image after the dataset when metadata is provided', () => {
            component.metadata = {resource_title: 'Air quality'} as Metadata;
            fixture.detectChanges();

            const layersElement: HTMLElement = fixture.nativeElement.querySelector('.ol-layers');
            expect(layersElement.getAttribute('aria-label')).toBe('metaData.mapDataTab.dataMapLabel:Air quality');
        });
    });
});
