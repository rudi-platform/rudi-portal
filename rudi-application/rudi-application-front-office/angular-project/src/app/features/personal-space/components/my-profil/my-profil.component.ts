import {NgIf} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {MatCard, MatCardContent} from '@angular/material/card';
import {UserService} from '@core/services/user.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {User} from 'micro_service_modules/acl/acl-model';

@Component({
    selector: 'app-my-profil',
    templateUrl: './my-profil.component.html',
    styleUrls: ['./my-profil.component.scss'],
    imports: [MatCard, MatCardContent, NgIf, LoaderComponent, TranslatePipe]
})
export class MyProfilComponent implements OnInit {
    public user: User | undefined;
    email: string;
    isLoading: boolean;

    constructor(
        private readonly translateService: TranslateService,
        private readonly utilisateurService: UserService,
    ) {
    }

    ngOnInit(): void {
        this.isLoading = true;
        // récupération de l'évènement d'authentification
        this.utilisateurService.getAuthenticatedUser()
            .subscribe(
                {
                    next: (user: User | undefined) => {
                        this.user = user;
                        this.email = this.getEmail();
                        if (!this.user?.firstname && !this.user?.lastname) {
                            this.user.lastname = user?.login;
                        }
                        this.isLoading = false;
                    },
                    error: (e) => {
                        console.error(e);
                        this.isLoading = false;
                    }
                }
            );
    }

    getEmail(): string {
        return this.utilisateurService.lookupEMailAddress(this.user);
    }
}
