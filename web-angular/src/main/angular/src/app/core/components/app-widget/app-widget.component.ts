import {Component, ComponentFactoryResolver, Injector, Input, OnInit} from '@angular/core';
import {Subject, Observable, fromEvent} from 'rxjs';
import {map, takeUntil, pluck, startWith} from 'rxjs/operators';

import {UrlPathId} from 'app/shared/models';
import {NewUrlStateNotificationService, DynamicPopupService} from 'app/shared/services';
import {
    TransactionIdSearchContainerComponent
} from 'app/core/components/transaction-id-search/transaction-id-search-container.component';

const enum ScreenWidth {
    MIN = 1380
}

@Component({
    selector: 'pp-app-widget',
    templateUrl: './app-widget.component.html',
    styleUrls: ['./app-widget.component.css']
})
export class AppWidgetComponent implements OnInit {
    @Input() showServermapOption = true;
    @Input() showTransactionIdSearch = true;
    private unsubscribe = new Subject<void>();

    isAppSelected$: Observable<boolean>;
    isScreenWideEnough$: Observable<boolean>;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {
    }

    ngOnInit() {
        this.isAppSelected$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.APPLICATION))
        );
        this.addEventListener();
    }

    private addEventListener(): void {
        this.isScreenWideEnough$ = fromEvent(window, 'resize').pipe(
            takeUntil(this.unsubscribe),
            pluck('target', 'innerWidth'),
            startWith(window.innerWidth),
            map((width: number) => width >= ScreenWidth.MIN)
        );
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onClickSearchIcon(btn: HTMLElement): void {
        const {left, top, width, height} = btn.getBoundingClientRect();
        const coord = {
            coordX: left + width / 2,
            coordY: top + height / 2
        };

        this.dynamicPopupService.openPopup({
            coord,
            component: TransactionIdSearchContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
