import {DataSize} from '@shared/models/data-size';

export interface WorkflowProperties {
    fileMaxSize?: DataSize;
    processDefinitionKey?: string;
}
