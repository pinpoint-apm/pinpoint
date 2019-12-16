import { Filter } from '../models/filter';

export class FilterParamMaker {
    static makeParam(currentFilter: string, filter: Filter): string {
        const aCurrentFilter: Filter[] = Filter.instanceFromString(currentFilter || '[]');
        if (aCurrentFilter.length === 0) {
            aCurrentFilter.push(filter);
        } else {
            let searchIndex = -1;
            for ( let i = 0 ; i < aCurrentFilter.length ; i++ ) {
                if ( aCurrentFilter[i].equal(filter) ) {
                    searchIndex = i;
                    aCurrentFilter[i] = filter; // replace previous param object
                    break;
                }
            }
            if (searchIndex === -1) {
                aCurrentFilter.push(filter);
            }
        }
        return '/' + encodeURIComponent('[' + aCurrentFilter.map(f => f.toString()).join(',') + ']');
    }
}
