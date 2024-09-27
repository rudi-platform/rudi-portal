import {Injectable} from '@angular/core';
import {Address} from 'micro_service_modules/api-rva';
import {SelfdataService} from 'micro_service_modules/selfdata/selfdata-api';
import {Observable} from 'rxjs';
import {RvaService} from '../rva.service';

@Injectable({
    providedIn: 'root'
})
export class SelfdataRvaService extends RvaService {

    constructor(
        private readonly selfdataService: SelfdataService
    ) {
        super();
    }

    getAddressById(query: string): Observable<Address> {
        return this.selfdataService.getAddressById(query);
    }

    searchAddresses(query: string): Observable<Address[]> {
        return this.selfdataService.searchAddresses(query);
    }
}
