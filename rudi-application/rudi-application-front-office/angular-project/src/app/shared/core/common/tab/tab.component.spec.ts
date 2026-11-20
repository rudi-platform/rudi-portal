import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TabComponent} from '@shared/core/common/tab/tab.component';

describe('TabComponent', () => {
    let component: TabComponent;
    let fixture: ComponentFixture<TabComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TabComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TabComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should render a plain button without tab semantics when no tabId is given', () => {
        const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');

        expect(button.getAttribute('role')).toBeNull();
        expect(button.getAttribute('aria-selected')).toBeNull();
    });

    it('should expose the ARIA tab semantics when a tabId is given (RGAA 7.1)', () => {
        fixture.componentRef.setInput('tabId', 'tab-1');
        fixture.componentRef.setInput('controls', 'panel-1');
        fixture.componentRef.setInput('active', false);
        fixture.detectChanges();
        const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');

        expect(button.getAttribute('role')).toBe('tab');
        expect(button.id).toBe('tab-1');
        expect(button.getAttribute('aria-controls')).toBe('panel-1');
        expect(button.getAttribute('aria-selected')).toBe('false');
        expect(button.getAttribute('tabindex')).toBe('-1');
    });
});
