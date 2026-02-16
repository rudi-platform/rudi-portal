import {Component, OnInit} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';

import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {PageTitleService} from '@core/services/page-title.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {RouteHistoryService} from '@core/services/route-history.service';
import {TranslateService} from '@ngx-translate/core';
import {FooterComponent} from '@shared/core/layout/footer/footer.component';
import {HeaderComponent} from '@shared/core/layout/header/header.component';
import {Script} from 'micro_service_modules/konsult/konsult-model';
import {filter, map} from 'rxjs/operators';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    imports: [HeaderComponent, RouterOutlet, FooterComponent]
})
export class AppComponent implements OnInit {

    constructor(
        private readonly routeHistoryService: RouteHistoryService,
        private readonly breakpointObserver: BreakpointObserverService,
        private readonly translate: TranslateService,
        private readonly router: Router,
        private readonly pageTitleService: PageTitleService,
        private readonly propertiesService: PropertiesMetierService,
    ) {
        translate.setFallbackLang('fr');
        router.events.pipe(
            filter(event => event instanceof NavigationEnd),
            map(event => event as NavigationEnd),
            map(event => event.url)
        ).subscribe(url => {
            pageTitleService.setPageTitleFromUrl(url);
            this.loadScripts();
        });
    }

    mediaSize: MediaSize;

    /**
     * Méthode de chargement de scripts supplémentaires
     * @private
     */
    private static loadScript(script: Script): void {
        const scriptUrl = script.url;
        if (!scriptUrl) {
            return;
        }

        const existing = document.querySelector(`script[src="${CSS.escape(scriptUrl)}"]`);
        if (existing) {
            if (!script.reload) {
                return;
            }
            existing.remove();
        }

        const node = document.createElement('script');
        node.src = scriptUrl;
        node.type = 'text/javascript';
        node.async = true;
        document.head.appendChild(node);
    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();

        this.loadScripts();
    }

    loadScripts(): void {
        // appel aux properties back + passer le résultat au loadscript
        this.propertiesService.getScripts('scripts').subscribe(scripts => scripts?.forEach(script => AppComponent.loadScript(script)));
    }
}
