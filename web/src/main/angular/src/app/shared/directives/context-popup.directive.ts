import { Directive, ViewContainerRef } from '@angular/core';

@Directive({
    selector: '[ppContextPopup]',
})
export class ContextPopupDirective {
    constructor(public viewContainerRef: ViewContainerRef) {}
}
