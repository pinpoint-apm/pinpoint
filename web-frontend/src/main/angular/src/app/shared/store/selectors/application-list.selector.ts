import { createSelector } from '@ngrx/store';

import { AppState } from 'app/shared/store/reducers';
import { IApplicationListState } from 'app/shared/store/reducers/application-list.reducer';

export const applicationList = createSelector(
    (state: AppState) => state.applicationList,
    (state: IApplicationListState) => state.item
);

export const applicationListError = createSelector(
    (state: AppState) => state.applicationList,
    (state: IApplicationListState) => state.error,
);
