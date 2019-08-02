import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable()
export class ApplicationListInteractionForConfigurationService {
    private outSelectApplication = new Subject<IApplication>();

    onSelectApplication$: Observable<IApplication>;

    constructor() {
        this.onSelectApplication$ = this.outSelectApplication.asObservable();
    }

    setSelectedApplication(application: IApplication): void {
        this.outSelectApplication.next(application);
    }
}
