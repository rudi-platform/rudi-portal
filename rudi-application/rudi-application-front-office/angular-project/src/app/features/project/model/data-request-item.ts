import {ProjectDatasetPictoType} from '@features/project/model/project-dataset-picto-type';

/**
 * Représente une demande de nouvelles données côté Front
 */
export interface DataRequestItem {
    title: string;
    description: string;
    uuid: string;
    /**
     * Le type de picto de cet item, ici Static car ndew Dataset Request
     */
    pictoType: ProjectDatasetPictoType;
}
