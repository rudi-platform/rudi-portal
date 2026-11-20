import {NgClass, NgTemplateOutlet} from '@angular/common';
import {Component, Input} from '@angular/core';
import {MatDivider} from '@angular/material/divider';
import {MatIcon} from '@angular/material/icon';
import {RouterLink} from '@angular/router';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {TranslatePipe} from '@ngx-translate/core';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {OrganizationLogoComponent} from '../../../organisation/organization-logo/organization-logo.component';

@Component({
    selector: 'app-dataset-infos',
    templateUrl: './dataset-infos.component.html',
    styleUrls: ['./dataset-infos.component.scss'],
    imports: [NgClass, OrganizationLogoComponent, MatIcon, MatDivider, RouterLink, NgTemplateOutlet, TranslatePipe]
})
export class DatasetsInfosComponent {
    /**
     * affiche ou pas  le logo
     */
    @Input() showLogo: boolean;
    /**
     * le nom de l'organisation
     */
    @Input() organizationName: string;
    /**
     * Le titre du dataset
     */
    @Input() resourceTitle: string;
    /**
     *  le jdd est restreint ou non ?
     */
    @Input() isRestricted: boolean;
    /**
     * l'id de l'organisation
     */
    @Input() organizationId: string;
    /**
     * Ajoute un diviseur sous chaque élément de la card. Par défaut : false.
     */
    @Input() divider = false;
    /**
     * Id du jdd courrant
     */
    @Input() currentJddId: string;
    restrictedDatasetIcon = 'key_icon_88_secondary-color';
    mediaSize: MediaSize;

    @Input() defaultAlt: string;

    @Input() defaultLogo: string;


    constructor(
        private readonly breakpointObserverService: BreakpointObserverService,
        private readonly uriComponentCodec: URIComponentCodec,
        iconRegistryService: IconRegistryService,
    ) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
        this.mediaSize = this.breakpointObserverService.getMediaSize();
    }

    /**
     * Lien vers le détail du jdd, uniquement si un id et un titre sont fournis
     * (ex. non fourni quand ce composant sert juste d'étiquette dans une popover).
     */
    get detailsRouterLink(): string[] | null {
        if (!this.currentJddId || !this.resourceTitle) {
            return null;
        }
        return ['/catalogue/detail/' + this.currentJddId + '/' + this.uriComponentCodec.normalizeString(this.resourceTitle)];
    }
}
