import {Component, Input, OnInit} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {MatRadioChange} from '@angular/material/radio';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {TranslateService} from '@ngx-translate/core';
import {OwnerType} from 'micro_service_modules/projekt/projekt-model';
import {forkJoin} from 'rxjs';
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

    error: {
        isError: boolean,
        message: {
            messageBeforeLink: string,
            linkLabel: string,
            messageAfterLink: string
        },
        linkHref: string,
    };

    constructor(
        private readonly translateService: TranslateService,
        private readonly propertiesMetierService: PropertiesMetierService
    ) {
    }

    ngOnInit(): void {
        forkJoin({
            messageBeforeLink: this.translateService.get('project.stepper.submission.step2.ownerType.organization.userHasNoOrganization.messageBeforeLink'),
            linkHref: this.propertiesMetierService.get('front.contact'),
            linkLabel: this.translateService.get('project.stepper.submission.step2.ownerType.organization.userHasNoOrganization.linkLabel'),
            messageAfterLink: this.translateService.get('project.stepper.submission.step2.ownerType.organization.userHasNoOrganization.messageAfterLink'),
        }).subscribe(({messageBeforeLink, linkHref, linkLabel, messageAfterLink}) => {
            this.error = {
                isError: false,
                message: {
                    messageBeforeLink,
                    linkLabel,
                    messageAfterLink
                },
                linkHref
            };
        });
    }

    onChangeOwnerType($event: MatRadioChange): void {
        const ownerType = $event.value as OwnerType;
        this.error.isError = ownerType === OwnerType.Organization && !this.organizationItems?.length;
    }

    redirectContact(): void {
        window.open(this.error.linkHref, '_blank');
    }
}
