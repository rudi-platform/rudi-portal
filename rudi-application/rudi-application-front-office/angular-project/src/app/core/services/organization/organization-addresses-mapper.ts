import {inject, Injectable} from '@angular/core';
import {AddressRoleSearchCriteria} from '@core/bean/strukture/address-role-search-criteria';
import {OrganizationAddresses} from '@core/services/organization/organization-addresses';
import {OrganizationMetierService} from '@core/services/organization/organization-metier.service';
import {AbstractAddress, AddressRole, TelephoneAddress, WebsiteAddress} from 'micro_service_modules/strukture/api-strukture/model/models';
import {AddressType, EmailAddress} from 'micro_service_modules/strukture/strukture-model';
import {Observable, of, switchMap} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export abstract class OrganizationAddressesMapper {

    private readonly organizationMetierService = inject(OrganizationMetierService);

    mapAbstractAddressesToOrganizationAddresses(addresses: AbstractAddress[]): OrganizationAddresses {
        const organizationAddresses: OrganizationAddresses = {};
        if (addresses) {
            addresses.forEach((address) => {
                switch (address.type) {
                    case AddressType.Email: {
                        const emailAddress = address as EmailAddress;
                        organizationAddresses.email = emailAddress.email;
                        break;
                    }
                    case AddressType.Website: {
                        const websiteAddress = address as WebsiteAddress;
                        organizationAddresses.url = websiteAddress.url;
                        break;
                    }
                    case AddressType.Phone: {
                        const phoneAddress = address as TelephoneAddress;
                        organizationAddresses.phoneNumber = phoneAddress.phoneNumber;
                        break;
                    }
                    default: {
                        console.warn('Address Type not supported yet');
                    }
                }
            });
        }
        return organizationAddresses;
    }

    mapOrganizationAddressesToAbstractAddresses(organizationAddresses: OrganizationAddresses): Observable<AbstractAddress[] | any[]> {
        const criteria = new AddressRoleSearchCriteria();
        criteria.code = 'CONTACT';
        criteria.active = true;

        return this.organizationMetierService.searchAddressRole(criteria).pipe(
            switchMap((addressRoles) => {
                if (!addressRoles || addressRoles.length === 0) {
                    console.warn('No address role found for code CONTACT');
                    return of([]);
                }
                const addresses: AbstractAddress[] = [];
                if (organizationAddresses.email) {
                    const addressRole: AddressRole = addressRoles.find((role) => role.code === 'CONTACT' && role.type === AddressType.Email);
                    const emailAddress = {
                        type: AddressType.Email,
                        email: organizationAddresses.email,
                        addressRole: addressRole
                    } as EmailAddress;
                    addresses.push(emailAddress);
                }
                if (organizationAddresses.url) {
                    const addressRole: AddressRole = addressRoles.find((role) => role.code === 'CONTACT' && role.type === AddressType.Website);
                    const websiteAddress = {
                        type: AddressType.Website,
                        url: organizationAddresses.url,
                        addressRole: addressRole
                    } as WebsiteAddress;
                    addresses.push(websiteAddress);
                }
                if (organizationAddresses.phoneNumber) {
                    const addressRole: AddressRole = addressRoles.find((role) => role.code === 'CONTACT' && role.type === AddressType.Phone);
                    const phoneAddress = {
                        type: AddressType.Phone,
                        phoneNumber: organizationAddresses.phoneNumber,
                        addressRole: addressRole
                    } as TelephoneAddress;
                    addresses.push(phoneAddress);
                }
                return of(addresses);
            })
        );
    }
}
