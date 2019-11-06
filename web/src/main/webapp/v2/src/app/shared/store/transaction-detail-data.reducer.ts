import { Action } from '@ngrx/store';

const UPDATE_TRANSACTION_DETAIL_DATA = 'UPDATE_TRANSACTION_DETAIL_DATA';

export class UpdateTransactionDetailData implements Action {
    readonly type = UPDATE_TRANSACTION_DETAIL_DATA;
    constructor(public payload: ITransactionDetailData) {}
}

export function Reducer(state: ITransactionDetailData, action: UpdateTransactionDetailData): ITransactionDetailData {
    switch ( action.type ) {
        case UPDATE_TRANSACTION_DETAIL_DATA:
            if (state && (state.agentId === action.payload.agentId && state.applicationId === action.payload['applicationId'] && state.transactionId === action.payload.transactionId)) {
                return state;
            } else {
                return action.payload;
            }
        default:
            return state;
    }
}
