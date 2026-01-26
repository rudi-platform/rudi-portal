import {ComponentFixture, TestBed} from '@angular/core/testing';

import {NotificationTemplateComponent} from '@shared/core/layout/notification-template/notification-template.component';

describe('NotificationTemplateComponent', () => {
    let component: NotificationTemplateComponent;
    let fixture: ComponentFixture<NotificationTemplateComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [NotificationTemplateComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(NotificationTemplateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
