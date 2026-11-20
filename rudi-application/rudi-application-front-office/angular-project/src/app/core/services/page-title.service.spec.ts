import {TestBed} from '@angular/core/testing';
import {Title} from '@angular/platform-browser';
import {LogService} from '@core/services/log.service';
import {PageTitleService} from '@core/services/page-title.service';
import {TranslateService} from '@ngx-translate/core';
import {GetBackendPropertyPipe} from '@shared/utils/pipes/get-backend-property.pipe';
import {of} from 'rxjs';

describe('PageTitleService', () => {
    let service: PageTitleService;
    let title: Title;
    const translations: Record<string, string> = {'pageTitle./catalogue': 'Catalogue'};

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                {provide: GetBackendPropertyPipe, useValue: {transform: () => of('Rudi')}},
                {provide: TranslateService, useValue: {get: (key: string) => of(translations[key] ?? key)}},
                {provide: LogService, useValue: jasmine.createSpyObj('LogService', ['error'])},
            ],
        });
        service = TestBed.inject(PageTitleService);
        title = TestBed.inject(Title);
    });

    it('should set the route title (RGAA 8.6)', () => {
        service.setPageTitleFromUrl('/catalogue');
        expect(title.getTitle()).toBe('Catalogue - Rudi');
    });

    it('should ignore query params and fragment when resolving the route title (RGAA 8.6)', () => {
        service.setPageTitleFromUrl('/catalogue?themes=economy#main-content');
        expect(title.getTitle()).toBe('Catalogue - Rudi');
    });

    it('should use the first non empty title', () => {
        service.setPageTitle('', 'Recherche « eau » - Catalogue');
        expect(title.getTitle()).toBe('Recherche « eau » - Catalogue - Rudi');
    });
});
