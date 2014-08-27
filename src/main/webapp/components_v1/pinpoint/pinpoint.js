if ($.tmpl) {
	$.extend($.tmpl.tag, {
	    "var": {
	        open: "var $1;"
	    }
	});
	$.extend($.tmpl.tag, { "eval": { open: "$1;"} });
}

function openTrace(uuid, timestamp) {
    window.open("/transactionInfo.pinpoint?traceId=" + uuid + "&focusTimestamp=" + timestamp);
}

function formatDate(date, ignoreSeconds) {
	var padZero = function(i) {
	    return (i < 10) ? "0" + i : "" + i;
	}
	
	var dateStr = [];
	
	dateStr.push(date.getFullYear());
	dateStr.push("/");
	dateStr.push(padZero(date.getMonth())); 
	dateStr.push("/");
	dateStr.push(padZero(date.getDate()));
	dateStr.push(" ");
	dateStr.push(padZero(date.getHours())); 
	dateStr.push(":");
	dateStr.push(padZero(date.getMinutes()));
	if (!ignoreSeconds) {
		dateStr.push(":");
		dateStr.push(padZero(date.getSeconds()));
	}
					
    return dateStr.join('');
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