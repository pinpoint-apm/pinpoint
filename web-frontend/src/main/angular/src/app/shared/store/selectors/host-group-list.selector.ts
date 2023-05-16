import { createSelector } from '@ngrx/store';

import { AppState } from 'app/shared/store/reducers';
import { IHostGroupListState } from 'app/shared/store/reducers/host-group-list.reducer';

export const hostGroupList = createSelector(
    (state: AppState) => state.hostGroupList,
    (state: IHostGroupListState) => state.item
);

export const hostGroupListError = createSelector(
    (state: AppState) => state.hostGroupList,
    (state: IHostGroupListState) => state.error,
);
