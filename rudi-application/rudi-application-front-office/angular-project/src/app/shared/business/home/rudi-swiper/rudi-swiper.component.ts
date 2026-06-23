import {AfterViewInit, Component, ElementRef, Input, ViewChild} from '@angular/core';
import {SwiperBreakpoint} from '@shared/business/home/rudi-swiper/types';
import Swiper from 'swiper';
import {Autoplay, Controller, Grid, Keyboard, Navigation, Pagination} from 'swiper/modules';

@Component({
    selector: 'app-rudi-swiper',
    templateUrl: './rudi-swiper.component.html',
    styleUrls: ['./rudi-swiper.component.scss']
})
export class RudiSwiperComponent implements AfterViewInit {
    private swiper: Swiper;

    @ViewChild('swiperWrapper', {static: true}) swiperWrapper: ElementRef<HTMLDivElement>;

    @Input()
    containerName: string;
    @Input()
    autoplay: boolean;
    @Input()
    loop: boolean;
    @Input()
    slidesPerView: number;
    @Input()
    spaceBetween: number;
    @Input()
    rowsPerView: number;
    @Input()
    breakpoints?: SwiperBreakpoint;
    @Input()
    ariaLabel?: string;
    @Input()
    ariaRoledescription?: string;


    constructor() {
        this.containerName = 'rudi-swiper';
        this.autoplay = false;
        this.loop = true;
        this.slidesPerView = 1;
        this.spaceBetween = 20;
        this.rowsPerView = 1;
        this.breakpoints = undefined;
        this.ariaLabel = undefined;
        this.ariaRoledescription = undefined;
    }

    ngAfterViewInit(): void {
        Array.from(this.swiperWrapper.nativeElement.children).forEach((element: Element): void => {
            element.classList.add('swiper-slide');
        });

        if (!!this.swiper) {
            this.swiper.destroy(true, true);
        }

        this.swiper = this.buildSwiperInstance().init();
        // Mise à jour initiale du tabindex après initialisation
        this.updateSlidesTabindex();
    }

    /**
     * Met tabindex="0" sur les éléments focusables des slides visibles à l'écran,
     * et tabindex="-1" sur ceux des slides non visibles (ou clonées en mode loop).
     * Cela garantit que la navigation Tab passe uniquement par les cartes visibles.
     */
    private updateSlidesTabindex(): void {
        const slides = Array.from(this.swiperWrapper.nativeElement.children) as HTMLElement[];
        slides.forEach((slide: HTMLElement) => {
            const isVisible = slide.classList.contains('swiper-slide-visible')
                && !slide.classList.contains('swiper-slide-duplicate');
            const focusableElements = slide.querySelectorAll<HTMLElement>('[tabindex]');
            focusableElements.forEach((el: HTMLElement) => {
                el.tabIndex = isVisible ? 0 : -1;
            });
        });
    }

    private buildSwiperInstance(): Swiper {
        return new Swiper(`.${this.containerName}`, {
            // used modules
            modules: [Autoplay, Controller, Navigation, Pagination, Grid, Keyboard],

            // required options
            centerInsufficientSlides: true,
            centeredSlidesBounds: true,
            centeredSlides: true,
            watchSlidesProgress: true,
            pagination: {
                el: '.swiper-pagination',
            },
            navigation: {
                nextEl: '.swiper-button-next',
                prevEl: '.swiper-button-prev',
            },
            keyboard: {
                enabled: true
            },
            on: {
                slideChange: () => this.updateSlidesTabindex(),
                transitionEnd: () => this.updateSlidesTabindex(),
                resize: () => this.updateSlidesTabindex(),
            },

            // customizable options
            grid: {
                fill: 'row',
                rows: this.rowsPerView,
            },
            autoplay: this.autoplay,
            loop: this.loop,
            slidesPerView: this.slidesPerView,
            spaceBetween: this.spaceBetween,
            breakpoints: this.breakpoints,
        });
    }
}
