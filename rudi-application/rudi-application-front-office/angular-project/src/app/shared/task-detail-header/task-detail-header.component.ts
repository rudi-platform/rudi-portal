import {Component, Input} from '@angular/core';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {ALL_TYPES} from '../models/title-icon-type';

@Component({
    selector: 'app-task-detail-header',
    templateUrl: './task-detail-header.component.html',
    styleUrls: ['./task-detail-header.component.scss'],
    standalone: false
})
export class TaskDetailHeaderComponent {

    @Input() headingLoading: boolean;
    @Input() pageTitle: string;
    @Input() pageSubTitle: string;
    @Input() status: string;
    @Input() icon: string;

    constructor(iconRegistryService: IconRegistryService,
    ) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }
}
