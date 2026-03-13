
import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {ActivatedRoute, Router} from '@angular/router';
import {LogService} from '@core/services/log.service';
import {SnackBarService} from '@core/services/snack-bar.service';

import {
    LinkedDatasetDependencies,
    LinkedDatasetTask,
    LinkedDatasetTaskDependenciesService,
    LinkedDatasetTaskDependencyFetchers
} from '@core/services/tasks/projekt/linked-dataset-task-dependencies.service';
import {LinkedDatasetTaskMetierService} from '@core/services/tasks/projekt/linked-dataset-task-metier.service';
import {ProjektTaskSearchCriteria} from '@core/services/tasks/projekt/projekt-task-search-criteria.interface';
import {RequestDetailDependencies} from '@features/personal-space/pages/request-detail-dependencies';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {BannerButtonComponent} from '@shared/core/banner/banner-button/banner-button.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {TaskDetailHeaderComponent} from '@shared/core/workflow/common/task-detail-header/task-detail-header.component';
import {TaskDetailComponent} from '@shared/core/workflow/common/task-detail/task-detail.component';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {TabContentDirective} from '@shared/utils/directives/tab-content-directive/tab-content.directive';
import {TabsLayoutDirective} from '@shared/utils/directives/tab-layout-directive/tabs-layout.directive';
import {LinkedDataset} from 'micro_service_modules/projekt/projekt-model';

import moment from 'moment';
import {map, tap} from 'rxjs/operators';
import {ProjectDetailComponent} from '../../components/project-detail/project-detail.component';
import {ProjectOwnerDetailComponent} from '../../components/project-owner-detail/project-owner-detail.component';
import {TaskDetailComponent as TaskDetailComponent_1} from '../../components/task-detail/task-detail.component';


@Component({
    selector: 'app-dataset-task-detail',
    templateUrl: './dataset-task-detail.component.html',
    styleUrls: ['./dataset-task-detail.component.scss'],
    imports: [PageComponent, TaskDetailHeaderComponent, TabsComponent, TabComponent, TaskDetailComponent_1, ProjectDetailComponent, BannerButtonComponent, TabsLayoutDirective, TabContentDirective, ProjectOwnerDetailComponent, TranslatePipe]
})
export class DatasetTaskDetailComponent
    extends TaskDetailComponent<LinkedDataset, LinkedDatasetDependencies, LinkedDatasetTask, ProjektTaskSearchCriteria>
    implements OnInit {

    headingLoading: boolean;
    dependencies: RequestDetailDependencies;

    constructor(private readonly route: ActivatedRoute,
                private readonly router: Router,
                private readonly linkedDatasetDependencyFetchers: LinkedDatasetTaskDependencyFetchers,
                private readonly iconRegistry: MatIconRegistry,
                private readonly sanitizer: DomSanitizer,
                protected logger: LogService,
                readonly dialog: MatDialog,
                readonly translateService: TranslateService,
                readonly snackBarService: SnackBarService,
                readonly taskWithDependenciesService: LinkedDatasetTaskDependenciesService,
                readonly linkedDatasetTaskMetierService: LinkedDatasetTaskMetierService
    ) {
        super(dialog, translateService, snackBarService, taskWithDependenciesService, linkedDatasetTaskMetierService, logger);
        iconRegistry.addSvgIcon(
            'request',
            sanitizer.bypassSecurityTrustResourceUrl('assets/icons/key_icon_circle.svg'));
        iconRegistry.addSvgIcon(
            'project',
            sanitizer.bypassSecurityTrustResourceUrl('assets/icons/projet.svg'));
        iconRegistry.addSvgIcon(
            'historical',
            sanitizer.bypassSecurityTrustResourceUrl('assets/icons/historique.svg'));
        iconRegistry.addSvgIcon(
            'restricted-dataset',
            sanitizer.bypassSecurityTrustResourceUrl('assets/icons/jdd_restreint.svg'));
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.taskId = params.taskId;
        });
    }

    set taskId(idTask: string) {
        if (idTask) {
            this.headingLoading = true;
            this.taskWithDependenciesService.getTaskWithDependencies(idTask).pipe(
                tap(taskWithDependencies => this.taskWithDependencies = taskWithDependencies),
                injectDependencies({
                    dataset: this.linkedDatasetDependencyFetchers.dataset,
                    project: this.linkedDatasetDependencyFetchers.project,
                }),
                injectDependencies({
                    ownerInfo: this.linkedDatasetDependencyFetchers.ownerInfo,
                }),
                map(({task, asset, dependencies}) => {
                    return {
                        ownerName: dependencies.ownerInfo.name,
                        ownerEmail: dependencies.project.contact_email,
                        receivedDate: moment(task.updatedDate),
                        datasetTitle: dependencies.dataset.resource_title
                    };
                })
            ).subscribe({
                next: (dependencies: RequestDetailDependencies) => {
                    this.headingLoading = false;
                    this.dependencies = dependencies;
                },
                error: (error) => {
                    this.headingLoading = false;
                    console.error(error);
                }
            });
        }
    }

    protected goBackToList(): Promise<boolean> {
        return this.router.navigate(['/personal-space/my-notifications']);
    }

}
