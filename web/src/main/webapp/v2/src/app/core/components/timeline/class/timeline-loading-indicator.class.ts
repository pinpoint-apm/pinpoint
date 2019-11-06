declare var Snap: any;
declare var mina: any;

export class TimelineLoadingIndicator {
    private clockWiseRect: any;
    private antiClockWiseRect: any;
    private bRun = false;

    constructor(private snap: any, private group: any, private options: { [key: string]: number }) {
        this.addElements();
        this.show();
    }
    addElements(): void {
        const centerPosition = this.getCenterPosition(this.options.width, this.options.height, this.options.size);
        const backgroundRect = this.snap.rect(0, 0, this.options.width, this.options.height);

        this.clockWiseRect = this.snap.rect(centerPosition.x, centerPosition.y, this.options.size, this.options.size).attr({
            'stroke': 'rgba(197, 197, 197, .9)'
        });
        this.antiClockWiseRect = this.snap.rect(centerPosition.x, centerPosition.y, this.options.size, this.options.size).attr({
            'stroke': 'rgba(239, 246, 105)'
        });
        this.group.add(backgroundRect, this.clockWiseRect, this.antiClockWiseRect);
    }
    getCenterPosition(width: number, height: number, size: number): {x: number, y: number} {
        const halfSize = size / 2;
        return {
            x: width / 2 - halfSize,
            y: height / 2 - halfSize
        };
    }
    show() {
        this.group.attr('display', 'block');
        this.bRun = true;
        this.animate( this.clockWiseRect, 0, 360, mina.easeout );
        this.animate( this.antiClockWiseRect, 45, -315, mina.easein );
    }
    animate(ele, from, to, fnEase) {
        Snap.animate(from, to, (val) => {
            ele.attr('transform', 'rotate(' + val + 'deg)');
        }, this.options.duration, fnEase, () => {
            if ( this.bRun === true ) {
                this.animate( ele, to, from, fnEase );
            }
        });
    }
    hide() {
        this.group.attr('display', 'none');
        this.bRun = false;
    }
    destroy(): void {}
}
