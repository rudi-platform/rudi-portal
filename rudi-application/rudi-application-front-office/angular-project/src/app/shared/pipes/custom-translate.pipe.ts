import {ChangeDetectorRef, Pipe, PipeTransform} from '@angular/core';
import {LogService} from '@core/services/log.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {GetBackendPropertyPipe} from '@shared/pipes/get-backend-property.pipe';

@Pipe({
    name: 'translate',
    pure: false
})
export class CustomTranslatePipe extends TranslatePipe implements PipeTransform {

    private teamName: string;
    private projectName: string;

    constructor(
        translateService: TranslateService,
        _ref: ChangeDetectorRef,
        private readonly getBackendPropertyPipe: GetBackendPropertyPipe,
        private readonly logger: LogService
    ) {
        super(translateService, _ref);
        this.getBackendPropertyPipe.transform('front.teamName').subscribe({
            next: teamName => this.teamName = teamName,
            error: err => {
                this.logger.error(err);
                this.teamName = "Rudi";
            }
        })
        this.getBackendPropertyPipe.transform('front.projectName').subscribe({
            next: projectName => this.projectName = projectName,
            error: err => {
                this.logger.error(err);
                this.projectName = "Rudi";
            }
        })
    }

    transform(value: any, ...args: any[]): any {
        return super.transform(value, {
            ...args,
            teamName: this.teamName,
            projectName: this.projectName
        });
    }
}
