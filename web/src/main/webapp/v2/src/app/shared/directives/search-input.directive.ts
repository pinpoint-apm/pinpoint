import { Directive, OnInit, OnChanges, OnDestroy, SimpleChanges, EventEmitter, ElementRef, HostListener, Input, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, takeUntil } from 'rxjs/operators';

@Directive({
    selector: '[ppSearchInput]'
})
export class SearchInputDirective implements OnInit, OnChanges, OnDestroy {
    @Input() searchMinLength = 0;
    @Input() searchMaxLength = Number.MAX_SAFE_INTEGER;
    @Input() useEnter: boolean;
    @Input() debounceTime = 100;
    @Output() outCancel: EventEmitter<void> = new EventEmitter();
    @Output() outSearch: EventEmitter<string> = new EventEmitter();
    @Output() outArrowKey: EventEmitter<number> = new EventEmitter();
    private unsubscribe: Subject<void> = new Subject();
    private userInput: Subject<string> = new Subject();

    @HostListener('keydown', ['$event']) onKeyDown($event: KeyboardEvent): void {
        const keyCode = $event.keyCode;
        if (this.isArrowKey(keyCode)) {
            this.outArrowKey.emit(keyCode);
            return;
        }
    }
    @HostListener('keyup', ['$event']) onKeyUp($event: KeyboardEvent): void {
        const keyCode = $event.keyCode;
        const element = ($event.srcElement as HTMLInputElement);
        const value = element.value.trim();
        if (this.isArrowKey(keyCode)) {
            return;
        }
        if (this.isESC(keyCode)) {
            element.value = '';
            this.outCancel.next();
            return;
        }
        if (this.useEnter) {
            if (this.isEnter(keyCode) && this.isValidLength(value)) {
                this.outSearch.next(value);
            }
        } else {
            this.userInput.next(value);
        }
    }
    constructor(private elementRef: ElementRef) {}
    ngOnInit() {
    }
    ngOnChanges(changes: SimpleChanges) {
        if (changes['useEnter']) {
            if (this.useEnter) {
                this.unsubscribe.next();
            } else {
                this.setObservable();
            }
        }
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private setObservable(): void {
        this.userInput.pipe(
            takeUntil(this.unsubscribe),
            debounceTime(this.debounceTime),
            distinctUntilChanged(),
            filter((query: string) => {
                return this.isValidLength(query);
            })
        ).subscribe((query: string) => {
            this.outSearch.next(query);
        });
    }
    private isValidLength(value: string): boolean {
        return value.length === 0 || (value.length >= this.searchMinLength && value.length < this.searchMaxLength);
    }
    private isESC(key: number): boolean {
        return key === 27;
    }
    private isEnter(key: number): boolean {
        return key === 13;
    }
    private isArrowKey(key: number): boolean {
        return key >= 37 && key <= 40;
    }
    clear(): void {
        this.elementRef.nativeElement.value = '';
    }
    setFocus(): void {
        this.elementRef.nativeElement.focus();
    }
}
