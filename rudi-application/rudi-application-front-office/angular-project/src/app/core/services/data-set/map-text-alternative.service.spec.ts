import {TestBed} from '@angular/core/testing';
import {MapTextAlternativeService} from '@core/services/data-set/map-text-alternative.service';

describe('MapTextAlternativeService', () => {
    let service: MapTextAlternativeService;

    beforeEach(() => {
        service = TestBed.inject(MapTextAlternativeService);
    });

    it('should not report any focus request by default', () => {
        expect(service.consumeFocusRequest()).toBeFalse();
    });

    it('should report a focus request only once', () => {
        service.requestFocus();
        expect(service.consumeFocusRequest()).toBeTrue();
        expect(service.consumeFocusRequest()).toBeFalse();
    });
});
