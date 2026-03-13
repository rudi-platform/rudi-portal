import {AsyncPipe} from '@angular/common';
import {Component, OnDestroy, OnInit, inject} from '@angular/core';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {ActivatedRoute} from '@angular/router';
import {HtmlService} from '@core/services/html/html.service';
import {TaskHistoryDetailService} from '@features/personal-space/services/task-history-detail.service';
import {ProcessHistoricInformation} from 'micro_service_modules/api-bpmn';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {TaskDetailHeaderComponent} from '@shared/core/workflow/common/task-detail-header/task-detail-header.component';
import {ProjectTaskHistoricComponent} from '@features/personal-space/components/project-task-historic/project-task-historic.component';
import {OwnerInformationComponent} from '@features/personal-space/components/owner-information/owner-information.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {Subject, Observable, of} from 'rxjs';
import {catchError, map, switchMap, takeUntil, tap, shareReplay, distinctUntilChanged} from 'rxjs/operators';
import {ProjectBearerResolverService, ProjectBearer} from '@features/personal-space/services/project-bearer-resolver.service';

@Component({
    selector: 'app-my-task-history-detail',
    templateUrl: './my-task-history-detail.component.html',
    styleUrls: ['./my-task-history-detail.component.scss'],
    standalone: true,
    imports: [
        PageComponent,
        TaskDetailHeaderComponent,
        LoaderComponent,
        AsyncPipe,
        TranslatePipe,
        ProjectTaskHistoricComponent,
        OwnerInformationComponent,
        TabsComponent,
        TabComponent,
    ]
})
export class MyTaskHistoryDetailComponent implements OnInit, OnDestroy {

    loading = false;

    processHistoricInformation$: Observable<ProcessHistoricInformation | null>;

    pageTitle: string;
    bearer$: Observable<ProjectBearer | null>;
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

    private readonly taskHistoryDetailService = inject(TaskHistoryDetailService);
    private readonly projectBearerResolverService = inject(ProjectBearerResolverService);

    constructor(
        private readonly route: ActivatedRoute,
        private readonly translateService: TranslateService,
        private readonly htmlService: HtmlService,
        iconRegistry: MatIconRegistry,
        sanitizer: DomSanitizer,
    ) {
        // Default title, will be updated once the historic is loaded.
        this.pageTitle = this.translateService.instant(this.defaultPageTitleKey);


        // Reuse the same icons as MyTasksHistoriesTab so the header is consistent.
        iconRegistry.addSvgIcon('organizationTasksIcon', sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/organization_definition_key.svg'));
        iconRegistry.addSvgIcon('linkedProducerTasksIcon', sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/organization_definition_key.svg'));
        iconRegistry.addSvgIcon('projectTasksIcon', sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/project_definition_key.svg'));
        iconRegistry.addSvgIcon('selfdataTasksIcon', sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/self_data_icon_definition_key.svg'));
        iconRegistry.addSvgIcon('newDatasetRequestTasksIcon', sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/nouvelles_donnees_definition_key.svg'));
        iconRegistry.addSvgIcon('key_icon_88_secondary-color', sanitizer.bypassSecurityTrustResourceUrl('assets/icons/key_icon_88_secondary-color.svg'));

        this.processHistoricInformation$ = of(null);
        this.icon = this.defaultIconKey;
        this.bearer$ = of(null);
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
                if (fromState?.id === historicId ) {
                    return of(clean(fromState));
                }

                // 2) on cherche l'id dans les historics de chaque microservice (short-circuit dès que trouvé)
                return this.taskHistoryDetailService.findHistoricAcrossServices(historicId).pipe(map(clean));
            }),
            tap((value) => {
                this.icon = this.getIcon(value?.processDefinitionKey ?? null);
                this.pageTitle = this.getPageTitle(value);
                this.loading = false;
            }),
            catchError(() => {
                this.loading = false;
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
}
