export class TimelineUIEvent {
    changedSelectedTime = false;
    changedSelectionRange = false;
    changedRange = false;
    data = {
        selectedTime: 0,
        range: [0, 0],
        selectionRange: [0, 0]
    };
    setData(time: number, selectionRange: number[], range: number[]): TimelineUIEvent {
        this.data.selectedTime = time;
        this.data.selectionRange = selectionRange;
        this.data.range = range;
        return this;
    }
    setSelectedTime(time: number): TimelineUIEvent {
        this.data.selectedTime = time;
        return this;
    }
    setSelectionRange(range: number[]): TimelineUIEvent {
        this.data.selectionRange = range;
        return this;
    }
    setRange(range: number[]): TimelineUIEvent {
        this.data.range = range;
        return this;
    }
    setOnChangedSelectedTime(): void {
        this.changedSelectedTime = true;
    }
    setOffChangedSelectedTime(): void {
        this.changedSelectedTime = false;
    }
    setOnChangedSelectionRange(): void {
        this.changedSelectionRange = true;
    }
    setOffChangedSelectionRange(): void {
        this.changedSelectionRange = false;
    }
    setOnRange(): void {
        this.changedRange = true;
    }
    setOffRange(): void {
        this.changedRange = false;
    }
}
