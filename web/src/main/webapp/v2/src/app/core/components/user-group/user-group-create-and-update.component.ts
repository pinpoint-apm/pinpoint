import { Component, OnInit, AfterViewChecked, Input, Output, EventEmitter } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
    selector: 'pp-user-group-create-and-update',
    templateUrl: './user-group-create-and-update.component.html',
    styleUrls: ['./user-group-create-and-update.component.css']
})
export class UserGroupCreateAndUpdateComponent implements OnInit, AfterViewChecked {
    @Input() showCreate = false;
    @Input() minLength: number;
    @Input() nameLabel: string;
    @Input() nameGuide: string;
    @Input() nameLengthGuide: string;
    @Output() outCreateUserGroup: EventEmitter<string> = new EventEmitter();
    @Output() outClose: EventEmitter<null> = new EventEmitter();
    newUserGroupModel = '';
    userGroupForm: FormGroup;
    title = 'User Group';
    constructor() {}
    ngOnInit() {
        this.userGroupForm = new FormGroup({
            'userGroupName': new FormControl(this.newUserGroupModel, [
                Validators.required,
                Validators.minLength(3)
            ])
        });
    }
    ngAfterViewChecked() {}
    onCreateOrUpdate(): void {
        this.outCreateUserGroup.emit(this.userGroupForm.get('userGroupName').value);
        this.onClose();
    }
    onClose(): void {
        this.outClose.emit();
        this.userGroupForm.reset();
    }
    get userGroupName() {
        return this.userGroupForm.get('userGroupName');
    }
}
