import {AsyncPipe} from '@angular/common';
import {Component, OnInit, signal} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle} from '@angular/material/expansion';
import {ActivatedRoute, Router} from '@angular/router';
import {AttachmentService} from '@core/services/attachment.service';
import {Base64EncodedLogo, ImageLogoService} from '@core/services/image-logo.service';
import {LogService} from '@core/services/log.service';
import {OrganizationAttachmentService} from '@core/services/organization-attachment.service';
import {PageTitleService} from '@core/services/page-title.service';
import {ProcessDefinitionsKeyIconRegistryService} from '@core/services/process-definitions-key-icon-registry.service';
import {ProducersMetierService} from '@core/services/producers-metier.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {
    OrganizationDependencies,
    OrganizationTask,
    OrganizationTaskDependenciesService,
    OrganizationTaskDependencyFetchers
} from '@core/services/tasks/strukture/organization/organization-task-dependencies.service';
import {OrganizationTaskMetierService} from '@core/services/tasks/strukture/organization/organization-task-metier.service';
import {OrganizationTaskSearchCriteria} from '@core/services/tasks/strukture/organization/organization-task-search-criteria.interface';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {BannerButtonComponent} from '@shared/core/banner/banner-button/banner-button.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {TaskDetailHeaderComponent} from '@shared/core/workflow/common/task-detail-header/task-detail-header.component';
import {TaskDetailComponent} from '@shared/core/workflow/common/task-detail/task-detail.component';
import {WorkflowExpansionComponent} from '@shared/core/workflow/workflow-expansion/workflow-expansion.component';
import {PROCESS_DEFINITION_KEY_TYPES} from '@shared/models/title-icon-type';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {ProjectStatus, Section, Task} from 'micro_service_modules/projekt/projekt-api';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {Organization, OrganizationStatus, OwnerInfo} from 'micro_service_modules/strukture/strukture-model';
import {Observable, of} from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';
import {OwnerContactCardComponent} from 'src/app/features/personal-space/components/contact-card/owner-contact-card.component';
import {
    TaskOrganizationInformationComponent
} from 'src/app/features/personal-space/components/organization-information/task-organization-information.component';

@Component({
    selector: 'app-organization-task-detail',
    templateUrl: './organization-task-detail.component.html',
    styleUrls: ['./organization-task-detail.component.scss'],
    imports: [PageComponent, TaskDetailHeaderComponent, TabsComponent,
        TabComponent, MatAccordion, MatExpansionPanel,
        MatExpansionPanelHeader, MatExpansionPanelTitle, TaskOrganizationInformationComponent,
        OwnerContactCardComponent, BannerButtonComponent, WorkflowExpansionComponent, TranslatePipe, AsyncPipe],
    providers: [{provide: AttachmentService, useExisting: OrganizationAttachmentService}]
})
export class OrganizationTaskDetailComponent
    extends TaskDetailComponent<Organization, OrganizationDependencies, OrganizationTask, OrganizationTaskSearchCriteria>
    implements OnInit {

    isLoading: boolean;
    dependencies: OrganizationDependencies;
    idTask: string;
    currentTask: Task;
    hasSections = false;
    isDeclaration = false;
    ownerInfo: Observable<OwnerInfo>;
    headerLibelle: string;
    currentOrganizationImageBase64: Base64EncodedLogo;
    protected readonly ProjectStatus = ProjectStatus;
    readonly panelInitialTaskOpenState = signal(false);

    constructor(
        readonly dialog: MatDialog,
        readonly translateService: TranslateService,
        readonly snackBarService: SnackBarService,
        readonly taskWithDependenciesService: OrganizationTaskDependenciesService,
        readonly organizationTaskMetierService: OrganizationTaskMetierService,
        readonly organizationService: OrganizationService,
        protected logger: LogService,
        private readonly route: ActivatedRoute,
        private readonly pageTitleService: PageTitleService,
        readonly organizationTaskDependencyFetchers: OrganizationTaskDependencyFetchers,
        private readonly processDefinitionsKeyIconRegistryService: ProcessDefinitionsKeyIconRegistryService,
        private readonly router: Router,
        private readonly organizationAttachmentService: OrganizationAttachmentService,
        private readonly imageLogoService: ImageLogoService,
        private readonly producersMetierService: ProducersMetierService
    ) {
        super(dialog, translateService, snackBarService, taskWithDependenciesService, organizationTaskMetierService, logger);
        this.processDefinitionsKeyIconRegistryService.addAllSvgIcons(PROCESS_DEFINITION_KEY_TYPES);
        this.headerLibelle = this.translateService.instant('personalSpace.organizationDetails.declaration');
    }

    set taskId(idTask: string) {
        if (idTask) {
            this.isLoading = true;
            this.idTask = idTask;

            this.organizationTaskMetierService.getTask(this.idTask).subscribe({
                next: (task: Task) => {
                    this.currentTask = task;
                    this.hasSections = !!task?.asset?.form?.sections;
                },
                error: (err) => this.logger.error('Error fetching task:', err)
            });
            this.taskWithDependenciesService.getTaskWithDependencies(idTask).pipe(
                tap(taskWithDependencies => {

                    const isArchive = taskWithDependencies.task?.asset?.form?.sections.some(section => section?.fields.some(field => field?.definition.name === 'organizationArchiveMode'));
                    const taskValidated = taskWithDependencies.asset.organizationStatus === OrganizationStatus.Validated;

                    // Une demande de déclaration (création) concerne une organisation pas encore validée :
                    // dans ce cas on n'affiche que le récapitulatif "Informations de l'organisation".
                    this.isDeclaration = !taskValidated;

                    if (taskValidated) {
                        this.headerLibelle = isArchive ?
                            this.translateService.instant('personalSpace.organizationDetails.archive.task.title') :
                            this.translateService.instant('personalSpace.organizationDetails.update.task.title');
                    }


                    this.taskWithDependencies = taskWithDependencies;
                }),
                injectDependencies({
                    organization: this.organizationTaskDependencyFetchers.organization,
                    userInfo: this.organizationTaskDependencyFetchers.userInfo
                }),
                map(({dependencies}) => {
                    return {
                        organization: dependencies.organization,
                        userInfo: dependencies.userInfo
                    };
                })
            ).subscribe({
                next: (dependencies: OrganizationDependencies) => {
                    this.dependencies = dependencies;
                    // On définit ici le titre de l'onglet en se basant sur le name de l'organization
                    this.pageTitleService.setPageTitle(this.dependencies.organization.name);
                    // On récupère les informations du Owner
                    this.getOwnerInfo(this.dependencies.organization.uuid);
                    // On charge le logo actuel de l'organisation (isolé : une absence de logo ne doit pas casser l'affichage)
                    this.loadCurrentOrganizationLogo(this.dependencies.organization.uuid);

                    this.isLoading = false;
                },
                error: (error) => {
                    this.isLoading = false;
                    this.logger.error(error);
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
        this.ownerInfo = this.organizationService.getOrganizationOwnerInfo(uuid);
    }

    /**
     * Une section est affichable si elle contient au moins un champ non masqué avec une valeur renseignée.
     */
    isSectionVisible(section: Section): boolean {
        return section?.fields?.some(field =>
            field.definition?.type !== 'HIDDEN' && field.values?.some(value => value != null && value.trim() !== '')
        ) ?? false;
    }

    /**
     * Organisation à afficher dans le récapitulatif.
     * Pour une demande de déclaration (création), l'organisation n'est pas encore persistée avec les
     * coordonnées saisies : on reporte donc les valeurs renseignées dans le formulaire de la tâche
     * (email, url, téléphone, adresse...) sur l'organisation affichée.
     */
    get displayedOrganization(): Organization {
        const organization = {...this.dependencies?.organization};
        const orga = {...this.dependencies?.organization} as Organization;
        // if (this.isDeclaration) {
        //     const target = organization as unknown as Record<string, string>;
        //     this.currentTask?.asset?.form?.sections?.forEach(section =>
        //         section?.fields?.forEach(field => {
        //             const fieldName = field?.definition?.name;
        //             const value = field?.values?.find(fieldValue => fieldValue != null && fieldValue.trim() !== '');
        //             if (fieldName && value != null) {
        //                 target[fieldName] = value;
        //             }
        //         })
        //     );
        // }
        console.log('OrganizationTaskDetail', 'organization', organization);
        console.log('OrganizationTaskDetail', 'organization.addresses', organization.addresses);
        console.log('OrganizationTaskDetail', 'orga', orga);
        console.log('OrganizationTaskDetail', 'orga.addresses', orga.addresses);
        return organization;
    }

    protected goBackToList(): Promise<boolean> {
        return this.router.navigate(['/personal-space/my-notifications']);
    }

    /**
     * Charge le logo actuel de l'organisation (image stockée avant modification).
     * Isolé avec catchError : une organisation sans logo ne doit pas casser l'affichage des autres informations.
     */
    private loadCurrentOrganizationLogo(organizationUuid: string): void {
        this.producersMetierService.getLogo(organizationUuid).pipe(
            catchError(() => of(null))
        ).subscribe({
            next: (logo: string) => this.currentOrganizationImageBase64 = logo,
            error: () => this.currentOrganizationImageBase64 = null
        });
    }
}
