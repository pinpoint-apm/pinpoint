import { Action } from '@ngrx/store';

const UPDATE_SERVER_MAP_LOADING_STATE = 'UPDATE_LOADING_STATE';

export class UpdateServerMapLoadingState implements Action {
    readonly type = UPDATE_SERVER_MAP_LOADING_STATE;
    constructor(public payload: string) {}
}
export function Reducer(state = 'LOADING', action: UpdateServerMapLoadingState): string {
    switch (action.type) {
        case UPDATE_SERVER_MAP_LOADING_STATE:
            return action.payload;
        default:
            return state;
    }
}
