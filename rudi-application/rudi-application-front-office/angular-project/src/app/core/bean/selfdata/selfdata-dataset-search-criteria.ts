/**
 * Critères de recherche frontend pour les datasets selfdata.
 * Bean utilisé en interne côté frontend.
 */
export interface SelfdataDatasetSearchCriteria {
    /**
     * Index de début
     */
    offset?: number;

    /**
     * Limite de résultats par page
     */
    limit?: number;

    /**
     * Critère de tri
     */
    order?: string;
}

