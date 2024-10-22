import {Injectable} from '@angular/core';
import {LinkedProducersService} from 'micro_service_modules/strukture/api-strukture/api/linkedProducers.service';
import {LinkedProducer} from 'micro_service_modules/strukture/api-strukture/model/linkedProducer';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export abstract class LinkedProducerMetierService {

    protected constructor(
        protected linkedProducersService: LinkedProducersService,
    ) {
    }

    getLinkedProducerByUuid(producerLinkUuid: string): Observable<LinkedProducer> {
        return this.linkedProducersService.getLinkedProducer(producerLinkUuid);
    }

}
