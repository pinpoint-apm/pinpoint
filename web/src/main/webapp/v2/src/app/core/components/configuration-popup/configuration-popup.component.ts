import { Component, OnInit, OnDestroy, Input, ViewChild, ViewContainerRef, ComponentFactoryResolver, Output, EventEmitter, HostBinding } from '@angular/core';

import { ConfigurationPopupGeneralContainerComponent } from './configuration-popup-general-container.component';
import { ConfigurationPopupUsergroupComponent } from './configuration-popup-usergroup.component';
import { ConfigurationPopupAlarmComponent } from './configuration-popup-alarm.component';
import { ConfigurationPopupInstallationContainerComponent } from './configuration-popup-installation-container.component';
import { ConfigurationPopupHelpContainerComponent } from './configuration-popup-help-container.component';

@Component({
    selector: 'pp-configuration-popup',
    templateUrl: './configuration-popup.component.html',
    styleUrls: ['./configuration-popup.component.css']
})
export class ConfigurationPopupComponent implements OnInit, OnDestroy {
    @ViewChild('contentContainer', { read: ViewContainerRef }) contentContainer: ViewContainerRef;
    @Input() funcImagePath: Function;
    @Output() outClosePopup = new EventEmitter<void>();
    @HostBinding('class.font-opensans') fontFamily = true;

    private componentMap = new Map<string, any>();
    private componentList = [
        ConfigurationPopupGeneralContainerComponent,
        ConfigurationPopupUsergroupComponent,
        ConfigurationPopupAlarmComponent,
        ConfigurationPopupInstallationContainerComponent,
        ConfigurationPopupHelpContainerComponent
    ];
    tabList: any[];

    constructor(
        private componentFactoryResolver: ComponentFactoryResolver,
    ) {}

    ngOnInit() {
        this.initTabList();
        this.initComponentMap();
        this.loadComponent(this.tabList.find((tab) => tab.isActive).id);
    }

    ngOnDestroy() {
        this.componentMap.forEach((value) => {
            if (value.componentRef) {
                value.componentRef.destroy();
            }
        });
    }

    onTabClick(tabName: string): void {
        this.setActiveTab(tabName);
        this.contentContainer.detach(0);
        if (!this.isComponentLoaded(tabName)) {
            this.loadComponent(tabName);
        } else {
            this.contentContainer.insert(this.componentMap.get(tabName).componentRef.hostView);
        }
    }

    private initTabList(): void {
        this.tabList = [{
            id: 'general',
            displayText: 'General',
            isActive: true
        },
        {
            id: 'usergroup',
            displayText: 'User Group',
            isActive: false
        },
        {
            id: 'alarm',
            displayText: 'Alarm',
            isActive: false
        },
        {
            id: 'installation',
            displayText: 'Installation',
            isActive: false
        },
        {
            id: 'help',
            displayText: 'Help',
            isActive: false
        }];
    }


    private initComponentMap(): void {
        this.tabList.forEach((value, i) => {
            this.componentMap.set(value.id, {
                component: this.componentList[i],
                isLoaded: false,
                componentRef: undefined
            });
        });
    }

    private loadComponent(key: string): void {
        const componentObj = this.componentMap.get(key);
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(componentObj.component);

        componentObj.componentRef = this.contentContainer.createComponent(componentFactory);
        componentObj.isLoaded = true;
    }

    private setActiveTab(tabName: string): void {
        this.tabList.forEach((tab) => tab.isActive = tabName === tab.id);
    }

    private isComponentLoaded(key: string): boolean {
        return this.componentMap.get(key).isLoaded;
    }

    onClickFilter(): void {
        this.onClickClose();
    }

    onClickClose(): void {
        this.outClosePopup.emit();
    }

    getIconFullPath(applicationName: string): string {
        return this.funcImagePath(applicationName);
    }
}
