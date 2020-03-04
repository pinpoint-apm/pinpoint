import { Component, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-configuration-icon',
    templateUrl: './configuration-icon.component.html',
    styleUrls: ['./configuration-icon.component.css']
})
export class ConfigurationIconComponent implements OnInit {
    @Output() outOpenConfigurationPopup = new EventEmitter<{[key: string]: ICoordinate}>();

    constructor() {}
    ngOnInit() {}
    onOpenConfigurationPopup($event: MouseEvent): void {
        const { left, top, width, height } = ($event.currentTarget as HTMLElement).getBoundingClientRect();

        this.outOpenConfigurationPopup.emit({
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            }
        });
    }
}
