import { Component, OnInit, Input } from '@angular/core';
import { style, animate, transition, trigger } from '@angular/animations';

@Component({
    selector: 'pp-no-data',
    animations: [
        trigger('fadeIn', [
            transition(':enter', [ // is alias to 'void => *'
                style({opacity: 0}),
                animate(1000, style({opacity: 1}))
            ]),
        ])
    ],
    templateUrl: './no-data.component.html',
    styleUrls: ['./no-data.component.css']
})
export class NoDataComponent implements OnInit {
    @Input() message: string;
    @Input() showNoData: boolean;
    constructor() { }

    ngOnInit() {
    }

}
