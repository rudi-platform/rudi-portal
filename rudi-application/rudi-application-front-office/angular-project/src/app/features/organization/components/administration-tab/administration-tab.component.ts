import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {FiltersService} from '@core/services/filters.service';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {Organization} from 'micro_service_modules/strukture/strukture-model';

@Component({
    selector: 'app-administration-tab',
    templateUrl: './administration-tab.component.html',
    standalone: false
})
export class AdministrationTabComponent implements OnInit {

    @Input() isLoading: boolean;
    @Input() organization: Organization;

    /**
     * Indique si le captcha doit s'activer sur cette page
     */
    enableCaptchaOnPage: boolean = true;

    constructor(
        private readonly iconRegistryService: IconRegistryService,
        private readonly route: ActivatedRoute,
        private readonly filterService: FiltersService,
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
    }
}
