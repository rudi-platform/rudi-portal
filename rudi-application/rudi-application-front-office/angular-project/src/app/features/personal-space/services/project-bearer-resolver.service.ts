import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { ProcessHistoricInformation } from 'micro_service_modules/api-bpmn';
import { ProjektService, OwnerInfo, OwnerType, Project } from 'micro_service_modules/projekt/projekt-api';
import { OrganizationService, User as StruktureUser, Organization } from 'micro_service_modules/strukture/api-strukture';
import { AclService } from 'micro_service_modules/acl/acl-api';

export interface ProjectBearer {
    name: string;
    email: string;
}

@Injectable({
    providedIn: 'root'
})
export class ProjectBearerResolverService {

    constructor(
        private readonly projektService: ProjektService,
        private readonly organizationService: OrganizationService,
        private readonly aclService: AclService,
    ) {}

    resolve(processHistoricInformation: ProcessHistoricInformation | null): Observable<ProjectBearer | null> {
        const businessKey = (processHistoricInformation?.businessKey ?? '').trim();
        if (!businessKey) return of(null);

        const processDefinitionKey = (processHistoricInformation?.processDefinitionKey ?? '').trim();

        switch (processDefinitionKey) {
            case 'project-process':
                return this.resolveFromProjectUuid(businessKey);
            case 'new-dataset-request-process':
                return this.projektService.findProjectByNewDatasetRequest(businessKey).pipe(
                    switchMap(project => this.resolveFromProject(project)),
                    catchError(() => of(null)),
                );
            case 'linked-dataset-process':
                return this.resolveFromLinkedDatasetUuid(businessKey);
            case 'organization-process':
            case 'linked-producer-process':
                return this.resolveFromOrganizationUuid(businessKey);
            default:
                return of(null);
        }
    }

    private resolveFromOrganizationUuid(organizationUuid: string): Observable<ProjectBearer | null> {
        return this.organizationService.getOrganization(organizationUuid).pipe(
            switchMap((organization: Organization) =>
                this.organizationService.getOrganizationUserFromOrganizationUuid(organizationUuid).pipe(
                    map((user: StruktureUser) => ({
                        name: (organization?.name ?? '').trim(),
                        email: (user?.login ?? '').trim(),
                    })),
                    catchError(() => of({
                        name: (organization?.name ?? '').trim(),
                        email: '',
                    })),
                )
            ),
            catchError(() => of(null)),
        );
    }

    private resolveFromLinkedDatasetUuid(linkedDatasetUuid: string): Observable<ProjectBearer | null> {
        return this.projektService.searchProjects(
            undefined,
            [linkedDatasetUuid],
            undefined, undefined, undefined, undefined, undefined, undefined, undefined, 0, 1
        ).pipe(
            map(result => result?.elements?.[0] ?? null),
            switchMap((project: Project | null) => {
                if (project) return this.resolveFromProject(project);
                return this.projektService.getLinkedDatasetOwner(linkedDatasetUuid).pipe(
                    map(owner => ({ name: (owner ?? '').trim(), email: '' })),
                );
            }),
            catchError(() => this.projektService.getLinkedDatasetOwner(linkedDatasetUuid).pipe(
                map(owner => ({ name: (owner ?? '').trim(), email: '' })),
                catchError(() => of(null)),
            )),
        );
    }

    private resolveFromProjectUuid(projectUuid: string): Observable<ProjectBearer | null> {
        return this.projektService.getProject(projectUuid).pipe(
            switchMap(project => this.resolveFromProject(project)),
            catchError(() => of(null)),
        );
    }

    private resolveFromProject(project: Project): Observable<ProjectBearer> {
        const fallbackEmail = (project.contact_email ?? '').trim();
        return this.projektService.getOwnerInfo(project.owner_type, project.owner_uuid).pipe(
            switchMap((ownerInfo: OwnerInfo) =>
                this.resolveOwnerEmail(project.owner_type, project.owner_uuid, fallbackEmail).pipe(
                    map(email => ({
                        name: (ownerInfo?.name ?? '').trim(),
                        email,
                    })),
                )
            ),
            catchError(() => of({ name: '', email: fallbackEmail })),
        );
    }

    private resolveOwnerEmail(ownerType: OwnerType, ownerUuid: string, fallbackEmail: string): Observable<string> {
        if (!ownerUuid) return of(fallbackEmail);

        switch (ownerType) {
            case 'USER':
                return this.aclService.getUserInfo(ownerUuid).pipe(
                    map(user => (user?.login ?? '').trim() || fallbackEmail),
                    catchError(() => of(fallbackEmail)),
                );
            case 'ORGANIZATION':
                return this.organizationService.getOrganizationUserFromOrganizationUuid(ownerUuid).pipe(
                    map(user => (user?.login ?? '').trim() || fallbackEmail),
                    catchError(() => of(fallbackEmail)),
                );
            default:
                return of(fallbackEmail);
        }
    }
}
