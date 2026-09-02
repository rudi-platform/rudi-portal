import {Component, Input, OnInit} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {RouterLink} from '@angular/router';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {Theme} from '@features/home/types';
import {TranslatePipe} from '@ngx-translate/core';
import {RudiSwiperComponent} from '@shared/business/home/rudi-swiper/rudi-swiper.component';
import {SwiperBreakpoint} from '@shared/business/home/rudi-swiper/types';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';

@Component({
    selector: 'app-themes-section',
    templateUrl: './themes-section.component.html',
    styleUrls: ['./themes-section.component.scss'],
    imports: [LoaderComponent, RudiSwiperComponent, MatIcon, TranslatePipe, RouterLink]
})
export class ThemesSectionComponent implements OnInit {
    @Input() themes: Theme[];
    @Input() isLoading: boolean;
    mediaSize: MediaSize;
    selectedThemeCode: string | null = null;
    cardSwiperBreakpoints: SwiperBreakpoint = {
        380: {
            slidesPerView: 3,
        },
        600: {
            slidesPerView: 3,
        },
        768: {
            slidesPerView: 4,
        },
        1024: {
            slidesPerView: 5,
        },
        1280: {
            slidesPerView: 6,
        },
        1480: {
            slidesPerView: 7,
        },
        1680: {
            slidesPerView: 8,
        },
    };

    constructor(
        private readonly breakpointObserver: BreakpointObserverService
    ) {
        this.themes = [];
        this.isLoading = true;
    }

    /**
     * Commandes de route vers le catalogue filtré sur une thématique, utilisées par [routerLink]
     * (balise <a>) afin de bénéficier du comportement natif du navigateur (clic simple : navigation
     * dans le même onglet ; clic droit / ctrl+clic / clic molette : ouverture dans un nouvel onglet).
     */
    get catalogueUrl(): string[] {
        return ['/catalogue'];
    }

    queryParamsFor(themeCode: string): { themes: string } {
        return {themes: themeCode};
    }

    onClickThemeCard(themeCode: string): void {
        this.selectedThemeCode = themeCode;
    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();
    }

    get canDisplaySwiper(): boolean {

        return (this.mediaSize.isXs && this.themes.length >= 2)
            || (this.mediaSize.isSm && this.themes.length >= 4)
            || (this.mediaSize.isMd && this.themes.length >= 6)
            || (this.mediaSize.isLg && this.themes.length >= 7)
            || (this.mediaSize.isXl && this.themes.length >= 8)
            || (this.mediaSize.isXxl && this.themes.length >= 10);
    }

}
