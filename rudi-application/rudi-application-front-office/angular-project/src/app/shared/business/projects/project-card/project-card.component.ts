import {Component, Input, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ProjektMetierService} from '@core/services/asset/project/projekt-metier.service';
import {BreakpointObserverService, MediaSize, NgClassObject} from '@core/services/breakpoint-observer.service';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {Base64EncodedLogo} from '@core/services/image-logo.service';
import {ProjectCatalogItem} from '@features/project/model/project-catalog-item';

@Component({
    selector: 'app-project-card',
    templateUrl: './project-card.component.html',
    styleUrls: ['./project-card.component.scss'],
    standalone: false
})
export class ProjectCardComponent implements OnInit {
    @Input() projectCatalogItem: ProjectCatalogItem;
    @Input() mediaSize: MediaSize;
    defaultLogo: Base64EncodedLogo;

    constructor(
        private readonly breakpointObserver: BreakpointObserverService,
        private readonly uriComponentCodec: URIComponentCodec,
        private readonly router: Router,
        private readonly projektMetierService: ProjektMetierService,
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
        return this.projectCatalogItem?.logo !== null ? this.projectCatalogItem.logo : this.defaultLogo;
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
            return this.projectCatalogItem.project.description;
        }
        return '';
    }

    private projectIsNotNull(): boolean {
        return (this.projectCatalogItem != null && this.projectCatalogItem.project != null);
    }

    clickCard(): void {
        this.router.navigate(['/projets/detail/' + this.projectCatalogItem.project.uuid + '/' + this.uriComponentCodec.normalizeString(this.projectCatalogItem.project.title)]);
    }
}
