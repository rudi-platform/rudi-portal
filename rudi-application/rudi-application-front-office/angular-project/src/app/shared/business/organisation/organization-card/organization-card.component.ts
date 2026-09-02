import {Component, Input} from '@angular/core';
import {RouterLink} from '@angular/router';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {OrganizationBean} from 'micro_service_modules/strukture/api-strukture';
import {OrganizationStatus} from 'micro_service_modules/strukture/strukture-model';
import {MatCard, MatCardContent} from '@angular/material/card';
import { NgClass } from '@angular/common';
import {OrganizationLogoComponent} from '../organization-logo/organization-logo.component';
import {MatButton} from '@angular/material/button';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {TranslatePipe} from '@ngx-translate/core';
import {TruncateTextPipe} from '@shared/utils/pipes/truncate-text.pipe';

@Component({
    selector: 'app-organization-card',
    templateUrl: './organization-card.component.html',
    styleUrls: ['./organization-card.component.scss'],
    imports: [MatCard, NgClass, MatCardContent, OrganizationLogoComponent, MatButton, LoaderComponent, TranslatePipe, TruncateTextPipe, RouterLink]
})
export class OrganizationCardComponent {
    @Input() organizationBean: OrganizationBean;
    @Input() datasetCountLoading: boolean;
    @Input() projectCountLoading: boolean;

    constructor(private readonly uriComponentCodec: URIComponentCodec) {
    }

    /**
     * Commandes de route vers le detail d'une organisation, utilisées par [routerLink] (balise <a>)
     * afin de bénéficier du comportement natif du navigateur (clic droit "ouvrir dans un nouvel
     * onglet", ctrl/cmd+clic, clic molette).
     */
    get detailUrl(): string[] {
        return ['/organization/detail', this.organizationBean.uuid, this.uriComponentCodec.normalizeString(this.organizationBean.name)];
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
