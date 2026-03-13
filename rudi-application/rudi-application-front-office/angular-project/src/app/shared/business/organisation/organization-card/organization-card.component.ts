import {Component, Input} from '@angular/core';
import {Router} from '@angular/router';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {OrganizationBean} from 'micro_service_modules/strukture/api-strukture';
import {OrganizationStatus} from 'micro_service_modules/strukture/strukture-model';
import {MatCard, MatCardContent} from '@angular/material/card';
import { NgClass } from '@angular/common';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {OrganizationLogoComponent} from '../organization-logo/organization-logo.component';
import {MatButton} from '@angular/material/button';
import {LoaderComponent} from '../../../core/common/loader/loader.component';
import {TranslatePipe} from '@ngx-translate/core';
import {TruncateTextPipe} from '@shared/utils/pipes/truncate-text.pipe';

@Component({
    selector: 'app-organization-card',
    templateUrl: './organization-card.component.html',
    styleUrls: ['./organization-card.component.scss'],
    imports: [MatCard, NgClass, ExtendedModule, MatCardContent, OrganizationLogoComponent, MatButton, LoaderComponent, TranslatePipe, TruncateTextPipe]
})
export class OrganizationCardComponent {
    @Input() organizationBean: OrganizationBean;
    @Input() datasetCountLoading: boolean;
    @Input() projectCountLoading: boolean;

    constructor(private readonly router: Router, private readonly uriComponentCodec: URIComponentCodec) {
    }

    onClickOrganization(uuid: string, name: string): Promise<boolean> {
        return this.router.navigate(['/organization/detail/' + uuid + '/' + this.uriComponentCodec.normalizeString(name)]);
    }

    get datasetsCountTranslationKey(): string {
        return `organization.card.datasetsCount.${this.organizationBean.datasetCount > 1 ? 'plural' : 'single'}`;
    }

    get projectsCountTranslationKey(): string {
        return `organization.card.projectsCount.${this.organizationBean.projectCount > 1 ? 'plural' : 'single'}`;
    }

    protected isArchived(): boolean {
        return this.organizationBean!.organizationStatus === OrganizationStatus.Disengaged;
    }
}
