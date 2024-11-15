import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {LogService} from '@core/services/log.service';
import {PageTitleService} from '@core/services/page-title.service';
import {ProcessDefinitionsKeyIconRegistryService} from '@core/services/process-definitions-key-icon-registry.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {
    LinkedProducerDependencies,
    LinkedProducerTask,
    LinkedProducerTaskDependenciesService,
    LinkedProducerTaskDependencyFetcher
} from '@core/services/tasks/strukture/linked-producer/linked-producer-task-dependencies.service';
import {LinkedProducerTaskMetierService} from '@core/services/tasks/strukture/linked-producer/linked-producer-task-metier.service';
import {
    LinkedProducerTaskSearchCriteria
} from '@core/services/tasks/strukture/linked-producer/linked-producer-task-search-criteria.interface';
import {TranslateService} from '@ngx-translate/core';
import {PROCESS_DEFINITION_KEY_TYPES} from '@shared/models/title-icon-type';
import {TaskDetailComponent} from '@shared/task-detail/task-detail.component';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {ProjectStatus, Task} from 'micro_service_modules/projekt/projekt-api';
import {LinkedProducer, LinkedProducersService} from 'micro_service_modules/strukture/api-strukture';
import {LinkedProducerStatus, OwnerInfo} from 'micro_service_modules/strukture/strukture-model';
import {Observable} from 'rxjs';
import {map, tap} from 'rxjs/operators';

@Component({
    selector: 'app-producer-link-task-detail',
    templateUrl: './linked-producer-task-detail.component.html',
    styleUrls: ['./linked-producer-task-detail.component.scss']
})
export class LinkedProducerTaskDetailComponent
    extends TaskDetailComponent<LinkedProducer, LinkedProducerDependencies, LinkedProducerTask, LinkedProducerTaskSearchCriteria>
    implements OnInit {

    isLoading: boolean;
    dependencies: LinkedProducerDependencies;
    idTask: string;
    currentTask: Task;
    hasSections: boolean = false;
    ownerInfo: Observable<OwnerInfo>;
    headerLibelle: string;
    protected readonly ProjectStatus = ProjectStatus;

    constructor(
        readonly dialog: MatDialog,
        readonly translateService: TranslateService,
        readonly snackBarService: SnackBarService,
        readonly taskWithDependenciesService: LinkedProducerTaskDependenciesService,
        readonly linkedProducerTaskMetierService: LinkedProducerTaskMetierService,
        protected logger: LogService,
        private readonly route: ActivatedRoute,
        private readonly pageTitleService: PageTitleService,
        readonly linkedProducerTaskDependencyFetcher: LinkedProducerTaskDependencyFetcher,
        private readonly processDefinitionsKeyIconRegistryService: ProcessDefinitionsKeyIconRegistryService,
        private readonly router: Router,
        private readonly linkedProducersService: LinkedProducersService
    ) {
        super(dialog, translateService, snackBarService, taskWithDependenciesService, linkedProducerTaskMetierService, logger);
        this.processDefinitionsKeyIconRegistryService.addAllSvgIcons(PROCESS_DEFINITION_KEY_TYPES);
    }

    set taskId(idTask: string) {
        if (idTask) {
            this.isLoading = true;
            this.idTask = idTask;

            this.linkedProducerTaskMetierService.getTask(this.idTask).subscribe({
                next: (task: Task) => {
                    this.currentTask = task;
                    this.hasSections = !!task?.asset?.form?.sections;
                },
                error: (err) => console.error('Error fetching task:', err)
            });
            this.taskWithDependenciesService.getTaskWithDependencies(idTask).pipe(
                tap(taskWithDependencies => {
                    this.taskWithDependencies = taskWithDependencies;
                    // Détermine le titre fonction du status de l'asset.
                    this.headerLibelle = this.translateService.instant(taskWithDependencies.asset.linked_producer_status == LinkedProducerStatus.Validated ? 'personalSpace.linkedProducerDetails.detachement' : 'personalSpace.linkedProducerDetails.rattachement');
                }),
                injectDependencies({
                    linkedProducer: this.linkedProducerTaskDependencyFetcher.linkedProducer
                }),
                map(({task, asset, dependencies}) => {
                    return {
                        linkedProducer: dependencies.linkedProducer
                    };
                })
            ).subscribe({
                next: (dependencies: LinkedProducerDependencies) => {
                    this.dependencies = dependencies;
                    // On définit ici le titre de l'onglet en se basant sur le name de l'organization
                    this.pageTitleService.setPageTitle(this.dependencies.linkedProducer.title);
                    // On récupère les informations du Owner
                    this.getOwnerInfo(this.dependencies.linkedProducer.uuid);
                    this.isLoading = false;
                },
                error: (error) => {
                    this.isLoading = false;
                    console.error(error);
                }
            });
        }
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.taskId = params.taskId;
        });
    }

    public getOwnerInfo(uuid: string): void {
        this.ownerInfo = this.linkedProducersService.getLinkedProducerOwnerInfo(uuid);
    }

    protected goBackToList(): Promise<boolean> {
        return this.router.navigate(['/personal-space/my-notifications']);
    }
}
