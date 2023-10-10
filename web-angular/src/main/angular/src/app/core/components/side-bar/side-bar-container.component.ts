import {
    Component,
    OnInit,
    OnDestroy,
    ElementRef,
    Renderer2,
    ChangeDetectionStrategy,
    ChangeDetectorRef
} from '@angular/core';
import {Router, RouterEvent, NavigationStart} from '@angular/router';
import {Subject, Observable, merge, fromEvent} from 'rxjs';
import {filter, tap, mapTo, map, takeUntil, delay} from 'rxjs/operators';

import {MessageQueueService, MESSAGE_TO, NewUrlStateNotificationService} from 'app/shared/services';
import {ServerMapData} from 'app/core/components/server-map/class';

@Component({
    selector: 'pp-side-bar-container',
    templateUrl: './side-bar-container.component.html',
    styleUrls: ['./side-bar-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SideBarContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    useDisable = true;
    showLoading = true;
    showDivider = false;
    isTargetMerged$: Observable<boolean>;
    sidebarVisibility = 'hidden';
    renderInfoPerServer: boolean;

    constructor(
        private router: Router,
        private messageQueueService: MessageQueueService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private el: ElementRef,
        private renderer: Renderer2,
        private cd: ChangeDetectorRef,
    ) {
    }

    ngOnInit() {
        this.renderInfoPerServer = !this.newUrlStateNotificationService.isRealTimeMode();
        this.addPageLoadingHandler();
        this.connectStore();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private addPageLoadingHandler(): void {
        merge(
            this.router.events.pipe(
                takeUntil(this.unsubscribe),
                filter((e: RouterEvent) => {
                    return e instanceof NavigationStart;
                })
            ),
            fromEvent(document, 'visibilitychange').pipe(
                takeUntil(this.unsubscribe),
                filter(() => this.newUrlStateNotificationService.isRealTimeMode()),
                filter(() => document.hidden),
                delay(10000),
                filter(() => document.hidden)
            )
        ).subscribe(() => {
            this.showLoading = true;
            this.useDisable = true;
            this.cd.detectChanges();
        });
    }

    private connectStore(): void {
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DATA_UPDATE).pipe(
            map(({serverMapData}: { serverMapData: ServerMapData }) => serverMapData.getNodeCount() === 0)
        ).subscribe((isEmpty: boolean) => {
            this.renderer.setStyle(this.el.nativeElement, 'display', isEmpty ? 'none' : 'block');
        });

        // TODO: MessageQueue로 바꿔주기.
        this.isTargetMerged$ = merge(
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT_BY_LIST).pipe(mapTo(false)),
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT).pipe(
                tap(({isNode, isWAS, isMerged}: ISelectedTarget) => {
                    this.showLoading = false;
                    this.useDisable = false;
                    this.showDivider = isNode && isWAS && !isMerged;
                    this.sidebarVisibility = 'visible';
                }),
                map(({isMerged}: ISelectedTarget) => isMerged)
            )
        );
    }
}
