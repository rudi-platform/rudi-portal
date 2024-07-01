import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {DialogClosedData} from '@features/data-set/models/dialog-closed-data';
import {
    DialogSubscribeDatasetsComponent,
    DialogSubscribeDatasetsData
} from '@features/personal-space/components/dialog-subscribe-datasets/dialog-subscribe-datasets.component';
import {
    ProjectModificationConfirmationPopinComponent
} from '@features/project/components/project-modification-confirmation-popin/project-modification-confirmation-popin.component';
import {
    DeletionConfirmationPopinComponent
} from '@shared/project-datasets-tables/deletion-confirmation-popin/deletion-confirmation-popin.component';
import {Project} from 'micro_service_modules/projekt/projekt-api';
import {Observable} from 'rxjs';
import {LinkedDatasetMetadatas} from './asset/project/project-dependencies.service';
import {DefaultMatDialogConfig} from './default-mat-dialog-config';


@Injectable({
    providedIn: 'root'
})
export class DialogSubscribeDatasetsService {

    constructor(private readonly dialog: MatDialog) {
    }

    /**
     * Ouverture d'une dialog permettant de saisir les JDDs auxquels souscrire
     * @param linkedDatasetMetadatas tous les JDDs des demandes d'un projet et leurs métadatas
     * @param project le projet
     */
    public openDialogSelectDatasetsToSubscribe(linkedDatasetMetadatas: LinkedDatasetMetadatas[],
                                               project: Project): Observable<DialogClosedData<void>> {
        const dialogConfig = new DefaultMatDialogConfig<DialogSubscribeDatasetsData>();
        dialogConfig.data = {
            data: {
                linkedDatasetMetadatas,
                project
            }
        };

        const dialogRef = this.dialog.open(DialogSubscribeDatasetsComponent, dialogConfig);
        return dialogRef.afterClosed();
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
    public openDialogUpdateConfirmation(): Observable<DialogClosedData<string>> {
        const dialogConfig = new DefaultMatDialogConfig<string>();
        const dialogRef = this.dialog.open(ProjectModificationConfirmationPopinComponent, dialogConfig);
        return dialogRef.afterClosed();
    }
}
