import {ProcessHistoricInformation} from 'micro_service_modules/api-bpmn';

export interface TaskHistoryItem {
    id: string;
    endDate: Date;
    description: string;
    initiator: string;
    functionalStatus: string;
    processDefinitionKey?: string;

    /**
     * Données complètes renvoyées par l'API (utile pour la page détail).
     */
    processHistoricInformation?: ProcessHistoricInformation;

    /**
     * Segment(s) de route relatif(s) pour accéder au détail.
        * Exemple: "my-task-history-detail".
     */
    url?: string;

    /**
     * Identifiant à passer à la route de détail.
     * Pour l'historique, c'est l'id Activiti côté back.
     */
    taskId?: string;
}
