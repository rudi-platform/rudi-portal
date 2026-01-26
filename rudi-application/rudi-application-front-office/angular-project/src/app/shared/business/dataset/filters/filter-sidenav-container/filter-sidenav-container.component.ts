import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatMiniFabButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-filter-sidenav-container',
    templateUrl: './filter-sidenav-container.component.html',
    styleUrl: './filter-sidenav-container.component.scss',
    imports: [
        MatMiniFabButton,
        MatIcon,
        TranslatePipe,
    ],
})
export class FilterSidenavContainerComponent {
    @Input() titleKey: string;
    @Output() closeSidenav = new EventEmitter<void>();
}
