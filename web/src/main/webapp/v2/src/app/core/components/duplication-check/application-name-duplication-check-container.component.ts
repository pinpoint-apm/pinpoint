import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { map, filter, switchMap, pluck } from 'rxjs/operators';

import { TranslateReplaceService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { ApplicationNameDuplicationCheckDataService, IApplicationAvailable } from './application-name-duplication-check-data.service';
import { ApplicationNameDuplicationCheckInteractionService } from './application-name-duplication-check-interaction.service';

@Component({
    selector: 'pp-application-name-duplication-check-container',
    templateUrl: './duplication-check-container.component.html',
    styleUrls: ['./duplication-check-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationNameDuplicationCheckContainerComponent implements OnInit {
    message: string;
    isValueValid: boolean;
    placeholder$: Observable<string>;

    private lengthGuide: string;
    private MAX_CHAR = 24;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private applicationNameDuplicationCheckDataService: ApplicationNameDuplicationCheckDataService,
        private applicationNameDuplicationCheckInteractionService: ApplicationNameDuplicationCheckInteractionService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.initPlaceholder();
        this.initLengthGuide();
    }

    private initPlaceholder(): void {
        this.placeholder$ = this.translateService.get('CONFIGURATION.INSTALLATION.APPLICATION_NAME_PLACEHOLDER');
    }

    private initLengthGuide(): void {
        this.translateService.get('CONFIGURATION.INSTALLATION.LENGTH_GUIDE').pipe(
            map((lengthGuide: string) => {
                return this.translateReplaceService.replace(lengthGuide, this.MAX_CHAR.toString());
            })
        ).subscribe((lengthGuide: string) => {
            this.lengthGuide = lengthGuide;
        });
    }

    onCheckValue(inputValue: string): void {
        of(inputValue).pipe(
            filter((value: string) => {
                return this.isLengthValid(value.length) ? true : (this.onCheckFail(this.lengthGuide), false);
            }),
            switchMap((value) => {
                return this.fetchResponse(value).pipe(
                    map((res: IApplicationAvailable) => {
                        return { value, res };
                    })
                );
            }),
            filter(({ res }) => {
                return this.isValueAvailable(res.code) ? true : (this.onCheckFail(res.message), false);
            }),
            pluck('value')
        ).subscribe((value: string) => {
            this.onCheckSuccess(value, '');
        }, (error: IServerErrorFormat) => {
            this.onCheckFail(error.exception.message);
        });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHECK_APPLICATION_NAME_DUPLICATION);
    }

    private isLengthValid(length: number): boolean {
        return !(length === 0 || length > this.MAX_CHAR);
    }

    private fetchResponse(value: string): Observable<IApplicationAvailable> {
        return this.applicationNameDuplicationCheckDataService.getResponseWithParams(value);
    }

    private isValueAvailable(code: number): boolean {
        return code === 0;
    }

    private onCheckFail(message: string): void {
        this.message = message;
        this.isValueValid = false;
        this.changeDetectorRef.detectChanges();
    }

    private onCheckSuccess(value: string, message: string): void {
        this.message = message;
        this.isValueValid = true;
        this.notifyCheckSuccess(value);
        this.changeDetectorRef.detectChanges();
    }

    private notifyCheckSuccess(value: string): void {
        this.applicationNameDuplicationCheckInteractionService.notifyCheckSuccess(value);
    }
}
