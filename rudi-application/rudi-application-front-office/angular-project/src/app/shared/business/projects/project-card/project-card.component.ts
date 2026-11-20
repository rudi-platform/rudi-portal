import {NgClass, NgTemplateOutlet} from '@angular/common';
import {Component, Input, OnInit} from '@angular/core';
import {MatCard, MatCardContent, MatCardImage} from '@angular/material/card';
import {RouterLink} from '@angular/router';
import {ProjektMetierService} from '@core/services/asset/project/projekt-metier.service';
import {BreakpointObserverService, MediaSize, NgClassObject} from '@core/services/breakpoint-observer.service';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {HtmlService} from '@core/services/html/html.service';
import {Base64EncodedLogo} from '@core/services/image-logo.service';
import {ProjectCatalogItem} from '@features/project/model/project-catalog-item';
import {TruncatedTextComponent} from '@shared/core/common/truncated-text/truncated-text.component';
import {StretchedLinkClickDirective} from '@shared/utils/directives/stretched-link-click-directive/stretched-link-click.directive';

@Component({
    selector: 'app-project-card',
    templateUrl: './project-card.component.html',
    styleUrls: ['./project-card.component.scss'],
    standalone: true,
    imports: [
        MatCard, NgClass, NgTemplateOutlet, MatCardImage, MatCardContent, RouterLink, TruncatedTextComponent, StretchedLinkClickDirective
    ]
})
export class ProjectCardComponent implements OnInit {
    @Input() projectCatalogItem: ProjectCatalogItem;
    @Input() mediaSize: MediaSize;
    /** Heading level of the card title, depending on the page outline */
    @Input() titleLevel: 2 | 3 = 2;
    defaultLogo: Base64EncodedLogo;

    constructor(
        private readonly breakpointObserver: BreakpointObserverService,
        private readonly uriComponentCodec: URIComponentCodec,
        private readonly projektMetierService: ProjektMetierService,
        private readonly htmlService: HtmlService,
    ) {
        this.defaultLogo = '/assets/images/logo_projet_par_defaut.png';
    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();
        if (!this.projectCatalogItem.logo) {
            this.projektMetierService.getProjectLogo(this.projectCatalogItem.project.uuid).subscribe((logo) => {
                this.projectCatalogItem.logo = logo;
            });
        }
    }

    get ngClass(): NgClassObject {
        const ngClassFromMediaSize: NgClassObject = this.breakpointObserver.getNgClassFromMediaSize('project-card');
        return {
            ...ngClassFromMediaSize,
        };
    }

    get projectPicture(): string {
        return this.projectCatalogItem?.logo ?? this.defaultLogo;
    }

    get projectOwnerInfo(): string {
        if (this.projectCatalogItem != null) {
            // ownerInfo n'est plus nullable, il renvoie [Utilisateur inconnu] si user not found (RUDI-2408)
            return this.projectCatalogItem?.ownerInfo?.name;
        }
        return '';
    }

    get projectTitle(): string {
        if (this.projectIsNotNull()) {
            return this.projectCatalogItem.project.title;
        }
        return '';
    }

    get projectDescription(): string {
        if (this.projectIsNotNull()) {
            return this.htmlService.stripHtml(this.projectCatalogItem.project.description);
        }
        return '';
    }

    private projectIsNotNull(): boolean {
        return (this.projectCatalogItem?.project != null);
    }

    /**
     * Commandes de route vers le detail d'un projet (réutilisation), utilisées par [routerLink]
     * (balise <a>) afin de bénéficier du comportement natif du navigateur (clic droit "ouvrir
     * dans un nouvel onglet", ctrl/cmd+clic, clic molette). Retourne null si la navigation
     * n'est pas possible.
     */
    get detailUrl(): string[] | null {
        if (this.projectIsNotNull()) {
            return ['/projets/detail', this.projectCatalogItem.project.uuid, this.uriComponentCodec.normalizeString(this.projectCatalogItem.project.title)];
        }
        return null;
    }
}
