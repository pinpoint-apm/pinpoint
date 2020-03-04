import {
    Injectable,
    ComponentFactoryResolver,
    ComponentRef,
    ApplicationRef,
    Injector,
    EmbeddedViewRef,
    Renderer2,
    RendererFactory2,
    EventEmitter
} from '@angular/core';

export interface DynamicPopup {
    data?: any;
    coord?: ICoordinate;
    outCreated?: EventEmitter<ICoordinate>;
    outClose?: EventEmitter<void>;
    outReInit?: EventEmitter<{[key: string]: any}>; // HelpViewer처럼 컴포넌트를 아예 destroy하고 새로 init해야 하는 경우
    onInputChange?: Function;
}

export interface IContext {
    resolver: ComponentFactoryResolver;
    injector: Injector;
}

interface IPopupParam {
    data?: any;
    coord?: ICoordinate;
    component: any;
    onCloseCallback?: Function;
}

export const enum PopupConstant {
    TOOLTIP_TRIANGLE_HEIGHT = 7, // 툴팁 삼각형 높이
    TOOLTIP_TRIANGLE_WIDTH = 14, // 툴팁 삼각형 넓이
    SPACE_FROM_BUTTON = 9, // 클릭한 버튼에서 살짝 떨어뜨려줄 길이
    SPACE_FROM_LEFT = 20 // 팝업 앞단의 여유
}

@Injectable()
export class DynamicPopupService {
    private renderer: Renderer2;
    private componentRef: ComponentRef<DynamicPopup>;
    private injector: Injector;
    private resolver: ComponentFactoryResolver;

    constructor(
        private appRef: ApplicationRef,
        rendererFactory: RendererFactory2,
    ) {
        this.renderer = rendererFactory.createRenderer(null, null);
    }

    openPopup($param: IPopupParam, { resolver, injector }: IContext): void {
        this.injector = injector;
        this.resolver = resolver;

        const {data, coord, component} = $param;

        if (!this.componentRef) {
            // The very first
            this.initPopup($param);
        } else if (this.componentRef && this.componentRef.instance instanceof component) {
            // Update input binding
            this.bindInputProps(this.componentRef.instance, data, coord);
        } else {
            // Close the current popup and create a new one
            this.closePopup();
            this.initPopup($param);
        }
    }

    closePopup(): void {
        this.appRef.detachView(this.componentRef.hostView);
        this.componentRef.destroy();
        this.componentRef = null;
    }

    private initPopup({data, coord, component, onCloseCallback}: IPopupParam): void {
        this.componentRef = this.resolver.resolveComponentFactory<DynamicPopup>(component).create(this.injector);
        const popupComponent = this.componentRef.instance;
        const domElem = (this.componentRef.hostView as EmbeddedViewRef<DynamicPopup>).rootNodes[0] as HTMLElement;

        this.bindInputProps(popupComponent, data, coord);
        this.bindOutputProps(popupComponent, domElem, onCloseCallback);
        this.renderer.addClass(domElem, 'popup');
        this.renderer.appendChild((this.appRef.components[0].location).nativeElement, domElem);
        this.appRef.attachView(this.componentRef.hostView);
    }

    private bindInputProps(popupComponent: DynamicPopup, data: any, coord: ICoordinate): void {
        if (popupComponent.onInputChange) {
            popupComponent.onInputChange({data, coord});
        }
        if (data) {
            popupComponent.data = data;
        }
        if (coord) {
            popupComponent.coord = coord;
        }
    }

    private bindOutputProps(popupComponent: DynamicPopup, domElem: HTMLElement, onCloseCallback: Function): void {
        if (popupComponent.outCreated) {
            popupComponent.outCreated.subscribe(({coordX, coordY}: {coordX: number, coordY: number}) => {
                this.renderer.setStyle(domElem, 'left', coordX < 0 ? 0 : coordX + 'px');
                this.renderer.setStyle(domElem, 'top', coordY < 0 ? 0 : coordY + 'px');
            });
        }
        if (popupComponent.outClose) {
            popupComponent.outClose.subscribe(() => {
                if (onCloseCallback) {
                    onCloseCallback();
                }

                this.closePopup();
            });
        }
        if (popupComponent.outReInit) {
            popupComponent.outReInit.subscribe(({data, coord}: {data: any, coord: ICoordinate}) => {
                const component = this.componentRef.componentType;

                this.closePopup();
                this.openPopup({
                    data,
                    coord,
                    component
                }, {
                    resolver: this.resolver,
                    injector: this.injector
                });
            });
        }
    }
}
