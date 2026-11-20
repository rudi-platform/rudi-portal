/**
 * Critères de recherche frontend pour les demandes d'informations Selfdata.
 * Bean utilisé en interne côté frontend pour gérer la recherche.
 */
export interface SelfdataInformationRequestSearchCriteria {
    /**
     * Index de début (positionne le curseur pour parcourir les résultats de la recherche)
     */
    offset?: number;

    /**
     * Le nombre de résultats à retourner par page
     */
    limit?: number;

    /**
     * Le critère de tri des résultats
     */
    order?: string;

    /**
     * UUID du dataset (optionnel)
     */
    datasetUuid?: string;

    /**
     * Statuts de la demande
     */
    status?: any[];
}

