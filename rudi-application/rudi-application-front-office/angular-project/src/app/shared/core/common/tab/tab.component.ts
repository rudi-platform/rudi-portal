import {Component, ContentChild, Input, TemplateRef} from '@angular/core';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {ALL_TYPES, TitleIconType} from '@shared/models/title-icon-type';
import {MatButton} from '@angular/material/button';
import { NgClass } from '@angular/common';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {MatIcon} from '@angular/material/icon';

@Component({
    selector: 'app-tab',
    templateUrl: './tab.component.html',
    styleUrls: ['./tab.component.scss'],
    imports: [MatButton, NgClass, ExtendedModule, MatIcon]
})
export class TabComponent {

    @Input()
    icon?: TitleIconType;

    @Input()
    label = 'tab';

    @Input()
    active = false;

    @Input()
    disabled = false;

    @Input()
    invisible?: boolean;

    @ContentChild(TemplateRef)
        // tslint:disable-next-line:no-any
    templateRef: TemplateRef<any>;

    constructor(
        private readonly iconRegistryService: IconRegistryService,
    ) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }
}
