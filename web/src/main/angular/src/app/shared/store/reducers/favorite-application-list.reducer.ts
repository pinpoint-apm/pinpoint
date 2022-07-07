import { createReducer, on, Action } from '@ngrx/store';
import {
    initFavoriteApplicationList,
    addFavApplicationSuccess,
    getFavApplicationListSuccess,
    getFavApplicationListFail,
    removeFavApplicationSuccess,
    addFavApplicationFail,
    removeFavApplicationFail
} from 'app/shared/store/actions';

export const enum ErrorType {
    ADD,
    REMOVE,
    GET
}

export interface IErrorState {
    errorType: ErrorType;
    error: IServerError;
}

export interface IFavoriteApplicationListState {
    item: IApplication[];
    error: IErrorState;
}

const initialState = {
    // item: [] as IApplication[],
    item: null as IApplication[],
    error: {} as IErrorState
};

const favoriteApplicationListReducer = createReducer(
    initialState,
    on(initFavoriteApplicationList, (state: IFavoriteApplicationListState) => {
        return {
            ...state,
            ...{...initialState, item: [...state.item]} // reset it with the last item in case of add/remove error
        };
    }),
    on(getFavApplicationListSuccess, (state: IFavoriteApplicationListState, {favAppList}) => {
        return {
            ...state,
            item: sortAppList(favAppList),
            error: null
        };
    }),
    on(getFavApplicationListFail, (state: IFavoriteApplicationListState, {errorType, error}) => {
        return {
            ...state,
            item: [],
            error: {
                errorType,
                error
            }
        };
    }),
    on(
        addFavApplicationFail,
        removeFavApplicationFail,
        (state: IFavoriteApplicationListState, {errorType, error}) => {
        return {
            ...state,
            item: [...state.item],
            error: {
                errorType,
                error
            }
        };
    }),
    on(addFavApplicationSuccess, (state: IFavoriteApplicationListState, {favApp}) => {
        return {
            ...state,
            item: sortAppList([...state.item, favApp]),
            error: null
        };
    }),
    on(removeFavApplicationSuccess, (state: IFavoriteApplicationListState, {favApp}) => {
        return {
            ...state,
            item: state.item.filter((app: IApplication) => !app.equals(favApp)),
            error: null
        };
    }),
);

export function Reducer(state: IFavoriteApplicationListState, action: Action): IFavoriteApplicationListState {
    return favoriteApplicationListReducer(state, action);
}

function sortAppList(appList: IApplication[]): IApplication[] {
    return [...appList].sort((a, b) => {
        const aName = a.applicationName.toUpperCase();
        const bName = b.applicationName.toUpperCase();

        return aName < bName ? -1 : aName > bName ? 1 : 0;
    });
}
