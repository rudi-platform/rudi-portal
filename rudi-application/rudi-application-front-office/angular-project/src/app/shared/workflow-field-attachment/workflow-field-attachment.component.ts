import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {AttachmentService} from '@core/services/attachment.service';
import {DefaultMatDialogConfig} from '@core/services/default-mat-dialog-config';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {TaskDependencyFetchers} from '@core/services/tasks/task-with-dependencies-service';
import {SELFDATA_PROCESS_KEY_DEFINITION, TaskDependencyFetcherFactory} from '@core/services/tasks/TaskDependencyFetcherFactory';
import {DataSize} from '@shared/models/data-size';
import {UploaderAdapter} from '@shared/uploader/uploader.adapter';
import {DocumentMetadata} from 'micro_service_modules/selfdata/selfdata-api';
import {Observable} from 'rxjs';
import {ALL_TYPES} from '../models/title-icon-type';
import {AttachmentPopinData} from '../workflow-field-attachment-popin/attachment-popin-data';
import {WorkflowFieldAttachmentPopinComponent} from '../workflow-field-attachment-popin/workflow-field-attachment-popin.component';
import {WorkflowFieldComponent} from '../workflow-field/workflow-field.component';

@Component({
    selector: 'app-workflow-field-attachment',
    templateUrl: './workflow-field-attachment.component.html',
    styleUrls: ['./workflow-field-attachment.component.scss']
})
export class WorkflowFieldAttachmentComponent extends WorkflowFieldComponent implements OnInit {
    attachmentLoading: boolean = false;
    fileSizeLoading: boolean = false;
    attachment: DocumentMetadata;
    allowedFileExtensions: string[] = [];
    attachmentService: AttachmentService;
    attachmentAdapter: UploaderAdapter<string>;
    private taskDependencyFetchers: TaskDependencyFetchers<any, any, any>;

    constructor(
        protected readonly dialog: MatDialog,
        private readonly taskDependencyFetcherFactory: TaskDependencyFetcherFactory,
        private readonly iconRegistryService: IconRegistryService
    ) {
        super();
        this.iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }

    ngOnInit(): void {
        const processDefinitionKey: string = this.properties.processDefinitionKey ?? SELFDATA_PROCESS_KEY_DEFINITION;
        this.taskDependencyFetchers = this.taskDependencyFetcherFactory.getService(processDefinitionKey);

        this.attachmentService = this.taskDependencyFetchers.attachmentService;
        this.attachmentAdapter = this.taskDependencyFetchers.attachmentAdapter;

        if (this.readonly) {
            this.lookupAttachment();
        }
        this.loadAllowedFileExtensions();
    }

    get attachmentNotFound(): boolean {
        return !this.attachment;
    }

    lookupAttachment(): void {
        this.attachmentLoading = true;
        this.attachmentService.getAttachmentMetadata(this.formControl.value).subscribe({
            next: (result: DocumentMetadata) => {
                this.attachment = result;
                this.attachmentLoading = false;
            },
            complete: () => {
                this.attachmentLoading = false;
            },
            error: (e) => {
                this.attachmentLoading = false;
            }
        });
    }

    loadAllowedFileExtensions(): void {
        this.attachmentLoading = true;

        this.taskDependencyFetchers.getAllowedExtensions(this.field).subscribe({
            next: (values: string[]) => {
                values.forEach((v: string) => this.allowedFileExtensions.push(v));
                this.attachmentLoading = false;
            },
            complete: () => {
                this.attachmentLoading = false;
            },
            error: (e) => {
                this.attachmentLoading = false;
            }
        });

        if (!this.properties.fileMaxSize) {
            this.fileSizeLoading = true;
            this.taskDependencyFetchers.getFileMaxSize(this.field).subscribe({
                next: (value: DataSize) => {
                    this.properties.fileMaxSize = value;
                    this.fileSizeLoading = false;
                },
                complete: () => {
                    this.fileSizeLoading = false;
                },
                error: (e) => {
                    this.fileSizeLoading = false;
                }
            });
        }
    }

    public handleClickAttachment(): void {
        this.openDialogWorkflowFieldAttachment(this.formControl.value).subscribe();
    }

    private openDialogWorkflowFieldAttachment(attachmentUuid: string): Observable<void> {
        const dialogConfig = new DefaultMatDialogConfig<AttachmentPopinData>();
        dialogConfig.data = {attachmentUuid};
        const dialogRef = this.dialog.open(WorkflowFieldAttachmentPopinComponent, dialogConfig);
        dialogRef.componentInstance.attachmentService = this.attachmentService;
        return dialogRef.afterClosed();
    }
}
