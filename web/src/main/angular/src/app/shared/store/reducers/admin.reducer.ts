import { Action } from '@ngrx/store';

const UPDATE_ADMIN_AGENT_LIST = 'UPDATE_ADMIN_AGENT_LIST';
export class UpdateAdminAgentList implements Action {
    readonly type = UPDATE_ADMIN_AGENT_LIST;
    constructor(public payload: IAgentList) {}
}

export function Reducer(state: IAgentList, action: UpdateAdminAgentList): IAgentList {
    switch (action.type) {
        case UPDATE_ADMIN_AGENT_LIST:
            return action.payload;
        default:
            return state;
    }
}
