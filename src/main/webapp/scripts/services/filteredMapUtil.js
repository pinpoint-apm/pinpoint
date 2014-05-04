'use strict';

pinpointApp.factory('filteredMapUtil', [ 'filterConfig', 'encodeURIComponentFilter', 'ServerMapFilterVo',
    function (cfg, encodeURIComponentFilter, ServerMapFilterVo) {
        // define private variables
        var self;

        return {

            /**
             * merge filters
             * @param oNavbarVo
             * @param oServerMapFilterVo
             * @returns {Array}
             */
            mergeFilters: function (oNavbarVo, oServerMapFilterVo) {
                var newFilter = [];
                if (oNavbarVo.getFilter()) {
                    var prevFilter = JSON.parse(oNavbarVo.getFilter());
                    if (angular.isArray(prevFilter)) {
                        newFilter = prevFilter;

                        var result = this.findFilterInNavbarVo(
                            oServerMapFilterVo.getFromApplication(),
                            oServerMapFilterVo.getFromServiceType(),
                            oServerMapFilterVo.getToApplication(),
                            oServerMapFilterVo.getToServiceType(),
                            oNavbarVo
                        );
                        if (result) {
                            newFilter[result.index] = oServerMapFilterVo.toJson();
                            return newFilter;
                        }
                    }
                }
                newFilter.push(oServerMapFilterVo.toJson());
                return newFilter;
            },

            /**
             * merge hints
             * @param oNavbarVo
             * @param oServerMapHintVo
             * @returns {{}}
             */
            mergeHints: function (oNavbarVo, oServerMapHintVo) {
                var newHint = {},
                    prevHint = this.parseShortHintToLongHint(JSON.parse(oNavbarVo.getHint())),
                    nowHint = oServerMapHintVo.getHint();
                if (prevHint) {
                    if (nowHint) {
                        var nowHintKey = _.keys(nowHint)[0],
                            nowHintValue = nowHint[nowHintKey];
                        newHint = angular.copy(prevHint);

                        if (angular.isDefined(newHint[nowHintKey])) {
                            newHint[nowHintKey] = _.union(newHint[nowHintKey], nowHintValue);
                            newHint[nowHintKey] = this.uniqueHintValue(newHint[nowHintKey]);
                        } else {
                            newHint[nowHintKey] = nowHintValue;
                        }
                    } else {
                        newHint = prevHint;
                    }
                } else {
                    newHint = oServerMapHintVo.getHint();
                }
                return newHint;
            },

            /**
             * unique hint value
             * @param hintValue
             * @returns {array}
             */
            uniqueHintValue: function (hintValue) {
                for (var i=0; i<hintValue.length; ++i) {
                    for (var j=i+1; j<hintValue.length; ++j) {
                        if (hintValue[i]['rpc'] === hintValue[j]['rpc'] &&
                            hintValue[i]['rpcServiceTypeCode'] === hintValue[j]['rpcServiceTypeCode']) {
                            hintValue.splice(j--, 1);
                        }
                    }
                }
                return hintValue;
            },

            /**
             * parse short hint to long hint
             * @param shortHint
             * @returns {*}
             */
            parseShortHintToLongHint: function (shortHint) {
                var newHint = angular.copy(shortHint);
                angular.forEach(newHint, function (val, key) {
                    var hintData = [];
                    for(var i = 0; i<val.length; i+=2) {
                        hintData.push({
                            rpc: val[i],
                            rpcServiceTypeCode: val[i+1]
                        });
                    }
                    newHint[key] = hintData;
                });
                return newHint;
            },

            /**
             * parse long hint to short hint
             * @param longHint
             * @returns {*}
             */
            parseLongHintToShortHint: function (longHint) {
                var newHint = angular.copy(longHint);
                angular.forEach(newHint, function (val, key) {
                    var hintData = [];
                    for(var k in val) {
                        hintData.push(val[k]['rpc']);
                        hintData.push(val[k]['rpcServiceTypeCode']);
                    }
                    newHint[key] = hintData;
                });
                return newHint;
            },

            /**
             * get start value for filter by label
             * @param label
             * @param values
             * @returns {number}
             */
            getStartValueForFilterByLabel: function (label, values) {
                var labelKey = (function () {
                        for (var key in values) {
                            if (values[key].label === label) {
                                return key;
                            }
                        }
                        return false;
                    })(),
                    startValue = 0;

                if (labelKey > 0) {
                    startValue = parseInt(values[labelKey - 1].label, 10);
                }
                return startValue;
            },

            /**
             * get filtered map url with filter vo
             * @param oNavbarVo
             * @param oServerMapFilterVo
             * @param oServerMapHintVo
             * @returns {string}
             */
            getFilteredMapUrlWithFilterVo: function (oNavbarVo, oServerMapFilterVo, oServerMapHintVo) {
                var newFilter = this.mergeFilters(oNavbarVo, oServerMapFilterVo),
                    mainApplication = oServerMapFilterVo.getMainApplication() + '@' + oServerMapFilterVo.getMainServiceTypeCode(),
                    url = '#/filteredMap/' + mainApplication + '/' + oNavbarVo.getReadablePeriod() + '/' +
                        oNavbarVo.getQueryEndDateTime() + '/' + encodeURIComponentFilter(JSON.stringify(newFilter));
                if (oNavbarVo.getHint() || oServerMapHintVo.getHint()) {
                    var newLongHint = this.mergeHints(oNavbarVo, oServerMapHintVo),
                        newShortHint = this.parseLongHintToShortHint(newLongHint);
                    url += '/' + encodeURIComponentFilter(JSON.stringify(newShortHint));
                }
                return url;
            },

            /**
             * find filter in navbar vo
             * @param fa
             * @param fst
             * @param ta
             * @param tst
             * @param oNavbarVo
             * @returns {boolean}
             */
            findFilterInNavbarVo: function (fa, fst, ta, tst, oNavbarVo) {
                var filters = JSON.parse(oNavbarVo.getFilter()),
                    result = false;
                if (fst === 'USER') {
                    fa = 'USER';
                }
                if (angular.isArray(filters)) {
                    angular.forEach(filters, function(filter, index) {
                        var oServerMapFilterVo = new ServerMapFilterVo(filter);
                        if (fa === oServerMapFilterVo.getFromApplication() &&
                            ta === oServerMapFilterVo.getToApplication() &&
                            fst === oServerMapFilterVo.getFromServiceType() &&
                            tst === oServerMapFilterVo.getToServiceType()) {
                            result = {
                                oServerMapFilterVo : oServerMapFilterVo,
                                index: index
                            };
                        }
                    });
                }
                return result;
            },

            /**
             * do filters have unknown node
             * @param filters
             * @returns {boolean}
             */
            doFiltersHaveUnknownNode: function (filters) {
                for (var k in filters) {
                    if (filters[k].tst === 'UNKNOWN') return true;
                }
                return false;
            }
        }
    }
]);
