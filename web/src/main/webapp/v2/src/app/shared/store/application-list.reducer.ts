import { Action } from '@ngrx/store';

const UPDATE_APPLICATION_LIST = 'UPDATE_APPLICATION_LIST';

export class UpdateApplicationList implements Action {
    readonly type = UPDATE_APPLICATION_LIST;
    constructor(public payload: IApplication[]) {}
}

export function Reducer(state: IApplication[] = [], action: UpdateApplicationList): IApplication[] {
    switch (action.type) {
        case UPDATE_APPLICATION_LIST:
            return action.payload;
        default:
            return state;
    }
}
