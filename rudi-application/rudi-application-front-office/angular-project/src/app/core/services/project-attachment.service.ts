import { HttpEvent } from '@angular/common/http';
import {Injectable} from '@angular/core';
import {AttachmentService} from '@core/services/attachment.service';
import {FilePreviewModel} from '@sleiss/ngx-awesome-uploader';
import {DocumentMetadata} from 'micro_service_modules/selfdata/selfdata-api';
import {ProjektService} from 'micro_service_modules/projekt/projekt-api';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ProjectAttachmentService extends AttachmentService {

    constructor(
        private readonly projektService: ProjektService,
    ) {
        super();
    }

    uploadAttachment(file: FilePreviewModel): Observable<HttpEvent<string>> {
        return this.projektService.uploadAttachment(file.file, 'events', true);
    }

    deleteAttachment(uuid: string): Observable<void> {
        return this.projektService.deleteAttachment(uuid);
    }

    downloadAttachement(uuid: string): Observable<Blob> {
        return this.projektService.downloadAttachment(uuid);
    }

    getAttachmentMetadata(uuid: string): Observable<DocumentMetadata> {
        return this.projektService.getAttachmentMetadata(uuid);
    }
}
