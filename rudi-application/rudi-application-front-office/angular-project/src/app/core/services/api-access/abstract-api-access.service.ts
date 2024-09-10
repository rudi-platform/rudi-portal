import {TranslateService} from '@ngx-translate/core';
import {AclService} from 'micro_service_modules/acl/acl-api';
import {KonsultService} from 'micro_service_modules/konsult/konsult-api';
import {Project, ProjektService} from 'micro_service_modules/projekt/projekt-api';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {from, Observable} from 'rxjs';
import {filter, map, mergeMap, reduce} from 'rxjs/operators';
import {LinkedDatasetMetadatas} from '../asset/project/project-dependencies.service';
import {LinkWithSubscribability} from './link-with-subscribability';

export abstract class AbstractApiAccessService {

    protected constructor(
        protected readonly aclService: AclService,
        protected readonly konsultService: KonsultService,
        protected readonly projektService: ProjektService,
        protected readonly organizationService: OrganizationService,
        protected readonly translateService: TranslateService
    ) {

    }

    /**
     * Filtre les demandes fournies pour ne récupérer que les demandes concernant des JDDs auxquels on peut souscrire
     * @param linkAndMetadatas l'ensemble des demandes + JDD d'un projet
     * @param project le projet pour checker si on a les droits pour souscrire
     */
    public filterSubscribableMetadatas(linkAndMetadatas: LinkedDatasetMetadatas[], project: Project): Observable<LinkedDatasetMetadatas[]> {
        return from(linkAndMetadatas).pipe(
            mergeMap((linkAndMetadata: LinkedDatasetMetadatas) => {
                return this.projektService.hasAccessToDataset(project.owner_uuid, linkAndMetadata.dataset.global_id).pipe(
                    map((hasAccess: boolean) => new LinkWithSubscribability(linkAndMetadata, hasAccess))
                );
            }),
            filter((linkWithSubscribability: LinkWithSubscribability) => linkWithSubscribability.canSubscribe),
            reduce((filteredLinkAndMetadatas: LinkedDatasetMetadatas[], curentLink: LinkWithSubscribability) => {
                filteredLinkAndMetadatas.push(curentLink.link);
                return filteredLinkAndMetadatas;
            }, [])
        );
    }
}
