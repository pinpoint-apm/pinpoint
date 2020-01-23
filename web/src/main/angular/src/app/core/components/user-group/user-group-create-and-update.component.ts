import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { CustomFormValidatorService } from 'app/shared/services/custom-form-validator.service';

@Component({
    selector: 'pp-user-group-create-and-update',
    templateUrl: './user-group-create-and-update.component.html',
    styleUrls: ['./user-group-create-and-update.component.css']
})
export class UserGroupCreateAndUpdateComponent implements OnInit {
    @Input() showCreate = false;
    @Input() i18nLabel: { [key: string]: string };
    @Input() i18nGuide: { [key: string]: IFormFieldErrorType };
    @Input() minLength: number;
    @Output() outCreateUserGroup = new EventEmitter<string>();
    @Output() outClose = new EventEmitter<void>();

    userGroupForm = new FormGroup({
        userGroupName: new FormControl('', [
            Validators.required,
            CustomFormValidatorService.validate(/^[\w\-]{4,30}$/)
        ])
    });

    constructor() {}
    ngOnInit() {}
    onCreateOrUpdate(): void {
        this.outCreateUserGroup.emit(this.userGroupForm.get('userGroupName').value);
        this.onClose();
    }

    onClose(): void {
        this.outClose.emit();
        this.userGroupForm.reset();
    }
}
