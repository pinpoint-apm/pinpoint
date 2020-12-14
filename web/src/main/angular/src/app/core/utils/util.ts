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

export function isEmpty(obj: Object | any[]): boolean {
    return Array.isArray(obj) ? obj.length === 0 : Object.keys(obj).length === 0;
}

/**
 *  1. Assume it consists of primitive values
 *  2. Order doesn't matter
 * */
export function isSameArray(arr1: any[], arr2: any[]): boolean {
    if (arr1.length !== arr2.length) {
        return false;
    }

    return arr1.reduce((_, item: any, i: number, arr: any[]) => {
        return arr2.includes(item) ? true : (arr.splice(i + 1), false);
    }, true);
}


export function sumObjByKey(...objs: {[key: string]: any}[]): {[key: string]: any} {
    return objs.reduce((acc: {[key: string]: any}, curr: {[key: string]: any}) => {
        for (const key in curr) {
            if (curr.hasOwnProperty(key)) {
                acc[key] = (acc[key] || 0) + curr[key];
            }
        }

        return acc;
      }, {});
}
