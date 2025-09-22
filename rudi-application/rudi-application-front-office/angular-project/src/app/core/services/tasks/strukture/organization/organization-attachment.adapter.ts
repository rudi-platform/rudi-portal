import { HttpEvent } from '@angular/common/http';
import {Injectable} from '@angular/core';
import {UploaderAdapter} from '@shared/uploader/uploader.adapter';
import {FilePreviewModel} from '@sleiss/ngx-awesome-uploader';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class OrganizationAttachmentAdapter implements UploaderAdapter<string> {

    constructor(
        private readonly organizationService: OrganizationService,
    ) {
    }

    uploadFile(file: FilePreviewModel): Observable<HttpEvent<string>> {
        return this.organizationService.uploadAttachment(file.file, 'events', true);
    }

    removeFile(fileItem: FilePreviewModel): Observable<void> {
        return this.organizationService.deleteAttachment(fileItem.uploadResponse);
    }
}
