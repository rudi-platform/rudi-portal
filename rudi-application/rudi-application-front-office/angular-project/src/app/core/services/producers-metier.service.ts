import {Injectable} from '@angular/core';
import {UserService} from '@core/services/user.service';
import {KindOfData} from 'micro_service_modules/api-kmedia';
import {OrganizationService, ProducersService, StruktureService} from 'micro_service_modules/strukture/api-strukture';
import {Observable} from 'rxjs';
import {ImageLogoService} from './image-logo.service';
import {OrganizationMetierService} from './organization/organization-metier.service';

@Injectable({
    providedIn: 'root'
})
export class ProducersMetierService extends OrganizationMetierService {

    constructor(
        protected imageLogoService: ImageLogoService,
        private readonly producersService: ProducersService,
        protected readonly organizationService: OrganizationService,
        protected readonly userService: UserService,
        protected readonly struktureService: StruktureService
    ) {
        super(imageLogoService, organizationService, userService, struktureService);
    }

    protected downloadProducerMediaByType(producerUuid: string, kindOfData: KindOfData): Observable<Blob> {
        return this.producersService.downloadProducerMediaByType(producerUuid, KindOfData.Logo);
    }
}
