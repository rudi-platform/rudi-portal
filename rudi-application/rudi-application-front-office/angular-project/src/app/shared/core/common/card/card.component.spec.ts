import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CardComponent} from '@shared/core/common/card/card.component';

describe('CardComponent', () => {
    let component: CardComponent;
    let fixture: ComponentFixture<CardComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [CardComponent],
            providers: [provideHttpClient(), provideHttpClientTesting()]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    [undefined, 'subtitle'].forEach(subTitle => {
        it(`should hide the decorative title icon from assistive technologies (RGAA 1.2, subTitle=${subTitle})`, () => {
            component.header = true;
            component.title = 'title';
            component.icon = 'icon_tab_organization';
            component.subTitle = subTitle;
            fixture.detectChanges();

            const icon: HTMLElement = fixture.nativeElement.querySelector('mat-icon.mat-icon-card');
            expect(icon.getAttribute('aria-hidden')).toBe('true');
        });
    });

    it('should keep a non-heading title by default', () => {
        component.header = true;
        component.title = 'title';
        fixture.detectChanges();

        expect(fixture.nativeElement.querySelector('div.card-title')).not.toBeNull();
        expect(fixture.nativeElement.querySelector('h2, h3, h4')).toBeNull();
    });

    [2, 3, 4].forEach((level: 2 | 3 | 4) => {
        it(`should render the title as a level ${level} heading (RGAA 9.1)`, () => {
            component.header = true;
            component.title = 'title';
            component.icon = 'icon_tab_organization';
            component.titleLevel = level;
            fixture.detectChanges();

            const heading: HTMLElement = fixture.nativeElement.querySelector(`h${level}.card-title`);
            expect(heading?.textContent.trim()).toBe('title');
        });
    });
});
