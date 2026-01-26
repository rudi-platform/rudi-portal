import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatFormField, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
import {MatIcon} from '@angular/material/icon';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-password',
    templateUrl: './password.component.html',
    styleUrls: ['./password.component.scss'],
    imports: [MatFormField, MatLabel, MatInput, FormsModule, MatIcon, MatSuffix, TranslatePipe]
})
export class PasswordComponent {
    /**
     * Cache ou non le password
     */
    @Input() hidePassword: boolean;
    /**
     * label du password
     */
    @Input() label: string;
    /**
     * Emitter du password
     */
    @Output() passwordEmitter: EventEmitter<string> = new EventEmitter<string>();
    /**
     * Mot de passe
     */
    password: string;

    /**
     * Méthode qui renvoie le password entré par l'utilisateur au composant parent
     */
    emitPassword(): void {
        this.passwordEmitter.emit(this.password);
    }
}
