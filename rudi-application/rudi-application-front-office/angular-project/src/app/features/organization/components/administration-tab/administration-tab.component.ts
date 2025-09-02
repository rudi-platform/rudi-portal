import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {FiltersService} from '@core/services/filters.service';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {KonsultMetierService} from '@core/services/konsult-metier.service';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {Organization} from 'micro_service_modules/strukture/strukture-model';

@Component({
    selector: 'app-administration-tab',
    templateUrl: './administration-tab.component.html',
})
export class AdministrationTabComponent implements OnInit {

    @Input() isLoading: boolean;
    @Input() organization: Organization;

    /**
     * Indique si le captcha doit s'activer sur cette page
     */
    enableCaptchaOnPage: boolean = true;
    enableArchive: boolean = false;
    isLoadingDatasets: boolean = false;

    constructor(
        private readonly iconRegistryService: IconRegistryService,
        private readonly route: ActivatedRoute,
        private readonly filterService: FiltersService,
        private readonly konsultMetierService: KonsultMetierService,
    ) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }

    ngOnInit(): void {
        if (this.route.snapshot.data?.aclAppInfo) {
            // Si on a pu recuperer l'info d'activation du captcha sinon il reste à false par défaut
            this.enableCaptchaOnPage = this.route.snapshot.data.aclAppInfo.captchaEnabled;
        } else {
            this.enableCaptchaOnPage = false;
        }

        this.filterService.currentFilters.producerUuids = [this.organization.uuid];
        this.isLoadingDatasets = true;
        this.konsultMetierService.searchMetadatas(this.filterService.currentFilters)
            .subscribe({
                next: (response) => {
                    this.enableArchive = response.total == 0;
                },
                complete: () => {
                    this.isLoadingDatasets = false;
                }
            });
    }
}
