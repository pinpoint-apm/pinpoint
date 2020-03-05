import { Action } from '@ngrx/store';

const UPDATE_TRANSACTION_DATA = 'UPDATE_TRANSACTION_DATA';

export class UpdateTransactionData implements Action {
    readonly type = UPDATE_TRANSACTION_DATA;
    constructor(public payload: ITransactionMetaData) {}
}

export function Reducer(state: ITransactionMetaData, action: UpdateTransactionData): ITransactionMetaData {
    switch (action.type) {
        case UPDATE_TRANSACTION_DATA:
            if (state && (state.traceId === action.payload.traceId && state.collectorAcceptTime === action.payload.collectorAcceptTime && state.elapsed === action.payload.elapsed)) {
                return state;
            } else {
                return action.payload;
            }
        default:
            return state;
    }
}

