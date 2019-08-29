import { Component, OnInit, ElementRef, Input, ViewChild, OnDestroy } from '@angular/core';
import * as go from 'gojs';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { ServerMapInteractionService } from './server-map-interaction.service';

@Component({
    selector: 'pp-server-map-overview',
    templateUrl: './server-map-overview.component.html',
    styleUrls: ['./server-map-overview.component.css']
})
export class ServerMapOverviewComponent implements OnInit, OnDestroy {
    @ViewChild('wrapper', { static: true }) overviewWrapper: ElementRef;
    @Input() showOverview = false;
    overview: go.Overview;
    initialized = false;
    unsubscribe: Subject<null> = new Subject();
    constructor(
        private serverMapInteractionService: ServerMapInteractionService
    ) {}
    ngOnInit() {
        this.serverMapInteractionService.onCurrentDiagram$.pipe(
            takeUntil(this.unsubscribe),
            filter((diagram: go.Diagram) => {
                // TODO: visjs구현체에서 overview핸들링이 되기전까지 visjs옵션의 경우(null)엔 잠시 막아둠.
                return !!diagram;
            })
        ).subscribe((diagram: go.Diagram) => {
            if (this.initialized === true) {
                this.overview.observed = diagram;
            } else {
                this.overview = go.GraphObject.make(go.Overview, this.overviewWrapper.nativeElement, {
                    observed: diagram
                });
                this.overview.box.elt(0)['figure'] = 'Rectangle';
                this.overview.box.elt(0)['stroke'] = '#E7555A';
                this.overview.box.elt(0)['strokeWidth'] = .5;
                this.initialized = true;
            }
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
