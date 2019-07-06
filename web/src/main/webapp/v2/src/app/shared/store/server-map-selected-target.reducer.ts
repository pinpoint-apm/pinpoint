import { Action } from '@ngrx/store';

const UPDATE_SERVER_MAP_TARGET_SELECTED = 'UPDATE_SERVER_MAP_TARGET_SELECTED';

export class UpdateServerMapTargetSelected implements Action {
    readonly type = UPDATE_SERVER_MAP_TARGET_SELECTED;
    constructor(public payload: ISelectedTarget) {}
}

export function Reducer(state: ISelectedTarget, action: UpdateServerMapTargetSelected): ISelectedTarget {
    switch (action.type) {
        case UPDATE_SERVER_MAP_TARGET_SELECTED:
            if (action.payload === null) {
                return {} as ISelectedTarget;
            } else if (
                state &&
                state.clickParam === action.payload.clickParam &&
                state.endTime === action.payload.endTime &&
                state.period === action.payload.period &&
                state.isWAS === action.payload.isWAS &&
                (state.isNode === action.payload.isNode
                    ? (state.isNode ? state.node[0] === action.payload.node[0] : state.link[0] === action.payload.link[0])
                    : false
                )
            ) {
                return state;
            } else {
                return action.payload;
            }
        default:
            return state;
    }
}
