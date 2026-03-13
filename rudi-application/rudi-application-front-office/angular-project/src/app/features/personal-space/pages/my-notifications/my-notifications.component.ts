
import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {RequestToStudy} from '@core/services/tasks-aggregator/request-to-study.interface';
import {TasksAggregatorService} from '@core/services/tasks-aggregator/tasks-aggregator.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {Level} from '@shared/core/layout/notification-template/notification-template.component';
import {PageTitleComponent} from '@shared/core/layout/page-title/page-title.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {MyTasksHistoriesTabComponent} from '../../components/my-tasks-histories-tab/my-tasks-histories-tab.component';
import {TasksComponent} from '../../components/tasks/tasks.component';

@Component({
    selector: 'app-my-notifications',
    templateUrl: './my-notifications.component.html',
    styleUrls: ['./my-notifications.component.scss'],
    imports: [PageComponent, LoaderComponent, PageTitleComponent, TabsComponent, TabComponent, TasksComponent, MyTasksHistoriesTabComponent, TranslatePipe]
})
export class MyNotificationsComponent implements OnInit {
    searchIsRunning = false;
    requestsToStudy: RequestToStudy[];
    urlToDoc: string;
    searchUrlLoading = false;

    constructor(
        iconRegistryService: IconRegistryService,
        private readonly router: Router,
        private readonly tasksAggregratorService: TasksAggregatorService,
        private readonly snackBarService: SnackBarService,
        private readonly translateService: TranslateService,
        private readonly propertiesMetierService: PropertiesMetierService,
    ) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
        this.getUrlToDoc();
    }

    ngOnInit(): void {
        this.loadTasks();
    }

    /**
     * Récupère la liste des tâches à étudier via le service agrégateur.
     * Cette méthode peut être appelée plusieurs fois (initialisation ou refresh via les onglets).
     */
    loadTasks(): void {
        this.searchIsRunning = true;
        this.tasksAggregratorService.loadTasks()
            .subscribe({
                next: (requestsToStudy) => this.requestsToStudy = requestsToStudy,
                error: (e) => {
                    console.error('Cannot retrieve requests to study', e);
                    this.snackBarService.openSnackBar({
                        message: this.translateService.instant('error.technicalError'),
                        level: Level.ERROR
                    });
                    this.searchIsRunning = false;
                },
                complete: () => this.searchIsRunning = false
            });
    }

    /**
     * on ouvre un nouvel onglet vers la documentation de l'utilisation de l'URL
     */
    getUrlToDoc(): void {
        this.searchUrlLoading = true;
        this.propertiesMetierService.get('front.docRudi').subscribe({
            next: (link: string) => {
                this.urlToDoc = link;
                this.searchUrlLoading = false;
            },
            error: (error) => {
                this.searchUrlLoading = false;
                console.log(error);
            }
        });
    }
}
