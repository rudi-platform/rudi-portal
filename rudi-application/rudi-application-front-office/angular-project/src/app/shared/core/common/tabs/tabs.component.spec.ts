import {Component} from '@angular/core';
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';

@Component({
    template: `
        <app-tabs>
            <app-tab label="One"><ng-template>One content</ng-template></app-tab>
            <app-tab label="Two" [disabled]="true"><ng-template>Two content</ng-template></app-tab>
            <app-tab label="Three"><ng-template>Three content</ng-template></app-tab>
        </app-tabs>`,
    imports: [TabsComponent, TabComponent]
})
class TabsHostComponent {
}

describe('TabsComponent ARIA tabs pattern (RGAA 7.1)', () => {
    let fixture: ComponentFixture<TabsHostComponent>;
    let element: HTMLElement;

    const tabButtons = (): HTMLButtonElement[] => Array.from(element.querySelectorAll<HTMLButtonElement>('[role="tablist"] [role="tab"]'));

    beforeEach(fakeAsync(() => {
        TestBed.configureTestingModule({
            imports: [TabsHostComponent],
            providers: [{provide: IconRegistryService, useValue: {addAllSvgIcons: () => undefined}}]
        });
        fixture = TestBed.createComponent(TabsHostComponent);
        element = fixture.nativeElement;
        fixture.detectChanges();
        tick();
        fixture.detectChanges();
    }));

    it('should expose a tablist, tabs and a labelled tabpanel', () => {
        const buttons = tabButtons();
        const panel = element.querySelector('[role="tabpanel"]');

        expect(buttons.length).toBe(3);
        expect(buttons.map(button => button.getAttribute('aria-selected'))).toEqual(['true', 'false', 'false']);
        expect(buttons.map(button => button.getAttribute('tabindex'))).toEqual(['0', '-1', '-1']);
        expect(buttons[0].getAttribute('aria-controls')).toBe('tab-content-container');
        expect(panel.id).toBe('tab-content-container');
        expect(panel.getAttribute('aria-labelledby')).toBe(buttons[0].id);
    });

    it('should move the focus with arrows, Home and End, skipping disabled tabs', () => {
        const buttons = tabButtons();
        const press = (key: string): void => {
            (document.activeElement as HTMLElement).dispatchEvent(new KeyboardEvent('keydown', {key, bubbles: true}));
        };

        buttons[0].focus();
        press('ArrowRight');
        expect(document.activeElement).toBe(buttons[2]);
        press('ArrowRight');
        expect(document.activeElement).toBe(buttons[0]);
        press('ArrowLeft');
        expect(document.activeElement).toBe(buttons[2]);
        press('Home');
        expect(document.activeElement).toBe(buttons[0]);
        press('End');
        expect(document.activeElement).toBe(buttons[2]);
    });

    it('should select a tab on click and update the tabpanel label', () => {
        const buttons = tabButtons();

        buttons[2].click();
        fixture.detectChanges();

        expect(buttons[2].getAttribute('aria-selected')).toBe('true');
        expect(buttons[2].getAttribute('tabindex')).toBe('0');
        expect(element.querySelector('[role="tabpanel"]').getAttribute('aria-labelledby')).toBe(buttons[2].id);
        expect(element.querySelector('[role="tabpanel"]').textContent).toContain('Three content');
    });
});

describe('TabsComponent', () => {
    let component: TabsComponent;
    let fixture: ComponentFixture<TabsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TabsComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TabsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
