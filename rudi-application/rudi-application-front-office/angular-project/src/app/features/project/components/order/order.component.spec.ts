import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ORDERS} from '@core/services/asset/project/projekt-metier.service';

import {OrderComponent} from './order.component';

describe('OrderComponent', () => {
    let component: OrderComponent;
    let fixture: ComponentFixture<OrderComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [OrderComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(OrderComponent);
        component = fixture.componentInstance;
        component.order = ORDERS[0];
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
