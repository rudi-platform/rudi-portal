import {NgFor, NgIf} from '@angular/common';
import {Component, OnInit} from '@angular/core';

import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {LogService} from '@core/services/log.service';
import {SelfdataInformationRequestDetailService} from '@core/services/selfdata-information-request-detail.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {
    SelfdataInformationRequestDependencies,
    SelfdataInformationRequestTask,
    SelfdataInformationRequestTaskDependenciesService,
    SelfdataInformationRequestTaskDependencyFetchers
} from '@core/services/tasks/selfdata/selfdata-information-request-task-dependencies.service';
import {SelfdataInformationRequestTaskMetierService} from '@core/services/tasks/selfdata/selfdata-information-request-task-metier.service';
import {SelfdataTaskSearchCriteria} from '@core/services/tasks/selfdata/selfdata-task-search-criteria.interface';
import {RequestDetailDependencies} from '@features/personal-space/pages/request-detail-dependencies';
import {TranslateDirective, TranslatePipe, TranslateService} from '@ngx-translate/core';
import {ContactCardComponent} from '@shared/business/contacts/contact-card/contact-card.component';
import {BannerButtonComponent} from '@shared/core/banner/banner-button/banner-button.component';
import {CardComponent} from '@shared/core/common/card/card.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {Level} from '@shared/core/layout/notification-template/notification-template.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {TaskDetailHeaderComponent} from '@shared/core/workflow/common/task-detail-header/task-detail-header.component';
import {TaskDetailComponent} from '@shared/core/workflow/common/task-detail/task-detail.component';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {TabContentDirective} from '@shared/utils/directives/tab-content-directive/tab-content.directive';
import {TabsLayoutDirective} from '@shared/utils/directives/tab-layout-directive/tabs-layout.directive';
import {Period} from 'micro_service_modules/api-kaccess';
import {Form, SelfdataInformationRequest} from 'micro_service_modules/selfdata/selfdata-api';
import moment from 'moment';
import {map, tap} from 'rxjs/operators';
import {SelfdataMainInformationComponent} from '../../components/selfdata-main-information/selfdata-main-information.component';
import UnitEnum = Period.UnitEnum;


@Component({
    selector: 'app-selfdata-information-request-task-detail',
    templateUrl: './selfdata-information-request-task-detail.component.html',
    styleUrls: ['./selfdata-information-request-task-detail.component.scss'],
    imports: [PageComponent, TaskDetailHeaderComponent, NgIf, TabsComponent, TabComponent, SelfdataMainInformationComponent, NgFor, BannerButtonComponent, TabsLayoutDirective, TabContentDirective, CardComponent, LoaderComponent, TranslateDirective, ContactCardComponent, TranslatePipe]
})
export class SelfdataInformationRequestTaskDetailComponent
    extends TaskDetailComponent<SelfdataInformationRequest, SelfdataInformationRequestDependencies,
        SelfdataInformationRequestTask, SelfdataTaskSearchCriteria>
    implements OnInit {

    dependencies: RequestDetailDependencies;
    taskLoading: boolean;
    formLoading: boolean;
    formToDisplay: Form;

    constructor(private readonly router: Router,
                private readonly route: ActivatedRoute,
                private readonly selfdataInformationRequestDetailService: SelfdataInformationRequestDetailService,
                private readonly selfdataInformationRequestTaskDependencyFetchers: SelfdataInformationRequestTaskDependencyFetchers,
                protected logger: LogService,
                iconRegistryService: IconRegistryService,
                dialog: MatDialog,
                translateService: TranslateService,
                snackBarService: SnackBarService,
                taskWithDependenciesService: SelfdataInformationRequestTaskDependenciesService,
                taskMetierService: SelfdataInformationRequestTaskMetierService,
    ) {
        super(dialog, translateService, snackBarService, taskWithDependenciesService, taskMetierService, logger);
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.getForm(params.taskId);
            this.taskId = params.taskId;
        });
    }

    set taskId(idTask: string) {
        if (idTask) {
            this.taskLoading = true;
            this.taskWithDependenciesService.getTaskWithDependencies(idTask).pipe(
                tap(taskWithDependencies => this.taskWithDependencies = taskWithDependencies),
                injectDependencies({
                    initiatorInfo: this.selfdataInformationRequestTaskDependencyFetchers.initiatorInfo,
                    dataset: this.selfdataInformationRequestTaskDependencyFetchers.dataset,
                }),
                map(({task, asset, dependencies}) => {
                    return {
                        ownerName: dependencies.initiatorInfo,
                        ownerEmail: task.initiator,
                        receivedDate: moment(task.creationDate),
                        datasetTitle: asset.description,
                        taskStatus: asset.functional_status,
                        processDefinitionKey: asset.process_definition_key,
                        expiredDate: moment(
                            this.getSelfDataEndTreatmentDay(
                                dependencies.dataset.ext_metadata.ext_selfdata.ext_selfdata_content.treatment_period, asset.updated_date)),
                    };
                })
            ).subscribe({
                next: (dependencies: RequestDetailDependencies) => {
                    this.taskLoading = false;
                    this.dependencies = dependencies;
                },
                error: (error) => {
                    this.taskLoading = false;
                    console.error(error);
                }
            });
        }
    }


    getForm(idTask: string): void {
        this.formLoading = true;
        this.selfdataInformationRequestDetailService.lookupFilledMatchingDataForm(idTask).subscribe({
            next: (result: Form) => {
                this.formToDisplay = result;
            },
            complete: () => {
                this.formLoading = false;
            },
            error: (e) => {
                this.formLoading = false;
                console.error(e);
                this.snackBarService.openSnackBar({
                    message: this.translateService.instant('error.technicalError'),
                    level: Level.ERROR
                });
            }
        });
    }

    getSelfDataEndTreatmentDay(period: Period, receivedDate: string): string {
        const date = new Date(receivedDate);
        switch (period.unit) {
            case UnitEnum.Days:
                date.setDate(date.getDate() + period.value);
                break;
            case UnitEnum.Months:
                date.setMonth(date.getMonth() + period.value);
                break;
            case UnitEnum.Years:
                date.setFullYear(date.getFullYear() + period.value);
                break;
        }

        return date.toString();
    }

    protected goBackToList(): Promise<boolean> {
        return this.router.navigate(['/personal-space/my-notifications']);
    }
}
