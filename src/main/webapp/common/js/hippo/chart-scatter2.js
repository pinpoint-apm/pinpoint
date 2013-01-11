var scatter;

function updateScatter(start, end, scatter_data, targetId, limit) {
	if (!scatter) {
		alert("[ERROR] scatter chart is not initialized.")
	}

	if (limit) {
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
	} else {
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
	}
}

function drawScatter(start, end, scatter_data, targetId) {
	console.log("Draw scatter from=" + new Date(start) + ", end="
			+ new Date(end));

	scatter = new d3.chart.scatter({
		target : document.getElementById("scatter"),
		data : [],
		chart : {
			width : 960,
			height : 500,
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

	updateScatter(start, end, scatter_data, targetId);
}