declare var Snap: any;
declare var mina: any;

export class TimelineSelectionPoint {
    constructor(private snap: any, private group: any, private options: { [key: string]: number } ) {
        this.addElements();
        this.setPointing( options.x );
    }
    addElements(): void {
        const halfRadius = this.options.radius / 2;
        this.group.add(
            this.snap.line(0, 0, 0, this.options.height),
            this.snap.circle(0, this.options.height / 2, this.options.radius).attr({
                'filter': this.snap.filter( Snap.filter.shadow(0, 0, 4, '#FF0', 1))
            })
        );
    }
    setPointing(x: number): void {
        this.group.animate({
            'transform': `translate(${x}, ${this.options.y})`
        }, this.options.duration, mina.easein);
    }
    destroy(): void {}
}
