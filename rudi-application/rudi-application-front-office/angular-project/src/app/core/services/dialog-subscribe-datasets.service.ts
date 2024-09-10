import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {DialogClosedData} from '@features/data-set/models/dialog-closed-data';
import {
    ProjectModificationConfirmationPopinComponent
} from '@features/project/components/project-modification-confirmation-popin/project-modification-confirmation-popin.component';
import {
    DeletionConfirmationPopinComponent
} from '@shared/project-datasets-tables/deletion-confirmation-popin/deletion-confirmation-popin.component';
import {Observable} from 'rxjs';
import {DefaultMatDialogConfig} from './default-mat-dialog-config';


@Injectable({
    providedIn: 'root'
})
export class DialogSubscribeDatasetsService {

    constructor(private readonly dialog: MatDialog) {
    }

    /**
     * Ouverture d'une dialog permettant de supprimer un jdd de notre projet
     * @param requestUuid
     */
    public openDialogDeletionConfirmation(requestUuid: string): Observable<DialogClosedData<string>> {
        const dialogConfig = new DefaultMatDialogConfig<string>();
        dialogConfig.data = requestUuid;
        const dialogRef = this.dialog.open(DeletionConfirmationPopinComponent, dialogConfig);
        return dialogRef.afterClosed();
    }

    /**
     * Ouverture d'une dialog permettant de confirmer la modification de la réutilisation
     */
    public openDialogUpdateConfirmation(dialogDescription: string): Observable<DialogClosedData<string>> {
        const dialogConfig = new DefaultMatDialogConfig<string>();
        const dialogRef = this.dialog.open(ProjectModificationConfirmationPopinComponent, dialogConfig);
        dialogRef.componentInstance.dialogDescription = dialogDescription;
        return dialogRef.afterClosed();
    }
}
