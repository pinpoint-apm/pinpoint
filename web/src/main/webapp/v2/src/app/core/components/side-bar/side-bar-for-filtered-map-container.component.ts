import { Component, OnInit, OnDestroy, ElementRef, Renderer2 } from '@angular/core';
import { Router, RouterEvent, NavigationStart } from '@angular/router';
import { Subject } from 'rxjs';
import { filter } from 'rxjs/operators';

import { StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-side-bar-for-filtered-map-container',
    templateUrl: './side-bar-for-filtered-map-container.component.html',
    styleUrls: ['./side-bar-for-filtered-map-container.component.css'],
})
export class SideBarForFilteredMapContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    target: ISelectedTarget;
    useDisable = true;
    showLoading = true;

    constructor(
        private router: Router,
        private storeHelperService: StoreHelperService,
        private el: ElementRef,
        private renderer: Renderer2
    ) {}

    ngOnInit() {
        this.addPageLoadingHandler();
        this.connectStore();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private addPageLoadingHandler(): void {
        this.router.events.pipe(
            filter((e: RouterEvent) => {
                return e instanceof NavigationStart;
            })
        ).subscribe(() => {
            this.showLoading = true;
            this.useDisable = true;
        });
    }

    private connectStore(): void {
        this.storeHelperService.getServerMapLoadingState(this.unsubscribe).subscribe((state: string) => {
            switch (state) {
                case 'loading':
                    this.showLoading = true;
                    this.useDisable = true;
                    break;
                case 'pause':
                case 'completed':
                    this.showLoading = false;
                    this.useDisable = false;
                    break;
            }
        });
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isNode === true || target.isNode === false) ? true : false;
            })
        ).subscribe((target: ISelectedTarget) => {
            this.target = target;
            this.renderer.setStyle(this.el.nativeElement, 'width', '477px');
        });
    }

    hasTopElement(): boolean {
        return this.target && (this.target.isNode || this.target.isMerged);
    }
}
