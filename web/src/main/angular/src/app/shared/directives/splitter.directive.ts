import { Directive, OnInit, HostListener } from '@angular/core';
import { GutterEventService } from 'app/shared/services';

@Directive({
    selector: '[ppSplitter]'
})
export class SplitterDirective implements OnInit {
    constructor(
        private gutterEventService: GutterEventService
    ) { }

    ngOnInit() {
    }

    @HostListener('dragEnd', ['$event'])
    @HostListener('dragProgress', ['$event'])
    onDragEndProgress({sizes}: {sizes: number[]}) {
        this.gutterEventService.resizedGutter(sizes);
    }
}
