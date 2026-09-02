import {NgClass, SlicePipe} from '@angular/common';
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatCard, MatCardContent} from '@angular/material/card';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';
import {DomSanitizer} from '@angular/platform-browser';
import {RouterLink} from '@angular/router';
import {LanguageService} from '@core/i18n/language.service';
import {BreakpointObserverService, MediaSize, NgClassObject} from '@core/services/breakpoint-observer.service';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {ThemeCacheService} from '@core/services/theme-cache.service';
import {MetadataUtils} from '@shared/utils/metadata-utils';
import {SplitPipe} from '@shared/utils/pipes/split.pipe';
import {TruncateTextPipe} from '@shared/utils/pipes/truncate-text.pipe';
import {Metadata} from 'micro_service_modules/api-kaccess';
import {OrganizationLogoComponent} from '../../../organisation/organization-logo/organization-logo.component';

@Component({
    selector: 'app-data-set-card',
    templateUrl: './data-set-card.component.html',
    styleUrls: ['./data-set-card.component.scss'],
    imports: [MatCard, NgClass, MatCardContent, OrganizationLogoComponent, MatIcon, MatButton, MatTooltip, SlicePipe, SplitPipe, TruncateTextPipe, RouterLink]
})
export class DataSetCardComponent implements OnInit {
    @Input() metadata: Metadata;
    mediaSize: MediaSize;
    @Input() isSelectable = false;
    isSelected = false;
    isSingleClick = true;
    @Output() selectMetadata = new EventEmitter<Metadata>();
    @Output() dbSelectMetadata = new EventEmitter<Metadata>();

    constructor(
        private readonly themeCacheService: ThemeCacheService,
        private readonly breakpointObserver: BreakpointObserverService,
        private readonly languageService: LanguageService,
        private readonly uriComponentCodec: URIComponentCodec,
        private readonly matIconRegistry: MatIconRegistry,
        private readonly domSanitizer: DomSanitizer,
    ) {
        this.matIconRegistry.addSvgIcon(
            'key_icon_88_blue',
            this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/icons/key_icon_88_blue.svg')
        );
        this.matIconRegistry.addSvgIcon(
            'top-right-triangle',
            this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/icons/top-right-triangle.svg')
        );
        this.matIconRegistry.addSvgIcon(
            'top-right-triangle-self-data',
            this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/icons/top-right-triangle-self-data.svg')
        );
        this.matIconRegistry.addSvgIcon(
            'self-data-icon',
            this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/icons/self-data-icon.svg')
        );

        themeCacheService.init();
    }

    get themePicto(): string {
        return this.metadata.theme;
    }

    get themeLabel(): string {
        return this.themeCacheService.getThemeLabelFor(this.metadata);
    }

    get ngClass(): NgClassObject {
        const ngClassFromMediaSize: NgClassObject = this.breakpointObserver.getNgClassFromMediaSize('data-set-card');
        return {
            ...ngClassFromMediaSize,
            restricted: this.isRestricted,
            selfdata: this.isSelfdata
        };
    }

    get titleMaxLength(): number {
        return 60;
    }

    get descriptionMaxLength(): number {
        return 200;
    }

    get isRestricted(): boolean {
        return MetadataUtils.isRestricted(this.metadata);
    }

    get isSelfdata(): boolean {
        return MetadataUtils.isSelfdata(this.metadata);
    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();
    }

    /**
     * Permet de recuperer la description dans metadata
     */
    getSynopsis(item: Metadata): string {
        return this.languageService.getTextForCurrentLanguage(item.synopsis);
    }

    removeAccents(theme: string): string {
        return theme.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    }

    /**
     * Commandes de route vers le detail d'un jdd, utilisées par [routerLink] (balise <a>) afin de
     * bénéficier du comportement natif du navigateur (clic droit "ouvrir dans un nouvel onglet",
     * ctrl/cmd+clic, clic molette). Retourne null si la navigation n'est pas possible.
     */
    get detailUrl(): string[] | null {
        if (this.metadata?.global_id && this.metadata?.resource_title) {
            return ['/catalogue/detail', this.metadata.global_id, this.uriComponentCodec.normalizeString(this.metadata.resource_title)];
        }
        return null;
    }

    /**
     * Function permettant de verifier si le jdd est selectionnable
     * @param isSelected
     */
    select(isSelected = true): void {
        if (this.isSelectable) {
            this.isSelected = isSelected;
            if (isSelected) {
                this.selectMetadata.emit(this.metadata);
            }
        }
    }

    /**
     * Function SingleClick
     * @param isSelected
     */
    singleClickSelect(isSelected = true): void {
        this.isSingleClick = true;
        // eslint-disable-next-line no-undef
        setTimeout(() => {
            if (this.isSingleClick) {
                this.select(isSelected);
            }
        }, 250);
    }

    /**
     * Function doubleClick
     * @param isSelected
     */
    doubleClickSelect(isSelected = true): void {
        this.isSelected = isSelected;
        this.isSingleClick = false;
        this.dbSelectMetadata.emit(this.metadata);
    }


}
