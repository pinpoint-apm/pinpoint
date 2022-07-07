import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { concatMap, filter, map, switchMap, withLatestFrom, catchError } from 'rxjs/operators';

import { StoreHelperService, WebAppSettingDataService } from 'app/shared/services';
import {
    addFavApplication,
    addFavApplicationFail,
    addFavApplicationSuccess,
    getFavApplicationList,
    getFavApplicationListFail,
    getFavApplicationListSuccess,
    removeFavApplication,
    removeFavApplicationFail,
    removeFavApplicationSuccess
} from 'app/shared/store/actions';
import { FavoriteApplicationListDataService } from 'app/core/components/application-list/favorite-application-list-data.service';
import { isEmpty } from 'app/core/utils/util';

@Injectable()
export class FavoriteApplicationListEffect {
    getFavApplicationList$ = createEffect(() =>
        this.actions$.pipe(
            ofType(getFavApplicationList),
            withLatestFrom(this.storeHelperService.getFavoriteApplicationList()),
            // filter(([_, favAppList]: [null, IApplication[]]) => isEmpty(favAppList)),
            filter(([_, favAppList]: [null, IApplication[]]) => favAppList === null),
            switchMap(() => this.favoriteApplicationListDataService.getFavoriteApplicationList().pipe(
                map((favAppList: IApplication[]) => getFavApplicationListSuccess(favAppList)),
                catchError((error: IServerError) => of(getFavApplicationListFail(error)))
            )),
        )
    );

    addFavApplication$ = createEffect(() =>
        this.actions$.pipe(
            ofType(addFavApplication),
            concatMap(({favApp}: {favApp: IApplication}) => this.webAppSettingDataService.addFavoriteApplication(favApp).pipe(
                map((app: IApplication) => addFavApplicationSuccess(app)),
                catchError((error: IServerError) => of(addFavApplicationFail(error)))
            ))
        )
    );

    removeFavApplication$ = createEffect(() =>
        this.actions$.pipe(
            ofType(removeFavApplication),
            concatMap(({favApp}: {favApp: IApplication}) => this.webAppSettingDataService.removeFavoriteApplication(favApp).pipe(
                map((app: IApplication) => removeFavApplicationSuccess(app)),
                catchError((error: IServerError) => of(removeFavApplicationFail(error)))
            ))
        )
    );

    constructor(
        private actions$: Actions,
        private favoriteApplicationListDataService: FavoriteApplicationListDataService,
        private webAppSettingDataService: WebAppSettingDataService,
        private storeHelperService: StoreHelperService,
    ) {}
}
