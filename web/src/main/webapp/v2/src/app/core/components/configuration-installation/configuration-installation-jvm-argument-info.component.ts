import { Component, OnInit, Input } from '@angular/core';
import { trigger, style, animate, transition } from '@angular/animations';

import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-configuration-installation-jvm-argument-info',
    templateUrl: './configuration-installation-jvm-argument-info.component.html',
    styleUrls: ['./configuration-installation-jvm-argument-info.component.css'],
    animations: [
        trigger('showHide', [
            transition(':enter', [
                style({ opacity: 0 }),
                animate('1s ease-in-out', style({ opacity: 1 }))
            ]),
            transition(':leave', [
                animate('.5s ease-in-out', style({ opacity: 0 }))
            ])
        ]),
    ]
})
export class ConfigurationInstallationJVMArgumentInfoComponent implements OnInit {
    @Input() installationArgument: string;
    @Input() jvmArgument: string[];

    showCopiedNoti = false;

    constructor(
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {}
    getJVMArgumentInfoInView(): string {
        const [applicationName, agentId] = this.jvmArgument;

        return `${this.installationArgument}\n-Dpinpoint.applicationName=${applicationName}\n-Dpinpoint.agentId=${agentId}`;
    }

    onCopySuccess(): void {
        this.showCopiedNoti = true;
        setTimeout(() => {
            this.showCopiedNoti = false;
        }, 2000);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.COPY_JVM_ARGUMENT_INFO);
    }
}
