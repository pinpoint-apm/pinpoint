function showLinkHistogramDetailed() {
	$("#linkInfoChart").show();
	
	d3.json('/stackedTestData.json', function(data) {
		nv.addGraph(function() {
			var chart = nv.models.stackedAreaChart().x(function(d) {
				return d[0];
			}).y(function(d) {
				return d[1];
			}).clipEdge(true);

			chart.xAxis.tickFormat(function(d) {
				return d3.time.format('%x')(new Date(d));
			});

			chart.yAxis.tickFormat(d3.format(',.2f'));

			d3.select('#linkInfoChart svg')
				.datum(data)
				.transition()
				.duration(500)
				.call(chart);

			nv.utils.windowResize(chart.update);

			return chart;
		});
	})
}