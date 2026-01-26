import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {ProjectDatasetItem} from '../../model/project-dataset-item';
import {NgClass, NgIf} from '@angular/common';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {DataSetButtonComponent} from '../data-set-button/data-set-button.component';
import {MatError} from '@angular/material/form-field';
import {ProjectDatasetListComponent} from '../project-dataset-list/project-dataset-list.component';
import {SuccessStep3TemplateComponent} from '../success-step3-template/success-step3-template.component';
import {RouterLink} from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-step3-project',
    templateUrl: './step3-project.component.html',
    styleUrls: ['./step3-project.component.scss'],
    imports: [NgClass, ExtendedModule, NgIf, FormsModule, ReactiveFormsModule, DataSetButtonComponent, MatError, ProjectDatasetListComponent, SuccessStep3TemplateComponent, RouterLink, TranslatePipe]
})
export class Step3ProjectComponent {

    @Input()
    public step3FormGroup: FormGroup;

    @Input()
    public linkedDatasetsError: boolean;

    @Input()
    public isPublished: boolean;

    @Input()
    public isSubmitted: boolean;

    @Input()
    public datasetItems: ProjectDatasetItem[];

    @Input()
    public createdProjectLink: string;

    @Output()
    private datasetsDialogOpened: EventEmitter<void> = new EventEmitter<void>();

    @Output()
    private requestDatasetDialogOpened: EventEmitter<void> = new EventEmitter<void>();

    @Output()
    private itemRemoved: EventEmitter<ProjectDatasetItem> = new EventEmitter<ProjectDatasetItem>();

    @Output()
    private itemEdited: EventEmitter<ProjectDatasetItem> = new EventEmitter<ProjectDatasetItem>();

    constructor(public dialog: MatDialog) {
    }

    public openDialogDatasets(): void {
        this.datasetsDialogOpened.emit();
    }

    public openDialogRequestDataset(): void {
        this.requestDatasetDialogOpened.emit();
    }

    public handleRemoveItem(item: ProjectDatasetItem): void {
        this.itemRemoved.emit(item);
    }

    public handleEditItem(item: ProjectDatasetItem): void {
        this.itemEdited.emit(item);
    }
}
