import {Component, Input, SecurityContext} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {Router} from '@angular/router';
import {LanguageService} from '@core/i18n/language.service';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {FiltersService} from '@core/services/filters.service';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {Licence, LicenceCustom, Media, Metadata} from 'micro_service_modules/api-kaccess';
import * as mediaType from 'micro_service_modules/api-kaccess/model/media';
import {DetailFunctions} from '../../pages/detail/detail-functions';
import LicenceTypeEnum = Licence.LicenceTypeEnum;
import MediaTypeEnum = Media.MediaTypeEnum;

@Component({
    selector: 'app-dataset-informations',
    templateUrl: './dataset-informations.component.html',
    styleUrls: ['./dataset-informations.component.scss'],
    standalone: false
})
export class DatasetInformationsComponent {
    @Input() mediaType = mediaType.Media.MediaTypeEnum;
    @Input() mediaDataType = MediaTypeEnum;
    @Input() licenceLabel;
    @Input() conceptUri;
    @Input() themeLabel: string;
    @Input() downloadableMedias: Media[];
    @Input() metadata: Metadata | undefined;
    @Input() isRestricted: boolean;
    @Input() isSelfdata: boolean;
    @Input() themePicto: string;
    // Indique si on affiche le loader pendant le téléchargement du media
    @Input() isLoading = false;
    @Input() otherDatasets: Metadata[];
    // Pour filtrer à partir du même thème
    @Input() themeCode: string;
    @Input() mediasTitle: string;
    @Input() maxDatasetDiplayed: number;
    @Input() totalOtherDatasets: number;
    mediaSize: MediaSize;
    licenceType = Licence.LicenceTypeEnum;

    constructor(
        iconRegistryService: IconRegistryService,
        private domSanitizer: DomSanitizer,
        private readonly breakpointObserverService: BreakpointObserverService,
        private readonly dataSetDetailsFunctions: DetailFunctions,
        private readonly languageService: LanguageService,
        private readonly filtersService: FiltersService,
        private readonly router: Router,
    ) {
        this.mediaSize = this.breakpointObserverService.getMediaSize();
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }

    /**
     * Fonction permettant de verifier si available_formats n'est pas vide et affiche le media_type
     * @param mediaData
     */
    isAvailableFormat(mediaData: string): boolean {
        const result = this.metadata.available_formats.filter(element =>
            element.media_type === mediaData
        );
        return result.length > 0;
    }

    /**
     * Permet de récupérer le résumé long d'un metadata
     * @param item
     */
    getSummaryDescription(item: Metadata): SafeHtml {
        return this.sanitize(this.languageService.getRichTextForCurrentLanguage(item.summary));
    }


    getCustomLicenceLabel(licence: Licence): string {
        return this.dataSetDetailsFunctions.getCustomLicenceLabel(licence);
    }

    getCustomLicenceUri(): string {
        if (this.metadata.access_condition?.licence.licence_type === LicenceTypeEnum.Custom) {
            const licenceCustom = this.metadata.access_condition.licence as LicenceCustom;
            return licenceCustom.custom_licence_uri;
        }
        return '';
    }


    public filterOnTheSameThemeAndGoToCatalog(): void {
        this.filtersService.deleteAllFilters();
        this.filtersService.themesFilter.value = [this.themeCode];
        this.router.navigate(['/catalogue']);
    }

    public hasManyOtherDatasets(): boolean {
        return this.totalOtherDatasets > this.maxDatasetDiplayed;
    }


    sanitize(html: string): SafeHtml {

        return this.domSanitizer.sanitize(SecurityContext.HTML, html);
    }
}
