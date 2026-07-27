import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {MatMenuTrigger, MatMenu} from '@angular/material/menu';
import {Observable} from 'rxjs';
import { NgClass, AsyncPipe } from '@angular/common';
import {MatButton} from '@angular/material/button';
import {MatBadge} from '@angular/material/badge';
import {MatIcon} from '@angular/material/icon';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-filter-menu',
    templateUrl: './filter-menu.component.html',
    styleUrls: ['./filter-menu.component.scss'],
    imports: [MatButton, MatMenuTrigger, NgClass, MatBadge, MatIcon, MatMenu, AsyncPipe, TranslatePipe]
})
export class FilterMenuComponent {
    @Input() buttonTextKey: string;
    @Input() counter$: Observable<number>;
    @Input() isHidden = false;
    @Output() closed = new EventEmitter<void>();
    @ViewChild('matMenuTrigger') matMenuTrigger: MatMenuTrigger;
    private _menuIsOpened = false;

    get menuIsOpened(): boolean {
        return this._menuIsOpened;
    }

    openMenuOrSidenav(): void {
        this.matMenuTrigger.openMenu();
    }

    close(): void {
        this.matMenuTrigger.closeMenu();
    }

    onMenuOpen(): void {
        this._menuIsOpened = true;
    }

    onMenuClose(): void {
        this._menuIsOpened = false;
        this.closed.emit();
    }

}
