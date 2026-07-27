import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {CaptchaModel, CaptchaService} from 'micro_service_modules/acl/acl-api';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {Observable, Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';

const CAPTCHA_NAMESPACE = '/kaptcha';

interface CaptchaResponse {
    uuid: string;
    imageb64: string;
}

@Component({
    selector: 'app-rudi-captcha',
    templateUrl: './rudi-captcha.component.html',
    styleUrls: ['./rudi-captcha.component.scss'],
    imports: [ReactiveFormsModule, TranslateModule]
})
export class RudiCaptchaComponent implements OnInit, OnDestroy {

    @Input()
    nomCaptcha: string;

    form: FormGroup;
    FORM_CONTROL_CAPTCHA = 'captchaCode';
    idCaptcha: string;
    imageSrc: string;
    reloadTitle: string;
    audioTitle: string;

    private readonly baseUrl: string;
    private readonly destroy$ = new Subject<void>();

    constructor(
        private readonly httpClient: HttpClient,
        private readonly captchaService: CaptchaService,
        private readonly translateService: TranslateService,
    ) {
        this.baseUrl = this.captchaService.configuration.basePath + CAPTCHA_NAMESPACE;
    }

    ngOnInit(): void {
        this.setButtonTitles();
        this.form = new FormGroup({
            captchaCode: new FormControl('')
        });
        this.form.get(this.FORM_CONTROL_CAPTCHA)?.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(value => {
            const uppercasedValue = value.toUpperCase();
            if (value !== uppercasedValue) {
                this.form.get(this.FORM_CONTROL_CAPTCHA)?.setValue(uppercasedValue, {emitEvent: false});
            }
        });
        this.loadCaptcha();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    isFilled(): boolean {
        const captchaCode: string = this.form.get(this.FORM_CONTROL_CAPTCHA)?.value as string;
        return captchaCode !== '';
    }

    validateInput(): Observable<boolean> {
        const captcha: CaptchaModel = {
            uuid: this.idCaptcha,
            code: this.form.get(this.FORM_CONTROL_CAPTCHA)?.value as string
        };
        return this.captchaService.validateCaptcha(captcha);
    }

    reloadCaptcha(): void {
        this.loadCaptcha();
    }

    playCaptchaSound(): void {
        this.httpClient.get(`${this.baseUrl}?get=sound&c=${this.nomCaptcha}&t=${this.idCaptcha}`, {responseType: 'blob'}).pipe(takeUntil(this.destroy$)).subscribe({
            next: (blob: Blob) => {
                const url = URL.createObjectURL(blob);
                const audio = new Audio(url);
                audio.play().catch(() => {
                    this.reloadCaptcha();
                });
                audio.onended = () => URL.revokeObjectURL(url);
            },
            error: () => {
                this.reloadCaptcha();
            }
        });
    }

    /**
     * Appelle GET /kaptcha?get=image&c=captchaFR
     * L'API CaptchEtat v2 retourne directement {uuid, imageb64}
     */
    private loadCaptcha(): void {
        this.httpClient.get<CaptchaResponse>(`${this.baseUrl}?get=image&c=${this.nomCaptcha}`).subscribe({
            next: (response) => {
                this.idCaptcha = response.uuid;
                this.imageSrc = response.imageb64;
            },
            error: (error) => {
                console.error('Erreur lors du chargement du captcha', error);
            }
        });
    }

    private setButtonTitles(): void {
        this.reloadTitle = this.translateService.instant('authentification.captchaReload');
        this.audioTitle = this.translateService.instant('authentification.captchaAudio');
    }
}
