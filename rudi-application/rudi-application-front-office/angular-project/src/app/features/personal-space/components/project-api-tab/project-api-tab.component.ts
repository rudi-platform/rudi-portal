import {Clipboard} from '@angular/cdk/clipboard';
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {map} from 'rxjs/operators';
import {KonsultApiAccessService} from '@core/services/api-access/konsult/konsult-api-access.service';
import {ProjectDependenciesService} from '@core/services/asset/project/project-dependencies.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {UserService} from '@core/services/user.service';
import {ApiKeys} from 'micro_service_modules/konsult/konsult-api';
import {OwnerType, Project} from 'micro_service_modules/projekt/projekt-model';

@Component({
    selector: 'app-project-api-tab',
    templateUrl: './project-api-tab.component.html',
    styleUrls: ['./project-api-tab.component.scss']
})
export class ProjectApiTabComponent implements OnInit {

    private project: Project;
    public keys: ApiKeys;
    public loading: boolean;
    public passwordError: boolean;
    public hidePassword = true;
    public hideIdentificationCard = false;
    public isOwnerTypeUser = false;
    public password: string;
    public rudiDocLink: string;

    constructor(private readonly route: ActivatedRoute,
                private readonly apiAccessService: KonsultApiAccessService,
                private readonly projectDependenciesService: ProjectDependenciesService,
                private readonly propertiesMetierService: PropertiesMetierService,
                private readonly utilisateurService: UserService,
                private readonly clipboard: Clipboard,
                private readonly translateService: TranslateService) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.projectUuid = params.projectUuid;
        });

        this.propertiesMetierService.get('rudidatarennes.docRudiBzh').subscribe({
            next: (rudiDocLink: string) => {
                this.rudiDocLink = rudiDocLink;
            }
        });
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

    getApiKeys(): void {
        this.keys = null;
        this.loading = true;
        this.passwordError = false;
        this.apiAccessService.getConsumerKeys(this.password, this.project.owner_type, this.project.owner_uuid).subscribe({
                next: (keys: ApiKeys) => {
                    this.loading = false;
                    this.keys = keys;
                    this.hideIdentificationCard = true;
                },
                error: (e) => {
                    this.loading = false;
                    this.hideIdentificationCard = false;
                    console.error(e);
                    this.passwordError = true;
                }
            }
        );
    }

    /**
     * Méthode qui récupère le mot de passe entré par l'utilisateur
     */
    handlePasswordChanged(password: string): void {
        this.password = password;
    }

    /**
     * Affiche le bon label en fonction du Owner du projet
     */
    getLabel(): string {
        if (this.isOwnerTypeUser) {
            return this.translateService.instant('personalSpace.projectApi.textOwner');
        } else {
            return this.translateService.instant('personalSpace.projectApi.textUser');
        }
    }
}
