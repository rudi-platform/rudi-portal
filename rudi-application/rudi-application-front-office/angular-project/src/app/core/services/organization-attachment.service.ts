import { HttpEvent } from '@angular/common/http';
import {Injectable} from '@angular/core';
import {AttachmentService} from '@core/services/attachment.service';
import {FilePreviewModel} from '@sleiss/ngx-awesome-uploader';
import {DocumentMetadata} from 'micro_service_modules/selfdata/selfdata-api';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class OrganizationAttachmentService extends AttachmentService {

    constructor(
        private readonly organizationService: OrganizationService,
    ) {
        super();
    }

    uploadAttachment(file: FilePreviewModel): Observable<HttpEvent<string>> {
        return this.organizationService.uploadAttachment(file.file, 'events', true);
    }

    deleteAttachment(uuid: string): Observable<void> {
        return this.organizationService.deleteAttachment(uuid);
    }

    downloadAttachement(uuid: string): Observable<Blob> {
        return this.organizationService.downloadAttachment(uuid);
    }

    getAttachmentMetadata(uuid: string): Observable<DocumentMetadata> {
        return this.organizationService.getAttachmentMetadata(uuid);
    }
}
