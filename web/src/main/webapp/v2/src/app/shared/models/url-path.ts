export class UrlPath {
    static ADMIN = 'admin';
    static FILTERED_MAP = 'filteredMap';
    static INSPECTOR = 'inspector';
    static MAIN = 'main';
    static REAL_TIME = 'realtime';
    static SCATTER_FULL_SCREEN_MODE = 'scatterFullScreenMode';
    static THREAD_DUMP = 'threadDump';
    static TRANSACTION_DETAIL = 'transactionDetail';
    static TRANSACTION_LIST = 'transactionList';
    static TRANSACTION_VIEW = 'transactionView';
    static BROWSER_NOT_SUPPORT = 'browserNotSupported';
    static ERROR = 'error';
    static CONFIG = 'config';

    constructor() {}
    static getParamList(): string[] {
        return [
            UrlPath.CONFIG,
            UrlPath.ADMIN,
            UrlPath.ERROR,
            UrlPath.FILTERED_MAP,
            UrlPath.INSPECTOR,
            UrlPath.MAIN,
            UrlPath.REAL_TIME,
            UrlPath.SCATTER_FULL_SCREEN_MODE,
            UrlPath.THREAD_DUMP,
            UrlPath.TRANSACTION_DETAIL,
            UrlPath.TRANSACTION_LIST,
            UrlPath.TRANSACTION_VIEW
        ];
    }
}

export default UrlPath;
