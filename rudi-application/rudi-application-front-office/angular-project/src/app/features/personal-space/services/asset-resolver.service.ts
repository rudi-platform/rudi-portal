import {Injectable} from '@angular/core';
import {Observable, of, forkJoin} from 'rxjs';
import {catchError, map, switchMap} from 'rxjs/operators';
import {ProcessHistoricInformation} from 'micro_service_modules/api-bpmn';
import {ProjektService, LinkedDataset, NewDatasetRequest} from 'micro_service_modules/projekt/projekt-api';
import {Project} from 'micro_service_modules/projekt/projekt-model';
import {OrganizationService, Organization, LinkedProducersService} from 'micro_service_modules/strukture/api-strukture';
import {ProjectConsultationService} from '@core/services/asset/project/project-consultation.service';
import {ProjektMetierService} from '@core/services/asset/project/projekt-metier.service';
import {LinkedDatasetMetadatas} from '@core/services/asset/project/project-dependencies.service';

export type ResolvedAsset =
    | {type: 'project'; project: Project; openLinkedDatasets: LinkedDatasetMetadatas[]; restrictedLinkedDatasets: LinkedDatasetMetadatas[]; newDatasetsRequest: NewDatasetRequest[]}
    | {type: 'organization'; organization: Organization}
    | {type: 'linked-dataset'; project: Project; projectLogo: string; linkedDataset: LinkedDataset}
    | {type: 'new-dataset-request'; project: Project; projectLogo: string; newDatasetRequest: NewDatasetRequest}
    ;

@Injectable({
    providedIn: 'root'
})
export class AssetResolverService {

    constructor(
        private readonly projektService: ProjektService,
        private readonly organizationService: OrganizationService,
        private readonly linkedProducersService: LinkedProducersService,
        private readonly projectConsultationService: ProjectConsultationService,
        private readonly projektMetierService: ProjektMetierService,
    ) {}

    resolve(info: ProcessHistoricInformation | null): Observable<ResolvedAsset | null> {
        const businessKey = (info?.businessKey ?? '').trim();
        if (!businessKey) return of(null);

        switch (info?.processDefinitionKey) {
            case 'project-process':
                return this.resolveProject(businessKey);
            case 'organization-process':
                return this.resolveOrganization(businessKey);
            case 'linked-producer-process':
                return this.resolveLinkedProducer(businessKey);
            case 'linked-dataset-process':
                return this.resolveLinkedDataset(businessKey);
            case 'new-dataset-request-process':
                return this.resolveNewDatasetRequest(businessKey);
            default:
                return of(null);
        }
    }

    private resolveProject(projectUuid: string): Observable<ResolvedAsset | null> {
        return this.projektService.getProject(projectUuid).pipe(
            switchMap(project => forkJoin({
                openLinkedDatasets: this.projectConsultationService.getOpenedLinkedDatasetsMetadata(projectUuid).pipe(
                    catchError(() => of([]))),
                restrictedLinkedDatasets: this.projectConsultationService.getRestrictedLinkedDatasetsMetadata(projectUuid).pipe(
                    catchError(() => of([]))),
                newDatasetsRequest: this.projectConsultationService.getNewDatasetsRequest(projectUuid).pipe(
                    catchError(() => of([]))),
            }).pipe(
                map(({openLinkedDatasets, restrictedLinkedDatasets, newDatasetsRequest}) => ({
                    type: 'project' as const, project, openLinkedDatasets, restrictedLinkedDatasets, newDatasetsRequest,
                })),
            )),
            catchError(() => of(null)),
        );
    }

    private resolveOrganization(organizationUuid: string): Observable<ResolvedAsset | null> {
        return this.organizationService.getOrganization(organizationUuid).pipe(
            map(organization => ({type: 'organization' as const, organization})),
            catchError(() => of(null)),
        );
    }

    private resolveLinkedProducer(linkedProducerUuid: string): Observable<ResolvedAsset | null> {
        return this.linkedProducersService.getLinkedProducer(linkedProducerUuid).pipe(
            map(linkedProducer => {
                const organization = linkedProducer?.organization;
                if (!organization) return null;
                return {type: 'organization' as const, organization};
            }),
            catchError(() => of(null)),
        );
    }

    private resolveLinkedDataset(linkedDatasetUuid: string): Observable<ResolvedAsset | null> {
        return this.projektService.searchProjects(
            undefined, [linkedDatasetUuid],
            undefined, undefined, undefined, undefined, undefined, undefined, undefined, 0, 1,
        ).pipe(
            map(result => result?.elements?.[0] ?? null),
            switchMap((project: Project | null) => {
                if (!project) return of(null);
                return forkJoin({
                    linkedDataset: this.projektService.getLinkedDataset(project.uuid, linkedDatasetUuid),
                    projectLogo: this.projektMetierService.getProjectLogo(project.uuid).pipe(catchError(() => of(''))),
                }).pipe(
                    map(({linkedDataset, projectLogo}) => ({type: 'linked-dataset' as const, project, projectLogo, linkedDataset})),
                );
            }),
            catchError(() => of(null)),
        );
    }

    private resolveNewDatasetRequest(newDatasetRequestUuid: string): Observable<ResolvedAsset | null> {
        return this.projektService.findProjectByNewDatasetRequest(newDatasetRequestUuid).pipe(
            switchMap(project =>
                forkJoin({
                    newDatasetRequest: this.projektService.getNewDatasetRequestByUuid(project.uuid, newDatasetRequestUuid),
                    projectLogo: this.projektMetierService.getProjectLogo(project.uuid).pipe(catchError(() => of(''))),
                }).pipe(
                    map(({newDatasetRequest, projectLogo}) => ({type: 'new-dataset-request' as const, project, projectLogo, newDatasetRequest})),
                )
            ),
            catchError(() => of(null)),
        );
    }
}
