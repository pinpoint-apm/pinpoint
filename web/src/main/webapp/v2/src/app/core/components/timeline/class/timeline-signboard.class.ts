declare var Snap: any;
import { TimelinePositionManager } from './timeline-position-manager.class';

export class TimelineSignboard {
    private timeText: any;
    private textMaxWidth = 10;
    private xPadding = 10;
    constructor(private snap: any, private group: any, private options: { [key: string]: any }, private oPositionManager: TimelinePositionManager ) {
        this.addElements();
    }
    addElements(): void {
        const isIn = this.isIn(this.options.x);
        const x = this.options.x + (isIn ? this.xPadding : -this.xPadding);
        this.timeText = this.group.text(x, 26, this.oPositionManager.getFullTimeStr(this.options.x) ).attr({
            'text-anchor': this.getAnchorPosition(isIn)
        });
        this.group.add( this.timeText );
    }
    setX(x: number): void {
        const isIn = this.isIn(x);
        this.timeText.attr({
            'x': x + ( isIn ? this.xPadding : -this.xPadding ),
            'text': this.oPositionManager.getFullTimeStr(x),
            'text-anchor': this.getAnchorPosition(isIn)
        });
    }
    getAnchorPosition(innerPosition: boolean): string {
        return innerPosition ? 'start' : 'end';
    }
    isIn(x: number): boolean {
        if ( this.options.direction === 'left' ) {
            return x < this.textMaxWidth;
        } else {
            return x + this.textMaxWidth < this.oPositionManager.getTimelineEndPosition();
        }
    }
    onDragStart(x: number): void {
        this.setX(x);
    }
    onDragEnd(): void {}
    onDrag(x: number): void {
        this.setX(x);
    }
    destroy(): void {}
}
