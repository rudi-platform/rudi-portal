import {ComponentType} from '@angular/cdk/portal';
import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {DialogClosedData} from '@features/data-set/models/dialog-closed-data';
import {
    DeletionMemberConfirmationPopinComponent
} from '@features/organization/components/administration-tab/deletion-member-confirmation-popin/deletion-member-confirmation-popin.component';
import {
    OrganizationMemberDialogData
} from '@features/organization/components/administration-tab/organization-members-table/organization-member-dialog-data';
import {OrganizationMember} from 'micro_service_modules/strukture/strukture-model';
import {MemberPopinComponent} from '@shared/member-popin/member-popin.component';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class DialogMemberOrganizationService {

    constructor(private readonly dialog: MatDialog) {
    }

    /**
     * Ouverture d'une dialog permettant d'administrer les membres d'une organisation
     */
    public openDialogAddMember(
        addOrganizationMemberDialogData: OrganizationMemberDialogData
    ): Observable<DialogClosedData<OrganizationMember>> {
        return this.openDialog(MemberPopinComponent, addOrganizationMemberDialogData);
    }

    /**
     * Ouverture d'une dialog permettant d'administrer les membres d'une organisation
     */
    public openDialogUpdateMember(
        updateOrganizationMemberDialogData: OrganizationMemberDialogData
    ): Observable<DialogClosedData<OrganizationMember>> {
        return this.openDialog(MemberPopinComponent, updateOrganizationMemberDialogData);
    }

    /**
     * Ouverture d'une dialog permettant de detacher un membre d'une organisation
     */
    public openDialogDeletionConfirmation(
        organizationMemberDialogData: OrganizationMemberDialogData
    ): Observable<DialogClosedData<OrganizationMember>> {
        return this.openDialog(DeletionMemberConfirmationPopinComponent, organizationMemberDialogData);
    }

    private openDialog<T, D, R>(component: ComponentType<T>, data?: D): Observable<R> {
        return this.dialog.open(component, {data}).afterClosed();
    }
}
