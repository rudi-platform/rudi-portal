import {Component, Input, OnInit} from '@angular/core';
import {FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatRadioChange, MatRadioGroup, MatRadioButton} from '@angular/material/radio';
import {OwnerType} from 'micro_service_modules/projekt/projekt-model';
import {OrganizationItem} from '../../model/organization-item';

import {MatFormField, MatError} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatSelect} from '@angular/material/select';
import {MatOption} from '@angular/material/core';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-step2-project',
    templateUrl: './step2-project.component.html',
    styleUrls: ['./step2-project.component.scss'],
    imports: [FormsModule, ReactiveFormsModule, MatRadioGroup, MatRadioButton, MatFormField, MatInput, MatSelect, MatOption, MatError, TranslatePipe]
})
export class Step2ProjectComponent implements OnInit {

    @Input()
    public step2FormGroup: FormGroup;

    @Input()
    public organizationItems: OrganizationItem[];

    hasNoOrganizationError: boolean;

    ngOnInit(): void {
        this.hasNoOrganizationError = false;
    }

    onChangeOwnerType($event: MatRadioChange): void {
        const ownerType = $event.value as OwnerType;
        this.hasNoOrganizationError = ownerType === OwnerType.Organization && !this.organizationItems?.length;
    }

}
