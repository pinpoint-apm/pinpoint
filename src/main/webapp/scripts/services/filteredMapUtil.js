'use strict';

pinpointApp.factory('filteredMapUtil', [ 'filterConfig', 'encodeURIComponentFilter', 'ServerMapFilterVo',
    function (cfg, encodeURIComponentFilter, ServerMapFilterVo) {
        // define private variables
        var self;

        return {

            /**
             * merge filters
             * @param oServerMapFilterVo
             * @param oNavbarVo
             * @returns {Array}
             */
            mergeFilters: function (oServerMapFilterVo, oNavbarVo) {
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
             * @param oServerMapFilterVo
             * @param oNavbarVo
             * @returns {string}
             */
            getFilteredMapUrlWithFilterVo: function (oServerMapFilterVo, oNavbarVo) {
                var newFilter = this.mergeFilters(oServerMapFilterVo, oNavbarVo),
                    url = '#/filteredMap/' + oNavbarVo.getApplication() + '/' + oNavbarVo.getPeriod() + '/' +
                        oNavbarVo.getQueryEndTime() + '/' + encodeURIComponentFilter(JSON.stringify(newFilter));
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
            }
        }
    }
]);
