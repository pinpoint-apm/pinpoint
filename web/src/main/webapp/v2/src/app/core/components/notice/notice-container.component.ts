import { Component, OnInit, ElementRef, Renderer2, ChangeDetectionStrategy } from '@angular/core';
import { Observable } from 'rxjs';
import { tap, filter } from 'rxjs/operators';

import { BrowserSupportCheckService } from 'app/shared/services';

@Component({
    selector: 'pp-notice-container',
    templateUrl: './notice-container.component.html',
    styleUrls: ['./notice-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class NoticeContainerComponent implements OnInit {
    noticeMessage$: Observable<string>;

    constructor(
        private elementRef: ElementRef,
        private renderer: Renderer2,
        private browserSupportCheckService: BrowserSupportCheckService
    ) { }

    ngOnInit() {
        this.noticeMessage$ = this.browserSupportCheckService.getMessage().pipe(
            filter((message: string) => {
                return message.length !== 0;
            }),
            tap(() => this.show())
        );
    }

    onClose(): void {
        this.hide();
    }

    private hide(): void {
        this.renderer.setStyle(this.elementRef.nativeElement, 'display', 'none');
    }

    private show(): void {
        this.renderer.setStyle(this.elementRef.nativeElement, 'display', 'block');
    }
}
