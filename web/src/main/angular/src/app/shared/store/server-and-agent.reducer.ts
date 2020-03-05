import { Action } from '@ngrx/store';

const UPDATE_FILTER_OF_SERVER_AND_AGENT_LIST = 'UPDATE_FILTER_OF_SERVER_AND_AGENT_LIST';
export class UpdateFilterOfServerAndAgentList implements Action {
    readonly type = UPDATE_FILTER_OF_SERVER_AND_AGENT_LIST;
    constructor(public payload: string) {}
}

export function Reducer(state = '', action: UpdateFilterOfServerAndAgentList): string {
    switch (action.type) {
        case UPDATE_FILTER_OF_SERVER_AND_AGENT_LIST:
            return state === action.payload ? state : action.payload;
        default:
            return state;
    }
}
