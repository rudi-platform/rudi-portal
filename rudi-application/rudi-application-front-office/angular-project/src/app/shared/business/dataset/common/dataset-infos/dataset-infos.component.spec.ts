import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {provideTranslateService} from '@ngx-translate/core';
import {DatasetsInfosComponent} from '@shared/business/dataset/common/dataset-infos/dataset-infos.component';

describe('DatasetsInfosComponent', () => {
    let fixture: ComponentFixture<DatasetsInfosComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [DatasetsInfosComponent],
            providers: [
                URIComponentCodec,
                provideHttpClient(),
                provideHttpClientTesting(),
                provideRouter([]),
                provideTranslateService()
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(DatasetsInfosComponent);
        fixture.componentRef.setInput('resourceTitle', 'Qualité de l\'air');
        fixture.componentRef.setInput('currentJddId', 'uuid-1');
    });

    it('should give a text alternative to the restricted access icon (RGAA 1.1)', () => {
        fixture.componentRef.setInput('isRestricted', true);
        fixture.detectChanges();

        const element: HTMLElement = fixture.nativeElement;
        const icon = element.querySelector('mat-icon');

        expect(icon?.getAttribute('role')).toBe('img');
        expect(icon?.getAttribute('aria-hidden')).toBe('false');
        expect(icon?.getAttribute('aria-label')).toBe('conditionsUtilisationBox.donneesAccesRestreint');
        expect(icon?.getAttribute('title')).toBe('conditionsUtilisationBox.donneesAccesRestreint');
        expect(element.querySelector('.dataset-info-resource_title .visually-hidden')).toBeNull();
    });

    it('should not announce restricted access for an open dataset', () => {
        fixture.componentRef.setInput('isRestricted', false);
        fixture.detectChanges();

        const element: HTMLElement = fixture.nativeElement;

        expect(element.querySelector('mat-icon')).toBeNull();
        expect(element.querySelector('.dataset-info-resource_title .visually-hidden')).toBeNull();
    });
});
