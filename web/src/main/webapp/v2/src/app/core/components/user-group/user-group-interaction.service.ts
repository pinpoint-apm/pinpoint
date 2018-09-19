import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

@Injectable()
export class UserGroupInteractionService {
    private outSelect = new Subject<string>();
    onSelect$: Observable<string>;

    constructor() {
        this.onSelect$ = this.outSelect.asObservable();
    }
    setSelectedUserGroup(id: string): void {
        this.outSelect.next(id);
    }
}

