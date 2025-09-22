import { HttpEvent } from '@angular/common/http';
import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {AttachmentService} from '@core/services/attachment.service';
import {DataSize} from '@shared/models/data-size';
import {FilePreviewModel} from '@sleiss/ngx-awesome-uploader';
import {DocumentMetadata, SelfdataService} from 'micro_service_modules/selfdata/selfdata-api';
import {FrontOfficeProperties, SelfdataRequestAllowedAttachementType} from 'micro_service_modules/selfdata/selfdata-model';
import {Observable} from 'rxjs';
import {PropertiesAdapter} from './properties-adapter';


@Injectable({
    providedIn: 'root'
})
export class SelfdataAttachmentService extends AttachmentService {

    private readonly propertiesAdapter: PropertiesAdapter<FrontOfficeProperties>;

    constructor(
        private readonly selfdataService: SelfdataService,
        private readonly dialog: MatDialog,
    ) {
        super();
        this.propertiesAdapter = new class extends PropertiesAdapter<FrontOfficeProperties> {
            protected fetchBackendProperties(): Observable<FrontOfficeProperties> {
                return selfdataService.getFrontOfficeProperties();
            }
        }();
    }

    uploadAttachment(file: FilePreviewModel): Observable<HttpEvent<string>> {
        return this.selfdataService.uploadAttachment(file.file, 'events', true);
    }

    deleteAttachment(uuid: string): Observable<void> {
        return this.selfdataService.deleteAttachment(uuid);
    }

    getDataSizeProperty(key: string): Observable<DataSize> {
        return this.propertiesAdapter.getDataSize(key);
    }

    downloadAttachement(uuid: string): Observable<Blob> {
        return this.selfdataService.downloadAttachment(uuid);
    }

    getAttachmentMetadata(uuid: string): Observable<DocumentMetadata> {
        return this.selfdataService.getAttachmentMetadata(uuid);
    }

    getAllowedAttachementTypes(): Observable<Array<SelfdataRequestAllowedAttachementType>> {
        return this.selfdataService.getAllowedAttachementTypes();
    }
}
