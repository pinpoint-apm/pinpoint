import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, switchMap, withLatestFrom, filter, catchError } from 'rxjs/operators';

import { StoreHelperService } from 'app/shared/services';
import { getApplicationList, getApplicationListFail, getApplicationListSuccess } from 'app/shared/store/actions';
import { isEmpty } from 'app/core/utils/util';
import { ApplicationListDataService } from 'app/core/components/application-list/application-list-data.service';

@Injectable()
export class ApplicationListEffect {
    getApplicationList$ = createEffect(() =>
        this.actions$.pipe(
            ofType(getApplicationList),
            withLatestFrom(this.storeHelperService.getApplicationList()),
            filter(([{force}, appList]: [{force: boolean}, IApplication[]]) => force || isEmpty(appList)),
            switchMap(() => this.applicationListDataService.getApplicationList().pipe(
                map((appList: IApplication[]) => getApplicationListSuccess(appList)),
                catchError((error: IServerErrorFormat) => of(getApplicationListFail(error)))
            )),
        )
    );

    constructor(
        private actions$: Actions,
        private applicationListDataService: ApplicationListDataService,
        private storeHelperService: StoreHelperService,
    ) {}
}
