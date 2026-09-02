import {AsyncPipe, NgClass} from '@angular/common';
import {Component, Input, OnInit} from '@angular/core';
import {MatDivider} from '@angular/material/divider';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {CustomizationService} from '@core/services/customization.service';
import {Base64EncodedLogo, ImageLogoService} from '@core/services/image-logo.service';
import {LogService} from '@core/services/log.service';
import {RedirectService} from '@core/services/redirect.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {SocialMediaSectionComponent} from '@shared/business/home/social-media-section/social-media-section.component';
import {FooterUtils} from '@shared/utils/footer-utils';
import {GetBackendPropertyPipe} from '@shared/utils/pipes/get-backend-property.pipe';
import {AppInfo} from 'micro_service_modules/acl/acl-api/model/models';
import {CmsAsset, PagedCmsAssets} from 'micro_service_modules/api-cms';
import {CustomizationDescription, KonsultService, MiscellaneousService} from 'micro_service_modules/konsult/konsult-api';
import {CmsTermsDescription} from 'micro_service_modules/konsult/konsult-model';
import {switchMap} from 'rxjs';

const OFFSET = 0;
const LIMIT = 4;
const DEFAULT_PICTO: Base64EncodedLogo = '/assets/images/logo_bleu_orange.svg';


@Component({
    selector: 'app-footer',
    templateUrl: './footer.component.html',
    styleUrls: ['./footer.component.scss'],
    imports: [NgClass, SocialMediaSectionComponent, MatDivider, AsyncPipe, TranslatePipe, GetBackendPropertyPipe]
})
export class FooterComponent implements OnInit {

    @Input() mediaSize: MediaSize;
    appInfo: AppInfo;
    loading = false;
    footerLogoLink: string;

    customizationDescription: CustomizationDescription;
    cmsTermsDescription: CmsTermsDescription;
    customizationDescriptionIsLoading: boolean;

    logoIsLoading: boolean;
    logoSrc: Base64EncodedLogo;
    logoAlt: string;

    displayComponent: boolean;
    termsValues: Array<SafeHtml>;


    constructor(
        private readonly miscellaneousService: MiscellaneousService,
        private readonly redirectService: RedirectService,
        private readonly translateService: TranslateService,
        private readonly konsultService: KonsultService,
        private readonly logger: LogService,
        private readonly breakpointObserver: BreakpointObserverService,
        private readonly domSanitizer: DomSanitizer,
        private readonly imageLogoService: ImageLogoService,
        private readonly customizationService: CustomizationService
    ) {
        this.customizationDescriptionIsLoading = false;
        this.displayComponent = false;
        this.termsValues = [];
        this.logoIsLoading = false;
        this.initCustomizationDescription();
    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();
        this.miscellaneousService.getApplicationInformation().subscribe(appInfo => this.appInfo = appInfo);
        this.loading = true;
    }

    initTerms(): void {
        // Construire le tableau de catégories à partir des champs disponibles
        const categories: string[] = this.cmsTermsDescription.termsCategories ?? [this.cmsTermsDescription.category];
        // Augmenter la limite pour s'assurer de récupérer tous les assets disponibles
        const limit = categories.length ?? LIMIT;

        this.konsultService.renderAssets(
            'TERMS',
            this.cmsTermsDescription.template_simple,
            categories,
            [],
            this.translateService.getCurrentLang(),
            OFFSET,
            limit
        ).subscribe({
            next: (pagedCmsAssets: PagedCmsAssets): void => {

                if (pagedCmsAssets.total === 0) {
                    this.displayComponent = false;
                    return;
                }

                this.displayComponent = pagedCmsAssets.total > 0;
                if (this.displayComponent) {
                    pagedCmsAssets.elements.forEach((cmsAsset: CmsAsset) => {
                        this.termsValues.push(this.domSanitizer.bypassSecurityTrustHtml(cmsAsset.content));
                    });
                }
            },
            error: (err) => {
                this.logger.error(err);
                this.displayComponent = false;
            }
        });
    }

    goToTop(): void {
        this.redirectService.goToTop();
    }

    get currentYear(): number {
        return FooterUtils.getCurrentYear();
    }

    private initCustomizationDescription(): void {
        this.customizationDescriptionIsLoading = true;
        this.customizationService.getCustomizationDescription()
            .subscribe({
                next: (customizationDescription: CustomizationDescription) => {
                    this.customizationDescription = customizationDescription;
                    this.logoAlt = customizationDescription.footer_description.footerLogo.logo_alt_text;
                    this.initLogo(this.customizationDescription.footer_description.footerLogo.logo);
                    this.footerLogoLink = this.customizationDescription.footer_description.footerLogo.url;
                    this.cmsTermsDescription = customizationDescription.cms_terms_description;
                    this.customizationDescriptionIsLoading = false;
                    console.log(this.customizationDescription);
                    this.initTerms();
                },
                error: (error) => {
                    this.logger.error(error);
                    this.customizationDescriptionIsLoading = false;
                }
            });
    }

    private initLogo(uuid: string): void {
        this.logoIsLoading = true;
        this.konsultService.downloadCustomizationResource(uuid)
            .pipe(
                switchMap((blob: Blob) => {
                    return this.imageLogoService.createImageFromBlob(blob);
                })
            ).subscribe({
            next: (logo: Base64EncodedLogo) => {
                this.logoSrc = logo;
                this.logoIsLoading = false;
            },
            error: (error) => {
                this.logger.error(error);
                this.logoSrc = DEFAULT_PICTO;
                this.logoIsLoading = false;
            }
        });
    }

    public redirectToRudi(): void {
        window.open(this.footerLogoLink).focus();
    }
}
