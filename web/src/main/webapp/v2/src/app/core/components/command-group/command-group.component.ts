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
    @Output() outOpenConfigurationPopup: EventEmitter<null> = new EventEmitter();

    constructor() {}
    ngOnInit() {}
    onOpenConfigurationPopup(): void {
        this.outOpenConfigurationPopup.emit();
    }
    onOpenRepository(): void {
        window.open('http://github.com/naver/pinpoint');
    }
}
