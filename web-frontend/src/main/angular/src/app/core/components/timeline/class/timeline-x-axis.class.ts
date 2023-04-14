declare var Snap: any;
import { TimelinePositionManager } from './timeline-position-manager.class';

export class TimelineXAxis {
    aText: any[];
    aAxisGroup: any[] = [];

    constructor(private snap: any, private group: any, private options: { [key: string]: any }, private oPositionManager: TimelinePositionManager) {
        this.aText = [];
        this.init();
    }
    init() {
        const aXBarPosition = this.oPositionManager.getXAxisPositionData();
        const centerX = this.options.width / 2;

        aXBarPosition.forEach((xBar: any, index: number) => {
            const g = this.group.g();
            const text = this.snap.text(0, this.options.textY, aXBarPosition[index].time);
            this.aText.push(text);
            g.attr('transform', 'translate(' + centerX + ', 0)');
            g.add(text, this.snap.line(0, this.options.startY, 0, this.options.endY));
            this.group.add(g);
            this.aAxisGroup.push(g);
            this.resetXPosition(g, aXBarPosition[index].x, centerX);
        });
    }
    resetXPosition(g: any, x: number, startX: number): void {
        Snap.animate(startX, x, (val: number) => {
            g.attr('transform', `translate(${val}, 0)`);
        }, this.options.duration);
    }
    reset() {
        const aYBarPosition = this.oPositionManager.getXAxisPositionData();
        for ( let i = 0 ; i < aYBarPosition.length ; i++ ) {
            this.aAxisGroup[i].attr('transform', `translate(${aYBarPosition[i].x}, 0)`);
            this.aText[i].attr('text', aYBarPosition[i].time);
        }
    }
    destroy(): void {}
}
