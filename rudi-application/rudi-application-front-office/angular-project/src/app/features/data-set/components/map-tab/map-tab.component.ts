import {Component, Input, OnInit} from '@angular/core';
import {MatCard} from '@angular/material/card';
import {DataSetAccessService} from '@core/services/data-set/data-set-access.service';
import {DisplayMapService} from '@core/services/data-set/display-map.service';
import {LogService} from '@core/services/log.service';
import {TranslatePipe} from '@ngx-translate/core';
import {ErrorBoxComponent} from '@shared/core/common/error-box/error-box.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {MapComponent} from '@shared/core/maps/map/map.component';
import {Media, Metadata} from 'micro_service_modules/api-kaccess';
import {LayerInformation} from 'micro_service_modules/konsult/konsult-model';
import {switchMap} from 'rxjs/operators';

@Component({
    selector: 'app-map-tab',
    templateUrl: './map-tab.component.html',
    styleUrls: ['./map-tab.component.scss'],
    imports: [MatCard, LoaderComponent, MapComponent, ErrorBoxComponent, TranslatePipe]
})
export class MapTabComponent implements OnInit {

    @Input()
    metadata: Metadata;

    @Input()
    mediaToDisplay: Media;

    isMapLoading: boolean;
    isErrorAccess: boolean;
    isErrorServer: boolean;

    baseLayers: LayerInformation[] = [];

    constructor(
        private readonly datasetAccessService: DataSetAccessService,
        private readonly displayMapService: DisplayMapService,
        private readonly logService: LogService
    ) {
    }

    ngOnInit(): void {
        if (this.metadata && this.mediaToDisplay) {
            this.isMapLoading = true;
            this.datasetAccessService.hasAccess(this.metadata).pipe(
                switchMap((hasAccess: boolean) => {
                    this.isErrorAccess = !hasAccess;
                    return this.displayMapService.getDatasetBaseLayers();
                })
            ).subscribe({
                next: (baseLayers: LayerInformation[]) => {
                    this.baseLayers = baseLayers;
                    this.isMapLoading = false;
                },
                error: (e) => {
                    this.logService.error(e);
                    this.isMapLoading = false;
                }
            });
        }
    }
}
