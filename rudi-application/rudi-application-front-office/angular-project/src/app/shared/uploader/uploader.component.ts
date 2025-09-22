import {Component, EventEmitter, Input, Output} from '@angular/core';
import {AbstractControl} from '@angular/forms';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {SnackBarService} from '@core/services/snack-bar.service';
import {TranslateService} from '@ngx-translate/core';
import {FilePreviewModel, ValidationError} from '@sleiss/ngx-awesome-uploader';
import {UploaderCaptions} from '@sleiss/ngx-awesome-uploader/lib/uploader-captions';
import saveAs from 'file-saver';
import {AdapterProxy} from './adapter-proxy';
import {UploaderAdapter} from './uploader.adapter';

@Component({
    selector: 'app-uploader',
    templateUrl: './uploader.component.html',
    styleUrls: ['./uploader.component.scss'],
    standalone: false
})
export class UploaderComponent<T> {

    @Input()
    set adapter(adapter: UploaderAdapter<T>) {
        this.adapterProxy = new AdapterProxy(this, adapter);
    }

    adapterProxy: AdapterProxy<T>;

    /**
     * On demande le formControl pour pouvoir le réinitialiser en cas d'erreur, mais on ne positionne pas sa valeur.
     * C'est au composant utilisant de le faire.
     */
    @Input() fileFormControl: AbstractControl;
    @Input() imageText: string;
    @Input() errorText: string;
    @Input() imageFormat: string;
    @Input() enableCropper = false;
    @Input() fileMaxCount: number;
    /** Max size of selected file in MB. Default: no limit */
    @Input() fileMaxSize: number;
    @Input() fileExtensions: string[];
    @Input() cropperOptions: object;

    @Output() fileChanged: EventEmitter<FilePreviewModel> = new EventEmitter<FilePreviewModel>();

    constructor(
        public snackBarService: SnackBarService,
        public translateService: TranslateService,
        private matIconRegistry: MatIconRegistry,
        private domSanitizer: DomSanitizer,
    ) {
        this.cropperOptions = {
            aspectRatio: 416 / 220,
            minContainerWidth: 600,
            minContainerHeight: 450,
            autoCropArea: 1,
        };
        this.matIconRegistry.addSvgIcon(
            'rudi_picto_image.svg',
            this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/rudi_picto_image.svg')
        );
    }

    public onValidationError(validationError: ValidationError): void {
        this.fileFormControl.reset();
        this.fileFormControl.updateValueAndValidity();
        const {error} = validationError;
        let formatsText: string;
        // Vérifie si l'erreur est liée aux extensions de fichiers.
        if (error === 'EXTENSIONS') {
            // Si la longueur de la liste des extensions de fichiers est égale à 1, on affiche seulement cette extension.
            if (this.fileExtensions.length === 1) {
                formatsText = `.${this.fileExtensions[0]}`;
            } else {
                // Si plusieurs extensions sont présentes, on les affiche avec un espace entre chaque extension sauf la dernière, à laquelle on ajoute "ou"
                formatsText = this.fileExtensions.map(format => `.${format}`).slice(0, -1).join(' ') + this.translateService.instant('common.fileValidationError.or') + this.fileExtensions[this.fileExtensions.length - 1];
            }
        }
        const key = `common.fileValidationError.${error}`;
        this.snackBarService.add(this.translateService.instant(key, {imageFormat: formatsText}));
    }

    public onUploadSuccess($event: FilePreviewModel): void {
        this.fileChanged.emit($event);
    }

    get captions(): UploaderCaptions {
        return {
            dropzone: {
                title: '',
                or: '',
                browse: '',
            },
            cropper: {
                crop: this.translateService.instant('uploader.cropper.crop'),
                cancel: this.translateService.instant('uploader.cropper.cancel'),
            },
            previewCard: {
                remove: '',
                uploadError: '',
                download: ''
            }
        };
    }

    onDownLoadFile($event: FilePreviewModel): void {
        if ($event && $event.file) {
            saveAs($event.file, $event.fileName, {autoBom: false});
        }
    }
}

