import { Injectable } from '@angular/core';
import { Subject, Observable, of, throwError } from 'rxjs';
import { WebSocketSubject, WebSocketSubjectConfig } from 'rxjs/webSocket';
import { timeout, catchError, map, filter } from 'rxjs/operators';

import { WindowRefService } from 'app/shared/services';

interface IWebSocketData {
    type: ResponseType;
    command: string;
    result: IWebSocketDataResult;
}

export interface IWebSocketDataResult {
    timeStamp: number;
    applicationName: string;
    activeThreadCounts: {[key: string]: IActiveThreadCounts};
}

export interface IActiveThreadCounts {
    code: ResponseCode;
    message: string;
    status?: number[];
}

export interface IWebSocketResponse {
    type: string;
    message: string | IWebSocketDataResult;
}

export const enum ResponseType {
    PING = 'PING',
    RESPONSE = 'RESPONSE'
}

export const enum ResponseCode {
    SUCCESS = 0,
    TIMEOUT = 211,
    ERROR_BLACK = 111,
    OVER_DELAY = 9999
}

@Injectable()
export class RealTimeWebSocketService {
    private url = 'agent/activeThread.pinpointws';
    private timeoutLimit = 5; // 서버로부터의 timeout response를 무시하는 최대횟수
    private timeoutCount: { [key: string]: number } = {}; // 각 agent별 timeout된 횟수
    private prevData: { [key: string]: IActiveThreadCounts } = {}; // Success일때의 데이터({ code, message, status })를 킵
    private delayLimit = 7000; // 서버로부터의 응답을 기다리는 최대시간(ms)
    private retryTimeout = 7000;
    private retryCount = 0;
    private maxRetryCount = 1;
    private connectTime: number;
    private isOpen = false;
    private socket$: WebSocketSubject<any> = null;
    private outMessage: Subject<IWebSocketResponse> = new Subject();

    onMessage$: Observable<IWebSocketResponse>;

    constructor(
        private windowRefService: WindowRefService
    ) {
        this.onMessage$ = this.outMessage.asObservable();
    }
    connect(): void {
        if (this.isOpen === false) {
            this.openWebSocket();
        }
    }
    isOpened(): boolean {
        return this.isOpen;
    }
    close(): void {
        if (this.isOpen) {
            this.socket$.complete();
        } else {
            this.outMessage.next({
                type: 'close',
                message: ''
            });
        }
    }
    send(message: object): void {
        if (this.isOpen) {
            this.socket$.next(message);
        }
    }
    private openWebSocket(): void {
        const location = this.windowRefService.nativeWindow.location;
        const protocol = location.protocol.indexOf('https') === -1 ? 'ws' : 'wss';
        const url = `${protocol}://${location.host}/${this.url}`;

        this.socket$ = new WebSocketSubject<any>({
            url: url,
            openObserver: {
                next: () => {
                    this.isOpen = true;
                    this.connectTime = Date.now();
                    this.outMessage.next({
                        type: 'open',
                        message: event.toString()
                    });
                }
            },
            closeObserver: {
                next: () => {
                    this.onCloseEvents();
                }
            }
        } as WebSocketSubjectConfig<any>);

        this.socket$.pipe(
            filter((message: IWebSocketData) => {
                return message.type === ResponseType.PING ? (this.send({ type: 'PONG' }), false) : true;
            }),
            map(({result}: {result: IWebSocketDataResult}) => this.parseResult(result)),
            // map(({timeStamp, applicationName}) => {
            //     const activeThreadCounts = {};
            //     for (let i = 0; i < 60; i++) {
            //         activeThreadCounts[i] = {
            //             code: 0,
            //             message: 'OK',
            //             status: [
            //                 Math.floor(2 * Math.random()),
            //                 Math.floor(3 * Math.random()),
            //                 Math.floor(1 * Math.random()),
            //                 Math.floor(4 * Math.random())
            //             ]
            //         };
            //     }
            //     return {
            //         timeStamp,
            //         applicationName,
            //         activeThreadCounts
            //     };
            // }),
            timeout(this.delayLimit),
            catchError((err: any) => err.name === 'TimeoutError' ? this.onTimeout() : throwError(err)),
            filter((message: IWebSocketDataResult | null) => {
                return !!message;
            }),
        ).subscribe((message: IWebSocketDataResult) => {
            this.outMessage.next({
                type: 'message',
                message
            });
        }, (err: any) => {
            console.log(err);
            this.closed();
        }, () => {
            console.log('Complete');
            this.closed();
        });
    }

    private parseResult(result: IWebSocketDataResult): IWebSocketDataResult {
        const {timeStamp, applicationName, activeThreadCounts} = result;
        const parsedATC = Object.keys(activeThreadCounts).reduce((prev: {[key: string]: IActiveThreadCounts}, curr: string) => {
            let agentData = activeThreadCounts[curr];
            const responseCode = agentData.code;

            switch (responseCode) {
                case ResponseCode.SUCCESS:
                    this.timeoutCount[curr] = 0;
                    this.prevData[curr] = agentData;
                    break;
                case ResponseCode.TIMEOUT:
                    this.timeoutCount[curr] = this.timeoutCount[curr] ? this.timeoutCount[curr] + 1 : 1;
                    agentData = this.prevData[curr] && this.timeoutCount[curr] < this.timeoutLimit ? this.prevData[curr] : agentData;
                    break;
                default:
                    break;
            }

            return {
                ...prev,
                [curr]: agentData
            };
        }, {} as {[key: string]: IActiveThreadCounts});

        return {timeStamp, applicationName, activeThreadCounts: parsedATC};
    }

    // TODO: No Response 메시지 띄워주기
    private onTimeout(): Observable<null> {
        this.close();

        return of(null);
    }

    private closed(): void {
        this.isOpen = false;
        this.socket$ = null;
        this.outMessage.next({
            type: 'close',
            message: ''
        });
    }

    private onCloseEvents(): void {
        if (Date.now() - this.connectTime < this.retryTimeout) {
            if (this.retryCount < this.maxRetryCount) {
                this.retryCount++;
                this.outMessage.next({
                    type: 'retry',
                    message: ''
                });
            } else {
                this.outMessage.next({
                    type: 'close',
                    message: ''
                });
            }
        }
    }
}
