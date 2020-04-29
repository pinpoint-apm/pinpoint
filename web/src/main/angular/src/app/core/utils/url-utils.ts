export function parseURL(url: string): {path: string[], queryParam: {[key: string]: any}} {
    const urlSegments = url.split('?');
    const path = [urlSegments[0]];
    const queryParam = urlSegments[1] ? urlSegments[1].split('&').reduce((acc: {[key: string]: any}, curr: string) => {
        const [key, value] = curr.split('=');

        return {...acc, [key]: value};
    }, {}) : null;

    return {path, queryParam};
}
