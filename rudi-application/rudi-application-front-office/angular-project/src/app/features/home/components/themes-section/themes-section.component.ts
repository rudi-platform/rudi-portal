import {Component, Input, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {FiltersService} from '@core/services/filters.service';
import {Theme} from '@features/home/types';
import {SwiperBreakpoint} from '@shared/rudi-swiper/types';

@Component({
    selector: 'app-themes-section',
    templateUrl: './themes-section.component.html',
    styleUrls: ['./themes-section.component.scss'],
    standalone: false
})
export class ThemesSectionComponent implements OnInit {
    @Input() themes: Theme[];
    @Input() isLoading: boolean;
    mediaSize: MediaSize;
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
        private readonly filtersService: FiltersService,
        private readonly router: Router,
        private readonly breakpointObserver: BreakpointObserverService
    ) {
        this.themes = [];
        this.isLoading = true;
    }

    onClickThemeCard(themeCode: string): void {
        this.filtersService.deleteAllFilters();
        this.filtersService.themesFilter.value = [themeCode];
        this.router.navigate(['/catalogue']);
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
