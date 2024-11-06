import {Inject, Injectable} from '@angular/core';
import {
    DEFAULT_LANGUAGE,
    MissingTranslationHandler,
    TranslateCompiler,
    TranslateLoader,
    TranslateParser,
    TranslateService,
    TranslateStore,
    USE_DEFAULT_LANG,
    USE_EXTEND,
    USE_STORE
} from '@ngx-translate/core';
import {Object} from 'ol';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class CustomTranslateService extends TranslateService {

    static teamName: string;
    static projectName: string;

    constructor(public store: TranslateStore,
                public currentLoader: TranslateLoader,
                public compiler: TranslateCompiler,
                public parser: TranslateParser,
                public missingTranslationHandler: MissingTranslationHandler,
                @Inject(USE_DEFAULT_LANG) useDefaultLang: boolean,
                @Inject(USE_STORE) isolate: boolean,
                @Inject(USE_EXTEND) extend: boolean,
                @Inject(DEFAULT_LANGUAGE) defaultLanguage: string) {
        super(store, currentLoader, compiler, parser, missingTranslationHandler, useDefaultLang, isolate, extend, defaultLanguage);
    }


    setTeamName(teamName: string) {
        CustomTranslateService.teamName = teamName;
    }

    setProjectName(projectName: string) {
        CustomTranslateService.projectName = projectName;
    }

    get(key: string | Array<string>, interpolateParams?: Object): Observable<any> {
        if (!interpolateParams) {
            interpolateParams = new Object();
        }
        interpolateParams['teamName'] = CustomTranslateService.teamName;
        interpolateParams['projectName'] = CustomTranslateService.projectName;
        return super.get(key, interpolateParams);
    }

    instant(key: string | Array<string>, interpolateParams?: Object): string | any {
        if (!interpolateParams) {
            interpolateParams = new Object();
        }
        interpolateParams['teamName'] = CustomTranslateService.teamName;
        interpolateParams['projectName'] = CustomTranslateService.projectName;

        return super.instant(key, interpolateParams);
    }

}
