import {TranslateService} from '@ngx-translate/core';
import {Subject} from 'rxjs';
import {TranslatedMatStepperIntl} from './translated-mat-stepper-intl';

describe('TranslatedMatStepperIntl', () => {
    it('should translate the hidden step labels once the translations are loaded (RGAA 8.7)', () => {
        const translateService = jasmine.createSpyObj<TranslateService>('TranslateService', ['instant']);
        translateService.instant.and.callFake((key: string | string[]) => key);
        const ready$ = new Subject<void>();
        const intl = new TranslatedMatStepperIntl(translateService, ready$);
        const changes = jasmine.createSpy('changes');
        intl.changes.subscribe(changes);
        expect(intl.completedLabel).toBe('Completed');

        ready$.next();

        expect(intl.completedLabel).toBe('stepper.completedLabel');
        expect(intl.optionalLabel).toBe('stepper.optionalLabel');
        expect(intl.editableLabel).toBe('stepper.editableLabel');
        expect(changes).toHaveBeenCalledTimes(1);
    });
});
