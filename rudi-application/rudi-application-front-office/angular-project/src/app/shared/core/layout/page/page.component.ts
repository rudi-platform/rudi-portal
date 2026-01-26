import {Component} from '@angular/core';
import {MatSidenavContainer, MatSidenavContent} from '@angular/material/sidenav';

@Component({
    selector: 'app-page',
    templateUrl: './page.component.html',
    styleUrls: ['./page.component.scss'],
    imports: [MatSidenavContainer, MatSidenavContent]
})
export class PageComponent {
}
