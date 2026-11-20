import {ComponentFixture, TestBed} from '@angular/core/testing';
import {RequestDetailDependencies} from '@features/personal-space/pages/request-detail-dependencies';
import moment from 'moment';

import {SelfdataMainInformationComponent} from './selfdata-main-information.component';

describe('SelfdataMainInformationComponent', () => {
    let component: SelfdataMainInformationComponent;
    let fixture: ComponentFixture<SelfdataMainInformationComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SelfdataMainInformationComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SelfdataMainInformationComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('task', {
            processDefinitionKey: 'selfdata-information-request-process',
            datasetTitle: 'Jeu de données',
            receivedDate: moment('2024-01-01'),
            expiredDate: moment('2024-02-01'),
        } as RequestDetailDependencies);
        fixture.componentRef.setInput('taskLoading', false);
        fixture.componentRef.setInput('formLoading', false);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
