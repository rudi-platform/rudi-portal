import {Component, inject, Input, OnChanges} from '@angular/core';
import {Validators} from '@angular/forms';
import {PasswordStrengthCriteria} from '@shared/core/form/password-strength/password-strength-criteria';
import {PasswordStrengthCriterion} from '@shared/core/form/password-strength/password-strength-criterion';
import { NgClass } from '@angular/common';
import { MatIcon } from '@angular/material/icon';
import {TranslatePipe} from '@ngx-translate/core';
import {IconRegistryService} from '@core/services/icon-registry.service';

@Component({
    selector: 'app-password-strength',
    templateUrl: './password-strength.component.html',
    styleUrls: ['./password-strength.component.scss'],
    imports: [NgClass, TranslatePipe, MatIcon]
})
export class PasswordStrengthComponent implements OnChanges {

    private readonly iconRegistryService = inject(IconRegistryService);

    /** Le mot-de-passe dont on cherche la force */
    @Input() password: string;

    /** Critères à vérifier */
    readonly criteria = new PasswordStrengthCriteria([
        new PasswordStrengthCriterion('lowerLetters', Validators.pattern(/[a-z]+/)),
        new PasswordStrengthCriterion('upperLetters', Validators.pattern(/[A-Z]+/)),
        new PasswordStrengthCriterion('numbers', Validators.pattern(/[0-9]+/)),
        // Same special characters as PASSWORD_REGEX (front and ACL backend)
        new PasswordStrengthCriterion('symbols', Validators.pattern(/[#?!@$%^&*-]/))
    ]);

    /** De 0 à n. n étant le nombre de critères à vérifier */
    strength: number;

    constructor() {
        this.iconRegistryService.addAllSvgIcons(['coche-verte', 'croix-rouge']);
    }

    ngOnChanges(): void {
        this.strength = this.criteria.getStrength(this.password);
    }

}
