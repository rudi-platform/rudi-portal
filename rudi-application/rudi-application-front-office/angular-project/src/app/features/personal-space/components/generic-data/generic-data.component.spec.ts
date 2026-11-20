import {ComponentFixture, TestBed} from '@angular/core/testing';
import {GdataDataInterface} from '@core/services/selfdata-dataset/gdataData.interface';

import {GenericDataComponent} from './generic-data.component';

describe('GenericDataComponent', () => {
    let component: GenericDataComponent;
    let fixture: ComponentFixture<GenericDataComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [GenericDataComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(GenericDataComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('isLoading', false);
        fixture.componentRef.setInput('genericDataObject', {title: 'Titre', genericDataObject: {data: []}} as GdataDataInterface);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
