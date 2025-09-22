import {Component, OnInit} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';

import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {PageTitleService} from '@core/services/page-title.service';
import {RouteHistoryService} from '@core/services/route-history.service';
import {TranslateService} from '@ngx-translate/core';
import {filter, map} from 'rxjs/operators';
import { PropertiesMetierService } from './core/services/properties-metier.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: false
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
        translate.setDefaultLang('fr');
        router.events.pipe(
            filter(event => event instanceof NavigationEnd),
            map(event => event as NavigationEnd),
            map(event => event.url)
        ).subscribe(url => pageTitleService.setPageTitleFromUrl(url));
    }

    mediaSize: MediaSize;

    /**
     * Méthode de chargement de scripts supplémentaires
     * @private
     */
    private static loadScript(scriptUrl: string): void {
        // Chargement du script en asynchrone
        if (scriptUrl) {
            const node = document.createElement('script');
            node.src = scriptUrl;
            node.type = 'text/javascript';
            node.async = true;
            document.getElementsByTagName('head')[0].appendChild(node);
        }
    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();
        // appel aux properties back + passer le résultat au loadscript
        this.propertiesService.getStrings('scripts').subscribe(scriptUrls => scriptUrls?.forEach(scriptUrl => 
            AppComponent.loadScript(scriptUrl)
        ));
    }
}
