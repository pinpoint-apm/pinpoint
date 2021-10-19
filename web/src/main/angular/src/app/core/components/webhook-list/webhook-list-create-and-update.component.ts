import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { filterObj } from 'app/core/utils/util';
import { IWebhook } from 'app/shared/services';
import { CustomFormValidatorService } from 'app/shared/services/custom-form-validator.service';

export interface IWebhookForm {
    alias?: string;
    url: string;
}

@Component({
    selector: 'pp-webhook-list-create-and-update',
    templateUrl: './webhook-list-create-and-update.component.html',
    styleUrls: ['./webhook-list-create-and-update.component.css']
})
export class WebhookListCreateAndUpdateComponent implements OnInit, OnChanges {
    @Input() checkerList: string[];
    @Input() userGroupList: string[];
    @Input() i18nText: {[key: string]: string};
    @Input() i18nFormGuide: {[key: string]: IFormFieldErrorType};
    @Input() editWebhook: IWebhook;
    @Output() outUpdateWebhook = new EventEmitter<IWebhookForm>();
    @Output() outCreateWebhook = new EventEmitter<IWebhookForm>();
    @Output() outClose = new EventEmitter<void>();
    @Output() outShowHelp = new EventEmitter<{[key: string]: ICoordinate}>();

    webhookForm = new FormGroup({
        'alias': new FormControl('', [
            Validators.max(100)
        ]),
        'url': new FormControl('', [
            Validators.required, 
            CustomFormValidatorService.validate(/((([A-Za-z]{3,9}:(?:\/\/)?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-]+|(?:www\.|[\-;:&=\+\$,\w]+@)[A-Za-z0-9\.\-]+)((?:\/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\.\!\/\\\w]*))?)/),
        ]),
    });

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        const editWebhook = changes['editWebhook'];

        if (editWebhook && editWebhook.currentValue) {
            const formattedObj = filterObj((key: string) => Object.keys(this.webhookForm.controls).includes(key), editWebhook.currentValue);
            this.webhookForm.reset(formattedObj);
        }
    }

    onCreateOrUpdate() {
        this.webhookForm.markAllAsTouched();

        if (this.webhookForm.invalid) {
            return;
        }
        const value = this.webhookForm.value;

        this.editWebhook ? this.outUpdateWebhook.emit(value) : this.outCreateWebhook.emit(value);
        this.onClose();
    }

    onClose() {
        this.outClose.emit();
    }

    get title(): string {
        return this.editWebhook ? 'Edit Webhook' : 'Add Webhook';
    }
}
