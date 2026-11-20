import {NgTemplateOutlet} from '@angular/common';
import {Component, Input, OnInit} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {RouterLink} from '@angular/router';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {Theme} from '@features/home/types';
import {TranslatePipe} from '@ngx-translate/core';
import {RudiSwiperComponent} from '@shared/business/home/rudi-swiper/rudi-swiper.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';

@Component({
    selector: 'app-themes-section',
    templateUrl: './themes-section.component.html',
    styleUrls: ['./themes-section.component.scss'],
    imports: [LoaderComponent, RudiSwiperComponent, MatIcon, TranslatePipe, RouterLink, NgTemplateOutlet]
})
export class ThemesSectionComponent implements OnInit {
    /** Nombre de cartes visibles avant le bouton "voir plus de thèmes" */
    private static readonly DISCLOSURE_VISIBLE_COUNT = 6;

    @Input() themes: Theme[];
    @Input() isLoading: boolean;
    mediaSize: MediaSize;
    selectedThemeCode: string | null = null;
    isThemesExpanded = false;

    constructor(
        private readonly breakpointObserver: BreakpointObserverService
    ) {
        this.themes = [];
        this.isLoading = true;
    }

    /**
     * Commandes de route pour [routerLink], pour garder le comportement natif du lien
     * (ctrl+clic, clic molette, etc.).
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

    toggleThemesExpanded(): void {
        this.isThemesExpanded = !this.isThemesExpanded;
    }

    get visibleThemes(): Theme[] {
        return this.themes.slice(0, ThemesSectionComponent.DISCLOSURE_VISIBLE_COUNT);
    }

    get hiddenThemes(): Theme[] {
        return this.themes.slice(ThemesSectionComponent.DISCLOSURE_VISIBLE_COUNT);
    }

    get hasMoreThemes(): boolean {
        return this.themes.length > ThemesSectionComponent.DISCLOSURE_VISIBLE_COUNT;
    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();
    }

}
