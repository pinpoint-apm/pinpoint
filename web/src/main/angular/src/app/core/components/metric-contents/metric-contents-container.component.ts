import { Component, OnInit, OnDestroy, ElementRef, Renderer2, ViewChild, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, EMPTY } from 'rxjs';
import { takeUntil, filter, switchMap, catchError, tap } from 'rxjs/operators';

import { NewUrlStateNotificationService, DynamicPopupService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { MetricContentsDataService } from './metric-contents-data.service';

@Component({
    selector: 'pp-metric-contents-container',
    templateUrl: './metric-contents-container.component.html',
    styleUrls: ['./metric-contents-container.component.css']
})
export class MetricContentsContainerComponent implements OnInit, OnDestroy {
    @ViewChild('chartGroupWrapper', {static: true}) chartGroupWrapper: ElementRef;
    private unsubscribe = new Subject<void>();
    private chartNumPerRow = 3; // Set this 3 temporarily. Should be responsive later

    metricList: string[];

    constructor(
        private renderer: Renderer2,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private metricContentsDataService: MetricContentsDataService
    ) {}

    ngOnInit() {
        this.renderer.setStyle(this.chartGroupWrapper.nativeElement, 'grid-template-columns', this.getGridLayout(this.chartNumPerRow));

        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => urlService.isValueChanged(UrlPathId.HOST_GROUP) || urlService.isValueChanged(UrlPathId.HOST)),
            tap(() => this.metricList = []),
            switchMap((urlService: NewUrlStateNotificationService) => {
                const hostGroup = urlService.getPathValue(UrlPathId.HOST_GROUP);
                const host = urlService.getPathValue(UrlPathId.HOST);

                return this.metricContentsDataService.getMetricList({hostGroup, host}).pipe(
                    catchError((error: IServerError) => {
                        this.dynamicPopupService.openPopup({
                            data: {
                                title: 'Server Error',
                                contents: error
                            },
                            component: ServerErrorPopupContainerComponent,
                        }, {
                            resolver: this.componentFactoryResolver,
                            injector: this.injector
                        });

                        return EMPTY;
                    })
                );
            })
        ).subscribe((metricList: string[]) => {
            this.metricList = [...metricList];
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private getGridLayout(chartNumPerRow: number): string {
        return `repeat(${chartNumPerRow}, 1fr)`;
    }
}
