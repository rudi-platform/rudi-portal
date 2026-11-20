import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DetailComponent} from './detail.component';

describe('DetailComponent', () => {
    let component: DetailComponent;
    let fixture: ComponentFixture<DetailComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [DetailComponent]
        })
            .compileComponents();
    });

    beforeEach(async () => {
        fixture = TestBed.createComponent(DetailComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
