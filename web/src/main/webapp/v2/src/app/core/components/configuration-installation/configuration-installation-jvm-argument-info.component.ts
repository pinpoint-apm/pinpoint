import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'pp-configuration-installation-jvm-argument-info',
    templateUrl: './configuration-installation-jvm-argument-info.component.html',
    styleUrls: ['./configuration-installation-jvm-argument-info.component.css'],
})
export class ConfigurationInstallationJVMArgumentInfoComponent implements OnInit {
    @Input() installationArgument: string;
    @Input() jvmArgument: string[];

    isArgumentInfoCopied: boolean;

    constructor() {}
    ngOnInit() {}

    getJVMArgumentInfoInView(): string {
        const [applicationName, agentId] = this.jvmArgument;

        return `${this.installationArgument}\n-Dpinpoint.applicationName=${applicationName}\n-Dpinpoint.agentId=${agentId}`;
    }

    onCopySuccess(): void {
        this.updateCopiedStatus(true);
        setTimeout(() => {
            this.updateCopiedStatus(false);
        }, 2000);
    }

    private updateCopiedStatus(status: boolean): void {
        this.isArgumentInfoCopied = status;
    }
}
