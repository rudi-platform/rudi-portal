import {NgIf} from '@angular/common';
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatButton, MatMiniFabButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderCellDef,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatTable,
    MatTableDataSource
} from '@angular/material/table';
import {NgbPopover} from '@ng-bootstrap/ng-bootstrap';
import {TranslateDirective, TranslatePipe} from '@ngx-translate/core';
import {DatasetsInfosComponent} from '@shared/business/dataset/common/dataset-infos/dataset-infos.component';
import {RowTableData} from '@shared/business/projects/projects-datasets-tables/dataset.interface';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';

@Component({
    selector: 'app-dataset-table',
    templateUrl: './dataset-table.component.html',
    styleUrls: ['./dataset-table.component.scss'],
    imports: [NgIf, TranslateDirective, LoaderComponent, MatTable, MatColumnDef, MatHeaderCellDef, MatHeaderCell, MatCellDef, MatCell, DatasetsInfosComponent, NgbPopover, MatMiniFabButton, MatIcon, MatHeaderRowDef, MatHeaderRow, MatRowDef, MatRow, MatButton, TranslatePipe]
})
export class DatasetTableComponent {
    displayedColumns: string[] = ['addedDate', 'title', 'status', 'comment-action', 'delete-action'];
    addButtonIcon: string;

    /**
     * titre du tableau
     */
    @Input()
    title: string;
    /**
     * titre du tableau
     */
    @Input()
    showHorizontalSeparator: boolean = true;

    /**
     * Label RGAA pour le tableau
     */
    @Input()
    ariaLabel: string;

    /**
     * Données (lignes) à afficher dans le tableau
     */
    @Input()
    dataSource: MatTableDataSource<RowTableData>;

    /**
     * Boolean indiquant l'état du tableau (chargement des données fini ou pas)
     */
    @Input()
    tableLoading: boolean;

    /**
     * Boolen permettant de désactiver le bouton d'ajout (si un autre ajout est déjà en cours dans un tableau)
     */
    @Input()
    disableAddButton: boolean;

    /**
     * Détermine si on affiche le bouton d'ajout de dataset
     */
    @Input()
    hasAddButton: boolean;

    /**
     * Détermine si on affiche le bouton de suppression de dataset
     */
    @Input()
    hasDeleteButton: boolean;

    @Input()
    hasCommentButton: boolean;

    @Input() defaultAlt: string;

    @Input() defaultLogo: string;

    @Output()
    addActionEvent: EventEmitter<void>;

    @Output()
    deleteActionEvent: EventEmitter<RowTableData>;

    @Output()
    commentActionEvent: EventEmitter<RowTableData>;

    constructor() {
        this.addButtonIcon = 'icon_add_blue';
        this.dataSource = new MatTableDataSource([]);
        this.tableLoading = true;
        this.disableAddButton = false;
        this.hasAddButton = false;
        this.hasCommentButton = false;

        this.addActionEvent = new EventEmitter();
        this.deleteActionEvent = new EventEmitter();
        this.commentActionEvent = new EventEmitter();
    }

    addAction(): void {
        this.addActionEvent.emit();
    }

    deleteAction(element: RowTableData): void {
        this.deleteActionEvent.emit(element);
    }

    commentAction(element: RowTableData): void {
        this.commentActionEvent.emit(element);
    }

    onMouseEnterAddButton(): void {
        this.addButtonIcon = 'icon_add_light';
    }

    onMouseLeaveAddButton(): void {
        this.addButtonIcon = 'icon_add_blue';
    }
}
