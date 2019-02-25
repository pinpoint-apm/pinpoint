import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { filter, takeUntil, map } from 'rxjs/operators';

export interface IMessageParam {
    to: string;
    param: any[];
}

@Injectable()
export class MessageQueueService {
    private messageQueue: Subject<IMessageParam> = new Subject();
    constructor() {}
    sendMessage(message: IMessageParam): void {
        this.messageQueue.next(message);
    }
    receiveMessage(unsubscribe: Subject<null>, to: string): Observable<any[]> {
        return this.messageQueue.pipe(
            takeUntil(unsubscribe),
            filter((message: IMessageParam) => {
                return message.to === to;
            }),
            map((message: IMessageParam) => {
                return message.param;
            })
        );
    }
}

export enum MESSAGE_TO {
    
}
