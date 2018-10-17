import { Component, OnInit } from '@angular/core';

import { SystemConfigurationDataService, UrlRouteManagerService, ISystemConfiguration } from 'app/shared/services';
import { ApplicationListDataService } from 'app/core/components/application-list/application-list-data.service';
@Component({
    templateUrl: './error-page.component.html',
    styleUrls: ['./error-page.component.css']
})
export class ErrorPageComponent implements OnInit {
    private configurationLoading = true;
    private applicationListLoading = true;
    private configurationSuccess = false;
    private applicationListSuccess = false;
    configurationErrorMessage = '';
    applicationListErrorMessage = '';
    showLoading = true;
    // funcImagePath: Function;
    // i18nText$: Observable<string>;

    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private systemConfigurationDataService: SystemConfigurationDataService,
        private applicationListDataService: ApplicationListDataService
    ) {}

    ngOnInit() {
        this.loadSystemConfiguration();
        this.loadApplicationList();
    }
    getConfigurationStateClass(): string {
        if (this.configurationLoading) {
            return 'fas fa-spinner fa-spin';
        } else {
            if (this.configurationSuccess) {
                return 'far fa-check-square l-success';
            } else {
                return 'far fa-times-circle l-fail';
            }
        }
    }
    getApplicationStateClass(): string {
        if (this.applicationListLoading) {
            return 'fas fa-spinner fa-spin';
        } else {
            if (this.applicationListSuccess) {
                return 'far fa-check-square l-success';
            } else {
                return 'far fa-times-circle l-fail';
            }
        }
    }
    private loadSystemConfiguration(): void {
        this.systemConfigurationDataService.getConfiguration().subscribe((configuration: ISystemConfiguration) => {
            this.configurationSuccess = true;
            this.configurationLoading = false;
        }, (error: IServerErrorFormat) => {
            this.configurationSuccess = false;
            this.configurationLoading = false;
            this.configurationErrorMessage = error.exception.message;
        });
    }
    private loadApplicationList(): void {
        this.applicationListDataService.getApplicationList().subscribe((applicatoinList: IApplication[]) => {
            this.applicationListSuccess = true;
            this.applicationListLoading = false;
        }, (error: IServerErrorFormat) => {
            this.applicationListSuccess = false;
            this.applicationListLoading = false;
            this.applicationListErrorMessage = error.exception.message;
        });
    }
    showErrorMessage(type: string): boolean {
        if (type === 'configuration') {
            return !(this.configurationLoading === false && this.configurationSuccess);
        } else if (type === 'applicationList') {
            return !(this.applicationListLoading === false && this.applicationListSuccess);
        }
        return false;
    }
    onMoveBack(): void {
        this.urlRouteManagerService.back();
    }
    onReload(): void {
        this.urlRouteManagerService.reload();
    }
    hasError(): boolean {
        return (this.configurationLoading === false && this.applicationListLoading === false) && !(this.configurationSuccess && this.applicationListSuccess);
    }
}
