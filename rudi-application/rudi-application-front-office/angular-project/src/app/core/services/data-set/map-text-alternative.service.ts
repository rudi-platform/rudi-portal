import {Injectable} from '@angular/core';

/**
 * Lets the map tab ask the Informations tab to reveal and focus the map text alternative
 * (bounding box coordinates) once it is rendered (RGAA 4.8).
 */
@Injectable({
    providedIn: 'root'
})
export class MapTextAlternativeService {
    private focusRequested = false;

    requestFocus(): void {
        this.focusRequested = true;
    }

    /**
     * @return true once per request, so that later renderings of the Informations tab are not affected
     */
    consumeFocusRequest(): boolean {
        const requested = this.focusRequested;
        this.focusRequested = false;
        return requested;
    }
}
