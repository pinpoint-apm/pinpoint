import { createAction } from '@ngrx/store';

export const initHostGroupList = createAction('[HostGroup List] Init HostGroup List');

export const getHostGroupList = createAction('[HostGroup List] Get HostGroup List', (force = false) => ({force}));
export const getHostGroupListSuccess = createAction('[HostGroup List] Get HostGroup List Success', (hostGroupList: string[]) => ({hostGroupList}));
export const getHostGroupListFail = createAction('[HostGroup List] Get HostGroup List Fail', (error: IServerError) => ({error}));
