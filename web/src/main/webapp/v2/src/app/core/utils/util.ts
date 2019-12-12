export function sliceObj<T extends object>(obj: T = {} as T, begin: number, end: number): T {
    return Object.keys(obj).slice(begin, end).reduce((acc: T, curr: string) => {
        return { ...(acc as object), [curr]: obj[curr as keyof T] } as T;
    }, {} as T);
}

export function filterObj<T extends object>(predi: Function, obj: T): T {
    return Object.keys(obj).reduce((acc: T, curr: string) => {
        return predi(curr) ? { ...(acc as object), [curr]: obj[curr as keyof T] } as T : acc;
    }, {} as T);
}

export function isThatType<T extends object>(obj: T | any, ...props: string[]): obj is T {
    return props.every((prop: string) => {
        return obj.hasOwnProperty(prop);
    });
}

export function isEmpty(obj: Object | Array<any>): boolean {
    return Array.isArray(obj) ? obj.length === 0 : Object.keys(obj).length === 0;
}
