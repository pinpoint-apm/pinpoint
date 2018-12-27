import { Component, OnInit, Output, EventEmitter } from '@angular/core';
/**
 * 도움말, 설정, repository 링크등을 제공하는 Component
 */
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
    onOpenRepository(): void {
        window.open('http://github.com/naver/pinpoint');
    }
}
