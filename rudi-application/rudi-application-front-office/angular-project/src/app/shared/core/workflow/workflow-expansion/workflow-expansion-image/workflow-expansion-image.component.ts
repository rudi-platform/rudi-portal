import {Component, Input, OnInit} from '@angular/core';
import {MatLabel} from '@angular/material/form-field';
import {Base64EncodedLogo, ImageLogoService} from '@core/services/image-logo.service';
import {ProjectAttachmentService} from '@core/services/project-attachment.service';
import {switchMap} from 'rxjs/operators';

declare let console;

@Component({
    selector: 'app-workflow-expansion-image',
    templateUrl: './workflow-expansion-image.component.html',
    styleUrl: './workflow-expansion-image.component.scss',
    imports: [MatLabel]
})
export class WorkflowExpansionImageComponent implements OnInit {
    @Input() label: string;
    @Input() uuid: string;

    imageBase64: Base64EncodedLogo;

    constructor(
        private readonly projectAttachmentService: ProjectAttachmentService,
        private readonly imageLogoService: ImageLogoService
    ) {
    }

    ngOnInit(): void {
        if (this.uuid) {
            this.projectAttachmentService.downloadAttachement(this.uuid).pipe(
                switchMap((blob: Blob) => this.imageLogoService.createImageFromBlob(blob))
            ).subscribe({
                next: (base64: Base64EncodedLogo) => {
                    this.imageBase64 = base64;
                },
                error: (err) => {
                    console.error('[WorkflowExpansionImage] Failed to load attachment', this.uuid, err);
                }
            });
        }
    }
}
