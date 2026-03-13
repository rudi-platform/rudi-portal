import {APP_BASE_HREF, LocationStrategy, PathLocationStrategy} from '@angular/common';
import {HTTP_INTERCEPTORS, HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http';
import {enableProdMode, importProvidersFrom, inject, Injector, LOCALE_ID, provideAppInitializer} from '@angular/core';
import {MAT_DATE_LOCALE} from '@angular/material/core';
import {MatPaginatorIntl} from '@angular/material/paginator';
import {bootstrapApplication, BrowserModule} from '@angular/platform-browser';
import {provideAnimations} from '@angular/platform-browser/animations';
import {CoreModule} from '@core/core.module';
import {TranslatedMatPaginatorIntl} from '@core/i18n/translated-mat-paginator-intl';
import {HttpTokenInterceptor} from '@core/interceptors/http.token.interceptor';
import {ResponseTokenInterceptor} from '@core/interceptors/response.token.interceptor';
import {LogService} from '@core/services/log.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {CustomTranslateService} from '@core/services/translate.service';
import {HomeModule} from '@features/home/home.module';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {TranslateCompiler, TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {SharedModule} from '@shared/shared.module';
import {MESSAGE_FORMAT_CONFIG, TranslateMessageFormatCompiler} from 'ngx-translate-messageformat-compiler';
import {appInitializerFactory} from './app/app-initializer-factory';
import {AppRoutingModule} from './app/app-routing.module';
import {AppComponent} from './app/app.component';

import {HttpLoaderFactory} from './app/app.module';
import {environment} from './environments/environment';

if (environment.production) {
    enableProdMode();
}

bootstrapApplication(AppComponent, {
    providers: [
        importProvidersFrom(BrowserModule, CoreModule, SharedModule, HomeModule, AppRoutingModule, TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [HttpClient]
            },
            compiler: {
                provide: TranslateCompiler,
                useClass: TranslateMessageFormatCompiler
            }
        }), NgbModule),
        LogService,
        {provide: LOCALE_ID, useValue: 'fr-FR'},
        {provide: LocationStrategy, useClass: PathLocationStrategy},
        {provide: APP_BASE_HREF, useValue: '/'},
        provideHttpClient(
            withInterceptors([HttpTokenInterceptor, ResponseTokenInterceptor])
        ),
        {provide: MAT_DATE_LOCALE, useValue: 'fr-FR'},
        {provide: MESSAGE_FORMAT_CONFIG, useValue: {locales: ['fr']}},
        {provide: TranslateService, useClass: CustomTranslateService},
        provideAppInitializer(() => {
            const initFn = appInitializerFactory(inject(CustomTranslateService), inject(Injector), inject(PropertiesMetierService));
            return initFn();
        }),
        {
            provide: MatPaginatorIntl,
            deps: [CustomTranslateService, Injector],
            useFactory: (translateService: CustomTranslateService) => new TranslatedMatPaginatorIntl(translateService)
        },
        provideAnimations()
    ]
})
    .catch(err => console.error(err));
