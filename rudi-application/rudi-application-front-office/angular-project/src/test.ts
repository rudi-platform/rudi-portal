// This file is required by karma.conf.js; spec files are discovered by the Angular karma builder

import 'zone.js/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {importProvidersFrom, NgModule, provideZoneChangeDetection} from '@angular/core';
import {getTestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {BrowserDynamicTestingModule, platformBrowserDynamicTesting} from '@angular/platform-browser-dynamic/testing';
import {provideRouter} from '@angular/router';
import {CoreModule} from '@core/core.module';
import {LogService} from '@core/services/log.service';
import {provideTranslateService} from '@ngx-translate/core';
import {ProcessDefinitionKeyTranslatePipe} from '@shared/utils/pipes/process-definition-key-translate.pipe';

/**
 * Providers applicatifs (main.ts / CoreModule / SharedModule) disponibles par défaut dans chaque TestBed,
 * surchargeables au niveau de chaque spec.
 */
@NgModule({
    providers: [
        provideZoneChangeDetection(),
        importProvidersFrom(CoreModule),
        LogService,
        ProcessDefinitionKeyTranslatePipe,
        provideTranslateService(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideNoopAnimations(),
        {provide: MatDialogRef, useValue: {close: (): void => undefined}},
        {provide: MAT_DIALOG_DATA, useValue: {}}
    ]
})
class DefaultTestProvidersModule {
}

// First, initialize the Angular testing environment.
getTestBed().initTestEnvironment(
  [BrowserDynamicTestingModule, DefaultTestProvidersModule],
  platformBrowserDynamicTesting()
);
