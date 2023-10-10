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
            return action.payload;
        default:
            return state;
    }
}
