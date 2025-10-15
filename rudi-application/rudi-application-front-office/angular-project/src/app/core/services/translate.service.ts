import {Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Object} from 'ol';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class CustomTranslateService extends TranslateService {

    static teamName: string;
    static projectName: string;

    constructor() {
        super();
    }


    setTeamName(teamName: string) {
        CustomTranslateService.teamName = teamName;
    }

    setProjectName(projectName: string) {
        CustomTranslateService.projectName = projectName;
    }

    get(key: string | string[], interpolateParams?: Object): Observable<any> {
        if (!interpolateParams) {
            interpolateParams = new Object();
        }
        interpolateParams['teamName'] = CustomTranslateService.teamName;
        interpolateParams['projectName'] = CustomTranslateService.projectName;
        return super.get(key, interpolateParams);
    }

    instant(key: string | string[], interpolateParams?: Object): string | any {
        if (!interpolateParams) {
            interpolateParams = new Object();
        }
        interpolateParams['teamName'] = CustomTranslateService.teamName;
        interpolateParams['projectName'] = CustomTranslateService.projectName;

        return super.instant(key, interpolateParams);
    }

}
