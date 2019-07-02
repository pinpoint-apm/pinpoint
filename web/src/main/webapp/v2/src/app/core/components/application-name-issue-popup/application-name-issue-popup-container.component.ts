import { Component, OnInit, ElementRef, AfterViewInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subject } from 'rxjs';
import { withLatestFrom, map, takeUntil } from 'rxjs/operators';

import { DynamicPopup, TranslateReplaceService, NewUrlStateNotificationService, PopupConstant } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';

@Component({
    selector: 'pp-application-name-issue-popup-container',
    templateUrl: './application-name-issue-popup-container.component.html',
    styleUrls: ['./application-name-issue-popup-container.component.css']
})
export class ApplicationNameIssuePopupContainerComponent implements OnInit, OnDestroy, AfterViewInit, DynamicPopup {
    @Input() data: {[key: string]: string};
    @Input() coord: ICoordinate;
    @Output() outCreated = new EventEmitter<ICoordinate>();
    @Output() outClose = new EventEmitter<void>();

    private unsubscribe = new Subject<void>();

    data$: Observable<{[key: string]: any}>;

    constructor(
        private elementRef: ElementRef,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
    ) {}

    ngOnInit() {
        const urlApplicationName$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            map((urlService: NewUrlStateNotificationService) => {
                return urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            })
        );

        this.data$ = this.translateService.get('INSPECTOR.APPLICAITION_NAME_ISSUE').pipe(
            withLatestFrom(urlApplicationName$),
            map(([message, urlAppName]: [{[key: string]: any}, string]) => {
                const { agentId, applicationName } = this.data;

                return {
                    prevAppName: urlAppName,
                    currAppName: applicationName,
                    message: {
                        ISSUE_MESSAGE: this.translateReplaceService.replace(message['ISSUE_MESSAGE'], urlAppName, applicationName),
                        ISSUE_CAUSES: [
                            this.translateReplaceService.replace(message['ISSUE_CAUSES'][0], urlAppName, applicationName),
                            this.translateReplaceService.replace(message['ISSUE_CAUSES'][1], agentId, applicationName)
                        ],
                        ISSUE_SOLUTIONS: [
                            this.translateReplaceService.replace(message['ISSUE_SOLUTIONS'][0], urlAppName, agentId),
                            message['ISSUE_SOLUTIONS'][1]
                        ]
                    }
                };
            })
        );
    }

    ngAfterViewInit() {
        this.outCreated.emit({
            coordX: this.coord.coordX - (this.elementRef.nativeElement.offsetWidth / 2),
            coordY: this.coord.coordY + PopupConstant.SPACE_FROM_BUTTON + PopupConstant.TOOLTIP_TRIANGLE_HEIGHT
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onInputChange(): void {
        this.outClose.emit();
    }

    onClickOutside(): void {
        this.outClose.emit();
    }
}
