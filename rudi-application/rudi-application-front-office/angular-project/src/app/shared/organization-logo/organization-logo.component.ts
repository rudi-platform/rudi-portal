import {Component, Input} from '@angular/core';
import {Base64EncodedLogo} from '@core/services/image-logo.service';
import {OrganizationMetierService} from '@core/services/organization/organization-metier.service';
import {ProducersMetierService} from '@core/services/producers-metier.service';
import {ProvidersMetierService} from '@core/services/providers-metier.service';

const PRODUCER = 'producer';
const PROVIDER = 'provider';
export type OrganizationType = 'producer' | 'provider';

@Component({
    selector: 'app-organization-logo',
    templateUrl: './organization-logo.component.html',
    styleUrls: ['./organization-logo.component.scss']
})
export class OrganizationLogoComponent {
    @Input() organizationType: OrganizationType;
    @Input() cssClass: string;
    content: Base64EncodedLogo;
    isLoading = false;

    constructor(
        private readonly producersMetierService: ProducersMetierService,
        private readonly providersMetierService: ProvidersMetierService,
    ) {
    }

    @Input() set organizationId(organizationId: string) {
        const service = this.getService();
        if (service && organizationId) {
            this.isLoading = true;
            service.getLogo(organizationId).subscribe({
                next: logo => {
                    this.content = logo;
                    this.isLoading = false;
                },
                error: () => {
                    this.content = '/assets/images/rudi_picto_profil_user.png';
                    this.isLoading = false;
                }
            });
        }
    }

    get name(): string {
        return `logo.alt.${this.organizationType}`;
    }

    private getService(): OrganizationMetierService | undefined {
        switch (this.organizationType) {
            case PRODUCER:
                return this.producersMetierService;
            case PROVIDER:
                return this.providersMetierService;
            default:
                return undefined;
        }
    }
}
