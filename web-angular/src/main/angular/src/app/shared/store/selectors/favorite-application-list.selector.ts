import { createSelector } from '@ngrx/store';

import { AppState } from 'app/shared/store/reducers';
import { IFavoriteApplicationListState } from 'app/shared/store/reducers/favorite-application-list.reducer';

export const favoriteApplicationList = createSelector(
    (state: AppState) => state.favoriteApplicationList,
    (state: IFavoriteApplicationListState) => state.item
);

export const favoriteApplicationListError = createSelector(
    (state: AppState) => state.favoriteApplicationList,
    (state: IFavoriteApplicationListState) => state.error,
);
