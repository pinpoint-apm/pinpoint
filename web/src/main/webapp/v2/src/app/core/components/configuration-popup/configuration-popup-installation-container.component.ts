import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ApplicationNameDuplicationCheckInteractionService } from 'app/core/components/duplication-check/application-name-duplication-check-interaction.service';
import { AgentIdDuplicationCheckInteractionService } from 'app/core/components/duplication-check/agent-id-duplication-check-interaction.service';
import { ConfigurationPopupInstallationDataService, IInstallationData } from './configuration-popup-installation-data.service';

import { Observable, combineLatest, of } from 'rxjs';
import { filter, catchError, pluck } from 'rxjs/operators';

@Component({
    selector: 'pp-configuration-popup-installation-container',
    templateUrl: './configuration-popup-installation-container.component.html',
    styleUrls: ['./configuration-popup-installation-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfigurationPopupInstallationContainerComponent implements OnInit {
    desc$: Observable<string>;
    installationInfo$: Observable<object>;
    jvmArgument$: Observable<string[]>;

    constructor(
        private translateService: TranslateService,
        private configurationPopupInstallationDataService: ConfigurationPopupInstallationDataService,
        private applicationNameDuplicationCheckInteractionService: ApplicationNameDuplicationCheckInteractionService,
        private agentIdDuplicationCheckInteractionService: AgentIdDuplicationCheckInteractionService
    ) {}

    ngOnInit() {
        this.initDescText();
        this.initInstallationInfo();
        this.initJVMArgument();
    }

    private initDescText(): void {
        this.desc$ = this.translateService.get('CONFIGURATION.INSTALLATION.DESC');
    }

    private initInstallationInfo(): void {
        this.installationInfo$ = this.configurationPopupInstallationDataService.getData()
            .pipe(
                filter((data: IInstallationData) => {
                    return data.code === 0;
                }),
                pluck('message'),
                catchError((err) => {
                    return this.onAjaxError(err);
                })
            );
    }

    private initJVMArgument(): void {
        this.jvmArgument$ = combineLatest(
            this.applicationNameDuplicationCheckInteractionService.onCheckSuccess$,
            this.agentIdDuplicationCheckInteractionService.onCheckSuccess$,
        );
    }

    private onAjaxError(err: Error): Observable<any> {
        // TODO: Error발생시 띄워줄 팝업 컴포넌트 Call - issue#170
        return of({
            downloadUrl: '',
            installationArgument: ''
        });
    }
}
