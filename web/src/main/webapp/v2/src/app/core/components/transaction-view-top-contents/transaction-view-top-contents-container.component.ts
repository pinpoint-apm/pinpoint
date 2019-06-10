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
    @ViewChild('chartContainer', { read: ViewContainerRef }) chartContainer: ViewContainerRef;

    tabList: any[];
    private aliveComponentRef: ComponentRef<any>;

    constructor(
        private componentFactoryResolver: ComponentFactoryResolver,
        private analyticsService: AnalyticsService
    ) {}

    ngOnInit() {
        this.initTabList();
        this.loadComponent(this.tabList.find((tab) => tab.isActive).id);
    }

    ngOnDestroy() {
        this.aliveComponentRef.destroy();
    }

    onTabClick(tabName: string): void {
        this.setActiveTab(tabName);
        this.clearContainer();
        this.loadComponent(tabName);
    }

    private clearContainer(): void {
        this.chartContainer.clear();
    }

    private initTabList(): void {
        this.tabList = [{
            id: 'heap',
            displayText: 'Heap',
            isActive: true
        },
        {
            id: 'nonHeap',
            displayText: 'Non Heap',
            isActive: false
        },
        {
            id: 'cpu',
            displayText: 'CPU Load',
            isActive: false
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
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(TransactionViewChartContainerComponent);

        this.aliveComponentRef = this.chartContainer.createComponent(componentFactory);
        this.aliveComponentRef.instance.chartType = this.matchChartType(key);
    }

    private setActiveTab(tabName: string): void {
        this.tabList.forEach((tab) => tab.isActive = tabName === tab.id);
    }
}
