import {Component, OnInit} from '@angular/core';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {ProjectTaskMetierService} from '@core/services/tasks/projekt/project-task-metier.service';
import {SelfdataInformationRequestTaskMetierService} from '@core/services/tasks/selfdata/selfdata-information-request-task-metier.service';
import {OrganizationTaskMetierService} from '@core/services/tasks/strukture/organization/organization-task-metier.service';
import {
    MyTasksHistoriesTableComponent
} from '@features/personal-space/components/my-tasks-histories-tables/my-tasks-histories-table.component';
import {SharedModule} from '@shared/shared.module';
import {ProcessHistoricInformation} from 'micro_service_modules/api-bpmn';

@Component({
    selector: 'app-my-tasks-histories-tab',
    imports: [
        SharedModule,
        MyTasksHistoriesTableComponent
    ],
    templateUrl: './my-tasks-histories-tab.component.html',
    styleUrl: './my-tasks-histories-tab.component.scss'
})
export class MyTasksHistoriesTabComponent implements OnInit {

    organizationTasksHistories: ProcessHistoricInformation[];
    linkedProducerTasksHistories: ProcessHistoricInformation[];
    linkedDatasetTasksHistories: ProcessHistoricInformation[];
    projectTasksHistories: ProcessHistoricInformation[];
    selfdataTasksHistories: ProcessHistoricInformation[];
    newDatasetRequestTasksHistories: ProcessHistoricInformation[];

    organizationTasksIcon: string = 'organizationTasksIcon';
    linkedProducerTasksIcon: string = 'linkedProducerTasksIcon';
    linkedDatasetTasksIcon: string = 'key_icon_88_secondary-color';
    projectTasksIcon: string = 'projectTasksIcon';
    selfdataTasksIcon: string = 'selfdataTasksIcon';
    newDatasetRequestTasksIcon: string = 'newDatasetRequestTasksIcon';

    isStruktureLoading: boolean = false;
    isProjektLoading: boolean = false;
    isSelfdataLoading: boolean = false;

    hasData: boolean = false;

    constructor(
        public readonly projektTaskMetierService: ProjectTaskMetierService, // Un seul suffit pour récupérer toutes les informations de son microservice
        public readonly struktureTaskMetierService: OrganizationTaskMetierService, // Un seul suffit pour récupérer toutes les informations de son microservice
        public readonly selfdataTaskMetierService: SelfdataInformationRequestTaskMetierService, // Un seul suffit pour récupérer toutes les informations de son microservice
        private readonly iconRegistry: MatIconRegistry,
        private readonly sanitizer: DomSanitizer,
    ) {
        iconRegistry.addSvgIcon(this.organizationTasksIcon, sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/organization_definition_key.svg'));
        iconRegistry.addSvgIcon(this.linkedProducerTasksIcon, sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/organization_definition_key.svg'));
        iconRegistry.addSvgIcon(this.projectTasksIcon, sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/project_definition_key.svg'));
        iconRegistry.addSvgIcon(this.selfdataTasksIcon, sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/self_data_icon_definition_key.svg'));
        iconRegistry.addSvgIcon(this.newDatasetRequestTasksIcon, sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/nouvelles_donnees_definition_key.svg'));

    }

    ngOnInit(): void {
        this.isStruktureLoading = true;
        this.isProjektLoading = true;
        this.isSelfdataLoading = true;

        this.struktureTaskMetierService.getMyHistoricInformations().subscribe({
            next: (values: ProcessHistoricInformation[]) => {
                if (values.length > 0) {
                    this.hasData = true;
                }
                this.linkedProducerTasksHistories = values.filter(value => value.processDefinitionKey === 'linked-producer-process');
                this.organizationTasksHistories = values.filter(value => value.processDefinitionKey === 'organization-process');
            },
            complete: () => {
                this.isStruktureLoading = false;
            }
        });

        this.projektTaskMetierService.getMyHistoricInformations().subscribe({
            next: (values: ProcessHistoricInformation[]) => {
                if (values.length > 0) {
                    this.hasData = true;
                }
                this.projectTasksHistories = values.filter(value => value.processDefinitionKey === 'project-process');
                this.linkedDatasetTasksHistories = values.filter(value => value.processDefinitionKey === 'linked-dataset-process');
                this.newDatasetRequestTasksHistories = values.filter(value => value.processDefinitionKey === 'new-dataset-request-process');
            },
            complete: () => {
                this.isProjektLoading = false;
            }
        });

        this.selfdataTaskMetierService.getMyHistoricInformations().subscribe({
            next: (values: ProcessHistoricInformation[]) => {
                if (values.length > 0) {
                    this.hasData = true;
                }
                this.selfdataTasksHistories = values;
            },
            complete: () => {
                this.isSelfdataLoading = false;
            }
        });
    }
}
