import { Action } from '@ngrx/store';

const UPDATE_AGENT_INFO = 'UPDATE_AGENT_INFO';

export class UpdateAgentInfo implements Action {
    readonly type = UPDATE_AGENT_INFO;
    constructor(public payload: IServerAndAgentData) {}
}

export function Reducer(state: IServerAndAgentData, action: UpdateAgentInfo): IServerAndAgentData {
    switch (action.type) {
        case UPDATE_AGENT_INFO:
            return action.payload;
        default:
            return state;
    }
}
