import {Component, DestroyRef, Input, inject} from '@angular/core';
import {HistoricInformation, ProcessHistoricInformation} from 'micro_service_modules/api-bpmn';
import {DatePipe} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';
import {ProjectTaskHistoryService} from '@features/personal-space/services/project-task-history.service';
import {of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatTableModule} from '@angular/material/table';
import {MatSortModule, Sort} from '@angular/material/sort';

type SortKey = 'endTime' | 'assignee' | 'activityName' | 'action';
type SortDirection = 'asc' | 'desc';

@Component({
    selector: 'app-project-task-historic',
    standalone: true,
    templateUrl: './project-task-historic.component.html',
    styleUrls: ['./project-task-historic.component.scss'],
    imports: [DatePipe, TranslatePipe, MatTableModule, MatSortModule]
})
export class ProjectTaskHistoricComponent {

    private readonly destroyRef = inject(DestroyRef);

    private readonly projectTaskHistoryService = inject(ProjectTaskHistoryService);

    private loadedProcessHistoricInformation: ProcessHistoricInformation | null = null;

    private explicitProcessHistoricInformation: ProcessHistoricInformation | null | undefined;

    private sortKey: SortKey = 'endTime';
    private sortDirection: SortDirection = 'desc';

    readonly displayedColumns: SortKey[] = ['endTime', 'assignee', 'activityName', 'action'];

    sortedHistoricInformations: HistoricInformation[] = [];

    @Input()
    set processHistoricInformation(value: ProcessHistoricInformation | null | undefined) {
        this.explicitProcessHistoricInformation = value;
        if (value != null) {
            this.loadedProcessHistoricInformation = null;
        }

        this.refreshDataSource();
    }

    get processHistoricInformation(): ProcessHistoricInformation | null | undefined {
        return this.explicitProcessHistoricInformation ?? this.loadedProcessHistoricInformation;
    }

    @Input()
    set taskId(value: string | null | undefined) {
        if (!value || this.explicitProcessHistoricInformation != null) {
            return;
        }

        this.projectTaskHistoryService.getProjectTaskHistoryByTaskId(value).pipe(
            catchError(() => of(null)),
            takeUntilDestroyed(this.destroyRef),
        ).subscribe(processHistoricInformation => {
            this.loadedProcessHistoricInformation = processHistoricInformation;
            this.refreshDataSource();
        });
    }

    onMatSortChange(sort: Sort): void {
        if (!sort.active) {
            return;
        }

        if (!this.isSortKey(sort.active)) {
            return;
        }

        if (sort.direction !== 'asc' && sort.direction !== 'desc') {
            return;
        }

        this.sortKey = sort.active;
        this.sortDirection = sort.direction;
        this.refreshDataSource();
    }

    private getSortedHistoricInformations(): HistoricInformation[] {
        const historicInformations = this.processHistoricInformation?.historicInformations ?? [];
        if (!this.sortKey) {
            return historicInformations;
        }

        const sortKey = this.sortKey;
        const directionFactor = this.sortDirection === 'asc' ? 1 : -1;

        return [...historicInformations].sort((a, b) => {
            const aValue = this.getComparableValue(a, sortKey);
            const bValue = this.getComparableValue(b, sortKey);

            if (aValue === bValue) {
                return 0;
            }

            return (aValue < bValue ? -1 : 1) * directionFactor;
        });
    }

    private refreshDataSource(): void {
        this.sortedHistoricInformations = this.getSortedHistoricInformations();
    }

    private isSortKey(value: string): value is SortKey {
        return value === 'endTime' || value === 'assignee' || value === 'activityName' || value === 'action';
    }

    private getComparableValue(historicInformation: HistoricInformation, key: SortKey): string {
        switch (key) {
            case 'endTime':
                return this.normalizeDate(historicInformation.endTime);
            case 'assignee':
                return this.getAssigneeDisplayName(historicInformation.assignee).toLocaleLowerCase();
            case 'activityName':
                return (historicInformation.activityName ?? '').toLocaleLowerCase();
            case 'action':
                return (historicInformation.action ?? '').toLocaleLowerCase();
        }
    }

    getAssigneeDisplayName(value?: string | null): string {
        const trimmed = (value ?? '').trim();
        if (!trimmed) {
            return '';
        }

        // Expected format: "Firstname Lastname (email)" => keep only the name.
        const match = /^(.*?)\s*\([^()]*\)\s*$/.exec(trimmed);
        if (match) {
            return (match[1] ?? '').trim();
        }

        return trimmed;
    }

    private normalizeDate(value?: string): string {
        if (!value) {
            return '';
        }

        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }

        return date.toISOString();
    }

    // Note: si processHistoricInformation est fourni, aucun appel HTTP n'est fait.
}
