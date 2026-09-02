import {Injectable} from '@angular/core';
import {UserService} from '@core/services/user.service';
import {KindOfData} from 'micro_service_modules/api-kmedia';
import {OrganizationService, ProvidersService, StruktureService} from 'micro_service_modules/strukture/api-strukture';
import {Observable} from 'rxjs';
import {ImageLogoService} from './image-logo.service';
import {OrganizationMetierService} from './organization/organization-metier.service';

@Injectable({
    providedIn: 'root'
})
export class ProvidersMetierService extends OrganizationMetierService {

    constructor(protected imageLogoService: ImageLogoService,
                private readonly providersService: ProvidersService,
                protected organizationService: OrganizationService,
                protected userService: UserService, protected struktureService: StruktureService) {
        super(imageLogoService, organizationService, userService, struktureService);
    }

    protected downloadProducerMediaByType(providerUuid: string, kindOfData: KindOfData): Observable<Blob> {
        return this.providersService.downloadProviderMediaByType(providerUuid, KindOfData.Logo);
    }

}
