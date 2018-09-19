declare var Snap: any;
import { TimelinePositionManager } from './timeline-position-manager.class';

export class TimelineSelectionZone {
    private elZone: any;
    constructor(private snap: any, private group: any, private options: { [key: string]: number }, private oPositionManager: TimelinePositionManager ) {
        this.addElements();
    }
    addElements() {
        const aSelectionZone = this.oPositionManager.getSelectionPosition();
        this.elZone = this.snap.rect(aSelectionZone[0], this.options.top, aSelectionZone[1] - aSelectionZone[0], this.options.height);
        this.group.add(this.elZone);
    }
    redraw() {
        const aSelectionZone = this.oPositionManager.getSelectionPosition();
        this.elZone.animate({
            'x': aSelectionZone[0],
            'width': aSelectionZone[1] - aSelectionZone[0]
        }, this.options.duration);
    }
    onDragXStart(x: number): void {
        const width = this.oPositionManager.getSelectionPosition()[1] - x;
        this.elZone.attr({
            'x': x,
            'width': width
        });
    }
    onDragXEnd(x: number): void {
        const selectionStartPosition = this.oPositionManager.getSelectionPosition()[0];
        this.elZone.attr({
            'x': selectionStartPosition,
            'width': x - selectionStartPosition
        });
    }
    destroy(): void {}
}
