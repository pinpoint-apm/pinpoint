import { Component, OnInit, Input, Output, EventEmitter, 
    ViewChildren, QueryList, ElementRef, AfterViewInit, AfterViewChecked } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';

import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { filter } from 'rxjs/operators';
import { ISNBItem } from './side-navigation-bar-container.component';

@Component({
    selector: 'pp-side-navigation-item',
    templateUrl: './side-navigation-item.component.html',
    styleUrls: ['./side-navigation-item.component.css']
})
export class SideNavigationItemComponent implements OnInit, AfterViewInit, AfterViewChecked {
    @Input() minimize: boolean;
    @Input() items: ISNBItem[];
    @Output() outClickItem = new EventEmitter<ISNBItem>();
    @ViewChildren('linkItem') linkItem: QueryList<ElementRef<HTMLDivElement>>;
    @ViewChildren('childLinkWrapper') childLinkWrapper: QueryList<ElementRef<HTMLDivElement>>;
    basePath = this.router.url;

    constructor(
        private router: Router,
        private analyticsService: AnalyticsService,
    ) {}
    
    ngOnInit() {
        this.router.events.pipe(
            filter(event => event instanceof NavigationEnd)
        ).subscribe((event: NavigationEnd) => {
            this.basePath = this.router.url;
        })
    }

    ngAfterViewInit() {
        this.positionLinkLayer()
    }

    ngAfterViewChecked() {
        this.positionLinkLayer()
    }

    isActive(item: ISNBItem) {
        if (item.childItems) {
            return item.childItems.some(child => child.path && this.basePath.startsWith(child.path))
        } else if (item.path) {
            return this.basePath.startsWith(item.path);
        }
        return false;
    }

    positionLinkLayer() {
        if (this.childLinkWrapper && this.childLinkWrapper.length > 0) {
            this.childLinkWrapper.forEach((el, i) => {
                const linkItemRect = this.linkItem.toArray()[i].nativeElement.getBoundingClientRect();
                const childLinkRect = el.nativeElement.getBoundingClientRect();
                const isYOverFlow = linkItemRect.top + childLinkRect.height > window.innerHeight - 10;
              
                if (isYOverFlow) {
                    el.nativeElement.style.top = `${-childLinkRect.height + linkItemRect.height * (i+1)}px`;
                } else {
                    el.nativeElement.style.top = `${linkItemRect.height * i}px`;
                }
            })
        }
    }
    
    onClickItem(event: MouseEvent, item: ISNBItem): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_SIDE_NAVIGATION_BAR, item.id);
        if (item.onClick) {
            item.onClick(item);
        }
    }

    showItem({showItem}: ISNBItem): boolean {
        return showItem === undefined || showItem === true;
    }
}
