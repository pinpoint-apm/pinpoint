import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EmptyContentsComponent } from './empty-contents.component';

describe('EmptyContentsComponent', () => {
    let component: EmptyContentsComponent;
    let fixture: ComponentFixture<EmptyContentsComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [EmptyContentsComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(EmptyContentsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
