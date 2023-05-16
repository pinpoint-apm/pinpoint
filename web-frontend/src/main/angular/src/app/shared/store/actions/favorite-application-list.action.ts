import { createAction } from '@ngrx/store';
import { ErrorType } from 'app/shared/store/reducers/favorite-application-list.reducer';

export const initFavoriteApplicationList = createAction('[Favorite Application List] Init Favorite Application List');

export const getFavApplicationList = createAction('[Favorite Application List] Get Favorite Application List');
export const getFavApplicationListSuccess = createAction('[Favorite Application List] Get Favorite Application List Success', (favAppList: IApplication[]) => ({favAppList}));
export const getFavApplicationListFail = createAction('[Favorite Application List] Get Favorite Application List Fail', (error: IServerError) => ({errorType: ErrorType.GET, error}));

export const addFavApplication = createAction('[Favorite Application List] Add Favorite Application', (favApp: IApplication) => ({favApp}));
export const addFavApplicationSuccess = createAction('[Favorite Application List] Add Favorite Application Success', (favApp: IApplication) => ({favApp}));
export const addFavApplicationFail = createAction('[Favorite Application List] Add Favorite Application Fail', (error: IServerError) => ({errorType: ErrorType.ADD, error}));

export const removeFavApplication = createAction('[Favorite Application List] Remove Favorite Application', (favApp: IApplication) => ({favApp}));
export const removeFavApplicationSuccess = createAction('[Favorite Application List] Remove Favorite Application Success', (favApp: IApplication) => ({favApp}));
export const removeFavApplicationFail = createAction('[Favorite Application List] Remove Favorite Application Fail', (error: IServerError) => ({errorType: ErrorType.REMOVE, error}));
