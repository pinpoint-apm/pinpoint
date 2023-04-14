import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit, ComponentFactoryResolver, Injector } from '@angular/core';
import { Observable, iif, of, EMPTY } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

import { DynamicPopup, DynamicPopupService } from 'app/shared/services';
import { SyntaxHighlightDataService } from './syntax-highlight-data.service';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';

@Component({
    selector: 'pp-syntax-highlight-popup-container',
    templateUrl: './syntax-highlight-popup-container.component.html',
    styleUrls: ['./syntax-highlight-popup-container.component.css'],
})
export class SyntaxHighlightPopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Input() data: ISyntaxHighlightData;
    @Output() outClose = new EventEmitter<void>();
    @Output() outCreated = new EventEmitter<ICoordinate>();

    data$: Observable<ISyntaxHighlightData>;

    constructor(
        private syntaxHighlightDataService: SyntaxHighlightDataService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.data$ = iif(() => !!this.data.bindValue,
            this.syntaxHighlightDataService.getData(this.data).pipe(
                catchError((error: IServerError) => {
                    this.dynamicPopupService.openPopup({
                        data: {
                            title: 'Error',
                            contents: error
                        },
                        component: ServerErrorPopupContainerComponent
                    }, {
                        resolver: this.componentFactoryResolver,
                        injector: this.injector
                    });

                    return EMPTY;
                }),
                map(({bindedQuery}: {bindedQuery: string}) => {
                    return {...this.data, bindedContents: bindedQuery};
                })
            ),
            of(this.data)
        );
    }

    ngAfterViewInit() {
        this.outCreated.emit({ coordX: 0, coordY: 0 });
    }

    onClosePopup(): void {
        this.outClose.emit();
    }
}
