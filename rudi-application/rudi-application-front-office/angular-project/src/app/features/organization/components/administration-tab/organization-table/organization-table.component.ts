import {Component, Input, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatTableDataSource} from '@angular/material/table';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {OrganizationTaskMetierService} from '@core/services/tasks/strukture/organization/organization-task-metier.service';
import {CloseEvent} from '@features/data-set/models/dialog-closed-data';
import {TranslateService} from '@ngx-translate/core';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {Level} from '@shared/notification-template/notification-template.component';
import {GetBackendPropertyPipe} from '@shared/pipes/get-backend-property.pipe';
import {WorkflowFormDialogComponent} from '@shared/workflow-form-dialog/workflow-form-dialog.component';
import {Form} from 'micro_service_modules/api-bpmn';
import {
    Field,
    Organization,
    OrganizationFormType,
    OrganizationStatus,
    Section,
    Task,
    TaskService
} from 'micro_service_modules/strukture/api-strukture';
import {Observable} from 'rxjs';
import {switchMap} from 'rxjs/operators';

@Component({
    selector: 'app-organization-table',
    templateUrl: './organization-table.component.html',
    styleUrls: ['./organization-table.component.scss'],
    standalone: false
})
export class OrganizationTableComponent implements OnInit {
    @Input() isLoading: boolean;
    @Input() organization: Organization;
    @Input() enableCaptchaOnPage: boolean;
    private urlToRedirectIfError: string;

    public form: Form;

    displayedColumns: string[] = ['name', 'id'];
    dataSource: MatTableDataSource<Organization> = new MatTableDataSource([]);


    constructor(
        private readonly iconRegistryService: IconRegistryService,
        private readonly taskService: TaskService,
        private readonly dialog: MatDialog,
        private readonly organizationTaskMetierService: OrganizationTaskMetierService,
        private readonly snackBarService: SnackBarService,
        private readonly translateService: TranslateService,
        private readonly getBackendProperty: GetBackendPropertyPipe
    ) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }

    ngOnInit(): void {
        this.getBackendProperty.transform('front.contact').subscribe({
            next: url => {
                this.urlToRedirectIfError = url;
            },
            error: err => {
                this.urlToRedirectIfError = 'https://rudi.fr/contact';
            }
        });
        this.dataSource = new MatTableDataSource([this.organization]);

        if (this.organization.organizationStatus == OrganizationStatus.Validated) {
            this.displayedColumns = ['name', 'id', 'dash'];
            this.getArchiveForm();

        }
    }

    getArchiveForm() {
        this.taskService.lookupOrganizationDraftForm(OrganizationFormType.DraftArchive).subscribe({
            next: (form) => {
                this.form = form;
            },
        });
    }

    openPopinArchive() {
        this.form.sections.forEach((section: Section) => {
            section.fields && section.fields.forEach((field: Field) => {
                if (field.definition.name == 'draftType') {
                    field.values = ['archive'];
                    return field;
                }
                return field;
            });
        });
        this.dialog.open(WorkflowFormDialogComponent, {
            data: {
                form: this.form,
                title: this.translateService.instant('personalSpace.organization.archive.dialog.title'),
            }
        }).afterClosed().subscribe(result => {
            if (result?.closeEvent == CloseEvent.VALIDATION) {
                this.archiveOrganization().subscribe({
                    next: (task: Task) => {
                        this.snackBarService.openSnackBar({
                            level: Level.SUCCESS,
                            message: this.translateService.instant('personalSpace.organization.archive.success')
                        });
                    },
                    error: (err) => {
                        if (err.status == 400) {
                            this.snackBarService.openSnackBar({
                                level: Level.ERROR,
                                message: this.translateService.instant('personalSpace.organization.archive.errorArchiveAlreadyInProgress')
                            });
                        } else if (err.status == 409) {
                            this.snackBarService.openSnackBar({
                                level: Level.ERROR,
                                message: this.translateService.instant('personalSpace.organization.archive.errorTaskInProgress')
                            });
                        } else {
                            this.snackBarService.openSnackBar({
                                level: Level.ERROR,
                                message: `${this.translateService.instant('personalSpace.organization.archive.error')} <a href="${this.urlToRedirectIfError}" target="_blank">${this.translateService.instant('common.ici')}</a>`,
                            });
                        }
                    }
                });
            }
        });
    }

    archiveOrganization(): Observable<Task> {
        this.organization.form = this.form;
        // 1) On crée le draft à partir de l'organisation
        return this.organizationTaskMetierService.createDraft(this.organization).pipe(
            // 2) On démarre le workflow avec le résultat du create draft
            switchMap((task: Task) => {
                return this.organizationTaskMetierService.startTask(task);
            }),
        );

    }

}
