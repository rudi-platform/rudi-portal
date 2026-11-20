import {LiveAnnouncer} from '@angular/cdk/a11y';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormGroup} from '@angular/forms';
import {ProjectDatasetItem} from '../../model/project-dataset-item';
import {ProjectDatasetListComponent} from '../project-dataset-list/project-dataset-list.component';
import {Step3ProjectComponent} from './step3-project.component';

describe('Step3ProjectComponent', () => {
    let fixture: ComponentFixture<Step3ProjectComponent>;
    let component: Step3ProjectComponent;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [Step3ProjectComponent]
        })
            .overrideComponent(Step3ProjectComponent, {
                remove: {imports: [ProjectDatasetListComponent]},
                add: {schemas: [CUSTOM_ELEMENTS_SCHEMA]}
            })
            .compileComponents();

        fixture = TestBed.createComponent(Step3ProjectComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('step3FormGroup', new FormGroup({}));
        fixture.componentRef.setInput('datasetItems', [{title: 'Dataset'} as ProjectDatasetItem]);
        fixture.detectChanges();
    });

    it('should move the focus to the add button when the last dataset is removed (RGAA 12.8)', () => {
        component['itemRemoved'].subscribe(() => fixture.componentRef.setInput('datasetItems', []));

        component.handleRemoveItem({title: 'Dataset'} as ProjectDatasetItem);
        fixture.detectChanges();

        const addButton: HTMLButtonElement = fixture.nativeElement.querySelector('app-dataset-button button');
        expect(fixture.nativeElement.ownerDocument.activeElement).toBe(addButton);
    });

    it('should leave the focus to the list while datasets remain', () => {
        const items = [{title: 'A'}, {title: 'B'}] as ProjectDatasetItem[];
        fixture.componentRef.setInput('datasetItems', items);
        fixture.detectChanges();
        component['itemRemoved'].subscribe((item: ProjectDatasetItem) => fixture.componentRef.setInput('datasetItems', items.filter(i => i !== item)));

        component.handleRemoveItem(items[0]);
        fixture.detectChanges();

        const addButton: HTMLButtonElement = fixture.nativeElement.querySelector('app-dataset-button button');
        expect(fixture.nativeElement.ownerDocument.activeElement).not.toBe(addButton);
    });

    it('should announce the removed dataset once the list is updated (RGAA 7.5)', () => {
        const announce = spyOn(TestBed.inject(LiveAnnouncer), 'announce').and.resolveTo();
        const item = component.datasetItems[0];
        component['itemRemoved'].subscribe(() => fixture.componentRef.setInput('datasetItems', []));

        component.handleRemoveItem(item);
        fixture.detectChanges();

        expect(announce).toHaveBeenCalledOnceWith('project.stepper.submission.step3.itemRemoved');
    });

    it('should not announce a removal the parent did not perform', () => {
        const announce = spyOn(TestBed.inject(LiveAnnouncer), 'announce').and.resolveTo();

        component.handleRemoveItem(component.datasetItems[0]);
        fixture.detectChanges();

        expect(announce).not.toHaveBeenCalled();
    });
});
