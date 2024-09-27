import {Injectable} from '@angular/core';
import {Address} from 'micro_service_modules/api-rva';
import {KonsultService} from 'micro_service_modules/konsult/konsult-api';
import {Observable} from 'rxjs';
import {RvaService} from '../rva.service';

@Injectable({
    providedIn: 'root'
})
export class KonsultRvaService extends RvaService {

    constructor(
        private readonly konsultService: KonsultService
    ) {
        super();
    }

    getAddressById(query: string): Observable<Address> {
        throw new Error('Method not existing');
    }

    searchAddresses(query: string): Observable<Address[]> {
        return this.konsultService.searchAddresses(query);
    }
}
