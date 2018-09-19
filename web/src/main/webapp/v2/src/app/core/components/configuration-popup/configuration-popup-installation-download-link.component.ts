import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'pp-configuration-popup-installation-download-link',
    templateUrl: './configuration-popup-installation-download-link.component.html',
    styleUrls: ['./configuration-popup-installation-download-link.component.css'],
})
export class ConfigurationPopupInstallationDownloadLinkComponent implements OnInit {
    @Input() downloadLink: string;

    constructor() {}
    ngOnInit() {}
}
