import { Action } from '@ngrx/store';

const UPDATE_TRANSACTION_TIMELINE_DATA = 'UPDATE_TRANSACTION_TIMELINE_DATA';

export class UpdateTransactionTimelineData implements Action {
    readonly type = UPDATE_TRANSACTION_TIMELINE_DATA;
    constructor(public payload: ITransactionTimelineData) {}
}

export function Reducer(state: ITransactionTimelineData, action: UpdateTransactionTimelineData): ITransactionTimelineData {
    switch ( action.type ) {
        case UPDATE_TRANSACTION_TIMELINE_DATA:
            if (state && (state.agentId === action.payload.agentId && state.applicationId === action.payload['applicationId'] && state.transactionId === action.payload.transactionId)) {
                return state;
            } else {
                return action.payload;
            }
        default:
            return state;
    }
}
