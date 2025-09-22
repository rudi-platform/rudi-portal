import {DataSize} from '@shared/models/data-size';
import {HelpLink} from 'micro_service_modules/konsult/konsult-model';
import {Observable} from 'rxjs';
import {map, shareReplay} from 'rxjs/operators';

function get<P, V, M>(key: string, properties: P, mapper: (value: V) => M): M | undefined {
    const childKey = key.split('.')[0];
    const child = properties[childKey];
    if (child) {
        if (key.length > childKey.length) {
            const childChildKey = key.slice(childKey.length + 1);
            return get(childChildKey, child, mapper);
        } else {
            return mapper(child);
        }
    } else {
        return undefined;
    }
}

export abstract class PropertiesAdapter<P> {

    private propertie$: Observable<P>;

    get(key: string): Observable<string> {
        return this.getProperties().pipe(
            map(properties =>
                get<P, string, string>(key, properties, value => value)
            ),
        );
    }

    getDataSize(key: string): Observable<DataSize> {
        return this.getProperties().pipe(
            map(properties =>
                get<P, string, DataSize>(key, properties, value => DataSize.parse(value))
            ),
        );
    }

    /**
     * Les valeurs (string) de la propriété sous forme de tableau.
     * La propriété source doit déjà être sous forme de tableau JSON.
     */
    getStrings(key: string): Observable<string[]> {
        return this.getProperties().pipe(
            map(properties =>
                get<P, string[], string[]>(key, properties, values => values)
            ),
        );
    }

    getNumber(key: string): Observable<number> {
        return this.getProperties().pipe(
            map(properties =>
                get<P, number, number>(key, properties, value => value)
            ),
        );
    }

    getHelpLinks(key: string): Observable<HelpLink[]> {
        return this.getProperties().pipe(
            map(properties =>
                get<P, HelpLink[], HelpLink[]>(key, properties, value => value)
            ),
        );
    }

    getHelpLink(key: string, propertyKey: string): Observable<HelpLink> {
        return this.getHelpLinks(key).pipe(
            map((value: HelpLink[]) => value.find((helpLink: HelpLink) => helpLink.key === propertyKey))
        );
    }

    /**
     * Les valeurs (numbers) de la propriété sous forme de tableau.
     * La propriété source doit déjà être sous forme de tableau JSON.
     */
    getNumbers(key: string): Observable<number[]> {
        return this.getProperties().pipe(
            map(properties =>
                get<P, number[], number[]>(key, properties, value => value)
            ),
        );
    }

    getProperties(): Observable<P> {
        if (!this.propertie$) {
            this.propertie$ = this.fetchBackendProperties().pipe(
                shareReplay(1)
            );
        }
        return this.propertie$;
    }

    protected abstract fetchBackendProperties(): Observable<P>;
}
