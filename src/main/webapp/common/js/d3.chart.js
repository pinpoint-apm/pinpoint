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
}

(function(){
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
	
	var default_chart_option = {
			width : 700,
			height : 400,
			padding : [100, 100, 100, 100],
			margin : [50, 50, 50, 50]
		},
		//default_chart_option_y
		default_chart_option_y = {
			range : null,
			ticks : 7,
			label : function(v) { return v; }
		},
		//default_chart_option_x
		default_chart_option_x = {
			count : 60,
			interval : 60000,
			tick : "minutes",
			tick_interval : 5,
			format : "%H:%M"
		},
		//default_chart_option_desc
		default_chart_option_desc = {
			sub_title : "1 hour @ 1 minute",
			period_format : "%m/%d %H:%M",
			tick_format : "%H:%M"
		};
		
	var prepareOption = function(option){
		option.format = option.format || 'auto';
		option.chart = deepcopy(option.chart, deepcopy(default_chart_option));
		option.chart.x = deepcopy(option.chart.x, deepcopy(default_chart_option_x));
		option.chart.y = deepcopy(option.chart.y, deepcopy(default_chart_option_y));
		option.chart.desc = deepcopy(option.chart.desc, deepcopy(default_chart_option_desc));
		return option;
	};
	
	
	d3.chart = function(option){
		this.option = prepareOption(option);
		this.data = [];
		if (Array.isArray(this.option.data) && this.option.data.length > 0) {
			this.setData(this.option.data);
		}
	};
	d3.chart.prototype.setData = function(a) {
		this.data = a;
	};
	d3.chart.prototype.destroy = function() {
		if (typeof this._destroy === "function") {
			this._destroy();
		}
	};

	d3.chart.prototype.autoDateTick = function(scale) {
		var domain = scale.domain(),
			interval = domain[1] - domain[0],
			unit, count, format;

		if (interval < 10000) { //10s
			unit = d3.time.seconds;
			count = 1;
			format = "%H:%M:%S";
		} else if (interval < 60000) { //1m
			unit = d3.time.seconds;
			count = 10;
			format = "%H:%M:%S";
		} else if (interval < 600000) { //10m
			unit = d3.time.minutes;
			count = 1;
			format = "%H:%M";
		} else if (interval < 3600000) { //1h
			unit = d3.time.minutes;
			count = 10;
			format = "%H:%M";
		} else if (interval < 10800000) { //3h
			unit = d3.time.minutes;
			count = 30;
			format = "%H:%M";
		} else if (interval < 21600000) { //6h
			unit = d3.time.hours;
			count = 1;
			format = "%H:%M";
		} else if (interval < 43200000) { //12h
			unit = d3.time.hours;
			count = 2;
			format = "%H:%M";
		} else if (interval < 86400000) { //1d
			unit = d3.time.hours;
			count = 3;
			format = "%H:%M";
		} else if (interval < 172800000) { //2d
			unit = d3.time.hours;
			count = 6;
			format = "%H:%M";
		} else if (interval < 259200000) { //3d
			unit = d3.time.hours;
			count = 12;
			format = "%H:%M";
		} else if (interval < 604800000) { //1w
			unit = d3.time.days;
			count = 1;
			format = "%m/%d";
		} else if (interval < 1209600000) { //2w
			unit = d3.time.days;
			count = 1;
			format = "%m/%d";
		} else if (interval < 2592000000) { //1m
			unit = d3.time.weeks;
			count = 1;
			format = "%m/%d";
		} else if (interval < 7776000000) { //3m
			unit = d3.time.weeks;
			count = 2;
			format = "%m/%d";
		} else if (interval < 15552000000) { //6m
			unit = d3.time.months;
			count = 1;
			format = "%Y/%m";
		} else if (interval < 31536000000) { //1y
			unit = d3.time.months;
			count = 3;
			format = "%Y/%m";
		} else if (interval < 94608000000) { //3y
			unit = d3.time.months;
			count = 6;
			format = "%Y/%m";
		} else if (interval < 315360000000) { //10y
			unit = d3.time.years;
			count = 1;
			format = "%Y";
		} else if (interval < 946080000000) { //30y
			unit = d3.time.years;
			count = 5;
			format = "%Y";
		} else if (interval < 3153600000000) { //100y
			unit = d3.time.years;
			count = 10;
			format = "%Y";
		}
		
		//00:00, month/01

		return [unit, count, format];
	};
}());
