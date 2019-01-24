import { Component, OnInit, HostBinding, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-configuration-popup',
    templateUrl: './configuration-popup.component.html',
    styleUrls: ['./configuration-popup.component.css']
})
export class ConfigurationPopupComponent implements OnInit {
    @HostBinding('class.font-opensans') fontFamily = true;
    @Output() outMenuClick = new EventEmitter<string>();
    @Output() outOpenLink = new EventEmitter<void>();

    constructor() {}
    ngOnInit() {}
    onMenuClick(type: string): void {
        this.outMenuClick.emit(type);
    }

    onOpenLink(): void {
        this.outOpenLink.emit();
    }
}
