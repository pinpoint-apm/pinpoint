import { Action } from '@ngrx/store';

const CHANGE_INFO_PER_SERVER_VISIBLE_STATE = 'CHANGE_INFO_PER_SERVER_VISIBLE_STATE';
const CHANGE_SERVER_MAP_DISABLE_STATE = 'CHANGE_SERVER_MAP_DISABLE_STATE';

const initState = {
    infoPerServer: false,
    serverMap: false
};

export class ChangeInfoPerServerVisibleState implements Action {
    readonly type = CHANGE_INFO_PER_SERVER_VISIBLE_STATE;
    constructor(public payload: boolean) {}
}
export class ChangeServerMapDisableState implements Action {
    readonly type = CHANGE_SERVER_MAP_DISABLE_STATE;
    constructor(public payload: boolean) {}
}

export function Reducer(state: IUIState = initState, action: ChangeInfoPerServerVisibleState | ChangeServerMapDisableState): IUIState {
    switch (action.type) {
        case CHANGE_INFO_PER_SERVER_VISIBLE_STATE:
            if (action.payload === state['infoPerServer']) {
                return state;
            } else {
                return {
                    ...state,
                    'infoPerServer': action.payload
                };
            }
        case CHANGE_SERVER_MAP_DISABLE_STATE:
            if (action.payload === state['serverMap']) {
                return state;
            } else {
                return {
                    ...state,
                    'serverMap': action.payload
                };
            }
        default:
            return state;
    }
}
