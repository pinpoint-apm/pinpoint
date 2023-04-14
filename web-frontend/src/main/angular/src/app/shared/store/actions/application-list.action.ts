import { createAction } from '@ngrx/store';

export const initApplicationList = createAction('[Application List] Init Application List');

export const getApplicationList = createAction('[Application List] Get Application List', (force = false) => ({force}));
export const getApplicationListSuccess = createAction('[Application List] Get Application List Success', (appList: IApplication[]) => ({appList}));
export const getApplicationListFail = createAction('[Application List] Get Application List Fail', (error: IServerError) => ({error}));
