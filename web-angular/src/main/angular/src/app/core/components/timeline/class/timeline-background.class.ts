declare var Snap: any;
declare var mina: any;

export class TimelineBackground {
    private backgroundRect: any;
    constructor(private snap: any, private group: any, private options: { [key: string]: number }) {
        this.addElements();
    }
    addElements(): void {
        this.backgroundRect = this.snap.rect(this.options.left, this.options.top, this.options.width, this.options.height);
        this.group.add(this.backgroundRect);
    }
    reset(width: number): void {
        this.backgroundRect.animate({ 'width': width }, this.options.duration, mina.easein);
    }
    destroy(): void {}
}
