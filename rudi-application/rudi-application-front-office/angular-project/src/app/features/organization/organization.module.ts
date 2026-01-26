import {CommonModule} from '@angular/common';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatTableModule} from '@angular/material/table';
import {CoreModule} from '@core/core.module';
import {DataSetModule} from '@features/data-set/data-set.module';
import {ListContainerComponent} from '@features/organization/components/list-container/list-container.component';
import {OrganizationOrderComponent} from '@features/organization/components/order/organization-order.component';
import {ListComponent} from '@features/organization/pages/list/list.component';
import {SharedModule} from '@shared/shared.module';
import {AdministrationTabComponent} from './components/administration-tab/administration-tab.component';
import {
    DeletionMemberConfirmationPopinComponent
} from './components/administration-tab/deletion-member-confirmation-popin/deletion-member-confirmation-popin.component';
import {
    OrganizationMembersTableComponent
} from './components/administration-tab/organization-members-table/organization-members-table.component';
import {OrganizationTableComponent} from './components/administration-tab/organization-table/organization-table.component';
import {OrganizationInformationsComponent} from './components/organization-informations/organization-informations.component';
import {OrganizationRoutingModule} from './organization-routing.module';
import {DetailComponent} from './pages/detail/detail.component';


@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        CoreModule,
        OrganizationRoutingModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        DataSetModule,
        OrganizationOrderComponent,
        ListContainerComponent,
        DetailComponent,
        ListComponent,
        OrganizationInformationsComponent,
        AdministrationTabComponent,
        OrganizationMembersTableComponent,
        OrganizationTableComponent,
        DeletionMemberConfirmationPopinComponent,
        DeletionMemberConfirmationPopinComponent,
    ],
    providers: [
        {
            provide: 'DEFAULT_LANGUAGE',
            useValue: 'fr'
        }
    ],
    exports: [
        OrganizationOrderComponent
    ],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ]
})
export class OrganizationModule {
}
