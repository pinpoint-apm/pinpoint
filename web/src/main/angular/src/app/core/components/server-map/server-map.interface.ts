export interface IHistogramType1 {
    '1s': number;
    '3s': number;
    '5s': number;
    'Slow': number;
    'Error': number;
}
export interface IHistogramType2 {
    '100ms': number;
    '300ms': number;
    '500ms': number;
    Error: number;
    Slow: number;
}
export interface IHistogramType3 {
    key: string;
    values: number[];
}
