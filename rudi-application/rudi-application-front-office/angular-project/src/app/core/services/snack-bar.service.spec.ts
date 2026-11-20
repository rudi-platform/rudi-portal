import {LiveAnnouncer} from '@angular/cdk/a11y';
import {TestBed} from '@angular/core/testing';
import {MatSnackBar, MatSnackBarConfig} from '@angular/material/snack-bar';
import {SnackBarService} from '@core/services/snack-bar.service';
import {Level} from '@shared/core/layout/notification-template/notification-template.component';

describe('SnackBarService', () => {
    let service: SnackBarService;
    let snackBar: jasmine.SpyObj<MatSnackBar>;
    let liveAnnouncer: jasmine.SpyObj<LiveAnnouncer>;

    beforeEach(() => {
        snackBar = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['openFromComponent']);
        liveAnnouncer = jasmine.createSpyObj<LiveAnnouncer>('LiveAnnouncer', ['announce']);
        TestBed.configureTestingModule({
            providers: [
                {provide: MatSnackBar, useValue: snackBar},
                {provide: LiveAnnouncer, useValue: liveAnnouncer},
            ]
        });
        service = TestBed.inject(SnackBarService);
    });

    const openedConfig = (): MatSnackBarConfig => snackBar.openFromComponent.calls.mostRecent().args[1];

    it('should keep the message until the user closes it (RGAA 13.1)', () => {
        service.add('Erreur');
        expect(openedConfig().duration).toBeUndefined();
    });

    it('should announce errors assertively as plain text, without a second snack bar live region (RGAA 7.5)', () => {
        service.openSnackBar({message: 'Erreur <a href="/aide">ici</a>', level: Level.ERROR});
        expect(openedConfig().politeness).toBe('off');
        expect(liveAnnouncer.announce).toHaveBeenCalledOnceWith('Erreur ici', 'assertive');
    });

    it('should announce other messages politely', () => {
        service.showSuccess('Enregistré');
        expect(liveAnnouncer.announce).toHaveBeenCalledOnceWith('Enregistré', 'polite');
    });
});
