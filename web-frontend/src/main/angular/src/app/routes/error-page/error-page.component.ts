import { Component, OnInit } from '@angular/core';

import { SystemConfigurationDataService, ServerTimeDataService, UrlRouteManagerService } from 'app/shared/services';

@Component({
    templateUrl: './error-page.component.html',
    styleUrls: ['./error-page.component.css']
})
export class ErrorPageComponent implements OnInit {
    stateList = ['serverTime', 'configuration'];
    state: any = {
        serverTime: {
            url: 'serverTime.pinpoint',
            loading: true,
            success: false,
            message: ''
        },
        configuration: {
            url: 'configuration.pinpoint',
            loading: true,
            success: false,
            message: ''
        }
    };
    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private systemConfigurationDataService: SystemConfigurationDataService,
        private serverTimeDataService: ServerTimeDataService
    ) {}

    ngOnInit() {
        this.checkServerTime(this.stateList[0]);
        this.checkSystemConfiguration(this.stateList[1]);
    }
    private setState(type: string, result: boolean, loading: boolean, message: string = ''): void {
        this.state[type].success = result;
        this.state[type].loading = loading;
        this.state[type].message = message;
    }
    private checkServerTime(type: string): void {
        this.serverTimeDataService.getServerTime().subscribe((time: number) => {
            this.setState(type, true, false);
        }, (error: IServerError) => {
            this.setState(type, false, false, error.message);
        });
    }
    private checkSystemConfiguration(type: string): void {
        this.systemConfigurationDataService.getConfiguration().subscribe((configuration: ISystemConfiguration) => {
            this.setState(type, true, false);
        }, (error: IServerError) => {
            this.setState(type, false, false, error.message);
        });
    }
    getErrorMessage(type: string): string {
        return this.state[type].message;
    }
    showErrorMessage(type: string): boolean {
        return !(this.state[type].loading === false && this.state[type].success);
    }
    getStateClass(type: string): string {
        const spin = 'fas fa-spinner fa-spin';
        const success = 'far fa-check-square l-success';
        const fail = 'far fa-times-circle l-fail';
        const typeState = this.state[type];

        if (typeState.loading) {
            return spin;
        } else {
            return typeState.success ? success : fail;
        }
    }
    getUrl(type: string): string {
        return this.state[type].url;
    }
    onMoveBack(): void {
        this.urlRouteManagerService.back();
    }
    onReload(): void {
        this.urlRouteManagerService.reload();
    }
    hasError(): boolean {
        return this.stateList.reduce((prevState: boolean, stateName: string) => {
            return prevState && (this.state[stateName].loading === false && !this.state[stateName].success);
        }, true);
    }
}
