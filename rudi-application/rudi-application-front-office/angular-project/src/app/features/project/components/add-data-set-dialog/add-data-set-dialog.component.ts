import {Component, Inject, Input, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {AccessStatusFiltersType} from '@core/services/filters/access-status-filters-type';
import {OrderValue} from '@core/services/filters/order-filter';
import {KonsultMetierService} from '@core/services/konsult-metier.service';
import {KosMetierService} from '@core/services/kos-metier.service';
import {Metadata} from 'micro_service_modules/api-kaccess';
import {SimpleSkosConcept} from 'micro_service_modules/kos/kos-model';
import {of} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {AddDataSetDialogData} from './add-data-set-dialog-data';

@Component({
    selector: 'app-add-data-set-dialog',
    templateUrl: './add-data-set-dialog.component.html',
    styleUrls: ['./add-data-set-dialog.component.scss'],
    standalone: false
})
export class AddDataSetDialogComponent implements OnInit {
    get selectedMetadata(): Metadata {
        return this._selectedMetadata;
    }

    themes: SimpleSkosConcept[];
    @Input() mediaSize: MediaSize;
    orders: OrderValue[] = [
        'resource_title',
        '-resource_title',
        'dataset_dates.created',
        '-dataset_dates.created',
        'producer.organization_name',
        '-producer.organization_name',
    ];
    maxResultPerPage = 12;
    private _selectedMetadata: Metadata;
    public readonly accessStatusForcedValue: AccessStatusFiltersType;
    public readonly accessStatusHiddenValues: AccessStatusFiltersType[];

    constructor(
        private matIconRegistry: MatIconRegistry,
        private readonly konsultMetierService: KonsultMetierService,
        private domSanitizer: DomSanitizer,
        public dialogRef: MatDialogRef<AddDataSetDialogComponent>,
        private readonly breakpointObserver: BreakpointObserverService,
        private readonly kosMetierService: KosMetierService,
        @Inject(MAT_DIALOG_DATA) public data: AddDataSetDialogData
    ) {

        this.matIconRegistry.addSvgIcon(
            'icon-close',
            this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/icon-close.svg')
        );
        this.accessStatusForcedValue = data.accessStatusForcedValue;
        this.accessStatusHiddenValues = data.accessStatusHiddenValues;
    }

    ngOnInit(): void {
        this.konsultMetierService.getThemeCodes().pipe(
            switchMap(themeCodes => themeCodes.length > 0 ? this.kosMetierService.getThemes(themeCodes) : of([]))
        ).subscribe(concepts => {
            this.themes = concepts;
        });
        this.mediaSize = this.breakpointObserver.getMediaSize();
    }

    /**
     * Validation après fermeture de la dialog
     */
    validate(): void {
        this.dialogRef.close(this._selectedMetadata);
    }

    /**
     * Function permettant de selectionner un jdd par un click sur la card
     * @param metadata
     */
    selectMetadata(metadata: Metadata): void {
        this._selectedMetadata = metadata;
    }

    /**
     * Function permettant de selectionner un jdd en double click
     * @param metadata
     */
    doubleSelect(metadata: Metadata): void {
        this.selectMetadata(metadata);
        this.validate();
    }
}
