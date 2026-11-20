import {AfterViewInit, Component, ElementRef, Input, ViewChild} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {TranslatePipe} from '@ngx-translate/core';
import Swiper from 'swiper';
import {Autoplay, Grid, Navigation} from 'swiper/modules';

@Component({
    selector: 'app-rudi-swiper',
    templateUrl: './rudi-swiper.component.html',
    styleUrls: ['./rudi-swiper.component.scss'],
    imports: [MatIcon, TranslatePipe],
})
export class RudiSwiperComponent implements AfterViewInit {
    private swiper: Swiper;

    @ViewChild('swiperWrapper', {static: true}) swiperWrapper: ElementRef<HTMLDivElement>;
    @ViewChild('liveRegion', {static: true}) liveRegion: ElementRef<HTMLDivElement>;

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
    ariaLabel?: string;


    constructor() {
        this.containerName = 'rudi-swiper';
        this.autoplay = false;
        this.loop = true;
        this.slidesPerView = 1;
        this.spaceBetween = 20;
        this.rowsPerView = 1;
        this.ariaLabel = undefined;
    }

    ngAfterViewInit(): void {
        Array.from(this.swiperWrapper.nativeElement.children).forEach((element: Element): void => {
            element.classList.add('swiper-slide');
        });

        if (this.swiper) {
            this.swiper.destroy(true, true);
        }

        this.swiper = this.buildSwiperInstance().init();
        this.updateSlidesAccessibility();
    }

    /**
     * Seule la slide pleinement visible reste accessible : tabindex 0 et aria-hidden retiré.
     * Les autres (hors écran, clones de boucle) passent en tabindex -1 + aria-hidden="true".
     *
     * Le tabindex seul ne suffit pas : sans aria-hidden, NVDA/JAWS continuent de lire les
     * slides hors champ en navigation par flèches, ce qui fait défiler le conteneur et perd
     * le focus visible.
     */
    private updateSlidesAccessibility(): void {
        const slides = this.getSlides();
        slides.forEach((slide: HTMLElement) => {
            const isFullyVisible = slide.classList.contains('swiper-slide-fully-visible')
                && !slide.classList.contains('swiper-slide-duplicate');
            const focusableElements = slide.querySelectorAll<HTMLElement>('a[href], button, [tabindex]');
            focusableElements.forEach((el: HTMLElement) => {
                el.tabIndex = isFullyVisible ? 0 : -1;
            });
            if (isFullyVisible) {
                slide.removeAttribute('aria-hidden');
            } else {
                slide.setAttribute('aria-hidden', 'true');
            }
        });
    }

    /**
     * Annonce dans la zone aria-live les slides pleinement visibles (hors clones). Le bouton
     * précédent/suivant avance par page entière, donc on annonce toutes les cartes affichées,
     * pas juste la première.
     */
    private announceCurrentSlide(): void {
        if (!this.liveRegion) {
            return;
        }
        const fullyVisibleSlides = this.getSlides().filter((slide: HTMLElement) =>
            slide.classList.contains('swiper-slide-fully-visible')
            && !slide.classList.contains('swiper-slide-duplicate')
        );
        const names = fullyVisibleSlides
            .map((slide: HTMLElement) => this.getSlideAccessibleName(slide))
            .filter((name: string) => !!name);
        this.liveRegion.nativeElement.textContent = names.join(', ');
    }

    /**
     * Nom accessible d'une slide : son propre aria-label, sinon celui du premier
     * descendant labellisé (ex. le lien de la carte), sinon son texte.
     */
    private getSlideAccessibleName(slide: HTMLElement): string {
        const ownLabel = slide.getAttribute('aria-label');
        if (ownLabel) {
            return ownLabel;
        }
        const labelledDescendant = slide.querySelector<HTMLElement>('[aria-label]');
        return labelledDescendant?.getAttribute('aria-label') ?? slide.textContent?.trim() ?? '';
    }

    private getSlides(): HTMLElement[] {
        return Array.from(this.swiperWrapper.nativeElement.children) as HTMLElement[];
    }

    private buildSwiperInstance(): Swiper {
        return new Swiper(`.${this.containerName}`, {
            modules: [Autoplay, Navigation, Grid],
            watchSlidesProgress: true,
            navigation: {
                nextEl: '.swiper-button-next',
                prevEl: '.swiper-button-prev',
            },
            on: {
                slideChange: () => {
                    this.updateSlidesAccessibility();
                    this.announceCurrentSlide();
                },
                transitionEnd: () => {
                    this.updateSlidesAccessibility();
                },
                resize: () => {
                    this.updateSlidesAccessibility();
                },
            },

            grid: {
                fill: 'row',
                rows: this.rowsPerView,
            },
            autoplay: this.autoplay,
            loop: this.loop,
            slidesPerView: this.slidesPerView,
            slidesPerGroup: this.slidesPerView,
            spaceBetween: this.spaceBetween,
        });
    }
}
