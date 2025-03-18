import {HttpEvent} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {FilePreviewModel} from '@sleiss/ngx-awesome-uploader';
import {DocumentMetadata} from 'micro_service_modules/selfdata/selfdata-api';
import {Observable} from 'rxjs';


@Injectable({
    providedIn: 'root'
})
export abstract class AttachmentService {
    
    abstract uploadAttachment(file: FilePreviewModel): Observable<HttpEvent<string>>;

    abstract deleteAttachment(uuid: string): Observable<void>;

    abstract downloadAttachement(uuid: string): Observable<Blob>;

    abstract getAttachmentMetadata(uuid: string): Observable<DocumentMetadata>;

}
