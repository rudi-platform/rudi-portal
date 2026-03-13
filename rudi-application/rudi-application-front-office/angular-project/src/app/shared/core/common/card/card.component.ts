import { NgClass } from '@angular/common';
import {Component, Input} from '@angular/core';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {MatCard} from '@angular/material/card';
import {MatIcon} from '@angular/material/icon';
import {NgClassObject} from '@core/services/breakpoint-observer.service';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {TranslatePipe} from '@ngx-translate/core';
import {ALL_TYPES} from '@shared/models/title-icon-type';

@Component({
    selector: 'app-card',
    templateUrl: './card.component.html',
    styleUrls: ['./card.component.scss'],
    imports: [MatCard, NgClass, ExtendedModule, MatIcon, TranslatePipe]
})
export class CardComponent {

    /**
     * Centrer tout le contenu de la card ?
     * Par défaut : false.
     */
    @Input()
    textCenter = false;

    /**
     * La card est centrée et prend la moitié de la largeur de l'écran ?
     * Par défaut : false.
     */
    @Input()
    centered = false;

    @Input()
    title: string;

    @Input()
    subTitle: string;

    @Input()
    icon: string;

    @Input()
    header = false;


    constructor(iconRegistryService: IconRegistryService,
    ) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }

    get ngClass(): NgClassObject {
        return {
            'text-center': this.textCenter,
            centered: this.centered,
        };
    }

}
