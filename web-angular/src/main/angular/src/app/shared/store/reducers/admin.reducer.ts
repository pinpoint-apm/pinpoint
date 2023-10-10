import { Action } from '@ngrx/store';

const UPDATE_ADMIN_AGENT_LIST = 'UPDATE_ADMIN_AGENT_LIST';
export class UpdateAdminAgentList implements Action {
    readonly type = UPDATE_ADMIN_AGENT_LIST;
    constructor(public payload: IServerAndAgentDataV2[]) {}
}

export function Reducer(state: IServerAndAgentDataV2[], action: UpdateAdminAgentList): IServerAndAgentDataV2[] {
    switch (action.type) {
        case UPDATE_ADMIN_AGENT_LIST:
            return action.payload;
        default:
            return state;
    }
}
