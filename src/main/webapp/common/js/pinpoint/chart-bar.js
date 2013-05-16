function showLinkHistogramSum() {
	$("#linkInfoBarChart").show();

	d3.json('/barData.json', function(data) {
		nv.addGraph(function() {
			var chart = nv.models.discreteBarChart().x(function(d) {
				return d.label
			}).y(function(d) {
				return d.value
			}).staggerLabels(true).tooltips(false).showValues(true)
	
			d3.select('#linkInfoBarChart svg')
					.datum(data)
					.transition()
					.duration(500)
					.call(chart);
	
			nv.utils.windowResize(chart.update);
	
			return chart;
		});
	});
}