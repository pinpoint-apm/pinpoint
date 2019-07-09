import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { style, animate, transition, trigger } from '@angular/animations';

@Component({
    selector: 'pp-retry',
    animations: [
        trigger('fadeIn', [
            transition(':enter', [ // is alias to 'void => *'
                style({opacity: 0}),
                animate(2000, style({opacity: 1}))
            ]),
        ])
    ],
    templateUrl: './retry.component.html',
    styleUrls: ['./retry.component.css']
})
export class RetryComponent implements OnInit {
    @Input() showRetry: boolean;
    @Input() message: string;
    @Output() outRetry = new EventEmitter<void>();

    constructor() {}
    ngOnInit() {}
    onRetry(): void {
        this.outRetry.emit();
    }
}
