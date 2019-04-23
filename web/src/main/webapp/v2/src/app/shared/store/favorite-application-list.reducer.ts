import { Action } from '@ngrx/store';

const ADD_FAVORITE_APPLICATION = 'ADD_FAVORITE_APPLICATION';
const REMOVE_FAVORITE_APPLICATION = 'REMOVE_FAVORITE_APPLICATION';

export class AddFavoriteApplication implements Action {
    readonly type = ADD_FAVORITE_APPLICATION;
    constructor(public payload: IApplication[]) {}
}
export class RemoveFavoriteApplication implements Action {
    readonly type = REMOVE_FAVORITE_APPLICATION;
    constructor(public payload: IApplication[]) {}
}

export function Reducer(state: IApplication[] = [], action: AddFavoriteApplication | RemoveFavoriteApplication): IApplication[] {
    switch (action.type) {
        case ADD_FAVORITE_APPLICATION:
            return [...state, ...action.payload].sort((a, b) => {
                const aName = a.applicationName.toUpperCase();
                const bName = b.applicationName.toUpperCase();
                return aName < bName ? -1 : aName > bName ? 1 : 0;
            });
        case REMOVE_FAVORITE_APPLICATION:
            return state.filter((application: IApplication) => {
                for (let i = 0 ; i < action.payload.length ; i++) {
                    if (application.equals(action.payload[i])) {
                        return false;
                    }
                }
                return true;
            });
        default:
            return state;
    }
}
