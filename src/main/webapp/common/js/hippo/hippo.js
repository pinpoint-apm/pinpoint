function expandToNewWindow() {
	alert("새 창으로 확대.\n\nSorry. Not implemented.");
}

function openTrace(uuid, timestamp) {
    window.open("/selectTransaction.hippo?traceId=" + uuid + "&focusTimestamp=" + timestamp);
}

function getServerMapData2(application, begin, end, callback) {
    var app = application.split("@");
	d3.json("/getServerMapData2.hippo?application=" + app[0] + "&serviceType=" + app[1] + "&from=" + begin + "&to=" + end, function(d) { callback(d); });
}

function getLastServerMapData2(application, period, callback) {
    var app = application.split("@");
	d3.json("/getLastServerMapData2.hippo?application=" + app[0] + "&serviceType=" + app[1] + "&period=" + period, function(d) { callback(d); });
}

function getServerMapData(application, begin, end, callback) {
    var app = application.split("@");
	d3.json("/getServerMapData.hippo?application=" + app[0] + "&from=" + begin + "&to=" + end, function(d) { callback(d); });
}

function getLastServerMapData(application, period, callback) {
    var app = application.split("@");
	d3.json("/getLastServerMapData.hippo?application=" + app[0] + "&period=" + period, function(d) { callback(d); });
}

function getScatterData(application, begin, end, callback) {
    var app = application.split("@");
	d3.json("/getScatterData.hippo?application=" + app[0] + "&from=" + begin + "&to=" + end + "&limit=5000", function(d) { callback(d); });
}

function getLastScatterData(application, period, callback) {
    var app = application.split("@");
	d3.json("/getLastScatterData.hippo?application=" + app[0] + "&period=" + period + "&limit=5000", function(d) { callback(d); });
}

function getRealtimeScatterData(application, begin, callback) {
    var app = application.split("@");
	d3.json("/getRealtimeScatterData.hippo?application=" + app[0] + "&from=" + begin + "&limit=5000", function(d) { callback(d); });
}

function getQueryPeriod() {
	return $("#period").find("button.active").val().split(",") * 1000 * 60;
}

function getQueryStartTime() {
	return getQueryEndTime() - getQueryPeriod();
}

function formatDate(date) {
	var padZero = function(i) {
	    return (i < 10) ? "0" + i : "" + i;
	}
	
	var dateStr = [];
	
	dateStr.push(date.getFullYear());
	dateStr.push("-");
	dateStr.push(padZero(date.getMonth())); 
	dateStr.push("-");
	dateStr.push(padZero(date.getDate()));
	dateStr.push(" ");
	dateStr.push(padZero(date.getHours())); 
	dateStr.push(":");
	dateStr.push(padZero(date.getMinutes()));
	dateStr.push(":");
	dateStr.push(padZero(date.getSeconds()));
					
    return dateStr.join('');
}

function getQueryEndTime() {
	var format = d3.time.format("%Y/%m/%d %H:%M:%S"),
	now = new Date(),
	input = format.parse($('#date').val() + ' ' + $('#time').val()) || now;
	
	if (input.getTime() > now.getTime()) {
		input = now;
	}
	return input.getTime();
}

function setQueryDateToNow() {
   	var date = new Date();
   	var format_date = d3.time.format("%Y/%m/%d"),
   		format_time = d3.time.format("%H:%M:%S");
   	
   	$('#date').val(format_date(date));
   	$('#time').val(format_time(date));
}

function showIndicator() {
	$(".indicator").show();
}

function hideIndicator() {
	$(".indicator").hide();
}

function isQueryFromNow() {
	return $(".btn#now.active").length > 0;
}

function formatNumber(num) {
	if (num == 0 || isNaN(num))
		return 0;

	var reg = /(^[+-]?\d+)(\d{3})/;
	var n = (num + '');

	while (reg.test(n))
		n = n.replace(reg, '$1' + ',' + '$2');

	return n;
}

function sliceTimeSpan(start, end) {
	console.log("Slice the time. " + new Date(start) + " (" + start + ") ~ " + new Date(end) + "(" + end + ")");
	
	var chunk = 10 * 60 * 1000;
	
	if (end - start < chunk) {
		return [{
			'start' : start,
			'end' : end
		}];
	}
	
	var timeslot = [];
	var s = start;
	var e = s + chunk;
	
	timeslot.push({
		'start' : s,
		'end' : e
	});
	
	while (true) {
		if (e >= end) {
			break;
		}
		
		s = e + 1; // plus 1ms
		e = s + chunk;

		if (e > end) {
			e = end;
			timeslot.push({
				'start' : s,
				'end' : e
			});
			break;
		} else {
			timeslot.push({
				'start' : s,
				'end' : e
			});
		}
	}
	return $(timeslot);
}