import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SearchCountComponent} from '@shared/core/search/search-count/search-count.component';

describe('SearchCountComponent', () => {
    let component: SearchCountComponent;
    let fixture: ComponentFixture<SearchCountComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SearchCountComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SearchCountComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
