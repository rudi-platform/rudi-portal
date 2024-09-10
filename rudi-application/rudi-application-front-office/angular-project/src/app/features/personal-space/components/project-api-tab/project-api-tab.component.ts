import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ActivatedRoute} from '@angular/router';
import {GenerateKeysDialogComponent} from '@features/personal-space/components/generate-keys-dialog/generate-keys-dialog.component';
import {ProjectKey} from 'micro_service_modules/acl/acl-model';
import { ProjectKeyPageResult, ProjektService} from 'micro_service_modules/projekt/projekt-api';
import * as moment from 'moment/moment';
import {map} from 'rxjs/operators';
import {ProjectDependenciesService} from '@core/services/asset/project/project-dependencies.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {ApiKeys} from 'micro_service_modules/konsult/konsult-api';
import {OwnerType, Project} from 'micro_service_modules/projekt/projekt-model';

export interface ProjectKeyTableData {
    creationDate: string;
    name: string;
    expirationDate: string;
    action: string;
}

@Component({
    selector: 'app-project-api-tab',
    templateUrl: './project-api-tab.component.html',
    styleUrls: ['./project-api-tab.component.scss']
})
export class ProjectApiTabComponent implements OnInit {

    @ViewChild(MatSort) sort: MatSort;

    private project: Project;
    public keys: ApiKeys;
    public loading: boolean;
    public hidePassword = true;
    public isOwnerTypeUser = false;
    public password: string;
    public rudiDocLink: string;
    public isKeysLoading = false;

    projectKeys: ProjectKeyTableData[] = [];
    projectKeysTotal: number = 0;
    displayedColumns: string[] = ['creationDate', 'name', 'expirationDate', 'action'];
    dataSource: MatTableDataSource<ProjectKeyTableData> = new MatTableDataSource(this.projectKeys);


    constructor(private readonly route: ActivatedRoute,
                private readonly projectDependenciesService: ProjectDependenciesService,
                private readonly propertiesMetierService: PropertiesMetierService,
                private projektService: ProjektService,
                private readonly dialog: MatDialog) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.projectUuid = params.projectUuid;
            this.getProjectKeys(params.projectUuid);
        });

        this.propertiesMetierService.get('front.docRudi').subscribe({
            next: (rudiDocLink: string) => {
                this.rudiDocLink = rudiDocLink;
            }
        });
    }

    ngAfterViewInit() {
        this.dataSource.sort = this.sort;
    }

    set projectUuid(uuid: string) {
        this.projectDependenciesService.getProject(uuid).pipe(
            map(({project, dependencies}) => {
                return project;
            }),
        ).subscribe({
            next: (project: Project) => {
                this.project = project;
                this.isOwnerTypeUser = this.project.owner_type === OwnerType.User;
            },
            error: (error) => {
                console.error(error);
            }
        });
    }


    /**
     * Générer une clé
     */
    generateKey(): void {
        const dialogConfig = new MatDialogConfig();

        dialogConfig.disableClose = true;
        dialogConfig.autoFocus = false;
        dialogConfig.width = '768px';
        dialogConfig.data = {data: this.project};
        const dialogRef = this.dialog.open(GenerateKeysDialogComponent, dialogConfig);
        dialogRef.afterClosed().subscribe(() => this.getProjectKeys(this.project.uuid));
    }

    /**
     * Récupérer les paires de clé
     */

    getProjectKeys(projectUuid: string): void {
        this.isKeysLoading = true;
        this.projektService.searchProjectKeys(projectUuid).subscribe({
            next: (keys: ProjectKeyPageResult) => {
                this.projectKeysTotal = keys.total;
                if (keys && keys.elements.length > 0) {
                    this.projectKeys = keys.elements.map((projectKey: ProjectKey) => {
                        return {
                            name: projectKey.name,
                            creationDate: moment(projectKey.creationDate).format('DD/MM/YYYY'),
                            expirationDate: moment(projectKey.expirationDate).format('DD/MM/YYYY'),
                            action: projectKey.client_id
                        };
                    });
                    this.dataSource = new MatTableDataSource(this.projectKeys);
                }
                this.isKeysLoading = false;
            },
            error: (error) => {
                console.error(error);
                this.isKeysLoading = false;
            }
        });
    }
}
