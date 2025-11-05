import {HttpEvent, HttpResponse} from '@angular/common/http';
import {UploaderAdapter} from '@shared/core/form/uploader/uploader.adapter';
import {FilePreviewModel} from '@sleiss/ngx-awesome-uploader';
import {Observable, of} from 'rxjs';

export class AdapterWithoutBackend implements UploaderAdapter<FilePreviewModel> {

    uploadFile(fileItem: FilePreviewModel): Observable<HttpEvent<FilePreviewModel>> {
        return of(new HttpResponse({
            body: fileItem
        }));
    }

    // tslint:disable-next-line:no-any : Dépend de la librairie ngx-awesome-uploader
    removeFile(fileItem: FilePreviewModel): Observable<any> {
        return of(void 0);
    }

}
