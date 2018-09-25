import { Observable } from 'rxjs';

export interface IChartDataFromServer {
    charts: {
        schema: {
            [key: string]: string[] | string;
        },
        x: number[];
        y: {
            [key: string]: number[][];
        }
    };
}

export interface IChartDataService {
    getData(range: number[]): Observable<IChartDataFromServer | IChartDataFromServer[] | AjaxException>;
}
