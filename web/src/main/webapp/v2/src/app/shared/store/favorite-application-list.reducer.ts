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
            // TODO: 프로젝트 구조 수정 후에는 따로 체크 할 필요없이 바로 Add하기.
            if (isSameFavAppList(state, action.payload)) {
                return state;
            } else {
                return [...state, ...action.payload];
            }
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

function isSameFavAppList(appList1: IApplication[], appList2: IApplication[]): boolean {
    const makeAppListStrFunc = (appList: IApplication[]) => appList.map((app: IApplication) => app.getApplicationName()).join(',');

    return makeAppListStrFunc(appList1) === makeAppListStrFunc(appList2);
}
