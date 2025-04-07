import {Component, Input, OnInit} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {MatRadioChange} from '@angular/material/radio';
import {OwnerType} from 'micro_service_modules/projekt/projekt-model';
import {OrganizationItem} from '../../model/organization-item';

@Component({
    selector: 'app-step2-project',
    templateUrl: './step2-project.component.html',
    styleUrls: ['./step2-project.component.scss']
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
