import { Component, OnInit, HostBinding, Output, EventEmitter, ChangeDetectionStrategy, Input } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';

@Component({
    selector: 'pp-configuration-popup',
    templateUrl: './configuration-popup.component.html',
    styleUrls: ['./configuration-popup.component.css'],
    animations: [
        trigger('collapseSpread', [
            state('collapsed', style({
                maxHeight: 0,
                overflow: 'hidden'
            })),
            state('spreaded', style({
                maxHeight: '300px'
            })),
            transition('collapsed <=> spreaded', [
                animate('0.3s')
            ])
        ]),
        trigger('rightDown', [
            state('collapsed', style({
                transform: 'none'
            })),
            state('spreaded', style({
                transform: 'rotate(90deg)'
            })),
            transition('collapsed <=> spreaded', [
                animate('0.1s')
            ])
        ])
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfigurationPopupComponent implements OnInit {
    @HostBinding('class.font-opensans') fontFamily = true;
    @Input() webhookEnable: boolean;
    @Input() currentTheme: string;
    @Output() outMenuClick = new EventEmitter<string>();
    @Output() outOpenLink = new EventEmitter<void>();
    @Output() outChangeTheme = new EventEmitter<string>();

    isMenuCollapsed: {[key: string]: boolean} = {
        admin: false,
        setting: false
    };

    constructor() {}
    ngOnInit() {}
    onMenuClick(type: string): void {
        this.outMenuClick.emit(type);
    }

    onOpenLink(): void {
        this.outOpenLink.emit();
    }

    onClickTheme($event: MouseEvent): void {
        const target = $event.target as HTMLElement;

        if (!Array.from(target.classList).includes('active')) {
            const theme = target.dataset.theme;

            this.outChangeTheme.emit(theme);
        }
    }

    isThemeActive(themeButtonElement: HTMLButtonElement): boolean {
        return themeButtonElement.dataset.theme === this.currentTheme ? true : false;
    }

    toggleMenu(menu: string): void {
        this.isMenuCollapsed[menu] = !this.isMenuCollapsed[menu];
    }

    getCollapsedState(menu: string): string {
        return this.isMenuCollapsed[menu] ? 'collapsed' : 'spreaded';
    }

    isActive(linkElement: HTMLAnchorElement): boolean {
        const listItem = linkElement.parentElement;

        return Array.from(listItem.nextElementSibling.querySelectorAll('.l-link')).some((element: HTMLElement) => {
            return element.classList.contains('active');
        });
    }
}
