import { HttpEvent } from '@angular/common/http';
import {Injectable} from '@angular/core';
import {SelfdataAttachmentService} from '@core/services/selfdata-attachment.service';
import {UploaderAdapter} from '@shared/uploader/uploader.adapter';
import {FilePreviewModel} from '@sleiss/ngx-awesome-uploader';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class SelfdataAttachmentAdapter implements UploaderAdapter<string> {

    constructor(
        private readonly selfdataAttachmentService: SelfdataAttachmentService,
    ) {
    }

    uploadFile(fileItem: FilePreviewModel): Observable<HttpEvent<string>> {
        return this.selfdataAttachmentService.uploadAttachment(fileItem);
    }

    removeFile(fileItem: FilePreviewModel): Observable<void> {
        return this.selfdataAttachmentService.deleteAttachment(fileItem.uploadResponse);
    }

}
