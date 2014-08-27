'use strict';

pinpointApp.factory('HelixChartVo', [ function () {
    return function () {
        var self = this;

        this._sType = 'stacked_line';
        this._elTarget = null;
        this._sGroup = '';
        this._htData = { time : [], value : []}; // htData = { time : [], value : [ { key : '', data : [] }, { key : '', data : [] }] };
        this._htInputData = { time : [], value : []}; // htData = { time : [], value : [ { key : '', data : [] }, { key : '', data : [] }] };
        this._nWidth = 0;
        this._nHeight = 0;
        this._aPadding = [0, 0, 0, 0];
        this._aMargin = [0, 0, 0, 0];
        this._nXCount = 0;
        this._nXInterval = 0;
        this._sXTick = 'minutes';
        this._nXTickInterval = 0;
        this._sXTickFormat = '%H:%M';
        this._nYTicks = 0;
        this._fYLabel = function (v) {
            if (v >= 1000000000 && v % 1000000000 === 0) {
                return v / 1000000000 + "g"
            }
            if (v >= 1000000 && v % 1000000 === 0) {
                return v / 1000000 + "m"
            }
            if (v >= 1000 && v % 1000 === 0) {
                return v / 1000 + "k"
            }
            return v;
        };
        this._sTitle = ' ';
        this._sLegend = '';
        this._sQueryValue = '';
        this._sQueryInterval = '5s';
        this._nQueryFrom = 0;
        this._nQueryTo = 0;


        this.setType = function (type) {
            if (angular.isString(type)) {
                switch (type) {
                    case 'stacked_line' :
                    case 'avg_line' :
                    case 'filled_multi_line' :
                    case 'line' :
                    case 'multi_line' :
                    case 'ratio_line' :
                    case 'reversed_line' :
                    case 'stacked_line' :
                    case 'sum_line' :
                        self._sType = type;
                        break;
                    default :
                        throw type + ' is not supported, in helixChartVo.';
                        break;
                }
            } else {
                throw 'Chart format should be string type in helixChartVo.';
            }
            return self;
        };
        this.getType = function () {
            return self._sType;
        };

        this.setTarget = function (target) {
            if (angular.isElement(target)) {
                self._elTarget = target;
            } else {
                throw 'Target is not element in helixChartVo.';
            }
            return self;
        };
        this.getTarget = function () {
            return self._elTarget;
        };

        this.setGroup = function (group) {
            if (angular.isString(group)) {
                this._sGroup = group;
            } else {
                throw 'Group format should be string type in helixChartVo.';
            }
            return self;
        };
        this.getGroup = function () {
            return self._sGroup;
        };

        this.setData = function (data) {
            if (angular.isObject(data)
                && angular.isDefined(data.time)
                && angular.isArray(data.time)
                && angular.isDefined(data.value)
                && angular.isArray(data.value)) {
                self._htInputData = angular.copy(data);
                self._setDataValues();
            } else {
                throw 'Chart data should be like this, {time: [], value : [{key:"", data: []}, {key:"", data: []}, ...]}';
            }
            return self;
        };
        this._setDataValues = function () {
            angular.forEach(self._htInputData.time, function (val, key) {
                var closeIndex = self.getCloseIndexInData(val, key);
                if (closeIndex > -1) {
                    self._copyDataValuesFromDataByIndex(closeIndex, key);
                }
            });
            return self;
        };
        this._copyDataValuesFromDataByIndex = function (generatedTimeIndex, inputTimeIndex) {
            angular.forEach(self._htInputData.value, function (val, key) {
//                console.log('val : ', val, ', key : ', key);
//                console.log('geneerateTimeIndex : ', generatedTimeIndex, ', inputTimeIndex : ', inputTimeIndex, ' val : ', val.data[inputTimeIndex]);
                self._htData.value[key].data[generatedTimeIndex] = val.data[inputTimeIndex];
            });
        };
        this.getData = function () {
            return self._htData;
        };
        this.getCloseIndexInData = function (time, from) {
            var index = -1;
            var interval = self._nXInterval;
            for (var i = from, len = self._htData.time.length; i < len; i++) {
                if (time <= self._htData.time[i] && time - self._htData.time[i] <= interval) {
                    index = i;
                    break;
                }
            }
            return index;
        }  ;
        this.parseDataTimestampToDateInstance = function () {
            if (angular.isObject(self._htData)
                && angular.isDefined(self._htData.time)
                && angular.isArray(self._htData.time)) {
                angular.forEach(self._htData.time, function (val, key) {
                    self._htData.time[key] = new Date(val);
                });
            } else {
                throw 'Chart data\'s time should be defined and array';
            }
            return self;
        };
        this.generateDataTime = function () {
            var count = self._nXCount,
                interval = self._nXInterval,
                a = [],
                t = self._nQueryTo,
                gap;

            if (interval < 86400000) {
                t = t - (t % interval);
                for (var i = count - 1, j = 0; i > -1; i--, j++) {
                    a[i] = new Date(t - j * interval);
                }
            } else if (interval < 604800000) {
                gap = new Date(0).setHours(0); //9시간
                for (var i = count - 1, j = 0; i > -1; i--, j++) {
                    a[i] = new Date(t - (t % interval) - j * interval + gap);
                }
            } else {
                gap = new Date(0).setHours(0); //9시간
                var gap_date = 4 * 86400000; //new Date(0)은 목요일
                for (var i = count - 1, j = 0; i > -1; i--, j++) {
                    a[i] = new Date(t - (t % interval) - j * interval + gap - gap_date);
                }
            }
            self._htData.time = a;
            return self;
        };
        this.generateDataValue = function () {
            var keys = self._sQueryValue.split(',');
            self._htData.value = [];
            angular.forEach(keys, function (k) {
                var val = {
                    key : k,
                    data : []
                };
                for (var i = 0, len = self._htData.time.length; i < len; i++) {
                    val.data.push(0);
//                    val.data.push(_.random(0, 10));
                }
                self._htData.value.push(val);
            });
            return self;
        };

        this.setWidth = function (width) {
            if (angular.isNumber(width) && width > 0) {
                self._nWidth = width;
            } else {
                throw 'Chart width should be number and bigger than 0 in helixChartVo';
            }
            return self;
        };
        this.getWidth = function () {
            return self._nWidth;
        };

        this.setHeight = function (height) {
            if (angular.isNumber(height) && height > 0) {
                self._nHeight = height;
            } else {
                throw 'Chart height should be number and bigger than 0 in helixChartVo';
            }
            return self;
        };
        this.getHeight = function () {
            return self._nHeight;
        };

        this.setPadding = function (padding) {
            if (angular.isArray(padding) && padding.length === 4) {
                self._aPadding = padding;
            } else {
                throw 'Chart padding should be array and the length is 4 like this [top, right, bottom, left] as number in helixChartVo';
            }
            return self;
        };
        this.getPadding = function () {
            return self._aPadding;
        };

        this.setMargin = function (margin) {
            if (angular.isArray(margin) && margin.length === 4) {
                self._aMargin = margin;
            } else {
                throw 'Chart margin should be array and the length is 4 like this [top, right, bottom, left] as number in helixChartVo';
            }
            return self;
        };
        this.getMargin = function () {
            return self._aMargin;
        };

        this.setXCount = function (count) {
            if (angular.isNumber(count) && count > 0) {
                self._nXCount = count;
            } else {
                throw 'Chart X axis\' count should be number and bigger than 0 in helixChartVo.';
            }
            return self;
        };
        this.getXCount = function () {
            return self._nXCount;
        };

        this.setXInterval = function (interval) {
            if (angular.isNumber(interval) && interval > 0) {
                self._nXInterval = interval;
            } else {
                throw 'Chart X axis\' interval should be number and bigger than 0 in helixChartVo.';
            }
            return self;
        };
        this.getXInterval = function () {
            return self._nXInterval;
        };

        this.setXTick = function (tick) {
            if (angular.isString(tick)) {
                self._sXTick = tick;
            } else {
                throw 'Chart X axis\' tick should be string in helixChartVo.';
            }
            return self;
        };
        this.getXTick = function () {
            return self._sXTick;
        };

        this.setXTickInterval = function (tickInterval) {
            if (angular.isNumber(tickInterval) && tickInterval > 0) {
                self._nXTickInterval = tickInterval;
            } else {
                throw 'Chart X axis\' tick interval should be number and bigger than 0 in helixChartVo.';
            }
            return self;
        };
        this.getXTickInterval = function () {
            return self._nXTickInterval;
        };

        this.setXTickFormat = function (tickFormat) {
            if (angular.isString(tickFormat)) {
                self._sXTickFormat = tickFormat;
            } else {
                throw 'Chart X axis\' tickFormat should be string in helixChartVo.';
            }
            return self;
        };
        this.getXTickFormat = function () {
            return self._sXTickFormat;
        };

        this.setYTicks = function (ticks) {
            if (angular.isNumber(ticks) && ticks > 0) {
                self._nYTicks = ticks;
            } else {
                throw 'Chart Y axis\' ticks should be number and bigger than 0 in helixChartVo.';
            }
            return self;
        };
        this.getYTicks = function () {
            return self._nYTicks;
        };

        this.setYLabel = function (label) {
            if (angular.isFunction(label)) {
                self._fYLabel = label;
            } else {
                throw 'Chart Y label should be function in helixChartVo';
            }
            return self;
        };
        this.getYLabel = function () {
            return self._fYLabel;
        };

        this.setTitle = function (title) {
            if (angular.isString(title)) {
                self._sTitle = title;
            } else {
                throw 'Chart title should be string in helixChartVo';
            }
            return self;
        };
        this.getTitle = function () {
            return self._sTitle;
        };

        this.setLegend = function (legend) {
            if (angular.isString(legend)) {
                self._sLegend = legend;
            } else {
                throw 'Chart legend should be string in helixChartVo';
            }
            return self;
        };
        this.getLegend = function () {
            return self._sLegend;
        };

        this.setQueryValue = function (queryValue) {
            if (angular.isString(queryValue)) {
                self._sQueryValue = queryValue;
            } else {
                throw 'Chart query value should be string and keys of data like this, "key1,key2,key3,key4" in helixChartVo';
            }
            return self;
        };
        this.getQueryValue = function () {
            return self._sQueryValue;
        };

        this.setQueryInterval = function (queryInterval) {
            if (angular.isString(queryInterval)) {
                self._sQueryInterval = queryInterval;
            } else {
                throw 'Chart query interval should be string like this "5s", "1m" in helixChartVo';
            }
            return self;
        };
        this.getQueryInterval = function () {
            return self._sQueryInterval;
        };

        this.setQueryFrom = function (from) {
            if (angular.isNumber(from) && from > 0) {
                this._nQueryFrom = from;
            } else {
                throw 'Chart query from should be number and bigger than 0 in helixChartVo';
            }
            return self;
        };
        this.getQueryFrom = function () {
            return self._nQueryFrom;
        };

        this.setQueryTo = function (to) {
            if (angular.isNumber(to) && to > 0) {
                this._nQueryTo = to;
            } else {
                throw 'Chart query to should be number and bigger than 0 in helixChartVo';
            }
            return self;
        };
        this.getQueryTo = function () {
            return self._nQueryTo;
        };

        this.generateEverythingForChart = function () {
            var gap = (self._nQueryTo - self._nQueryFrom) / 1000;
            self
                .autoAdjustXCountByGap(gap)
                .autoAdjustXIntervalByGap(gap)
                .autoAdjustXTickByGap(gap)
                .autoAdjustTickIntervalByGap(gap)
                .autoAdjustXTickFormatByGap(gap)
                .autoAdjustQueryInterval(gap)
                .generateDataTime()
                .generateDataValue();

//            QueryInterval, XCount, XInterval, XTick, XTickInterval, XTickFormat
//            "5s,60,5,minutes,2,%H:%M"         300
//            "30s,60,30,minutes,10,%H:%M"      1800
//            "1m,60,60,minutes,15,%H:%M"       3600
//            "1m,180,60,minutes,60,%H:%M"      10800
//            "5m,72,300,hours,2,%H:%M"         21600
//            "5m,144,300,hours,3,%H:%M"        43200
//            "10m,144,600,hours,6,%H:%M"       86400
//            "1d,7,86400,days,2,%m/%d"         604800
//            "1d,30,86400,weeks,1,%m/%d"       2592000
//            "1d,90,86400,months,1,%m/%d"      7776000
//            "1d,180,86400,months,1,%b"        15552000
//            "1w,52,604800,months,3,%b"        31449600
            return self;
        };
        this.autoAdjustXCountByGap = function (gap) {
            var xCount = 0;
            if (gap <= 3600) { // 1시간
                xCount = 60;
            } else if (gap <= 10800) { // 3시간
                xCount = 180;
            } else if (gap <= 21600) {
                xCount = 72;
            } else if (gap <= 86400) {
                xCount = 144;
            } else if (gap <= 604800) {
                xCount = 7;
            } else if (gap <= 2592000) {
                xCount = 30;
            } else if (gap <= 7776000) {
                xCount = 90;
            } else if (gap <= 15552000) {
                xCount = 180;
            } else if (gap <= 31449600) {
                xCount = 52;
            }
            self._nXCount = xCount;
            return self;
        };
        this.autoAdjustXIntervalByGap = function (gap) {
            self._nXInterval = Math.round(gap / self._nXCount) * 1000;
            return self;
        };
        this.autoAdjustXTickByGap = function (gap) {
            var xTick = 'minutes';
            if (gap <= 10800) {
                xTick = 'minutes';
            } else if (gap <= 86400) {
                xTick = 'hours';
            } else if (gap <= 604800) {
                xTick = 'days';
            } else if (gap <= 2592000) {
                xTick = 'weeks';
            } else {
                xTick = 'months';
            }
            self._sXTick = xTick;
            return self;
        };
        this.autoAdjustTickIntervalByGap = function (gap) {
            var xTickInterval = 0;
            if (gap <= 300) {
                xTickInterval = 2;
            } else if (gap <= 10800) {
                xTickInterval = Math.floor(gap / 180);
            } else if (gap <= 21600) {
                xTickInterval = 3;
            } else if (gap <= 86400) {
                xTickInterval = Math.floor(gap / 14400);
            } else if (gap <= 604800) {
                xTickInterval = 1;
            } else if (gap <= 15552000) {
                xTickInterval = 2;
            } else if (gap <= 31449600) {
                xTickInterval = Math.floor(gap / 10483200);
            }
            self._nXTickInterval = xTickInterval;
            return self;
        };
        this.autoAdjustXTickFormatByGap = function (gap) {
            var xTickFormat = '';
            if (gap <= 86400) {
                xTickFormat = '%H:%M';
            } else if (gap <= 7776000) {
                xTickFormat = '%m/%d';
            } else {
                xTickFormat = '%b';
            }
            self._sXTickFormat = xTickFormat;
            return self;
        };
        this.autoAdjustQueryInterval = function (gap) {
            var queryInterval = '5s';
            if (gap < 1800) {
                queryInterval = '5s';
            } else if (gap < 3600) {
                queryInterval = '30s';
            } else if (gap < 21600) {
                queryInterval = '1m';
            } else if (gap < 86400) {
                queryInterval = '5m';
            } else if (gap < 604800) {
                queryInterval = '10m';
            } else if (gap < 31449600) {
                queryInterval = '1d';
            } else {
                queryInterval = '1w';
            }
            self._sQueryInterval = queryInterval;
            return self;
        };


        this.toJSON = function () {
            return {
                type : self.getType(),
                target: self.getTarget(),
                group: self.getGroup(),
                data: self.getData(),
                chart: {
                    width: self.getWidth(),
                    height: self.getHeight(),
                    padding: self.getPadding(),
                    margin: self.getMargin(),
                    x: {
                        count: self.getXCount(),
                        interval: self.getXInterval(),
                        tick: self.getXTick(),
                        tick_interval: self.getXTickInterval(),
                        tick_format: self.getXTickFormat()
                    },
                    y: {
                        ticks: self.getYTicks(),
                        label: self.getYLabel()
                    },
                    desc: {
                        title: self.getTitle(),
                        legend: self.getLegend()
                    }
                },
                query: {
                    value: self.getQueryValue(),
                    interval: self.getQueryInterval(),
                    until: self.getQueryTo()
                }
            };
        };
    };
}
]);
