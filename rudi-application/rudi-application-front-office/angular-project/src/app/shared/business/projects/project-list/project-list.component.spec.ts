import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ProjectCatalogItemPage} from '@features/project/model/project-catalog-item';

import {ProjectListComponent} from '@shared/business/projects/project-list/project-list.component';

describe('ProjectListComponent', () => {
    let component: ProjectListComponent;
    let fixture: ComponentFixture<ProjectListComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ProjectListComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ProjectListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should render the cards as a list and forward the heading level', () => {
        component.isLoading = false;
        component.projectList = {
            total: 2,
            items: [
                {logo: 'a.png', project: {uuid: 'a', title: 'A'}},
                {logo: 'b.png', project: {uuid: 'b', title: 'B'}}
            ]
        } as ProjectCatalogItemPage;
        fixture.componentRef.setInput('cardTitleLevel', 3);
        fixture.detectChanges();

        const element: HTMLElement = fixture.nativeElement;
        expect(element.querySelectorAll('ul.project-cards-container > li > app-project-card').length).toBe(2);
        expect(element.querySelectorAll('app-project-card h3').length).toBe(2);
    });
});
