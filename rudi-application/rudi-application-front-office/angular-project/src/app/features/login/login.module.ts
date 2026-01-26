import {CommonModule} from '@angular/common';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {CoreModule} from '@core/core.module';
import {SharedModule} from '@shared/shared.module';
import {AccountValidationComponent} from './account-validation.component';
import {LoginRoutingModule} from './login-routing.module';
import {ForgotPasswordComponent} from './pages/forgot-password/forgot-password.component';
import {LoginComponent} from './pages/login/login.component';
import {NotAuthorizedComponent} from './pages/not-authorized/not-authorized.component';
import {ResetPasswordComponent} from './pages/reset-password/reset-password.component';
import {SignUpComponent} from './pages/sign-up/sign-up.component';

@NgModule({
    imports: [
        CommonModule,
        CoreModule,
        SharedModule,
        LoginRoutingModule,
        LoginComponent,
        NotAuthorizedComponent,
        SignUpComponent,
        AccountValidationComponent,
        ForgotPasswordComponent,
        ResetPasswordComponent
    ],
    exports: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [
        {provide: 'DEFAULT_LANGUAGE', useValue: 'fr'},
    ]
})
export class LoginModule {
}
