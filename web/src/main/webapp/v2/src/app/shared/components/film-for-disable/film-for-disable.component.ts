import { Component, OnInit, Input, ElementRef, Renderer2 } from '@angular/core';

@Component({
    selector: 'pp-film-for-disable',
    templateUrl: './film-for-disable.component.html',
    styleUrls: ['./film-for-disable.component.css']
})
export class FilmForDisableComponent implements OnInit {
    @Input() zIndex: number;
    @Input() marginWidth: number;
    constructor(
        private el: ElementRef,
        private renderer: Renderer2
    ) {}

    ngOnInit() {
        const el = this.el.nativeElement.querySelector('.l-disabled');
        this.renderer.setStyle(el, 'z-index', this.zIndex);
        this.renderer.setStyle(el, 'width', `calc(100% - ${this.marginWidth}px)`);
    }
}
