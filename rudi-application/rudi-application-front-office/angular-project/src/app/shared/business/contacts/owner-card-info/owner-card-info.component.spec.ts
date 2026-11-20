import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OwnerCardInfoComponent} from './owner-card-info.component';

describe('OwnerCardInfoComponent', () => {
    let fixture: ComponentFixture<OwnerCardInfoComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [OwnerCardInfoComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(OwnerCardInfoComponent);
    });

    it('should render the section titles as level 2 headings', () => {
        fixture.componentRef.setInput('title', 'Réutilisateur');
        fixture.componentRef.setInput('name', 'Ethan Hunt');
        fixture.componentRef.setInput('relatedTitle', 'Organisation partenaire');
        fixture.componentRef.setInput('relatedOrganizations', [{name: 'Rennes Métropole'}]);
        fixture.detectChanges();

        const headings = Array.from(fixture.nativeElement.querySelectorAll('h2.section-title') as NodeListOf<HTMLElement>)
            .map(heading => heading.textContent.trim());
        expect(headings).toEqual(['Réutilisateur', 'Organisation partenaire']);
    });

    it('should not render empty headings', () => {
        fixture.componentRef.setInput('name', 'Ethan Hunt');
        fixture.detectChanges();

        expect(fixture.nativeElement.querySelector('h2')).toBeNull();
    });
});
