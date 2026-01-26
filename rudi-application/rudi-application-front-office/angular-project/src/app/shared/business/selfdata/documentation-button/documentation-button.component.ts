import {Component} from '@angular/core';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {AsyncPipe} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';
import {GetBackendPropertyPipe} from '@shared/utils/pipes/get-backend-property.pipe';

@Component({
    selector: 'app-documentation-button',
    templateUrl: './documentation-button.component.html',
    styleUrls: ['./documentation-button.component.scss'],
    imports: [MatButton, MatIcon, AsyncPipe, TranslatePipe, GetBackendPropertyPipe]
})
export class DocumentationButtonComponent {


    constructor(private readonly iconRegistryService: IconRegistryService,) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }

}
