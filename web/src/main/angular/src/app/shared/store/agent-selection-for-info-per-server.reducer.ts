import { Action } from '@ngrx/store';

const CHANGE_AGENT_FOR_SERVER_LIST = 'CHANGE_AGENT_FOR_SERVER_LIST';

export class ChangeAgentForServerList implements Action {
    readonly type = CHANGE_AGENT_FOR_SERVER_LIST;
    constructor(public payload: IAgentSelection) {}
}

export function Reducer(state: IAgentSelection, action: ChangeAgentForServerList): IAgentSelection {
    switch (action.type) {
        case CHANGE_AGENT_FOR_SERVER_LIST:
            return action.payload;
        default:
            return state;
    }
}
