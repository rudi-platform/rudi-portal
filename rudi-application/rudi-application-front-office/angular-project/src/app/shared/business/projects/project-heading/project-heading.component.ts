import {Component, Input} from '@angular/core';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {MatCardImage} from '@angular/material/card';

import {ReplaceIfNullPipe} from '@shared/utils/pipes/replace-if-null.pipe';

@Component({
    selector: 'app-project-heading',
    templateUrl: './project-heading.component.html',
    styleUrls: ['./project-heading.component.scss'],
    imports: [MatCardImage, ReplaceIfNullPipe]
})
export class ProjectHeadingComponent {

    mediaSize: MediaSize;

    /**
     * CHaîne base 64 du logo du projet
     */
    @Input()
    logo: string;

    /**
     * Prénom NOM du porteur de projet ou nom de l'organisation porteuse du projet
     */
    @Input()
    ownerDescription: string;

    /**
     * Titre du projet
     */
    @Input()
    projectTitle: string;

    /**
     * Chaîne décrivant le statut du projet
     */
    @Input()
    status: string;

    constructor(private readonly breakpointObserverService: BreakpointObserverService) {
        this.mediaSize = this.breakpointObserverService.getMediaSize();
    }
}
