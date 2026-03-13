import {Injectable} from '@angular/core';
import {Address} from 'micro_service_modules/selfdata/selfdata-api/model/address';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export abstract class RvaService {
    private static readonly MIN_QUERY_LENGTH = 3;

    public static isValidQuery(query: string): boolean {
        return query.length > RvaService.MIN_QUERY_LENGTH;
    }

    abstract searchAddresses(query: string): Observable<Address[]>;


    abstract getAddressById(query: string): Observable<Address>;

}
