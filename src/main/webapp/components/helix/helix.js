var destroyObject = function (object) {
    var type = Object.prototype.toString.call(object[i]);
    for (var i in object) {
        // if (object.hasOwnProperty(i)) {
        if (type === '[object Object]' || type === '[object Array]') {
            destroyObject(object[i]);
        }
        object[i] = null;
        delete object[i];
        // }
    }
    if (type === '[object Array]') {
        object.length = 0;
    }
    object = null;
};

function emptyNode(node) {
    while (node.lastChild) {
        destroyNode(node.lastChild);
    }
}

function destroyNode(node){
    // WebReflection IE leak attemp!
    if (node.attributes) {
        Array.prototype.slice.call(node.attributes).forEach(function(v,i,o) {
            node.removeAttribute(v);
        });
    }
    // Hedger suggestion
    while (node.lastChild) {
        destroyNode(node.lastChild);
    }
    if (node.parentNode) {
        node.parentNode.removeChild(node);
    }
};

window.helix = (function(){
    //deepcopy
    var deepcopy = function (parent, child) {
        child = child || {};
        for (var i in parent) {
            if (parent.hasOwnProperty(i)) {
                if (typeof parent[i] === 'object') {
                    child[i] = (Object.prototype.toString.call(parent[i]) === '[object Array]') ? [] : {};
                    deepcopy(parent[i], child[i]);
                } else {
                    child[i] = parent[i];
                }
            }
        }
        return child;
    };

    var h = helix = {},
        CHART_TYPE = {},
        default_chart_option_x = {},
        default_chart_option_y = {},
        default_chart_option_desc = {},
        default_chart_option = {
            width : 700,
            height : 400,
            padding : [100, 100, 100, 100],
            margin : [50, 50, 50, 50]
        };
    //CHART_TYPE
    CHART_TYPE['5s'] = 's5';
    CHART_TYPE['30s'] = 's30';
    CHART_TYPE['1m'] = 'm1';
    CHART_TYPE['5m'] = 'm5';
    CHART_TYPE['10m'] = 'm10';
    CHART_TYPE['15m'] = 'm15';
    CHART_TYPE['3h'] = 'h3';
    CHART_TYPE['1d'] = 'd1';
    CHART_TYPE['1w'] = 'w1';

    //default_chart_option_y
    default_chart_option_y.s5 = {
        range : null,
        ticks : 7,
        label : function(v) { return v; }
    };
    default_chart_option_y.s30 = {
        range : null,
        ticks : 7,
        label : function(v) { return v; }
    };
    default_chart_option_y.m1 = {
        range : null,
        ticks : 7,
        label : function(v) { return v; }
    };
    default_chart_option_y.m5 = {
        range : null,
        ticks : 7,
        label : function(v) { return v; }
    };
    default_chart_option_y.m10 = {
        range : null,
        ticks : 7,
        label : function(v) { return v; }
    };
    default_chart_option_y.m15 = {
        range : null,
        ticks : 7,
        label : function(v) { return v; }
    };
    default_chart_option_y.h3 = {
        range : null,
        ticks : 7,
        label : function(v) { return v; }
    };
    default_chart_option_y.d1 = {
        range : null,
        ticks : 7,
        label : function(v) { return v; }
    };
    default_chart_option_y.w1 = {
        range : null,
        ticks : 7,
        label : function(v) { return v; }
    };

    //default_chart_option_x
    default_chart_option_x.s5 = {
        count : 60,
        interval : 5000,
        tick : "minutes",
        tick_interval : 2,
        tick_format : "%H:%M"
    };
    default_chart_option_x.s30 = {
        count : 60,
        interval : 30000,
        tick : "minutes",
        tick_interval : 5,
        tick_format : "%H:%M"
    };
    default_chart_option_x.m1 = {
        count : 60,
        interval : 60000,
        tick : "minutes",
        tick_interval : 5,
        tick_format : "%H:%M"
    };

    default_chart_option_x.m5 = {
        count : 72,
        interval : 300000,
        tick : "hours",
        tick_interval : 1,
        tick_format : "%H:%M"
    };

    default_chart_option_x.m10 = {
        count : 72,
        interval : 600000,
        tick : "hours",
        tick_interval : 1,
        tick_format : "%H:%M"
    };

    default_chart_option_x.m15 = {
        count : 96,
        interval : 900000,
        tick : "hours",
        tick_interval : 3,
        tick_format : "%H:%M"
    };

    default_chart_option_x.h3 = {
        count : 56,
        interval : 10800000,
        tick : "days",
        tick_interval : 1,
        tick_format : "%m/%d"
    };

    default_chart_option_x.d1 = {
        count : 31,
        interval : 86400000,
        tick : "weeks",
        tick_interval : 1,
        tick_format : "%b"
    };

    default_chart_option_x.w1 = {
        count : 52,
        interval : 604800000,
        tick : "weeks",
        tick_interval : 4,
        tick_format : "%b"
    };

    //default_chart_option_desc
    default_chart_option_desc.s5 = {
        sub_title : "5 minutes @ 5 seconds",
        period_format : "%m/%d %H:%M",
        tick_format : "%H:%M:%S"
    };
    default_chart_option_desc.s30 = {
        sub_title : "30 minutes @ 30 senconds",
        period_format : "%m/%d %H:%M",
        tick_format : "%H:%M:%S"
    };
    default_chart_option_desc.m1 = {
        sub_title : "1 hour @ 1 minute",
        period_format : "%m/%d %H:%M",
        tick_format : "%H:%M"
    };

    default_chart_option_desc.m5 = {
        sub_title : "6 hours @ 5 minutes",
        period_format : "%m/%d %H:%M",
        tick_format : "%H:%M"
    };

    default_chart_option_desc.m10 = {
        sub_title : "12 hours @ 10 minutes",
        period_format : "%m/%d %H:%M",
        tick_format : "%H:%M"
    };

    default_chart_option_desc.m15 = {
        sub_title : "1 day @ 15 minutes",
        period_format : "%m/%d %H:%M",
        tick_format : "%H:%M"
    };

    default_chart_option_desc.h3 = {
        sub_title : "1 week @ 3 hours",
        period_format : "%m/%d %H:%M",
        tick_format : "%m/%d"
    };

    default_chart_option_desc.d1 = {
        sub_title : "1 month @ 1 day",
        period_format : "%m/%d %H:%M",
        tick_format : "%m/%d"
    };

    default_chart_option_desc.w1 = {
        sub_title : "1 year @ 1 week",
        period_format : "%m/%d %H:%M",
        tick_format : "%m/%d"
    };
    /*
     var randomGenerator = function(count, min, max) {
     var a = [];
     for (var i = 0; i < count; i++) {
     a[i] = min + Math.round(Math.random() * (max - min));
     }
     return a;
     };
     */
    var Chart = function(option) {
        this.option = option;
        this.data = {};
        this.data.time = this._generateTime(this.option.query.until || Math.floor(+new Date() / 1000));
        this._setPeriod();
    };
    Chart.prototype.getKeys = function() {
        var keys = this.option.query.value.split(",");
        for (var i = 0, len = keys.length; i < len; i++) {
            keys[i] = keys[i].trim();
        }
        return keys;
    };
    Chart.prototype.getLegend = function() {
        var legend = (this.option.chart.desc.legend && this.option.chart.desc.legend.length > 0) ? this.option.chart.desc.legend : this.option.query.value,
            keys = legend.split(",");

        for (var i = 0, len = keys.length; i < len; i++) {
            keys[i] = keys[i].trim();
        }
        return keys;
    };
    Chart.prototype._setPeriod = function() {
        this.option.query.from = Math.floor(this.data.time[0] / 1000);
        this.option.query.until = Math.floor(this.data.time[this.data.time.length - 1] / 1000);
    };
//    Chart.prototype.update = function() {
//        this.data.time = this._generateTime(Math.floor(+new Date() / 1000));
//        this._setPeriod();
//
//        var query = this.option.query,
//            self = this;
//
//        $.ajax({
//            dataType : 'jsonp',
//            url : "http://127.0.0.1:8090/fetch/pull", //"http://10.64.84.42:9100/fetch/pull",
//            data : query
//        }).done(function(json) {
//                self.setJSON(json);
//                self._redraw();
//            });
//    };
    Chart.prototype._hasValue = function() {
        return (typeof this.data.value !== 'undefined' && Array.isArray(this.data.value));
    };
    Chart.prototype._hasCount = function() {
        return (typeof this.data.count !== 'undefined' && Array.isArray(this.data.count));
    };
    Chart.prototype.getData = function(key, type) {
        if (typeof type == "undefined") {
            if (this._hasValue()) {
                type = 'value';
            } else if (this._hasCount()) {
                type = 'count';
            } else {
                return this._generateEmptyData();
            }
        }
        if (typeof this.data[type] !== 'undefined' && Array.isArray(this.data[type])) {
            for (var i = 0, len = this.data[type].length; i < len; i++) {
                if (this.data[type][i].key === key) {
                    return this.data[type][i].data;
                }
            }
        }

        return this._generateEmptyData();
    };
    Chart.prototype.setJSON = function(json, parse) {
        if (typeof parse === "undefined") {
            parse = true;
        }
        this.json = json; //raw response data

        if (parse) {
            this._parse_data(json);
        }
    };
    Chart.prototype._generateTime = function(until) {
        var count = this.option.chart.x.count,
            interval = this.option.chart.x.interval,
            a = [],
            t = until,
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
        return a;
    };
    Chart.prototype._generateEmptyData = function() {
        var a = [];
        for (var i = 0, len = this.data.time.length; i < len; i++) {
            a[i] = 0;
        }
        return a;
    };
    Chart.prototype._get_slot_index = function (t, from) {
        var index = -1;
        var interval = this.option.chart.x.interval;
        for (var i = from, len = this.data.time.length; i < len; i++) {
            //if (t === +this.data.time[i]) {
            if (t <= +this.data.time[i] && t - this.data.time[i] <= interval) {
                index = i;
                break;
            }
        }
        return index;
    };
    Chart.prototype._parse_data = function(raw_data) {
        var option = this.option,
            slot_index;

        this._parse(raw_data, "value");
        this._parse(raw_data, "count");
        return;
    };
    Chart.prototype._parse = function(raw_data, key) {
        //key 'value' || 'count'
        var option = this.option,
            slot_index;

        if (typeof raw_data[key] !== 'undefined' && Array.isArray(raw_data[key])) {
            this.data[key] = [];
            for (var i = 0, len = raw_data[key].length; i < len; i++) {
                if (raw_data[key][i]) {
                    var temp_data = {
                        key : raw_data[key][i].key,
                        data : this._generateEmptyData()
                    }
                    for (var j = 0, len2 = raw_data[key][i].data.length; j < len2; j++) {
                        var data = raw_data[key][i].data[j];
                        slot_index = this._get_slot_index(data[0] * 1000, j-1); //time
                        //slot_index = j;
                        if (slot_index > -1) {
                            // FIX patch for Hubble
                            //temp_data.data[slot_index] += data[1] || 0; //datum
                            temp_data.data[slot_index] = Math.max(temp_data.data[slot_index], data[1] || 0);
                        }
                    }
                    this.data[key].push(temp_data);
                }
            }
        }
    };
    Chart.prototype.destroy = function() {
        // var container = this.option.target;
        if (typeof this._destroy === "function") {
            this._destroy();
        }
        // console.log(this)

        // destroyObject(this);
        // for (var key in this) {
        // if (this.hasOwnProperty(key)) {
        // console.log(key)
        // this[key] = null;
        // delete this[key];
        // }
        // h.instances.splice(h.instances.indexOf(this), 1);
        // }
        // emptyNode(container);
        // container.innerHTML = "";
    }

//    var ChartImage = function(option) {
//        Chart.call(this, option);
//    };
//    ChartImage.prototype = Object.create(Chart.prototype);
//    ChartImage.prototype._url = function() {
//        var option = this.option,
//            query = option.query,
//            a = [];
//
//        for(var key in query) {
//            a.push(key+"="+query[key]);
//        }
//
//        return "http://10.64.85.110:9999?type=" + option.type + "&" + a.join("&") + "&option=" + encodeURIComponent(JSON.stringify(option.chart));
//    };
//    ChartImage.prototype._draw = function() {
//        var option = this.option,
//            image;
//
//        image = new Image();
//        image.src = this._url();
//        $(image).css("margin", option.chart.margin.map(function(s){return s+"px"}).join(" "));
//        option.target.appendChild(image);
//
//        this.image = image;
//    };
//    ChartImage.prototype.update = function(){
//        this.data.time = this._generateTime(Math.floor(+new Date() / 1000));
//        this._setPeriod();
//        this.image.src = this._url() + "&" + +new Date();;
//    };

    //define helix's properties
    var RENDERER_OF_CHART_TYPE = {};
    h.ChartMaster = Chart;
    h.renderer = {};
    h.defineRenderer = function(type, name, def) {
        h.extendRenderer.call(null, type, name, Chart, def);
    };
    h.extendRenderer = function(type, name, parent, def) {
        if (typeof parent === "string") {
            if (typeof h.renderer[parent] == 'undefined') {
                throw Error("parent renderer '" + parent + "' is not exist!");
                return;
            }
            parent = h.renderer[parent];
        } else {
            if (typeof parent == 'undefined') {
                throw Error("parent renderer '" + name + "' is not exist!");
                return;
            }
        }
        // if (typeof h.renderer[name] !== 'undefined') {
        // throw Error("renderer '" + name + "' already exists!");
        // return;
        // }
//
        // if (typeof h.renderer[name] !== 'undefined') {
        // throw Error("renderer '" + name + "' already exists!");
        // return;
        // }

        h.renderer[name] = function(option) {
            parent.call(this, option);
            def = def || function(){ return {} };
            var prop = def.call(this);
            for (var key in prop) {
                this[key] = prop[key];
            }
            return this;
        };
        h.renderer[name].name = name;
        h.renderer[name].prototype = Object.create(parent.prototype);
        RENDERER_OF_CHART_TYPE[type] = h.renderer[name];

    };
    h.instances = h.instances || [];
    h.images = h.images || [];

    var prepareOption = function(option){
        var type = CHART_TYPE[option.query.interval];
        option.format = option.format || 'auto';
        option.chart = deepcopy(option.chart, deepcopy(default_chart_option));
        option.chart.x = deepcopy(option.chart.x, deepcopy(default_chart_option_x[type]));
        option.chart.y = deepcopy(option.chart.y, deepcopy(default_chart_option_y));
        option.chart.desc = deepcopy(option.chart.desc, deepcopy(default_chart_option_desc[type]));
    };
    h.master = function(option){
        prepareOption(option);
        return new Chart(option);
    };
    h.render = function(option){
        prepareOption(option);
        var constructor = RENDERER_OF_CHART_TYPE[option.type],
            chart,
            query = option.query;

        chart = new constructor(option);

        var format = option.format;
        if (format === 'auto') {
            if (Modernizr.svg && Modernizr.inlinesvg) {
                format = 'svg';
            } else {
                format = 'png'
            }
        }

        if (format === 'png' && option.serverside === true) {
            chart.setJSON(option.json);
            return chart._draw();
        }

        if (option.data) {
            chart.data = option.data;
            // chart.setJSON(option.json);
            chart._draw();

            h.instances.push(chart);
            return chart;
        } else {
            if (format === 'png') {
                chart = new ChartImage(option);
                chart._draw();
                h.images.push(chart);

                return chart;
            }

            //svg
            $.ajax({
                dataType : 'jsonp',
                url : "http://127.0.0.1:8090/fetch/pull", //"http://10.64.84.42:9100/fetch/pull",
                data : query
            }).done(function(json) {
                    chart.setJSON(json);
                    chart._draw();
                });
            // h.instances.push(chart);

            return chart;
        }
    };

    h.broadcast = function(event_name, group_name, e) {
        var event = jQuery.Event("broadcast");
        event.name = event_name;
        for (var key in e) {
            event[key] = e[key];
        }

        for (var i = 0, len = h.instances.length; i < len; i++) {
            if (h.instances[i].svg === e.origin || typeof h.instances[i].option.group !== "undefined" && h.instances[i].option.group.length > 0 && h.instances[i].option.group === group_name) {
                $(h.instances[i].svg).trigger(event);
            }
        }
    }

    return helix;
}());