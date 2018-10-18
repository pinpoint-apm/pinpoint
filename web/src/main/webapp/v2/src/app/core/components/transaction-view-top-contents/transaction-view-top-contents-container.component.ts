import { Component, OnInit, OnDestroy, ComponentFactoryResolver, ViewChild, ViewContainerRef, ComponentRef, ChangeDetectionStrategy } from '@angular/core';

import { TransactionViewJVMHeapChartContainerComponent } from 'app/core/components/inspector-chart/transaction-view-jvm-heap-chart-container.component';
import { TransactionViewJVMNonHeapChartContainerComponent } from 'app/core/components/inspector-chart/transaction-view-jvm-non-heap-chart-container.component';
import { TransactionViewCPUChartContainerComponent } from 'app/core/components/inspector-chart/transaction-view-cpu-chart-container.component';
import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-transaction-view-top-contents-container',
    templateUrl: './transaction-view-top-contents-container.component.html',
    styleUrls: ['./transaction-view-top-contents-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionViewTopContentsContainerComponent implements OnInit, OnDestroy {
    @ViewChild('chartContainer', { read: ViewContainerRef }) chartContainer: ViewContainerRef;

    tabList: any[];
    private componentMap = new Map<string, any>();
    private chartComponentList = [TransactionViewJVMHeapChartContainerComponent, TransactionViewJVMNonHeapChartContainerComponent, TransactionViewCPUChartContainerComponent];
    private aliveComponentRef: ComponentRef<any>;

    constructor(
        private componentFactoryResolver: ComponentFactoryResolver,
        private analyticsService: AnalyticsService
    ) {}

    ngOnInit() {
        this.initTabList();
        this.initComponentMap();
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

    private initComponentMap(): void {
        this.tabList.forEach((value, i) => this.componentMap.set(value.id, this.chartComponentList[i]));
    }

    private loadComponent(key: string): void {
        this.analyticsService.trackEvent((TRACKED_EVENT_LIST as any)[`CLICK_${key}`]);
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(this.componentMap.get(key));

        this.aliveComponentRef = this.chartContainer.createComponent(componentFactory);
    }

    private setActiveTab(tabName: string): void {
        this.tabList.forEach((tab) => tab.isActive = tabName === tab.id);
    }
}
