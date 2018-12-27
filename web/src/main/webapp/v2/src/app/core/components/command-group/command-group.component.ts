import { Component, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-command-group',
    templateUrl: './command-group.component.html',
    styleUrls: ['./command-group.component.css']
})
export class CommandGroupComponent implements OnInit {
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
