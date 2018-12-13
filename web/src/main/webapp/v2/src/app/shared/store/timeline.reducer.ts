import { Action } from '@ngrx/store';

const initState: ITimelineInfo = {
    range: [0, 0],
    selectedTime: 0,
    selectionRange: [0, 0],
};

const UPDATE_TIMELINE_INFO = 'UPDATE_TIMELINE_INFO';

export class UpdateTimelineData implements Action {
    readonly type = UPDATE_TIMELINE_INFO;
    constructor(public payload: ITimelineInfo) {}
}

export function Reducer(state = initState, action: UpdateTimelineData): ITimelineInfo {
    switch (action.type) {
        case UPDATE_TIMELINE_INFO:
            if (
                state.range[0] !== action.payload.range[0] ||
                state.range[1] !== action.payload.range[1] ||
                state.selectedTime !== action.payload.selectedTime ||
                state.selectionRange[0] !== action.payload.selectionRange[0] ||
                state.selectionRange[1] !== action.payload.selectionRange[1]
            ) {
                return action.payload;
            } else {
                return state;
            }
        default:
            return state;
    }
}
