import {HttpEvent} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ProjectAttachmentService} from '@core/services/project-attachment.service';
import {UploaderAdapter} from '@shared/core/form/uploader/uploader.adapter';
import {FilePreviewModel} from '@sleiss/ngx-awesome-uploader';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ProjectAttachmentAdapter implements UploaderAdapter<string> {

    constructor(
        private readonly projectAttachmentService: ProjectAttachmentService,
    ) {
    }

    uploadFile(fileItem: FilePreviewModel): Observable<HttpEvent<string>> {
        return this.projectAttachmentService.uploadAttachment(fileItem);
    }

    removeFile(fileItem: FilePreviewModel): Observable<void> {
        return this.projectAttachmentService.deleteAttachment(fileItem.uploadResponse);
    }
}
