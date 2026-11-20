import {Provider} from '@angular/core';
import {MatStepperIntl} from '@angular/material/stepper';
import {CustomTranslateService} from '@core/services/translate.service';
import {TranslateService} from '@ngx-translate/core';
import {Observable} from 'rxjs';
import {TRANSLATE_SERVICE_IS_READY$} from 'src/app/app-initializer-factory';

// Material only ships English hidden labels ("Completed"...): they must follow the page language (RGAA 8.7)
export class TranslatedMatStepperIntl extends MatStepperIntl {

    constructor(
        private readonly translateService: TranslateService,
        translateServiceIsReady$: Observable<void> = TRANSLATE_SERVICE_IS_READY$
    ) {
        super();
        translateServiceIsReady$.subscribe(() => {
            this.optionalLabel = this.translateService.instant('stepper.optionalLabel');
            this.completedLabel = this.translateService.instant('stepper.completedLabel');
            this.editableLabel = this.translateService.instant('stepper.editableLabel');
            this.changes.next();
        });
    }
}

export const TRANSLATED_MAT_STEPPER_INTL_PROVIDER: Provider = {
    provide: MatStepperIntl,
    deps: [CustomTranslateService],
    useFactory: (translateService: CustomTranslateService) => new TranslatedMatStepperIntl(translateService)
};
