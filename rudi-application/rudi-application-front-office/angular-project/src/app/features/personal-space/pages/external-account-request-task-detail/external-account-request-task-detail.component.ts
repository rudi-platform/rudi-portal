import {Component, OnInit, signal} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle} from '@angular/material/expansion';
import {ActivatedRoute, Router} from '@angular/router';
import {LogService} from '@core/services/log.service';
import {PageTitleService} from '@core/services/page-title.service';
import {ProcessDefinitionsKeyIconRegistryService} from '@core/services/process-definitions-key-icon-registry.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {
    ExternalAccountRequestDependencies,
    ExternalAccountRequestTask,
    ExternalAccountRequestTaskDependenciesService,
    ExternalAccountRequestTaskDependencyFetchers
} from '@core/services/tasks/acl/external-account-request-task-dependencies.service';
import {ExternalAccountRequestTaskMetierService} from '@core/services/tasks/acl/external-account-request-task-metier.service';
import {
    ExternalAccountRequestTaskSearchCriteria
} from '@core/services/tasks/acl/external-account-request-task-search-criteria.interface';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {ContactCardComponent} from '@shared/business/contacts/contact-card/contact-card.component';
import {BannerButtonComponent} from '@shared/core/banner/banner-button/banner-button.component';
import {CardComponent} from '@shared/core/common/card/card.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {TaskDetailHeaderComponent} from '@shared/core/workflow/common/task-detail-header/task-detail-header.component';
import {TaskDetailComponent} from '@shared/core/workflow/common/task-detail/task-detail.component';
import {WorkflowExpansionComponent} from '@shared/core/workflow/workflow-expansion/workflow-expansion.component';
import {PROCESS_DEFINITION_KEY_TYPES} from '@shared/models/title-icon-type';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {Action, FieldType, Section} from 'micro_service_modules/api-bpmn';
import {ExternalAccountRequest} from 'micro_service_modules/acl/acl-model';
import {tap} from 'rxjs/operators';

@Component({
    selector: 'app-external-account-request-task-detail',
    templateUrl: './external-account-request-task-detail.component.html',
    styleUrls: ['./external-account-request-task-detail.component.scss'],
    imports: [PageComponent, TaskDetailHeaderComponent, TabsComponent, TabComponent, CardComponent,
        MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle,
        ContactCardComponent, BannerButtonComponent, WorkflowExpansionComponent, TranslatePipe]
})
export class ExternalAccountRequestTaskDetailComponent
    extends TaskDetailComponent<ExternalAccountRequest, ExternalAccountRequestDependencies,
        ExternalAccountRequestTask, ExternalAccountRequestTaskSearchCriteria>
    implements OnInit {

    isLoading: boolean;
    dependencies: ExternalAccountRequestDependencies;
    readonly panelInitialTaskOpenState = signal(false);

    constructor(
        readonly dialog: MatDialog,
        readonly translateService: TranslateService,
        readonly snackBarService: SnackBarService,
        readonly taskWithDependenciesService: ExternalAccountRequestTaskDependenciesService,
        readonly externalAccountRequestTaskMetierService: ExternalAccountRequestTaskMetierService,
        protected logger: LogService,
        private readonly route: ActivatedRoute,
        private readonly router: Router,
        private readonly pageTitleService: PageTitleService,
        private readonly externalAccountRequestTaskDependencyFetchers: ExternalAccountRequestTaskDependencyFetchers,
        private readonly processDefinitionsKeyIconRegistryService: ProcessDefinitionsKeyIconRegistryService
    ) {
        super(dialog, translateService, snackBarService, taskWithDependenciesService, externalAccountRequestTaskMetierService, logger);
        this.processDefinitionsKeyIconRegistryService.addAllSvgIcons(PROCESS_DEFINITION_KEY_TYPES);
    }

    get asset(): ExternalAccountRequest {
        return this.taskWithDependencies?.asset;
    }

    get displayableSections(): Section[] {
        return (this.task?.asset?.form?.sections ?? []).filter(section =>
            section.fields?.some(field =>
                field.definition?.type !== FieldType.Hidden &&
                field.values?.some(value => value != null && value.trim() !== '')
            )
        );
    }

    get pageSubTitle(): string {
        if (!this.asset) {
            return null;
        }
        return `${this.asset.firstname} ${this.asset.lastname}`;
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.taskId = params.taskId;
        });
    }

    set taskId(idTask: string) {
        if (idTask) {
            this.isLoading = true;

            this.taskWithDependenciesService.getTaskWithDependencies(idTask).pipe(
                tap(taskWithDependencies => {
                    this.taskWithDependencies = taskWithDependencies;
                    const acceptAction: Action = this.task?.actions?.find(action => action.name === 'validated');
                    if (acceptAction) {
                        acceptAction.label = this.translateService.instant('personalSpace.externalAccountRequestTaskDetail.acceptButton');
                    }
                    const refuseAction: Action = this.task?.actions?.find(action => action.name === 'refused');
                    if (refuseAction) {
                        refuseAction.label = this.translateService.instant('personalSpace.externalAccountRequestTaskDetail.refuseButton');
                    }
                }),
                injectDependencies({
                    organization: this.externalAccountRequestTaskDependencyFetchers.organization
                })
            ).subscribe({
                next: ({dependencies}) => {
                    this.dependencies = dependencies;
                    this.pageTitleService.setPageTitle(
                        this.translateService.instant('personalSpace.externalAccountRequestTaskDetail.pageTitle'));
                    this.isLoading = false;
                },
                error: (error) => {
                    this.isLoading = false;
                    this.logger.error(error);
                }
            });
        }
    }

    protected goBackToList(): Promise<boolean> {
        return this.router.navigate(['/personal-space/my-notifications']);
    }
}
