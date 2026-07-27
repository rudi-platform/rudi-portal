import { NgClass } from '@angular/common';
import {Component, Input} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {OrganizationLogoComponent} from '../../../business/organisation/organization-logo/organization-logo.component';

@Component({
    selector: 'app-page-heading',
    templateUrl: './page-heading.component.html',
    styleUrls: ['./page-heading.component.scss'],
    imports: [NgClass, OrganizationLogoComponent, MatIcon]
})
export class PageHeadingComponent {

    @Input()
    organizationId: string;

    @Input()
    organizationName: string;

    @Input()
    icon: string;

    @Input()
    resourceTitle: string;

    @Input()
    status: string;

    mediaSize: MediaSize;

    constructor(private readonly breakpointObserverService: BreakpointObserverService) {
        this.mediaSize = this.breakpointObserverService.getMediaSize();
    }
}
