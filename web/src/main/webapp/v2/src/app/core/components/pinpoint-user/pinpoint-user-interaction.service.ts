import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

@Injectable()
export class PinpointUserInteractionService {
    private outAdd = new Subject<string>();
    private outUpdate = new Subject<any>();
    private outRemove = new Subject<string>();

    onAdd$: Observable<string>;
    onRemove$: Observable<string>;
    onUpdate$: Observable<any>;

    constructor() {
        this.onAdd$ = this.outAdd.asObservable();
        this.onRemove$ = this.outRemove.asObservable();
        this.onUpdate$ = this.outUpdate.asObservable();
    }
    setAddPinpointUser(memberId: string): void {
        this.outAdd.next(memberId);
    }
    setRemovePinpointUser(memberId: string): void {
        this.outRemove.next(memberId);
    }
    setUserUpdated(memberInfo: any): void {
        this.outUpdate.next(memberInfo);
    }
}

