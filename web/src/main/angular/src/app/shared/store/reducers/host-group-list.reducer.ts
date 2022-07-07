import { createReducer, on, Action } from '@ngrx/store';
import {
    getHostGroupListFail,
    getHostGroupListSuccess,
    initHostGroupList,
} from 'app/shared/store/actions';

export interface IHostGroupListState {
    item: string[];
    error: IServerError;
}

const initialState: IHostGroupListState = {
    item: [] as string[],
    error: {} as IServerError
};

const hostGroupListReducer = createReducer(
    initialState,
    on(initHostGroupList, (state: IHostGroupListState) => {
        return {
            ...state,
            ...initialState,
        };
    }),
    on(getHostGroupListSuccess, (state: IHostGroupListState, {hostGroupList}) => {
        return {
            ...state,
            item: [...hostGroupList],
            error: null
        };
    }),
    on(getHostGroupListFail, (state: IHostGroupListState, {error}) => {
        return {
            ...state,
            item: [],
            error: {...error}
        };
    }),
);

export function Reducer(state: IHostGroupListState, action: Action): IHostGroupListState {
    return hostGroupListReducer(state, action);
}
