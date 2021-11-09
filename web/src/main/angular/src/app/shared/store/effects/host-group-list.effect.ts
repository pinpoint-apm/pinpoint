import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, switchMap, withLatestFrom, filter, catchError } from 'rxjs/operators';

import { StoreHelperService } from 'app/shared/services';
import { getHostGroupList, getHostGroupListFail, getHostGroupListSuccess } from 'app/shared/store/actions';
import { isEmpty } from 'app/core/utils/util';
import { HostGroupListDataService } from 'app/core/components/host-group-list/host-group-list-data.service';

@Injectable()
export class HostGroupListEffect {
    getHostGroupList$ = createEffect(() =>
        this.actions$.pipe(
            ofType(getHostGroupList),
            withLatestFrom(this.storeHelperService.getHostGroupList()),
            filter(([{force}, hostGroupList]: [{force: boolean}, string[]]) => force || isEmpty(hostGroupList)),
            switchMap(() => this.hostGroupListDataService.getHostGroupList().pipe(
                map((hostGroupList: string[]) => getHostGroupListSuccess(hostGroupList)),
                catchError((error: IServerErrorFormat) => of(getHostGroupListFail(error)))
            )),
        )
    );

    constructor(
        private actions$: Actions,
        private hostGroupListDataService: HostGroupListDataService,
        private storeHelperService: StoreHelperService,
    ) {}
}
