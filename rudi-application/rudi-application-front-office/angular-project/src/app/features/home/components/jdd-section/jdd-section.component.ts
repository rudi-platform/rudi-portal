import { NgTemplateOutlet } from '@angular/common';
import {Component, Input} from '@angular/core';
import {RouterLink} from '@angular/router';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {TranslatePipe} from '@ngx-translate/core';
import {DatasetsInfosComponent} from '@shared/business/dataset/common/dataset-infos/dataset-infos.component';
import {RudiSwiperComponent} from '@shared/business/home/rudi-swiper/rudi-swiper.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {Metadata} from 'micro_service_modules/api-kaccess';

@Component({
    selector: 'app-jdd-section',
    templateUrl: './jdd-section.component.html',
    styleUrls: ['./jdd-section.component.scss'],
    imports: [LoaderComponent, NgTemplateOutlet, RudiSwiperComponent, RouterLink, DatasetsInfosComponent, TranslatePipe]
})
export class JddSectionComponent {
    @Input()
    jdds: Metadata[];

    @Input()
    isLoading: boolean;

    mediaSize: MediaSize;

    constructor(private readonly breakpointObserverService: BreakpointObserverService) {
        this.jdds = [];
        this.isLoading = true;
        this.mediaSize = this.breakpointObserverService.getMediaSize();
    }
}
