import {CustomizationService} from '@core/services/customization.service';
import {DatasetListBannerComponent} from '@shared/business/dataset/common/dataset-list-banner/dataset-list-banner.component';
import {of} from 'rxjs';

describe('DatasetListBannerComponent', () => {
    let customizationService: jasmine.SpyObj<CustomizationService>;
    let component: DatasetListBannerComponent;

    beforeEach(() => {
        customizationService = jasmine.createSpyObj<CustomizationService>('CustomizationService', ['getCustomizationDescription']);
        customizationService.getCustomizationDescription.and.returnValue(of({}));
        component = new DatasetListBannerComponent(customizationService, null, null, null, null);
    });

    it('should load the dataset publish button by default', () => {
        component.ngOnInit();

        expect(customizationService.getCustomizationDescription).toHaveBeenCalled();
    });

    it('should not load the dataset publish button when it is hidden', () => {
        component.hideDatasetPublishButton = true;
        component.ngOnInit();

        expect(customizationService.getCustomizationDescription).not.toHaveBeenCalled();
        expect(component.datasetPublishButtonHtml).toBeUndefined();
    });
});
