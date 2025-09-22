import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {BreakpointObserverService} from '@core/services/breakpoint-observer.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {ProjectKey} from 'micro_service_modules/acl/acl-model';
import {ProjectKeyCredential, ProjektService} from 'micro_service_modules/projekt/projekt-api';
import {Project} from 'micro_service_modules/projekt/projekt-model';
import moment from 'moment';

/**
 * Les données que peuvent accepter la Dialog
 */
export interface GenerateKeysDialogData {
    data: Project;
}

@Component({
    selector: 'app-generate-keys-dialog',
    templateUrl: './generate-keys-dialog.component.html',
    styleUrl: './generate-keys-dialog.component.scss',
    standalone: false
})
export class GenerateKeysDialogComponent implements OnInit {

    generateKeyForm1: FormGroup;
    generateKeyForm2: FormGroup;

    isFirstStepActive: boolean = true;
    isSecondStepActive: boolean = false;
    isLoading: boolean = false;
    public rudiDocLink: string;
    project: Project;

    public passwordError: boolean;
    public hidePassword;

    constructor(
        public dialogRef: MatDialogRef<GenerateKeysDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public dialogData: GenerateKeysDialogData,
        private readonly matIconRegistry: MatIconRegistry,
        private readonly propertiesMetierService: PropertiesMetierService,
        private readonly domSanitizer: DomSanitizer,
        private readonly formBuilder: FormBuilder,
        private readonly projektService: ProjektService,
        private readonly breakpointObserver: BreakpointObserverService) {
        if (this.dialogData) {
            this.project = this.dialogData.data;
        }
        this.matIconRegistry.addSvgIcon(
            'icon-close',
            this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/icon-close.svg')
        );
        this.hidePassword = true;
    }

    showPassword(event: MouseEvent) {
        this.hidePassword = !this.hidePassword;
        event.stopPropagation();
    }

    ngOnInit(): void {
        this.propertiesMetierService.get('front.docRudi').subscribe({
            next: (rudiDocLink: string) => {
                this.rudiDocLink = rudiDocLink;
            }
        });

        const aYearFromNow = new Date();
        aYearFromNow.setFullYear(aYearFromNow.getFullYear() + 1);

        this.generateKeyForm1 = this.formBuilder.group({
            name: ['', Validators.required],
            expirationDate: [moment(aYearFromNow), Validators.required],
            password: ['', Validators.required]
        });
        this.generateKeyForm2 = this.formBuilder.group({
            name: [''],
            consumerKey: [''],
            consumerSecret: [''],
            confirmed: [false, Validators.required]
        });
        this.generateKeyForm1.controls['expirationDate'].setValue(moment(aYearFromNow));
        this.generateKeyForm2.controls['name'].disable({emitEvent: false});
    }

    confirmForm1(): void {
        const expirationDateFromNow = new Date();
        expirationDateFromNow.setFullYear(expirationDateFromNow.getFullYear() + 1);
        const projectKey = {
            name: this.generateKeyForm1.get('name').value,
            creationDate: (new Date().toISOString()),
            expirationDate: expirationDateFromNow.toISOString()
        };
        const password = this.generateKeyForm1.get('password').value;
        const projectKeyCredential: ProjectKeyCredential = {
            project_key: projectKey,
            password: password
        };
        this.isFirstStepActive = false;
        this.isLoading = true;
        this.projektService.createProjectKey(this.project.uuid, projectKeyCredential).subscribe({
            next: (projectKey: ProjectKey) => {
                this.generateKeyForm2.controls['name'].setValue(projectKey.name);
                this.generateKeyForm2.controls['consumerKey'].setValue(projectKey.client_id);
                this.generateKeyForm2.controls['consumerSecret'].setValue(projectKey.client_secret);
                this.isLoading = false;
                this.isSecondStepActive = true;
            },
            error: (error) => {
                this.isLoading = false;
                this.passwordError = true;
                this.isFirstStepActive = true;
            }
        });
    }

}
