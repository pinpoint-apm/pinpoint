import { Action } from '@ngrx/store';

const CHANGE_LANGUAGE = 'CHANGE_LANGUAGE';

export class ChangeLanguage implements Action {
    readonly type = CHANGE_LANGUAGE;
    constructor(public payload: string) {}
}

export function Reducer(state: string, action: ChangeLanguage): string {
    switch (action.type) {
        case CHANGE_LANGUAGE:
            return action.payload;
        default:
            return state;
    }
}
