import {ComponentFixture, TestBed} from '@angular/core/testing';

import {BackPaginationComponent} from '@shared/core/common/back-pagination/back-pagination.component';

describe('BackPaginationComponent', () => {
    let component: BackPaginationComponent;
    let fixture: ComponentFixture<BackPaginationComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [BackPaginationComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(BackPaginationComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
