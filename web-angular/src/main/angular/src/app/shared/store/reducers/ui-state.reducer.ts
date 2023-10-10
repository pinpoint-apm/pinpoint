import { Action } from '@ngrx/store';

const CHANGE_INFO_PER_SERVER_VISIBLE_STATE = 'CHANGE_INFO_PER_SERVER_VISIBLE_STATE';

const initState = {
    infoPerServer: false,
};

export class ChangeInfoPerServerVisibleState implements Action {
    readonly type = CHANGE_INFO_PER_SERVER_VISIBLE_STATE;
    constructor(public payload: boolean) {}
}

export function Reducer(state: IUIState = initState, action: ChangeInfoPerServerVisibleState): IUIState {
    switch (action.type) {
        case CHANGE_INFO_PER_SERVER_VISIBLE_STATE:
            return {
                ...state,
                'infoPerServer': action.payload
            };
        default:
            return state;
    }
}
