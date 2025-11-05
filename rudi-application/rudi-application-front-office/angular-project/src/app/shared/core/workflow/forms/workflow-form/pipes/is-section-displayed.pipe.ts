import {Pipe, PipeTransform} from '@angular/core';
import {Section} from 'micro_service_modules/projekt/projekt-api';

@Pipe({
    name: 'isSectionDisplayed',
    standalone: false
})
export class IsSectionDisplayedPipe implements PipeTransform {
    transform(section: Section): boolean {
        return !(section == null || section.label == null || section.label === '');
    }
}
