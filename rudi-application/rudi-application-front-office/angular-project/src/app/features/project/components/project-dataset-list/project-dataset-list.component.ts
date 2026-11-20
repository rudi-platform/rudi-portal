import {afterNextRender, Component, ElementRef, EventEmitter, Injector, Input, Output, QueryList, ViewChildren} from '@angular/core';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {TranslatePipe} from '@ngx-translate/core';
import {OrganizationLogoComponent} from '@shared/business/organisation/organization-logo/organization-logo.component';
import {ALL_TYPES as ALL_TITLE_ICON_TYPES, TitleIconType} from '@shared/models/title-icon-type';
import {ProjectDatasetItem} from '../../model/project-dataset-item';
import {ProjectDatasetPictoType} from '../../model/project-dataset-picto-type';

@Component({
    selector: 'app-project-dataset-list',
    templateUrl: './project-dataset-list.component.html',
    styleUrls: ['./project-dataset-list.component.scss'],
    imports: [OrganizationLogoComponent, MatIcon, TranslatePipe]
})
export class ProjectDatasetListComponent {

    private static readonly DEFAULT_ORGANIZATION_LOGO: string = '/assets/images/rudi_default_picto_organization.png';
    private static readonly DEFAULT_NEW_DATASET_REQUEST_LOGO: string = '/assets/images/rudi_picto_nouvelle_demande.svg';
    // Access status conveyed by the title icon (RGAA 1.1)
    private static readonly TITLE_ICON_LABEL_KEYS: Partial<Record<TitleIconType, string>> = {
        'key_icon_88_secondary-color': 'metaData.accesRestreintLibelle',
        'self-data-icon': 'metaData.donneesPersonnellesLibelle'
    };

    @Input()
    public items: ProjectDatasetItem[];

    @Output()
    public edit: EventEmitter<ProjectDatasetItem> = new EventEmitter<ProjectDatasetItem>();

    @Output()
    public delete: EventEmitter<ProjectDatasetItem> = new EventEmitter<ProjectDatasetItem>();

    @ViewChildren('deleteButton')
    private readonly deleteButtons: QueryList<ElementRef<HTMLButtonElement>>;

    constructor(private readonly matIconRegistry: MatIconRegistry,
                private readonly domSanitizer: DomSanitizer,
                private readonly injector: Injector) {
        this.matIconRegistry.addSvgIcon(
            'rudi_picto_nouvelle_demande',
            this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/rudi_picto_nouvelle_demande.svg')
        );
        ALL_TITLE_ICON_TYPES.forEach(titleIconType => this.addSvgIconFromTitleIcon(titleIconType));
    }

    private addSvgIconFromTitleIcon(titleIconType: TitleIconType): void {
        this.matIconRegistry.addSvgIcon(
            titleIconType,
            this.domSanitizer.bypassSecurityTrustResourceUrl(`/assets/icons/${titleIconType}.svg`)
        );
    }

    public isItemLogo(item: ProjectDatasetItem): boolean {
        return item.pictoType === ProjectDatasetPictoType.LOGO;
    }

    public getTitleIconLabelKey(item: ProjectDatasetItem): string | null {
        return ProjectDatasetListComponent.TITLE_ICON_LABEL_KEYS[item.titleIcon] ?? null;
    }

    public handleEdit(item: ProjectDatasetItem): void {
        this.edit.emit(item);
    }

    public handleDelete(item: ProjectDatasetItem): void {
        const index = this.items.indexOf(item);
        this.delete.emit(item);
        // The removed row took the focus with it: move it to the next remove button
        afterNextRender(() => {
            const buttons = this.deleteButtons.toArray();
            buttons[Math.min(index, buttons.length - 1)]?.nativeElement.focus();
        }, {injector: this.injector});
    }

    getDefaultLogo(item: ProjectDatasetItem): string | undefined {
        if (this.isItemLogo(item)) {
            return ProjectDatasetListComponent.DEFAULT_ORGANIZATION_LOGO;
        }
        return ProjectDatasetListComponent.DEFAULT_NEW_DATASET_REQUEST_LOGO;
    }
}
