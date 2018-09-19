import { Action } from '@ngrx/store';

const initState: ITimelineInfo = {
    range: [0, 0],
    selectedTime: 0,
    selectionRange: [0, 0],
};

const UPDATE_TIMELINE_INFO = 'UPDATE_TIMELINE_INFO';
const UPDATE_TIMELINE_SELECTED_TIME = 'UPDATE_TIMELINE_SELECTED_TIME';
const UPDATE_TIMELINE_SELECTION_RANGE = 'UPDATE_TIMELINE_SELECTION_RANGE';
const UPDATE_TIMELINE_RANGE = 'UPDATE_TIMELINE_RANGE';

export class UpdateTimelineData implements Action {
    readonly type = UPDATE_TIMELINE_INFO;
    constructor(public payload: ITimelineInfo) {}
}
export class UpdateTimelineSelectedTime implements Action {
    readonly type = UPDATE_TIMELINE_SELECTED_TIME;
    constructor(public payload: number) {}
}
export class UpdateTimelineSelectionRange implements Action {
    readonly type = UPDATE_TIMELINE_SELECTION_RANGE;
    constructor(public payload: number[]) {}
}
export class UpdateTimelineRange implements Action {
    readonly type = UPDATE_TIMELINE_RANGE;
    constructor(public payload: number[]) {}
}
export function Reducer(state = initState, action: UpdateTimelineData | UpdateTimelineSelectedTime | UpdateTimelineSelectionRange | UpdateTimelineRange): ITimelineInfo {
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
        case UPDATE_TIMELINE_SELECTED_TIME:
            if (state.selectedTime === action.payload) {
                return state;
            } else {
                return {
                    ...state,
                    selectedTime : action.payload
                };
            }
        case UPDATE_TIMELINE_SELECTION_RANGE:
            if (state.selectionRange[0] === action.payload[0] && state.selectionRange[1] === action.payload[1]) {
                return state;
            } else {
                return {
                    ...state,
                    selectionRange: action.payload
                };
            }
        case UPDATE_TIMELINE_RANGE:
            if (state.range[0] === action.payload[0] && state.range[1] === action.payload[1]) {
                return state;
            } else {
                return {
                    ...state,
                    range: action.payload
                };
            }
        default:
            return state;
    }
}
