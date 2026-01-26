import {NgClass} from '@angular/common';
import {Component, Input, OnInit} from '@angular/core';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {MatSidenavContainer, MatSidenavContent} from '@angular/material/sidenav';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {TranslatePipe} from '@ngx-translate/core';
import {PageTitleComponent} from '@shared/core/layout/page-title/page-title.component';
import {ListContainerComponent} from '../../components/list-container/list-container.component';

@Component({
    selector: 'app-list',
    templateUrl: './list.component.html',
    styleUrls: ['./list.component.scss'],
    imports: [MatSidenavContainer, MatSidenavContent, NgClass, ExtendedModule, PageTitleComponent, ListContainerComponent, TranslatePipe]
})
export class ListComponent implements OnInit {

    @Input() mediaSize: MediaSize;

    constructor(
        private readonly breakpointObserver: BreakpointObserverService,
    ) {

    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();
    }
}
