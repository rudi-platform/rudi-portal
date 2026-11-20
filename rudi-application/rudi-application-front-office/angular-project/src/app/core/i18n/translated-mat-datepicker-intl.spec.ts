import {TranslateService} from '@ngx-translate/core';
import {Subject} from 'rxjs';
import {TranslatedMatDatepickerIntl} from './translated-mat-datepicker-intl';

describe('TranslatedMatDatepickerIntl', () => {
    let translateService: jasmine.SpyObj<TranslateService>;
    let ready$: Subject<void>;
    let intl: TranslatedMatDatepickerIntl;

    beforeEach(() => {
        translateService = jasmine.createSpyObj('TranslateService', ['instant']);
        translateService.instant.and.callFake((key: string, params?: Record<string, string>) =>
            params ? `${key}(${params.start},${params.end})` : key);
        ready$ = new Subject<void>();
        intl = new TranslatedMatDatepickerIntl(translateService, ready$);
    });

    it('should translate the labels once the translations are loaded (RGAA 8.7)', () => {
        const changes = jasmine.createSpy('changes');
        intl.changes.subscribe(changes);
        expect(intl.openCalendarLabel).toBe('Open calendar');

        ready$.next();

        expect(intl.openCalendarLabel).toBe('datepicker.openCalendarLabel');
        expect(intl.closeCalendarLabel).toBe('datepicker.closeCalendarLabel');
        expect(intl.switchToMultiYearViewLabel).toBe('datepicker.switchToMultiYearViewLabel');
        expect(intl.nextMultiYearLabel).toBe('datepicker.nextMultiYearLabel');
        expect(changes).toHaveBeenCalledTimes(1);
    });

    it('should translate the year range label', () => {
        expect(intl.formatYearRangeLabel('2000', '2023')).toBe('datepicker.yearRangeLabel(2000,2023)');
    });
});
