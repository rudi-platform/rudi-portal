import {Injectable} from '@angular/core';
import {Observable, Observer} from 'rxjs';

export type Base64EncodedLogo = string;

@Injectable({
    providedIn: 'root'
})
export class ImageLogoService {

    /**
     * Conversion asynchrone d'un Blob en data URL base64 (compatible PNG, SVG, JPEG...).
     * @param image le blob contenant l'image
     */
    public createImageFromBlob(image: Blob): Observable<Base64EncodedLogo> {
        return new Observable((observer: Observer<Base64EncodedLogo>) => {
            const reader = new FileReader();
            reader.addEventListener('load', () => {
                observer.next(reader.result as Base64EncodedLogo);
                observer.complete();
            }, false);

            if (image) {
                reader.readAsDataURL(image);
            } else {
                observer.error('Erreur tentative de conversion avec un blob nul');
            }
        });
    }
}
