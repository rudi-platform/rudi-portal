import {Pipe, PipeTransform} from '@angular/core';

/**
 * Tronque un texte à la longueur maximale indiquée, sans couper de mot, et ajoute des points de
 * suspension si le texte a été raccourci. Fonction pure, réutilisée par `TruncateTextPipe` et
 * `IsTruncatedPipe` afin de centraliser la logique de troncature.
 */
export function truncateTextValue(value: string, length: number): string {
    const biggestWord = 50;
    const elipses = '...';
    if (typeof value === 'undefined' || value === null) {
        return value;
    }
    if (value.length <= length) {
        return value;
    }
    // .. truncate to about correct lenght
    let truncatedText = value.slice(0, length + biggestWord);
    // .. now nibble ends till correct length
    while (truncatedText.length > length - elipses.length) {
        const lastSpace = truncatedText.lastIndexOf(' ');
        if (lastSpace === -1) {
            break;
        }
        truncatedText = truncatedText.slice(0, lastSpace).replace(/[!,.?;:]$/, '');
    }
    return truncatedText + elipses;
}

@Pipe({standalone: true, name: 'truncateText'})
export class TruncateTextPipe implements PipeTransform {

    transform(value: string, length: number): string {
        return truncateTextValue(value, length);
    }
}

/**
 * Pipe utilitaire indiquant si un texte est effectivement tronqué par `TruncateTextPipe` pour une
 * longueur maximale donnée.
 *
 * Utilisé dans les templates des cards (jeu de données, réutilisation, organisation, ...) afin de
 * n'afficher une infobulle (tooltip) qu'en présence d'un texte réellement incomplet, permettant
 * ainsi à l'utilisateur d'accéder à l'information complète au survol (ou au focus clavier) sans
 * avoir à cliquer.
 */
@Pipe({standalone: true, name: 'isTruncated'})
export class IsTruncatedPipe implements PipeTransform {

    transform(value: string, length: number): boolean {
        if (typeof value === 'undefined' || value === null) {
            return false;
        }
        return truncateTextValue(value, length) !== value;
    }
}
