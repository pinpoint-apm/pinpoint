export class Application implements IApplication {
    constructor(
        public applicationName: string,
        public serviceType: string,
        public code: number,
        public key?: string
    ) {}

    equals(target: IApplication): boolean {
        if (target) {
            return this.applicationName === target.applicationName && this.serviceType === target.serviceType;
        } else {
            return false;
        }
    }

    getApplicationName(): string {
        return this.applicationName;
    }

    getServiceType(): string {
        return this.serviceType;
    }

    getCode(): number {
        return this.code;
    }

    getUrlStr(): string {
        return `${this.applicationName}@${this.serviceType}`;
    }

    getKeyStr(): string {
        return this.key ? this.key : `${this.applicationName}^${this.serviceType}`;
    }
}
