import {LiveAnnouncer} from '@angular/cdk/a11y';
import {Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {TranslateService} from '@ngx-translate/core';

import {SortAnnouncerDirective} from './sort-announcer.directive';

@Component({
    template: `
        <table matSort appSortAnnouncer>
            <tr>
                <th class="mat-column-receivedDate" mat-sort-header="receivedDate"> Reçue le </th>
            </tr>
        </table>`,
    imports: [MatSort, MatSortHeader, SortAnnouncerDirective]
})
class HostComponent {
}

describe('SortAnnouncerDirective', () => {
    let fixture: ComponentFixture<HostComponent>;
    let liveAnnouncer: jasmine.SpyObj<LiveAnnouncer>;

    beforeEach(() => {
        liveAnnouncer = jasmine.createSpyObj<LiveAnnouncer>('LiveAnnouncer', ['announce']);
        TestBed.configureTestingModule({
            imports: [HostComponent],
            providers: [{provide: LiveAnnouncer, useValue: liveAnnouncer}]
        });
        const translate = TestBed.inject(TranslateService);
        translate.setTranslation('fr', {
            common: {
                sortAnnouncement: {
                    asc: 'Trié par {{column}}, ordre croissant',
                    desc: 'Trié par {{column}}, ordre décroissant',
                    none: 'Tri par {{column}} annulé'
                }
            }
        });
        translate.use('fr');
        fixture = TestBed.createComponent(HostComponent);
        fixture.detectChanges();
    });

    it('should announce each new sort with the column name and direction (RGAA 7.5)', () => {
        const sortButton: HTMLElement = fixture.nativeElement.querySelector('.mat-sort-header-container');

        sortButton.click();
        sortButton.click();
        sortButton.click();

        expect(liveAnnouncer.announce.calls.allArgs()).toEqual([
            ['Trié par Reçue le, ordre croissant', 'polite'],
            ['Trié par Reçue le, ordre décroissant', 'polite'],
            ['Tri par Reçue le annulé', 'polite']
        ]);
    });
});
