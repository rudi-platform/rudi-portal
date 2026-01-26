import {NgClass} from '@angular/common';
import {Component, Input} from '@angular/core';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {MatToolbar} from '@angular/material/toolbar';
import {Router} from '@angular/router';
import {MediaSize} from '@core/services/breakpoint-observer.service';
import {TranslatePipe} from '@ngx-translate/core';
import {PopoverComponent} from '@shared/core/layout/popover/popover.component';
import {from, Observable} from 'rxjs';

@Component({
    selector: 'app-banner',
    templateUrl: './banner.component.html',
    styleUrls: ['./banner.component.scss'],
    imports: [MatToolbar, NgClass, ExtendedModule, PopoverComponent, TranslatePipe]
})
export class BannerComponent {
    @Input() mediaSize: MediaSize;

    constructor(
        private router: Router) {
    }

    submitProject(): Observable<boolean> {
        return from(this.router.navigate(['/projets/soumettre-un-projet']));
    }

    declareReuse(): Observable<boolean> {
        return from(this.router.navigate(['/projets/declarer-une-reutilisation']));
    }
}
