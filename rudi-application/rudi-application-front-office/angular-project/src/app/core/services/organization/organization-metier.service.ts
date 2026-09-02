import {Injectable} from '@angular/core';
import {AddressRoleSearchCriteria} from '@core/bean/strukture/address-role-search-criteria';
import {UserService} from '@core/services/user.service';
import {PageResultUtils} from '@shared/utils/page-result-utils';
import {KindOfData} from 'micro_service_modules/api-kmedia';
import {OrganizationService, StruktureService} from 'micro_service_modules/strukture/api-strukture';
import {
    Organization,
    OrganizationMember,
    OrganizationMemberType,
    OwnerInfo,
    PagedOrganizationUserMembers
} from 'micro_service_modules/strukture/strukture-model';
import {Observable, of} from 'rxjs';
import {shareReplay, switchMap} from 'rxjs/operators';
import {Base64EncodedLogo, ImageLogoService} from '../image-logo.service';

@Injectable({
    providedIn: 'root'
})
export abstract class OrganizationMetierService {

    protected constructor(
        protected imageLogoService: ImageLogoService,
        protected organizationService: OrganizationService,
        protected userService: UserService,
        protected strukureService: StruktureService,
    ) {
    }

    private readonly logosByOrganizationId: { [key: string]: Observable<Base64EncodedLogo> } = {};

    getLogo(organizationId: string): Observable<Base64EncodedLogo> {
        if (this.logosByOrganizationId[organizationId]) {
            return this.logosByOrganizationId[organizationId];
        }

        this.logosByOrganizationId[organizationId] = this.downloadProducerMediaByType(organizationId, KindOfData.Logo).pipe(
            // Source pour la gestion du cache : https://betterprogramming.pub/how-to-create-a-caching-service-for-angular-bfad6cbe82b0
            shareReplay(1),
            switchMap(blob => this.imageLogoService.createImageFromBlob(blob))
        );

        return this.logosByOrganizationId[organizationId];
    }

    protected abstract downloadProducerMediaByType(organizationId: string, kindOfData: KindOfData): Observable<Blob>;

    getMyOrganizations(): Observable<Organization[]> {
        return PageResultUtils.fetchAllElementsUsing(
            offset => this.organizationService.searchMyOrganizations(null, null, null, offset, 100, 'name')
        );
    }

    getOrganizationByUuid(userUuid: string, full: boolean = false): Observable<Organization> {
        return this.organizationService.getOrganization(userUuid, full);
    }

    getOrganizationOwnerInfo(organizationUuid: string): Observable<OwnerInfo> {
        return this.organizationService.getOrganizationOwnerInfo(organizationUuid);
    }

    searchOrganizationMembers(
        organizationUuid: string, searchText: string, offset: number, limit: number, order: string
    ): Observable<PagedOrganizationUserMembers> {
        return this.organizationService.searchOrganizationUserMembers(
            organizationUuid, searchText, OrganizationMemberType.Person, offset, limit, order
        );
    }

    isAdministrator(organizationUuid?: string): Observable<boolean> {
        return this.organizationService.isAuthenticatedUserOrganizationAdministrator(organizationUuid);
    }

    isMember(organzationUuid?: string): Observable<boolean> {
        return this.userService.getAuthenticatedUser().pipe(
            switchMap(user => this.organizationService.getOrganizationMembers(organzationUuid).pipe(
                switchMap(members => of(members.find(m => m.uuid === user.uuid) !== null)))
            )
        );

    }

    addOrganizationMember(organizationUuid: string, organizationMember: OrganizationMember): Observable<OrganizationMember> {
        return this.organizationService.addOrganizationMember(organizationUuid, organizationMember);
    }

    removeOrganizationMember(organizationUuid: string, userUuid: string): Observable<any> {
        return this.organizationService.removeOrganizationMember(organizationUuid, userUuid);
    }

    updateOrganizationMember(organizationUuid: string, userUuid: string, organizationMember: OrganizationMember): Observable<any> {
        return this.organizationService.updateOrganizationMember(organizationUuid, userUuid, organizationMember);
    }

    searchAddressRole(criteria: AddressRoleSearchCriteria): Observable<any> {
        return this.strukureService.searchAddressRoles(criteria.code, criteria.type, criteria.active);
    }
}
