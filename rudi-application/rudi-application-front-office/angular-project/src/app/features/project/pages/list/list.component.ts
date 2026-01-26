import {NgClass, NgIf} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {MatSidenavContainer, MatSidenavContent} from '@angular/material/sidenav';
import {Router} from '@angular/router';
import {DEFAULT_PROJECT_ORDER, ProjektMetierService} from '@core/services/asset/project/projekt-metier.service';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {TranslatePipe} from '@ngx-translate/core';
import {ProjectListComponent} from '@shared/business/projects/project-list/project-list.component';
import {PageTitleComponent} from '@shared/core/layout/page-title/page-title.component';
import {AclService} from 'micro_service_modules/acl/acl-api';
import {BannerComponent} from '../../components/banner/banner.component';
import {OrderComponent} from '../../components/order/order.component';
import {ProjectCatalogItem} from '../../model/project-catalog-item';

@Component({
    selector: 'app-list',
    templateUrl: './list.component.html',
    styleUrls: ['./list.component.scss'],
    imports: [MatSidenavContainer, MatSidenavContent, NgClass, ExtendedModule, PageTitleComponent, BannerComponent, NgIf, OrderComponent, ProjectListComponent, TranslatePipe]
})
export class ListComponent implements OnInit {
    mediaSize: MediaSize;
    searchIsRunning = true;
    projectListTotal = 0;
    order = DEFAULT_PROJECT_ORDER;

    constructor(private projektMetierService: ProjektMetierService,
                private aclService: AclService,
                private readonly breakpointObserver: BreakpointObserverService,
                private readonly router: Router,
                private readonly uriComponentCodec: URIComponentCodec,
    ) {
    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();
    }

    onClickProject(project: ProjectCatalogItem): Promise<boolean> {
        return this.router.navigate(['/projets/detail/' + project.project.uuid + '/' + this.uriComponentCodec.normalizeString(project.project.title)]);

    }

}
