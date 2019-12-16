import { Component, OnInit, OnDestroy, ComponentFactoryResolver, ViewChild, ViewContainerRef, ComponentRef, HostBinding } from '@angular/core';

import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { ChartType } from 'app/core/components/inspector-chart/inspector-chart-container-factory';
import { TransactionViewChartContainerComponent } from 'app/core/components/inspector-chart/transaction-view-chart-container.component';

@Component({
    selector: 'pp-transaction-view-top-contents-container',
    templateUrl: './transaction-view-top-contents-container.component.html',
    styleUrls: ['./transaction-view-top-contents-container.component.css'],
})
export class TransactionViewTopContentsContainerComponent implements OnInit, OnDestroy {
    @HostBinding('class') hostClass = 'l-transaction-view-top-contents';
    @ViewChild('chartContainer', { read: ViewContainerRef, static: true }) chartContainer: ViewContainerRef;

    private componentRefMap = new Map<string, ComponentRef<TransactionViewChartContainerComponent>>();

    tabList: {id: string, display: string}[];
    activeTab = 'heap';

    constructor(
        private componentFactoryResolver: ComponentFactoryResolver,
        private analyticsService: AnalyticsService
    ) {}

    ngOnInit() {
        this.initTabList();
        this.loadComponent(this.activeTab);
    }

    ngOnDestroy() {
        this.chartContainer.get(0).destroy();
    }

    onTabClick(tab: string): void {
        if (tab === this.activeTab) {
            return;
        }

        this.activeTab = tab;
        this.chartContainer.detach(0);
        this.loadComponent(tab);
    }

    private initTabList(): void {
        this.tabList = [{
            id: 'heap',
            display: 'Heap',
        },
        {
            id: 'nonHeap',
            display: 'Non Heap',
        },
        {
            id: 'cpu',
            display: 'CPU Load',
        }];
    }

    private matchChartType(tabId: string): ChartType {
        switch (tabId) {
            case 'heap':
                return ChartType.AGENT_JVM_HEAP;
            case 'nonHeap':
                return ChartType.AGENT_JVM_NON_HEAP;
            case 'cpu':
                return ChartType.AGENT_CPU;
        }
    }

    private loadComponent(key: string): void {
        this.analyticsService.trackEvent((TRACKED_EVENT_LIST as any)[`CLICK_${key}`]);

        const componentRef = this.componentRefMap.get(key);

        if (componentRef) {
            this.chartContainer.insert(componentRef.hostView);
            componentRef.instance.onBackToTheView();
        } else {
            const componentFactory = this.componentFactoryResolver.resolveComponentFactory(TransactionViewChartContainerComponent);
            const component = this.chartContainer.createComponent(componentFactory);

            this.componentRefMap.set(key, component);
            component.instance.chartType = this.matchChartType(key);
        }
    }
}
