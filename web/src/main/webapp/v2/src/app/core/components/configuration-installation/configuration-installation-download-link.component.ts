import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'pp-configuration-installation-download-link',
    templateUrl: './configuration-installation-download-link.component.html',
    styleUrls: ['./configuration-installation-download-link.component.css'],
})
export class ConfigurationInstallationDownloadLinkComponent implements OnInit {
    @Input() downloadLink: string;

    constructor() {}
    ngOnInit() {}
}
