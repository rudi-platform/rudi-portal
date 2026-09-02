import {AsyncPipe} from '@angular/common';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnDestroy, OnInit} from '@angular/core';
import {MatCard, MatCardContent, MatCardTitle} from '@angular/material/card';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {ActivatedRoute} from '@angular/router';
import {ProjektMetierService} from '@core/services/asset/project/projekt-metier.service';
import {HtmlService} from '@core/services/html/html.service';
import {OwnerContactCardComponent} from '@features/personal-space/components/contact-card/owner-contact-card.component';
import {LinkedDatasetInfoComponent} from '@features/personal-space/components/linked-dataset-info/linked-dataset-info.component';
import {
    NewDatasetRequestInfoComponent
} from '@features/personal-space/components/new-dataset-request-info/new-dataset-request-info.component';
import {
    TaskOrganizationInformationComponent
} from '@features/personal-space/components/organization-information/task-organization-information.component';
import {ProjectTaskHistoricComponent} from '@features/personal-space/components/project-task-historic/project-task-historic.component';
import {AssetResolverService, ResolvedAsset} from '@features/personal-space/services/asset-resolver.service';
import {ProjectBearer, ProjectBearerResolverService} from '@features/personal-space/services/project-bearer-resolver.service';
import {TaskHistoryDetailService} from '@features/personal-space/services/task-history-detail.service';
import {ProjectMainInformationsComponent} from '@features/project/components/project-main-informations/project-main-informations.component';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {RelatedOrganizationInfo} from '@shared/business/contacts/owner-card-info/owner-card-info.component';
import {
    NewDatasetRequestTableComponent
} from '@shared/business/projects/projects-datasets-tables/new-dataset-request-table/new-dataset-request-table.component';
import {
    OpenDatasetTableComponent
} from '@shared/business/projects/projects-datasets-tables/open-dataset-table/open-dataset-table.component';
import {
    RestrictedDatasetTableComponent
} from '@shared/business/projects/projects-datasets-tables/restricted-dataset-table/restricted-dataset-table.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {TaskDetailHeaderComponent} from '@shared/core/workflow/common/task-detail-header/task-detail-header.component';
import {TitleIconType} from '@shared/models/title-icon-type';
import {ProcessHistoricInformation} from 'micro_service_modules/api-bpmn';
import {OwnerInfoRequest, OwnerType, Project} from 'micro_service_modules/projekt/projekt-model';
import {Observable, of, Subject} from 'rxjs';
import {catchError, distinctUntilChanged, map, shareReplay, switchMap, takeUntil, tap} from 'rxjs/operators';

@Component({
    selector: 'app-my-task-history-detail',
    templateUrl: './my-task-history-detail.component.html',
    styleUrls: ['./my-task-history-detail.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageComponent,
        TaskDetailHeaderComponent,
        LoaderComponent,
        TranslatePipe,
        ProjectTaskHistoricComponent,
        OwnerContactCardComponent,
        TabsComponent,
        TabComponent,
        ProjectMainInformationsComponent,
        TaskOrganizationInformationComponent,
        LinkedDatasetInfoComponent,
        NewDatasetRequestInfoComponent,
        MatCard,
        MatCardContent,
        MatCardTitle,
        OpenDatasetTableComponent,
        RestrictedDatasetTableComponent,
        NewDatasetRequestTableComponent,
        AsyncPipe,
    ]
})
export class MyTaskHistoryDetailComponent implements OnInit, OnDestroy {

    loading = false;

    processHistoricInformation$: Observable<ProcessHistoricInformation | null>;

    pageTitle: string;
    objectTabLabel: string;
    objectTabIcon: TitleIconType;
    bearer$: Observable<ProjectBearer | null>;
    resolvedAsset$: Observable<ResolvedAsset | null>;
    relatedOrganizations: RelatedOrganizationInfo[] = [];
    resolvedAssetLoading = true;
    processDefinitionKey: string | null = null;
    icon: string;

    private readonly destroyed$ = new Subject<void>();
    private readonly defaultIconKey = 'projectTasksIcon';
    private readonly iconByProcessDefinitionKey: Record<string, string> = {
        'organization-process': 'organizationTasksIcon',
        'linked-producer-process': 'linkedProducerTasksIcon',
        'linked-dataset-process': 'key_icon_88_secondary-color',
        'project-process': 'projectTasksIcon',
        'new-dataset-request-process': 'newDatasetRequestTasksIcon',
        'selfdata-information-request-process': 'selfdataTasksIcon',
        'selfdata-process': 'selfdataTasksIcon',
    };
    private readonly defaultPageTitleKey = 'personalSpace.projectDetails.headerTitlePublication';
    private readonly defaultObjectTabLabelKey = 'personalSpace.taskHistoryDetail.objectTab.project';
    private readonly defaultObjectTabIcon: TitleIconType = 'icon-reutilisation';
    private readonly objectTabIconByProcessDefinitionKey: Record<string, TitleIconType> = {
        'project-process': 'icon-reutilisation',
        'organization-process': 'organizationTabIcon',
        'linked-producer-process': 'organizationTabIcon',
        'linked-dataset-process': 'key_icon_circle',
        'new-dataset-request-process': 'icon-new-dataset-request',
        'selfdata-information-request-process': 'icone_donnees_personnelles',
        'selfdata-process': 'icone_donnees_personnelles',
    };
    private readonly objectTabLabelByProcessDefinitionKey: Record<string, string> = {
        'project-process': 'personalSpace.taskHistoryDetail.objectTab.project',
        'organization-process': 'personalSpace.taskHistoryDetail.objectTab.organization',
        'linked-producer-process': 'personalSpace.taskHistoryDetail.objectTab.linkedProducer',
        'linked-dataset-process': 'personalSpace.taskHistoryDetail.objectTab.linkedDataset',
        'new-dataset-request-process': 'personalSpace.taskHistoryDetail.objectTab.newDatasetRequest',
        'selfdata-information-request-process': 'personalSpace.taskHistoryDetail.objectTab.selfdata',
        'selfdata-process': 'personalSpace.taskHistoryDetail.objectTab.selfdata',
    };
    private readonly pageTitleByProcessDefinitionKey: Record<string, (info: ProcessHistoricInformation | null) => string> = {
        // Réutilisations
        'project-process': () => this.translateService.instant(this.defaultPageTitleKey),

        // Organisations
        'organization-process': info => this.isOrganizationArchive(info)
            ? this.translateService.instant('personalSpace.organizationDetails.archive.task.title')
            : this.translateService.instant('personalSpace.organizationDetails.declarationTaskTitle'),

        'linked-producer-process': info => this.isLinkedProducerDetachment(info)
            ? this.translateService.instant('personalSpace.linkedProducerDetails.detachement')
            : this.translateService.instant('personalSpace.linkedProducerDetails.rattachement'),

        // Jeux de donnees a acces restreint
        'linked-dataset-process': () => this.translateService.instant('personalSpace.requestDetail.accessRequest'),

        // Demande de nouvelles donnees
        'new-dataset-request-process': () => this.translateService.instant('personalSpace.requestDetail.details.new-data'),

        // Selfdata (hors liste fournie, mais on evite de garder un mauvais titre)
        'selfdata-information-request-process': () => this.translateService.instant('metaData.selfdataInformationRequest.consultation.pageTitle'),
        'selfdata-process': () => this.translateService.instant('metaData.selfdataInformationRequest.consultation.pageTitle'),
    };

    private readonly cdr = inject(ChangeDetectorRef);
    private readonly taskHistoryDetailService = inject(TaskHistoryDetailService);
    private readonly projectBearerResolverService = inject(ProjectBearerResolverService);
    private readonly assetResolverService = inject(AssetResolverService);
    private readonly projektMetierService = inject(ProjektMetierService);
    private readonly route = inject(ActivatedRoute);
    private readonly translateService = inject(TranslateService);
    private readonly htmlService = inject(HtmlService);
    private readonly iconRegistry = inject(MatIconRegistry);
    private readonly sanitizer = inject(DomSanitizer);

    constructor() {
        // Default title, will be updated once the historic is loaded.
        this.pageTitle = this.translateService.instant(this.defaultPageTitleKey);


        // Reuse the same icons as MyTasksHistoriesTab so the header is consistent.
        this.iconRegistry.addSvgIcon('organizationTasksIcon', this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/organization_definition_key.svg'));
        this.iconRegistry.addSvgIcon('organizationTabIcon', this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/organization_definition_key_tab.svg'));
        this.iconRegistry.addSvgIcon('linkedProducerTasksIcon', this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/organization_definition_key.svg'));
        this.iconRegistry.addSvgIcon('projectTasksIcon', this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/project_definition_key.svg'));
        this.iconRegistry.addSvgIcon('selfdataTasksIcon', this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/self_data_icon_definition_key.svg'));
        this.iconRegistry.addSvgIcon('newDatasetRequestTasksIcon', this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/nouvelles_donnees_definition_key.svg'));
        this.iconRegistry.addSvgIcon('key_icon_88_secondary-color', this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/key_icon_88_secondary-color.svg'));
        this.iconRegistry.addSvgIcon('key_icon_circle', this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/key_icon_circle.svg'));

        this.processHistoricInformation$ = of(null);
        this.icon = this.defaultIconKey;
        this.bearer$ = of(null);
        this.resolvedAsset$ = of(null);
    }

    ngOnInit(): void {
        const processHistoricInformation$ = this.route.paramMap.pipe(
            map(paramMap => ({
                historicId: paramMap.get('historicId'),
            })),
            tap(() => {
                this.loading = true;
            }),
            switchMap(({historicId}) => {
                if (!historicId) {
                    return of(null);
                }

                const clean = (value: ProcessHistoricInformation | null): ProcessHistoricInformation | null => {
                    if (value?.description) {
                        value.description = this.htmlService.stripHtml(value.description);
                    }
                    return value;
                };

                // 1) Priorité: données passées via navigation state au clic sur le tableau
                const state: any = (globalThis as any)?.history?.state;
                const fromState: ProcessHistoricInformation | undefined = state?.processHistoricInformation;
                if (fromState?.id === historicId) {
                    return of(clean(fromState));
                }

                // 2) on cherche l'id dans les historics de chaque microservice (short-circuit dès que trouvé)
                return this.taskHistoryDetailService.findHistoricAcrossServices(historicId).pipe(map(clean));
            }),
            tap((value) => {
                this.processDefinitionKey = value?.processDefinitionKey ?? null;
                this.icon = this.getIcon(this.processDefinitionKey);
                this.pageTitle = this.getPageTitle(value);
                this.objectTabLabel = this.getObjectTabLabel(this.processDefinitionKey);
                this.objectTabIcon = this.getObjectTabIcon(this.processDefinitionKey);
                this.loading = false;
                this.cdr.markForCheck();
            }),
            catchError(() => {
                this.loading = false;
                this.cdr.markForCheck();
                return of(null);
            }),
            takeUntil(this.destroyed$),
            shareReplay({bufferSize: 1, refCount: true}),
        );
        this.processHistoricInformation$ = processHistoricInformation$;
        this.bearer$ = processHistoricInformation$.pipe(
            switchMap(info => this.projectBearerResolverService.resolve(info)),
            catchError(() => of(null)),
            distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
            shareReplay({bufferSize: 1, refCount: true})
        );
        this.resolvedAssetLoading = true;
        this.resolvedAsset$ = processHistoricInformation$.pipe(
            switchMap(info => this.assetResolverService.resolve(info)),
            switchMap(resolvedAsset => this.loadRelatedOrganizations(resolvedAsset).pipe(
                map(() => resolvedAsset)
            )),
            catchError(() => {
                this.relatedOrganizations = [];
                return of(null);
            }),
            tap(() => {
                this.resolvedAssetLoading = false;
                this.cdr.markForCheck();
            }),
            shareReplay({bufferSize: 1, refCount: true})
        );
    }

    ngOnDestroy(): void {
        this.destroyed$.next();
        this.destroyed$.complete();
    }

    private getIcon(processDefinitionKey: string | null): string {
        if (!processDefinitionKey) {
            return this.defaultIconKey;
        }

        return this.iconByProcessDefinitionKey[processDefinitionKey] ?? this.defaultIconKey;
    }

    private getPageTitle(processHistoricInformation: ProcessHistoricInformation | null): string {
        const processDefinitionKey = processHistoricInformation?.processDefinitionKey ?? null;
        if (!processDefinitionKey) {
            return this.translateService.instant(this.defaultPageTitleKey);
        }

        return this.pageTitleByProcessDefinitionKey[processDefinitionKey]?.(processHistoricInformation)
            ?? this.translateService.instant(this.defaultPageTitleKey);
    }

    private getObjectTabLabel(processDefinitionKey: string | null): string {
        const key = processDefinitionKey
            ? (this.objectTabLabelByProcessDefinitionKey[processDefinitionKey] ?? this.defaultObjectTabLabelKey)
            : this.defaultObjectTabLabelKey;
        return this.translateService.instant(key);
    }

    private getObjectTabIcon(processDefinitionKey: string | null): TitleIconType {
        if (!processDefinitionKey) {
            return this.defaultObjectTabIcon;
        }
        return this.objectTabIconByProcessDefinitionKey[processDefinitionKey] ?? this.defaultObjectTabIcon;
    }

    private isOrganizationArchive(processHistoricInformation: ProcessHistoricInformation | null): boolean {
        return this.hasAnyHistoricMatch(processHistoricInformation, /archiv/i);
    }

    private isLinkedProducerDetachment(processHistoricInformation: ProcessHistoricInformation | null): boolean {
        return this.hasAnyHistoricMatch(processHistoricInformation, /detach/i);
    }

    private hasAnyHistoricMatch(processHistoricInformation: ProcessHistoricInformation | null, matcher: RegExp): boolean {
        const historicInformations = processHistoricInformation?.historicInformations ?? [];
        return historicInformations.some(h => {
            const haystack = `${h?.activityName ?? ''} ${h?.action ?? ''}`.toLowerCase();
            return matcher.test(haystack);
        });
    }

    private loadRelatedOrganizations(resolvedAsset: ResolvedAsset | null): Observable<RelatedOrganizationInfo[]> {
        const project = this.extractProjectFromResolvedAsset(resolvedAsset);
        const relatedOrganizations = project?.related_organizations;

        if (!relatedOrganizations?.length) {
            this.relatedOrganizations = [];
            return of([]);
        }

        const ownerInfoRequests: OwnerInfoRequest[] = relatedOrganizations.map(relatedOrganization => ({
            owner_uuid: relatedOrganization.organization_uuid,
            owner_type: OwnerType.Organization
        }) as OwnerInfoRequest);

        return this.projektMetierService.getOwnersInfos(ownerInfoRequests).pipe(
            map(ownerInfos => ownerInfos.map(ownerInfo => ({
                name: ownerInfo.name,
                adminEmail: ownerInfo.contact,
            }))),
            tap(relatedOrganizationsInfo => {
                this.relatedOrganizations = relatedOrganizationsInfo;
            }),
            catchError(() => {
                this.relatedOrganizations = [];
                return of([]);
            })
        );
    }

    private extractProjectFromResolvedAsset(resolvedAsset: ResolvedAsset | null): Project | null {
        if (!resolvedAsset) {
            return null;
        }

        if (resolvedAsset.type === 'project' || resolvedAsset.type === 'linked-dataset' || resolvedAsset.type === 'new-dataset-request') {
            return resolvedAsset.project;
        }

        return null;
    }
}
