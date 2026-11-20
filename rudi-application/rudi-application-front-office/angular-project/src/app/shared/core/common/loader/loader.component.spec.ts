import {ComponentFixture, TestBed} from '@angular/core/testing';
import {LoadingAnnouncerService} from '@core/services/loading-announcer.service';
import {TranslateService} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {of} from 'rxjs';

describe('LoaderComponent', () => {
    let fixture: ComponentFixture<LoaderComponent>;
    let loadingAnnouncer: jasmine.SpyObj<LoadingAnnouncerService>;

    beforeEach(() => {
        loadingAnnouncer = jasmine.createSpyObj('LoadingAnnouncerService', ['startLoading', 'stopLoading']);
        TestBed.configureTestingModule({
            imports: [LoaderComponent],
            providers: [
                {provide: LoadingAnnouncerService, useValue: loadingAnnouncer},
                {provide: TranslateService, useValue: {get: () => of('Chargement...'), instant: () => 'Chargement...'}},
            ],
        });
        fixture = TestBed.createComponent(LoaderComponent);
    });

    it('should not register an inactive loader', () => {
        fixture.componentRef.setInput('active', false);
        fixture.detectChanges();
        expect(loadingAnnouncer.startLoading).not.toHaveBeenCalled();
    });

    it('should register once when activated and unregister when deactivated (RGAA 7.5)', () => {
        fixture.componentRef.setInput('active', true);
        fixture.detectChanges();
        fixture.componentRef.setInput('isLight', true);
        fixture.detectChanges();
        expect(loadingAnnouncer.startLoading).toHaveBeenCalledTimes(1);

        fixture.componentRef.setInput('active', false);
        fixture.detectChanges();
        expect(loadingAnnouncer.stopLoading).toHaveBeenCalledTimes(1);
    });

    it('should unregister an active loader on destroy', () => {
        fixture.componentRef.setInput('active', true);
        fixture.detectChanges();
        fixture.destroy();
        expect(loadingAnnouncer.stopLoading).toHaveBeenCalledTimes(1);
    });

    it('should not unregister an inactive loader on destroy', () => {
        fixture.detectChanges();
        fixture.destroy();
        expect(loadingAnnouncer.stopLoading).not.toHaveBeenCalled();
    });
});
