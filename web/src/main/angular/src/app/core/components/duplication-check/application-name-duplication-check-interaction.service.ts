import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable()
export class ApplicationNameDuplicationCheckInteractionService {
    private outCheckSuccess = new BehaviorSubject<string>('');

    onCheckSuccess$ = this.outCheckSuccess.asObservable();

    constructor() {}

    notifyCheckSuccess(value: string): void {
        this.outCheckSuccess.next(value);
    }
}
