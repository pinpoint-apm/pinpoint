import { createReducer, on, Action } from '@ngrx/store';
import {
    getApplicationListSuccess,
    getApplicationListFail,
    initApplicationList,
} from 'app/shared/store/actions';

export interface IApplicationListState {
    item: IApplication[];
    error: IServerErrorFormat;
}

const initialState: IApplicationListState = {
    item: [] as IApplication[],
    error: {} as IServerErrorFormat
};

const applicationListReducer = createReducer(
    initialState,
    on(initApplicationList, (state: IApplicationListState) => {
        return {
            ...state,
            ...initialState,
        };
    }),
    on(getApplicationListSuccess, (state: IApplicationListState, {appList}) => {
        return {
            ...state,
            item: [...appList],
            error: null
        };
    }),
    on(getApplicationListFail, (state: IApplicationListState, {error}) => {
        return {
            ...state,
            item: [],
            error: {...error}
        };
    }),
);

export function Reducer(state: IApplicationListState, action: Action): IApplicationListState {
    return applicationListReducer(state, action);
}
