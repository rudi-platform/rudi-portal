import {LiveAnnouncer} from '@angular/cdk/a11y';
import {NgClass} from '@angular/common';
import {afterNextRender, Component, ElementRef, EventEmitter, Injector, Input, Output} from '@angular/core';
import {FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatError} from '@angular/material/form-field';
import {RouterLink} from '@angular/router';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {ProjectDatasetItem} from '../../model/project-dataset-item';
import {DataSetButtonComponent} from '../data-set-button/data-set-button.component';
import {ProjectDatasetListComponent} from '../project-dataset-list/project-dataset-list.component';
import {SuccessStep3TemplateComponent} from '../success-step3-template/success-step3-template.component';

@Component({
    selector: 'app-step3-project',
    templateUrl: './step3-project.component.html',
    styleUrls: ['./step3-project.component.scss'],
    imports: [
        NgClass,
        FormsModule,
        ReactiveFormsModule,
        DataSetButtonComponent,
        MatError,
        ProjectDatasetListComponent,
        SuccessStep3TemplateComponent,
        RouterLink,
        TranslatePipe,
    ],
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
    private readonly datasetsDialogOpened: EventEmitter<void> = new EventEmitter<void>();

    @Output()
    private readonly requestDatasetDialogOpened: EventEmitter<void> = new EventEmitter<void>();

    @Output()
    private readonly itemRemoved: EventEmitter<ProjectDatasetItem> = new EventEmitter<ProjectDatasetItem>();

    @Output()
    private readonly itemEdited: EventEmitter<ProjectDatasetItem> = new EventEmitter<ProjectDatasetItem>();

    constructor(public dialog: MatDialog,
                private readonly elementRef: ElementRef<HTMLElement>,
                private readonly injector: Injector,
                private readonly liveAnnouncer: LiveAnnouncer,
                private readonly translateService: TranslateService) {
    }

    public openDialogDatasets(): void {
        this.datasetsDialogOpened.emit();
    }

    public openDialogRequestDataset(): void {
        this.requestDatasetDialogOpened.emit();
    }

    public handleRemoveItem(item: ProjectDatasetItem): void {
        this.itemRemoved.emit(item);
        // The list is removed with its last item: keep the focus on the add button (RGAA 12.8)
        afterNextRender(() => {
            if (!this.datasetItems?.length) {
                this.elementRef.nativeElement.querySelector<HTMLButtonElement>('app-dataset-button button')?.focus();
            }
            // The removal is local until the project is submitted: confirm it once the parent has updated the list (RGAA 7.5)
            if (!this.datasetItems?.includes(item)) {
                this.liveAnnouncer.announce(this.translateService.instant('project.stepper.submission.step3.itemRemoved', {title: item.title}));
            }
        }, {injector: this.injector});
    }

    public handleEditItem(item: ProjectDatasetItem): void {
        this.itemEdited.emit(item);
    }
}
