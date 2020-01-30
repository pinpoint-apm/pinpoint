import { isEmpty } from 'app/core/utils/util';
import { Filter } from '../models/filter';

export class FilterParamMaker {
    static makeParam(currFilterStr: string, filter: Filter): {[key: string]: any}[] {
        const currFilter: Filter[] = currFilterStr ? Filter.instanceFromString(currFilterStr) : [];
        const resultFilter = isEmpty(currFilter) ? [filter] : [...currFilter.filter((f: Filter) => !f.equal(filter)), filter];

        return resultFilter.map((f: Filter) => f.toParamFormat());
    }
}
