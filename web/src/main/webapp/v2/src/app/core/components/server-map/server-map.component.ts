import { Component, Input, Output, EventEmitter, OnInit, OnChanges, OnDestroy, AfterViewInit, SimpleChanges, ElementRef, ViewChild } from '@angular/core';
import * as go from 'gojs';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { ServerMapData } from './class/server-map-data.class';
import { ServerMapInteractionService } from './server-map-interaction.service';
import { ServerMapDiagram } from './class/server-map-diagram.class';
import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { ServerMapFactory, ServerMapType } from './class/server-map-factory';

@Component({
    selector: 'pp-server-map',
    templateUrl: './server-map.component.html',
    styleUrls: ['./server-map.component.css'],
})

export class ServerMapComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit {
    @ViewChild('serverMap') el: ElementRef;
    @Input() mapData: ServerMapData;
    @Input() baseApplicationKey: string;
    @Input() funcImagePath: Function;
    @Input() funcServerMapImagePath: Function;
    @Input() type: ServerMapType;
    @Output() outClickNode: EventEmitter<any> = new EventEmitter();
    @Output() outClickGroupNode: EventEmitter<any> = new EventEmitter();
    @Output() outContextClickNode: EventEmitter<string> = new EventEmitter();
    @Output() outClickLink: EventEmitter<any> = new EventEmitter();
    @Output() outContextClickLink: EventEmitter<string> = new EventEmitter();
    @Output() outClickBackground: EventEmitter<void> = new EventEmitter();
    @Output() outDoubleClickBackground: EventEmitter<string> = new EventEmitter();
    @Output() outContextClickBackground: EventEmitter<ICoordinate> = new EventEmitter();
    @Output() outRenderCompleted: EventEmitter<{[key: string]: boolean}> = new EventEmitter();

    private hasRenderData = false;
    private serverMapDiagram: ServerMapDiagram;
    private unsubscribe: Subject<null> = new Subject();

    constructor(
        private serverMapInteractionService: ServerMapInteractionService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnChanges(changes: SimpleChanges) {
        if (changes['mapData'] && changes['mapData']['currentValue']) {
            if (this.serverMapDiagram) {
                this.serverMapDiagram.setMapData(this.mapData, this.baseApplicationKey);
                this.hasRenderData = false;
            } else {
                this.hasRenderData = true;
            }
        }
    }
    ngOnInit() {
        this.serverMapInteractionService.onSearchWord$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((query: string) => {
            this.serverMapInteractionService.setSearchResult(this.serverMapDiagram.searchNode(query));
        });
        this.serverMapInteractionService.onSelectedApplication$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((appKey: string) => {
            this.serverMapDiagram.selectNodeBySearch(appKey);
        });
        this.serverMapInteractionService.onRefresh$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe(() => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REFRESH_SERVER_MAP);
            this.serverMapDiagram.refresh();
        });
        this.serverMapInteractionService.onChangeMergeState$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((params: IServerMapMergeState) => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_SERVER_MAP_MERGE_STATE, `${params.state}`);
            this.serverMapDiagram.setMergeState(params);
            this.serverMapDiagram.resetMergeState();
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    ngAfterViewInit() {
        this.serverMapDiagram = ServerMapFactory.createServerMap(this.type, {
            container: this.el.nativeElement,
            funcServerMapImagePath: this.funcServerMapImagePath
        });
        this.addEventHandler();
        if (this.hasRenderData) {
            this.serverMapDiagram.setMapData(this.mapData, this.baseApplicationKey);
            this.hasRenderData = false;
        }
    }
    addEventHandler(): void {
        this.serverMapDiagram.outRenderCompleted.subscribe((diagram: go.Diagram) => {
            this.serverMapInteractionService.setCurrentDiagram(<go.Diagram>diagram);
            this.outRenderCompleted.emit({
                showOverView: !!diagram
            });
        });
        this.serverMapDiagram.outClickNode.subscribe((nodeData: any) => {
            this.outClickNode.emit(nodeData);
        });
        this.serverMapDiagram.outClickGroupNode.subscribe((nodeData: any) => {
            this.outClickGroupNode.emit(nodeData);
        });
        this.serverMapDiagram.outContextClickNode.subscribe((node: any) => {
            this.outContextClickNode.emit(node);
        });
        this.serverMapDiagram.outClickLink.subscribe((linkData: any) => {
            this.outClickLink.emit(linkData);
        });
        this.serverMapDiagram.outContextClickLink.subscribe((linkObj: any) => {
            this.outContextClickLink.emit(linkObj);
        });
        this.serverMapDiagram.outClickBackground.subscribe(() => {
            this.outClickBackground.emit();
        });
        this.serverMapDiagram.outDoubleClickBackground.subscribe((msg: any) => {
            this.outDoubleClickBackground.emit(msg);
        });
        this.serverMapDiagram.outContextClickBackground.subscribe((coord: ICoordinate) => {
            this.outContextClickBackground.emit(coord);
        });
    }
    clear(): void {
        this.serverMapDiagram.clear();
    }
}
