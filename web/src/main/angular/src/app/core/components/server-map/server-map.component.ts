import { Component, Input, Output, EventEmitter, OnInit, OnChanges, OnDestroy, AfterViewInit, SimpleChanges, ElementRef, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter, tap } from 'rxjs/operators';

import { ServerMapData } from './class/server-map-data.class';
import { ServerMapInteractionService } from './server-map-interaction.service';
import { ServerMapDiagram } from './class/server-map-diagram.class';
import { ServerMapFactory, ServerMapType } from './class/server-map-factory';

@Component({
    selector: 'pp-server-map',
    templateUrl: './server-map.component.html',
    styleUrls: ['./server-map.component.css'],
})

export class ServerMapComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit {
    @ViewChild('serverMap', {static: true}) el: ElementRef;
    @Input() mapData: ServerMapData;
    @Input() baseApplicationKey: string;
    @Input() funcImagePath: Function;
    @Input() funcServerMapImagePath: Function;
    @Input() type: ServerMapType;
    @Output() outClickNode = new EventEmitter<any>();
    @Output() outContextClickNode = new EventEmitter<string>();
    @Output() outClickLink = new EventEmitter<any>();
    @Output() outContextClickLink = new EventEmitter<string>();
    @Output() outContextClickBackground = new EventEmitter<ICoordinate>();
    @Output() outRenderCompleted = new EventEmitter<void>();

    private hasRenderData = false;
    private serverMapDiagram: ServerMapDiagram;
    private clickedKey: string;
    private hasDataUpdated = false;
    private unsubscribe = new Subject<void>();

    constructor(
        private serverMapInteractionService: ServerMapInteractionService,
    ) {}

    ngOnChanges(changes: SimpleChanges) {
        if (changes['mapData'] && changes['mapData']['currentValue']) {
            this.hasDataUpdated = true;
            if (this.serverMapDiagram) {
                this.serverMapDiagram.setMapData(this.mapData, this.baseApplicationKey);
                this.hasRenderData = false;
            } else {
                this.hasRenderData = true;
            }
        }
    }

    ngOnInit() {
        this.serverMapInteractionService.onSelectedApplication$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((appKey: string) => {
            this.serverMapDiagram.selectNodeBySearch(appKey);
        });
        this.serverMapInteractionService.onRefresh$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe(() => {
            this.serverMapDiagram.refresh();
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
        this.serverMapDiagram.outRenderCompleted.subscribe(() => {
            this.outRenderCompleted.emit();
        });
        this.serverMapDiagram.outClickNode.pipe(
            filter(({key}: any) => this.hasDataUpdated || this.clickedKey !== key),
            tap(({key}: any) => {
                this.clickedKey = key;
                this.hasDataUpdated = false;
            })
        ).subscribe((nodeData: any) => {
            this.outClickNode.emit(nodeData);
        });
        this.serverMapDiagram.outClickLink.pipe(
            filter(({key}: any) => this.hasDataUpdated || this.clickedKey !== key),
            tap(({key}: any) => {
                this.clickedKey = key;
                this.hasDataUpdated = false;
            })
        ).subscribe((linkData: any) => {
            this.outClickLink.emit(linkData);
        });
        this.serverMapDiagram.outContextClickLink.subscribe((linkObj: any) => {
            this.outContextClickLink.emit(linkObj);
        });
        this.serverMapDiagram.outContextClickNode.subscribe((node: any) => {
            this.outContextClickNode.emit(node);
        });
        this.serverMapDiagram.outContextClickBackground.subscribe((coord: ICoordinate) => {
            this.outContextClickBackground.emit(coord);
        });
    }

    clear(): void {
        this.serverMapDiagram.clear();
    }
}
