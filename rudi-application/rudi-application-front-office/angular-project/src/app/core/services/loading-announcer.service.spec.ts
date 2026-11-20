import {LiveAnnouncer} from '@angular/cdk/a11y';
import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {LOADING_ANNOUNCE_DELAY_MS, LoadingAnnouncerService} from '@core/services/loading-announcer.service';
import {TranslateService} from '@ngx-translate/core';
import {of} from 'rxjs';

describe('LoadingAnnouncerService', () => {
    let service: LoadingAnnouncerService;
    let liveAnnouncer: jasmine.SpyObj<LiveAnnouncer>;
    const messages: Record<string, string> = {
        'common.loadingAnnouncement': 'Chargement des données en cours',
        'common.loadedAnnouncement': 'Chargement des données effectué',
    };

    // Injected inside fakeAsync so that the debounce timers are faked
    const injectService = (): LoadingAnnouncerService => TestBed.inject(LoadingAnnouncerService);

    beforeEach(() => {
        liveAnnouncer = jasmine.createSpyObj('LiveAnnouncer', ['announce']);
        TestBed.configureTestingModule({
            providers: [
                {provide: LiveAnnouncer, useValue: liveAnnouncer},
                {provide: TranslateService, useValue: {get: (key: string) => of(messages[key])}},
            ],
        });
    });

    it('should announce politely once the loading lasts longer than the delay (RGAA 7.5)', fakeAsync(() => {
        service = injectService();
        service.startLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS);
        expect(liveAnnouncer.announce).toHaveBeenCalledOnceWith('Chargement des données en cours', 'polite');
    }));

    it('should not announce a loading shorter than the delay', fakeAsync(() => {
        service = injectService();
        service.startLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS - 100);
        service.stopLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS);
        expect(liveAnnouncer.announce).not.toHaveBeenCalled();
    }));

    it('should announce only once for many simultaneous loaders', fakeAsync(() => {
        service = injectService();
        for (let i = 0; i < 15; i++) {
            service.startLoading();
        }
        tick(LOADING_ANNOUNCE_DELAY_MS);
        for (let i = 0; i < 14; i++) {
            service.stopLoading();
        }
        tick(LOADING_ANNOUNCE_DELAY_MS);
        expect(liveAnnouncer.announce).toHaveBeenCalledOnceWith('Chargement des données en cours', 'polite');
    }));

    it('should not announce again when loaders follow each other within the delay', fakeAsync(() => {
        service = injectService();
        service.startLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS);
        service.stopLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS - 100);
        service.startLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS);
        expect(liveAnnouncer.announce).toHaveBeenCalledOnceWith('Chargement des données en cours', 'polite');
    }));

    it('should announce again for a new loading after an idle period', fakeAsync(() => {
        service = injectService();
        service.startLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS);
        service.stopLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS);
        service.startLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS);
        expect(liveAnnouncer.announce.calls.allArgs()).toEqual([
            ['Chargement des données en cours', 'polite'],
            ['Chargement des données effectué', 'polite'],
            ['Chargement des données en cours', 'polite'],
        ]);
    }));

    it('should ignore extra stop calls', fakeAsync(() => {
        service = injectService();
        service.stopLoading();
        service.startLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS);
        expect(liveAnnouncer.announce).toHaveBeenCalledTimes(1);
    }));

    it('should always announce the end of an announced loading', fakeAsync(() => {
        service = injectService();
        service.startLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS);
        service.stopLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS);
        expect(liveAnnouncer.announce).toHaveBeenCalledTimes(2);
        expect(liveAnnouncer.announce).toHaveBeenCalledWith('Chargement des données effectué', 'polite');
    }));

    it('should not announce the end of a loading that was never announced', fakeAsync(() => {
        service = injectService();
        service.startLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS - 100);
        service.stopLoading();
        tick(LOADING_ANNOUNCE_DELAY_MS);
        expect(liveAnnouncer.announce).not.toHaveBeenCalled();
    }));
});
