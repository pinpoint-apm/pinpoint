/**
 * Deprecated.
 */
var scatter;

function updateScatter(start, end, scatter_data, targetId, limit) {
	if (!scatter) {
		alert("[ERROR] scatter chart is not initialized.")
	}

	scatter.option.chart.x.start = start;
	scatter.option.chart.x.end = end;
	scatter.add(scatter_data, function(data) {
		var ret = [];
		data.forEach(function(d) {
			if (d.timestamp >= start && d.timestamp <= end) {
				ret.push(d);
			}
		});
		return ret;
	});
	scatter.showDataCount();
}

function drawScatter(start, end, targetId) {
	console.log("Draw scatter from=" + new Date(start) + ", end="
			+ new Date(end));

	scatter = new d3.chart.scatter({
		target : document.getElementById("scatter"),
		data : [],
		chart : {
			width : 550,
			height : 400,
			padding : [ 50, 50, 50, 50 ],
			margin : [ 0, 0, 0, 0 ],
			x : {
				unit : "Timestamp",
				start : start,
				end : end
			},
			y : {
				unit : "Execute time (ms)",
				limit : 10000
			},
			desc : {
				// title : "scatter"
			}
		}
	});
}