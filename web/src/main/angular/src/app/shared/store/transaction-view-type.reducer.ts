import { Action } from '@ngrx/store';

const CHANGE_TRANSACTION_VIEW_TYPE = 'CHANGE_TRANSACTION_VIEW_TYPE';

export class ChangeTransactionViewType implements Action {
    readonly type = CHANGE_TRANSACTION_VIEW_TYPE;
    constructor(public payload: string) {}
}

export function Reducer(state = 'callTree', action: ChangeTransactionViewType): string {
    switch (action.type) {
        case CHANGE_TRANSACTION_VIEW_TYPE:
            return action.payload;
        default:
            return state;
    }
}
