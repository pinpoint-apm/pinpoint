import { Component, OnInit, Input, ElementRef, Renderer2 } from '@angular/core';
import { style, animate, transition, trigger } from '@angular/animations';

@Component({
    selector: 'pp-loading',
    animations: [
        trigger('fadeOut', [
            transition(':leave', [ // is alias to '* => void'
                animate(1000, style({
                    opacity: 0
                }))
            ])
        ])
    ],
    templateUrl: './loading.component.html',
    styleUrls: ['./loading.component.css']
})
export class LoadingComponent implements OnInit {
    @Input() showLoading: boolean;
    @Input() zIndex: number;

    constructor(
        private el: ElementRef,
        private renderer: Renderer2
    ) {}

    ngOnInit() {
        this.renderer.setStyle(this.el.nativeElement, 'z-index', this.zIndex);
    }
}
