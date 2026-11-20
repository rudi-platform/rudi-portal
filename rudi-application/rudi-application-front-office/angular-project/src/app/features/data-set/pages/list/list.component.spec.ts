import {AsyncPipe, NgClass} from '@angular/common';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslatePipe} from '@ngx-translate/core';

import {ListComponent} from './list.component';

describe('DataSetsComponent', () => {
    let component: ListComponent;
    let fixture: ComponentFixture<ListComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ListComponent]
        })
            // Rendu superficiel : les composants enfants (sidenavs, filtres, liste) ont leurs propres tests
            .overrideComponent(ListComponent, {
                set: {imports: [AsyncPipe, NgClass, TranslatePipe], schemas: [NO_ERRORS_SCHEMA]}
            })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
