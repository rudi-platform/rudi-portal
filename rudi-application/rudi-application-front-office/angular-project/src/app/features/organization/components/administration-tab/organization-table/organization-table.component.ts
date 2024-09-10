import {Component, Input, OnInit} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {Organization} from 'micro_service_modules/strukture/strukture-model';

@Component({
    selector: 'app-organization-table',
    templateUrl: './organization-table.component.html',
    styleUrls: ['./organization-table.component.scss']
})
export class OrganizationTableComponent implements OnInit {
    @Input() isLoading: boolean;
    @Input() organization: Organization;
    @Input() enableCaptchaOnPage: boolean;
    displayedColumns: string[] = ['name', 'id'];
    dataSource: MatTableDataSource<Organization> = new MatTableDataSource([]);


    constructor(private readonly iconRegistryService: IconRegistryService) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }

    ngOnInit(): void {
        this.dataSource = new MatTableDataSource([this.organization]);
    }

}
