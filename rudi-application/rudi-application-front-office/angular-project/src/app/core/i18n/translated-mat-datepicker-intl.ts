import {Provider} from '@angular/core';
import {MatDatepickerIntl} from '@angular/material/datepicker';
import {CustomTranslateService} from '@core/services/translate.service';
import {TranslateService} from '@ngx-translate/core';
import {Observable} from 'rxjs';
import {TRANSLATE_SERVICE_IS_READY$} from 'src/app/app-initializer-factory';

// Material only ships English labels ("Open calendar"...): they must follow the page language (RGAA 8.7)
export class TranslatedMatDatepickerIntl extends MatDatepickerIntl {

    constructor(
        private readonly translateService: TranslateService,
        translateServiceIsReady$: Observable<void> = TRANSLATE_SERVICE_IS_READY$
    ) {
        super();
        translateServiceIsReady$.subscribe(() => {
            this.calendarLabel = this.translateService.instant('datepicker.calendarLabel');
            this.openCalendarLabel = this.translateService.instant('datepicker.openCalendarLabel');
            this.closeCalendarLabel = this.translateService.instant('datepicker.closeCalendarLabel');
            this.prevMonthLabel = this.translateService.instant('datepicker.prevMonthLabel');
            this.nextMonthLabel = this.translateService.instant('datepicker.nextMonthLabel');
            this.prevYearLabel = this.translateService.instant('datepicker.prevYearLabel');
            this.nextYearLabel = this.translateService.instant('datepicker.nextYearLabel');
            this.prevMultiYearLabel = this.translateService.instant('datepicker.prevMultiYearLabel');
            this.nextMultiYearLabel = this.translateService.instant('datepicker.nextMultiYearLabel');
            this.switchToMonthViewLabel = this.translateService.instant('datepicker.switchToMonthViewLabel');
            this.switchToMultiYearViewLabel = this.translateService.instant('datepicker.switchToMultiYearViewLabel');
            this.startDateLabel = this.translateService.instant('datepicker.startDateLabel');
            this.endDateLabel = this.translateService.instant('datepicker.endDateLabel');
            this.comparisonDateLabel = this.translateService.instant('datepicker.comparisonDateLabel');
            this.changes.next();
        });
    }

    formatYearRangeLabel(start: string, end: string): string {
        return this.translateService.instant('datepicker.yearRangeLabel', {start, end});
    }
}

// MatDatepickerModule (SharedModule) re-provides the English MatDatepickerIntl: datepicker components must provide this one too
export const TRANSLATED_MAT_DATEPICKER_INTL_PROVIDER: Provider = {
    provide: MatDatepickerIntl,
    deps: [CustomTranslateService],
    useFactory: (translateService: CustomTranslateService) => new TranslatedMatDatepickerIntl(translateService)
};
