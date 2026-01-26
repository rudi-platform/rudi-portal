import {registerLocaleData} from '@angular/common';
import {HttpClient} from '@angular/common/http';

import localeFr from '@angular/common/locales/fr';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';

registerLocaleData(localeFr);


export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http, 'assets/i18n/', '.json');
}
