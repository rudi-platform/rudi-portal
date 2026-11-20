import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatSort} from '@angular/material/sort';
import {By} from '@angular/platform-browser';
import {DialogMemberOrganizationService} from '@core/services/organization/dialog-member-organization.service';
import {OrganizationMetierService} from '@core/services/organization/organization-metier.service';
import {CloseEvent, DialogClosedData} from '@features/data-set/models/dialog-closed-data';
import {OrganizationRole} from 'micro_service_modules/strukture/api-strukture';
import {Organization, OrganizationMember} from 'micro_service_modules/strukture/strukture-model';
import {NEVER, Subject} from 'rxjs';

import {OrganizationMembersTableComponent} from './organization-members-table.component';

describe('OrganizationMembersTableComponent', () => {
    let component: OrganizationMembersTableComponent;
    let fixture: ComponentFixture<OrganizationMembersTableComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [OrganizationMembersTableComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(OrganizationMembersTableComponent);
        component = fixture.componentInstance;
        component.organization = {uuid: 'org-uuid', name: 'Org'} as Organization;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should label the search field and hide the decorative icon (RGAA 11.1, 1.2)', () => {
        const input: HTMLInputElement = fixture.nativeElement.querySelector('input[type="search"]');
        expect(input.getAttribute('aria-label')).toBe('metaData.administrationTab.membersTable.searchLabel');
        expect(input.getAttribute('title')).toBe('metaData.administrationTab.membersTable.searchLabel');
        expect(fixture.nativeElement.querySelector('.search-box-icon mat-icon').getAttribute('aria-hidden')).toBe('true');
    });

    it('should clear the search with a named button and give focus back to the field (RGAA 7.1)', () => {
        spyOn(component, 'onReset');
        component.searchText = 'term';
        fixture.detectChanges();

        const button: HTMLButtonElement = fixture.nativeElement.querySelector('button.search-box-clear-button');
        expect(button.getAttribute('type')).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('common.clearSearch');

        button.click();

        expect(component.onReset).toHaveBeenCalled();
        expect(document.activeElement).toBe(fixture.nativeElement.querySelector('input[type="search"]'));
    });

    it('should expose the title as a heading naming the members table (RGAA 9.1, 5.4)', () => {
        const heading: HTMLElement = fixture.nativeElement.querySelector('.title-label h2');
        expect(heading.textContent).toContain('metaData.administrationTab.membersTable.resultText');
        const table: HTMLElement = fixture.nativeElement.querySelector('table.mat-table-members');
        expect(table.getAttribute('aria-labelledby')).toBe(heading.id);
        expect(table.querySelector('caption')).toBeNull();
    });

    it('should name the actions button of each member and the actions column (RGAA 11.9, 5.6)', () => {
        component.organizationMembersList = [
            {user_uuid: 'user-uuid', firstname: 'Jane', lastname: 'Doe', login: 'jane@example.org', role: OrganizationRole.Editor}
        ];
        component.membersTotal = 1;
        fixture.detectChanges();

        const button: HTMLButtonElement = fixture.nativeElement.querySelector('td button.action-fab-btn-icon');
        expect(button.getAttribute('type')).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('metaData.administrationTab.actionsFor');
        const headers: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('th'));
        expect(headers[headers.length - 1].textContent.trim()).toBe('metaData.administrationTab.actionsHeader');
    });

    it('should announce the search result count in a status region present before the search (RGAA 7.5)', () => {
        const status: HTMLOutputElement = fixture.nativeElement.querySelector('output[aria-live="polite"]');
        expect(status).not.toBeNull();
        expect(status.classList).toContain('visually-hidden');

        const handleSuccess = (total: number): void => {
            component['handleSearchOrganizationMembersSuccess']({total, elements: []});
            fixture.detectChanges();
        };

        component.searchText = 'Mark';
        handleSuccess(1);
        expect(status.textContent.replace(/\s+/g, ' ').trim()).toBe('1 searchbox.results searchbox.resultsFor');

        component.searchText = 'zzzz';
        handleSuccess(0);
        expect(status.textContent.trim()).toBe('metaData.administrationTab.membersTable.noResultWithFilter');

        component.searchText = '';
        handleSuccess(3);
        expect(status.textContent.replace(/\s+/g, ' ').trim()).toBe('3 searchbox.resultss');
    });

    it('should expose the real default sort, a translated sort description and no unsorted state (RGAA 7.1, 8.7)', () => {
        const headers: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('th[mat-sort-header]'));
        expect(headers.length).toBe(6);
        headers.forEach(header => {
            const describedBy = header.querySelector('[role="button"]').getAttribute('aria-describedby');
            expect(document.getElementById(describedBy).textContent).toBe('common.sortBy');
        });
        const sorted = headers.filter(header => header.getAttribute('aria-sort') !== 'none');
        expect(sorted.length).toBe(1);
        expect(sorted[0].getAttribute('aria-sort')).toBe('descending');
        expect(sorted[0].textContent.trim()).toBe('metaData.administrationTab.membersTable.addDate');
        expect(fixture.debugElement.query(By.directive(MatSort)).injector.get(MatSort).disableClear).toBeTrue();
    });

    it('should open the member dialogs only once the actions menu is closed (RGAA 12.8)', () => {
        const member = {user_uuid: 'user-uuid', firstname: 'Jane', lastname: 'Doe', login: 'jane@example.org', role: OrganizationRole.Editor};
        const update = spyOn(component, 'handleClickUpdateMember');
        const remove = spyOn(component, 'handleClickDeleteMember');

        component.deferMenuAction('update', member);
        expect(update).not.toHaveBeenCalled();
        component.runPendingMenuAction();
        expect(update).toHaveBeenCalledOnceWith(member);

        component.runPendingMenuAction();
        expect(update).toHaveBeenCalledTimes(1);

        component.deferMenuAction('delete', member);
        component.runPendingMenuAction();
        expect(remove).toHaveBeenCalledOnceWith(member);
    });

    it('should not show nor announce the table loader while a member dialog is open (RGAA 7.5)', () => {
        const member = {user_uuid: 'user-uuid', firstname: 'Jane', lastname: 'Doe', login: 'jane@example.org', role: OrganizationRole.Editor};
        const dialogService = TestBed.inject(DialogMemberOrganizationService);
        spyOn(TestBed.inject(OrganizationMetierService), 'updateOrganizationMember').and.returnValue(NEVER);
        // The initial members search never resolves without backend
        component.searchIsRunning = false;

        const cancelled$ = new Subject<DialogClosedData<OrganizationMember>>();
        spyOn(dialogService, 'openDialogUpdateMember').and.returnValue(cancelled$);
        component.handleClickUpdateMember(member);
        expect(component.searchIsRunning).toBeFalse();
        cancelled$.next({data: null, closeEvent: CloseEvent.CANCEL});
        cancelled$.complete();
        expect(component.searchIsRunning).toBeFalse();

        const validated$ = new Subject<DialogClosedData<OrganizationMember>>();
        (dialogService.openDialogUpdateMember as jasmine.Spy).and.returnValue(validated$);
        component.handleClickUpdateMember(member);
        expect(component.searchIsRunning).toBeFalse();
        validated$.next({data: member, closeEvent: CloseEvent.VALIDATION});
        expect(component.searchIsRunning).toBeTrue();
    });
});
