/**
 * énumération des paramètres du connector
 */
export enum MAP_CONNECTOR_PARAMETERS {
    VERSIONS= 'versions', LAYER = 'layer', DEFAULT_CRS = 'default_crs', FORMATS = 'formats'
}

/**
 * Liste des paramètres obligatoires du connector
 */
export const MAP_CONNECTOR_PARAMETERS_REQUIRED: string[] = [
    MAP_CONNECTOR_PARAMETERS.VERSIONS, MAP_CONNECTOR_PARAMETERS.LAYER, MAP_CONNECTOR_PARAMETERS.DEFAULT_CRS, MAP_CONNECTOR_PARAMETERS.FORMATS
];
