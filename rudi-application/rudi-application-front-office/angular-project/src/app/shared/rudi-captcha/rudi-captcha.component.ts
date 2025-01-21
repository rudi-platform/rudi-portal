import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {CaptchetatAngularComponent} from 'captchetat-angular';
import {CaptchaModel, CaptchaService} from 'micro_service_modules/acl/acl-api';
import {Observable} from 'rxjs';

const ACL_SERVICE_BASEPATH = '/acl/v1';
const CAPTCHA_NAMESPACE = '/kaptcha';

@Component({
    selector: 'app-rudi-captcha',
    templateUrl: './rudi-captcha.component.html',
    styleUrls: ['./rudi-captcha.component.scss']
})
export class RudiCaptchaComponent implements OnInit {

    /**
     * Type du captcha qu'on veut
     */
    @Input()
    nomCaptcha: string;
    urlBackend: string;
    form: FormGroup;
    FORM_CONTROL_CAPTCHA: string = 'captchaCode';

    @ViewChild(CaptchetatAngularComponent) captchetatComponent: CaptchetatAngularComponent;

    constructor(
        private readonly captchaService: CaptchaService,
    ) {
    }

    ngOnInit(): void {
        this.urlBackend = ACL_SERVICE_BASEPATH + CAPTCHA_NAMESPACE;
        this.form = new FormGroup({
            captchaCode: new FormControl('')
        });
        this.form.get(this.FORM_CONTROL_CAPTCHA)?.valueChanges.subscribe(value => {
            const uppercasedValue = value.toUpperCase();
            if (value !== uppercasedValue) {
                this.form.get(this.FORM_CONTROL_CAPTCHA)?.setValue(uppercasedValue, {emitEvent: false});
            }
        });
    }

    /**
     * Indique si le champ de captcha a été rempli par l'utilisateur
     */
    isFilled(): boolean {
        const captchaCode: string = this.form.get(this.FORM_CONTROL_CAPTCHA)?.value as string;
        return captchaCode !== '';
    }

    /**
     * Envoie pour validation la saisie utilisateur du captcha auprès du back
     */
    validateInput(): Observable<boolean> {
        const captcha: CaptchaModel = {
            uuid: this.captchetatComponent.getIdCaptcha(),
            code: this.form.get(this.FORM_CONTROL_CAPTCHA)?.value as string
        };
        return this.captchaService.validateCaptcha(captcha);
    }
}
