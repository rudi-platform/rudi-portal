import { AsyncPipe } from '@angular/common';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatCard, MatCardContent} from '@angular/material/card';
import {MatDialog} from '@angular/material/dialog';
import {BreakpointObserverService, NgClassObject} from '@core/services/breakpoint-observer.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {OrganizationTaskMetierService} from '@core/services/tasks/strukture/organization/organization-task-metier.service';
import {UserService} from '@core/services/user.service';
import {CloseEvent} from '@features/data-set/models/dialog-closed-data';
import {
    OrganizationFormDialogComponent
} from '@features/personal-space/components/organization-form-dialog/organization-form-dialog.component';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {ListOrganizationCardComponent} from '@shared/business/organisation/list-organization-card/list-organization-card.component';
import {SearchOrganizationsService} from '@shared/business/organisation/list-organization-card/search-organizations.service';
import {ErrorBoxComponent} from '@shared/core/common/error-box/error-box.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {Level} from '@shared/core/layout/notification-template/notification-template.component';
import {Form, Organization, OrganizationBean, OrganizationService, Task, TaskService} from 'micro_service_modules/strukture/api-strukture';
import {Observable, switchMap} from 'rxjs';

export const SECTION_NAME_IMAGE_ORGANIZATION = 'image-organization';
export const FIELD_NAME_IMAGE_ORGANIZATION = 'organizationImage';


@Component({
    selector: 'app-organization-tab',
    templateUrl: './organization-tab.component.html',
    styleUrls: ['./organization-tab.component.scss'],
    imports: [LoaderComponent, ErrorBoxComponent, MatButton, MatCard, MatCardContent, ListOrganizationCardComponent, AsyncPipe, TranslatePipe]
})
export class OrganizationTabComponent implements OnInit, OnDestroy {
    isLoading: boolean;
    page: number;
    errorLoading: boolean;
    itemsPerPage: number;

    organizations$: Observable<OrganizationBean[]>;
    totalOrganizations$: Observable<number>;
    projectCountLoading$: Observable<boolean>;
    datasetCountLoading$: Observable<boolean>;
    public isSubmitted: boolean;
    draftForm: { isLoading: boolean, isError: boolean, form: Form };

    constructor(
        private readonly breakpointObserver: BreakpointObserverService,
        private readonly utilisateurService: UserService,
        private readonly organizationService: OrganizationService,
        private readonly organizationTaskMetierService: OrganizationTaskMetierService,
        private readonly propertiesMetierService: PropertiesMetierService,
        private readonly searchOrganizationsService: SearchOrganizationsService,
        private readonly dialog: MatDialog,
        private readonly snackBarService: SnackBarService,
        private readonly translateService: TranslateService,
        private readonly taskService: TaskService
    ) {
        this.itemsPerPage = 9;
        this.organizations$ = searchOrganizationsService.organizations$;
        this.totalOrganizations$ = searchOrganizationsService.totalOrganizations$;
        this.datasetCountLoading$ = searchOrganizationsService.datasetCountLoading$;
        this.projectCountLoading$ = searchOrganizationsService.projectsCountLoading$;
    }

    get paginationControlsNgClass(): NgClassObject {
        return this.breakpointObserver.getNgClassFromMediaSize('pagination-spacing');
    }

    onPageChange(page: number): void {
        this.searchOrganizationsService.currentPage$.next(page);
    }

    ngOnInit(): void {
        this.isLoading = true;
        this.getMyOrganisations();
        this.getDraftForm();
    }

    ngOnDestroy(): void {
        this.searchOrganizationsService.complete();
    }

    /**
     * Méthode qui permet de récupérer les organisations du user connecté
     */
    public getMyOrganisations(): void {
        this.searchOrganizationsService.initSubscriptions(true, this.itemsPerPage);
        this.isLoading = false;
        this.errorLoading = false;
    }

    /**
     * Méthode qui permet de récupérer le draft form d'organization
     */
    private getDraftForm(): void {
        this.draftForm = {
            isLoading: true,
            isError: false,
            form: null
        };
        this.taskService.lookupOrganizationDraftForm().subscribe({
            next: (form: Form) => {
                this.draftForm.isLoading = false;
                this.draftForm.form = form;
            },
            error: (err) => {
                this.draftForm.isLoading = false;
                this.draftForm.isError = true;
            }
        });
    }

    /**
     * Quand l'utilisateur click sur le lien equipe technique Rudi
     */
    handleClickContactRudi(): void {
        this.propertiesMetierService.get('front.contact').subscribe(link => {
            window.location.href = link;
        });
    }

    /**
     * Quand l'utilisateur clique sur déclarer une organisation
     */
    handleAddOrganization(): void {
        this.dialog.open(OrganizationFormDialogComponent, {
            data: {draftForm: this.draftForm.form}
        }).afterClosed().subscribe((result: { closeEvent: CloseEvent, data: Organization }) => {
            if (result?.closeEvent !== null && result.closeEvent === CloseEvent.VALIDATION) {
                this.createOrganization(result.data);
            }
        });
    }

    /**
     * Création de l'organisation avec l'image (si présente)
     */
    createOrganization(organization: Organization): void {
        this.organizationService.createOrganization(organization).pipe(
            // 1) On crée le draft à partir d'une organization
            switchMap((organization: Organization) => {
                return this.organizationTaskMetierService.createDraft(organization).pipe(
                    // Puis appeler le startTask
                    switchMap((task: Task) => {
                        task.asset.form = this.draftForm.form;
                        return this.organizationTaskMetierService.startTask(task);
                    })
                );
            })
        ).subscribe({
            next: (value: Task) => {
                this.snackBarService.openSnackBar({
                    level: Level.SUCCESS,
                    message: this.translateService.instant('personalSpace.organization.success')
                }, 3000);
                this.isSubmitted = true;
                this.isLoading = false;
            },
            error: err => {
                this.snackBarService.openSnackBar({
                    level: Level.ERROR,
                    message: this.translateService.instant('personalSpace.organization.error')
                }, 3000);
                this.isLoading = false;
            }
        });
    }
}
