d3.chart.scatter = function(option) {
	d3.chart.call(this, option);
	var obj = d3.chart.scatter._renderer.call(this);
	for (var p in obj) {
		this[p] = obj[p];
	}
	
	this.draw();
	return this;
};
d3.chart.scatter.prototype = Object.create(d3.chart.prototype);
d3.chart.scatter.prototype.draw = function() {
	this._draw();
};
d3.chart.scatter._renderer = function(){
	var COLOR_GREEN = "#347C2C"; //"#2CA02C";
	var COLOR_RED = "#C11B17"; //"#D62728";
	
	var self = this,
		option = this.option,
		data = this.data,
		chart_width = option.chart.width,
		chart_height = option.chart.height,
		top_margin = option.chart.margin[0],
		right_margin = option.chart.margin[1],
		bottom_margin = option.chart.margin[2],
		left_margin = option.chart.margin[3],
		top_padding = option.chart.padding[0],
		right_padding = option.chart.padding[1],
		bottom_padding = option.chart.padding[2],
		left_padding = option.chart.padding[3],
		axis_width = chart_width - left_padding - right_padding,
		axis_height = chart_height - top_padding - bottom_padding,
		ticks = 5, //option.chart.y.ticks,
		data_count = data.length,
		bar_width = axis_width / data_count,
		xScale = d3.time.scale().domain(d3.extent(data, function(d) { return d.timestamp; }).map(function(t){return new Date(t);})).range([left_padding, left_padding + axis_width - bar_width]),
		yScale = d3.scale.linear().domain(d3.extent(data, function(d) { return d.executionTime; })).rangeRound([top_padding + axis_height, top_padding]),
		container = d3.select(option.target),
		chart = container.append("svg").attr("width", chart_width).attr("height", chart_height).style("margin", option.chart.margin.map(function(s){return s+"px";}).join(" ")),
		color = d3.scale.category10(),
		selectedTraceIdSet = {},
		brush = d3.svg.brush(chart)
			.on("brushstart", function(){
				chart.selectAll("rect.extent").style("fill", "#444").style("fill-opacity", "0.5");
			})
			.on("brush", function(p) {
				// selectedTraceIdSet = {};
				selectedTraceIdSet = [];
				var e = brush.extent();
				chart.selectAll(".dot").each(function(d) {
					if (new Date(e[0][0]).getTime() <= d.timestamp && d.timestamp <= new Date(e[1][0]).getTime()) {
						if (typeof option.chart.y.limit === "number" && option.chart.y.limit > 0) {
							var time = Math.min(option.chart.y.limit, d.executionTime);
						}

					 	if (e[0][1] <= time && e[1][1] >= time) {
							// selectedTraceIdSet[d.traceId] = d;
							var v = {};
							v.traceId = d.traceId;
							v.timestamp = d.timestamp;
							v.executionTime = d.executionTime;
					 		
					 		selectedTraceIdSet.push(v);
						}
					}
				});
			})
			.on("brushend", function() {
				self._displaySelectedTraceIdList(selectedTraceIdSet);
			});
		
		this.svg = chart[0][0];
	
	//prototype
	return {
		_prepare : function() {
			this.axis = chart.append("g").attr("class", "axis");
			this.brush = chart.append("g").attr("class", "brush").attr("y", 0).attr("x", 0).attr("width", axis_width).attr("height", axis_height).style("fill", "none");
			this.dot_green = chart.append("g").attr("class", "dot-green");
			this.dot_red = chart.append("g").attr("class", "dot-red");
			this.desc = chart.append("g").attr("class", "desc");

			// title
			this.title = this.desc.append("text")
				.attr("class", "title")
				.attr("x", left_padding)
				.attr("y", top_padding - 20)
				.attr("text-anchor", "start")
				.style('font-size', '13px')
				.style('font-family', 'tahoma')
				.style('font-weight', 'bold');

			// x.title
			this.xunit = this.desc.append("text")
				.attr("class", "xunit")
				.attr("x", left_padding + (axis_width) / 2)
				.attr("y", top_padding + axis_height + 38)
				.attr("text-anchor", "middle")
				.style('font-size', '12px')
				.style('font-family', 'tahoma')
				.style('font-style', 'italic');
				
			this.yunit = this.desc.append("text")
				.attr("class", "yunit")
				.attr("x", left_padding + 15)
				.attr("y", top_padding)
				.attr("text-anchor", "middle")
				.style('font-size', '12px')
				.style('font-family', 'tahoma')
				.style('font-style', 'italic')
				.style("text-anchor", "end")
				.attr("transform", "rotate(-90 " + (left_padding + 15) + " " + (top_padding) + ")");
			
			this.progressbar = this.desc.append("rect")
				.attr("class", "prgressbar")
				.attr("x", 0)
				.attr("y", 0)
				.attr("width", 0)
				.attr("height", 0)
				.style("fill", "#FFE87C")
				.style("fill-opacity", "0.5");
			
			this.greenCounter = this.desc.append("text")
				.attr("x", chart_width - left_padding - 200)
				.attr("y", top_padding - 5)
				.attr("width", 100)
				.attr("height", 50)
				.style("fill", COLOR_GREEN)
				.style('font-size', '12px')
				.style('font-family', 'tahoma');
			
			this.redCounter = this.desc.append("text")
				.attr("x", chart_width - left_padding - 100)
				.attr("y", top_padding - 5)
				.attr("width", 100)
				.attr("height", 50)
				.style("fill", COLOR_RED)
				.style('font-size', '12px')
				.style('font-family', 'tahoma');
		},
		
		showProgressbar : function(begin, end) {
			xScale = d3.time.scale().domain([new Date(option.chart.x.start), new Date(option.chart.x.end)]).range([left_padding, left_padding + axis_width]);
			yScale = d3.scale.linear().domain([0, 1]).rangeRound([top_padding + axis_height, top_padding]);
			
			this.progressbar
				.attr("x", xScale(begin))
				.attr("width", xScale(end) - xScale(begin))
				.attr("y", top_padding)
				.attr("height", chart_height - bottom_padding - top_padding);
			
			this.progressbar.style("display", "");
		},
		
		showDataCount : function(count) {
			this.greenCounter.text($(".dot.green").length);
			this.redCounter.text($(".dot.red").length);
		},
		
		hideProgressbar : function() {
			this.progressbar.style("display", "none");
		},
		
		_setEventHandler : function() {
			/* event handling */
			this.brush.call(brush.x(xScale).y(yScale));
		},
		_draw_desc : function() {
			this.title.text(option.chart.desc.title);
			this.xunit.text(option.chart.x.unit);
			this.yunit.text(option.chart.y.unit);
		},
		
		_draw_bottom_line : function() {
			this.axis.selectAll("line.left_line").remove();
			this.axis.append("line")
				.attr("class", "left_line")
				.attr("y1", top_padding + 0.5)
				.attr("y2", chart_height - bottom_padding + 0.5)
				.attr("x1", left_padding + 0.5)
				.attr("x2", left_padding + 0.5)
				.style("stroke", "#000");
			
			this.axis.selectAll("line.bottom_line").remove();
			this.axis.append("line")
				.attr("class", "bottom_line")
				.attr("y1", chart_height - bottom_padding + 0.5)
				.attr("y2", chart_height - bottom_padding + 0.5)
				.attr("x1", left_padding + 0.5)
				.attr("x2", chart_width - right_padding + 0.5)
				.style("stroke", "#000");
		},
		
		_draw_axis : function() {
			var tick = this.autoDateTick(xScale);

			var h_subline = this.axis.append("g").attr("class", "h_subline");
			/* yAxis */
			if (data.length > 0) {
				this.axis.selectAll("text.v.label").data(yScale.ticks(ticks))
					.enter().append("text")
					.attr("class", "v label")
					.attr("x", left_padding - 10)
					.attr("y", function (d) {
						return yScale(d) + 4 + 0.5;
					})
					.attr("text-anchor", "end")
					.style('font-size', '12px')
					.style('font-family', 'tahoma')
					.style('font-weight', 'normal')
					.text(function(d) {
						return d;
					});
				
				// h_subline
				h_subline.selectAll("line.y").data(yScale.ticks(ticks))
					.enter().append("line").attr("class", "y")
					.style("stroke", "#ddd")
					.style("stroke-dasharray", "3,3")
					.attr("x1", function (d) {
						return left_padding + 0.5;
					})
					.attr("x2", function (d) {
						return chart_width - right_padding + 0.5;
					})
					.attr("y1", function (d) {
						return yScale(d) + 0.5;
					})
					.attr("y2", function (d) {
						return yScale(d) + 0.5;
					});
			}
			
			var v_subline = this.axis.append("g").attr("class", "v_subline");
			v_subline.selectAll("line.x").data(xScale.ticks(tick[0], tick[1]))
				.enter().append("line")
				.style("stroke", function(d) {
					if (d === 0) {
						return "#000";
					}
					return "#ddd";
				})
				.style("stroke-dasharray", "3,3")
				.attr("class", "x")
				.attr("x1", function (d) {
					return Math.floor(xScale(d) + bar_width / 2) + 0.5;
				})
				.attr("x2", function (d) {
					return Math.floor(xScale(d) + bar_width / 2) + 0.5;
				})
				.attr("y1", function (d) {
					return top_padding + 0.5;
				})
				.attr("y2", function (d) {
					return chart_height - bottom_padding + 0.5;
				});
				
			var h_label = this.axis.selectAll("text.h.label").data(xScale.ticks(tick[0], tick[1]));
			h_label.enter().append("text")
				.attr("class", "h label")
				.attr("x", function (d) {
					return xScale(d) + bar_width / 2 + 0.5;
				})
				.attr("y", top_padding + axis_height + 20)
				.attr("text-anchor", "middle")
				.style('font-size', '12px')
				.style('font-family', 'tahoma')
				.style('font-weight', 'normal')
				.text(function(d) {
					return d3.time.format(tick[2])(new Date(d));
				});
		},
		
		_draw_chart : function() {
			if (data.length > 0) {
				var dot_green = [],
					dot_red = [];

				for (var i = 0; i < data.length; i++) {
					if (data[i].resultCode == 1) {
						dot_red.push(data[i]);
						continue;
					}
					dot_green.push(data[i]);
				}

				this.dot_green.selectAll(".dot")
					.data(dot_green)
					.enter().append("circle")
					.attr("class", "dot green")
					.attr("r", 3)
					.attr("cx", function(d) { return xScale(d.timestamp); })
					.attr("cy", function(d) { 
						if (typeof option.chart.y.limit === "number" && option.chart.y.limit > 0) {
							return yScale(Math.min(option.chart.y.limit, d.executionTime));
						}
						return yScale(d.executionTime); 
					})
					.style("fill", COLOR_GREEN)
					.on("click", function(d) {
						self._openTrace(d.traceId);
					});

				this.dot_red.selectAll(".dot")
					.data(dot_red)
					.enter().append("circle")
					.attr("class", "dot red")
					.attr("r", 3)
					.attr("cx", function(d) { return xScale(d.timestamp); })
					.attr("cy", function(d) { 
						if (typeof option.chart.y.limit === "number" && option.chart.y.limit > 0) {
							return yScale(Math.min(option.chart.y.limit, d.executionTime));
						}
						return yScale(d.executionTime); 
					})
					.style("fill", COLOR_RED)
					.on("click", function(d) {
						self._openTrace(d.traceId);
					});
			} else {
				this.dot_green.append("text")
					.attr("class", "label nodata")
					.attr("x", left_padding + axis_width/2)
					.attr("y", top_padding + axis_height/2)
					.attr("text-anchor", "middle")
					.style('font-size', '14px')
					.style('font-family', 'tahoma')
					.style('font-weight', 'bold')
					.text("No Data");
			}
		},
		
		_destroy : function() {
			$(chart[0]).unbind("mousemove", this._onMouseMove);
			$(chart[0]).unbind("mouseleave", dehighlight);
			destroyNode(this.svg);
			destroyObject(this);
		},
		
		_openTrace : function(keys) {
			console.log(keys);
			openTrace(keys);
		},
		
		_displaySelectedTraceIdList : function(traces) {
			if (traces.length === 0) {
				return;
			}
			
			if (traces.length === 1) {
				self._openTrace(traces[0].traceId);
				return;
			}
			
			var query = [];
			var temp = {};
			for (var i = 0; i < traces.length; i++) {
				if (i > 0) {
					query.push("&");
				}
				query.push("tr");
				query.push(i);
				query.push("=");
				query.push(traces[i].traceId);
				
				query.push("&ti");
				query.push(i);
				query.push("=");
				query.push(traces[i].timestamp)
				
				query.push("&re");
				query.push(i);
				query.push("=");
				query.push(traces[i].executionTime)
			}
			
			// queryparameter를 POST로 보내야할 것 같음.
			d3.json("/requestmetadata.hippo?" + query.join(""), function(d) {
				$("#selectedBusinessTransactionsDetail TBODY").empty();
				
				console.log(d);
				
				var data = d.metadata;
				
				var html = [];
				for (var i = 0; i < data.length; i++) {
						
					if(data[i].exception) {
						html.push("<tr class='error'>");
					} else {
						html.push("<tr>");
					}

					html.push("<td style='padding-right:5px;text-align:right'>");
					html.push(i + 1);
					html.push("</td>");

					html.push("<td>");
					html.push("<a href='#' onclick='openTrace(\"");
					html.push(data[i].traceId);
					html.push("\");'>");
					html.push(new Date(data[i].startTime).format("HH:MM:ss L"));
					html.push("</a>");
					html.push("</td>");

					html.push("<td style='padding-right:30px;text-align:right'>");
					html.push(formatNumber(data[i].elapsed));
					html.push("</td>");

					html.push("<td>");
					if (data[i].exception) {
						html.push(data[i].exception);
					}
					html.push("</td>");
					
					html.push("<td>");
					html.push(data[i].application);
					html.push("</td>");
					
					html.push("<td>");
					html.push(data[i].agentId);
					html.push("</td>");
					
					html.push("<td>");
					html.push("<a href='#' onclick=\"alert('not implemented. ip정보 조회 페이지로 연결.');\">");
					html.push(data[i].remoteAddr);
					html.push("</a>");
					html.push("</td>");

					html.push("</tr>");
				}

				$("#selectedBusinessTransactionsDetail TBODY").append(html.join(''));
				$('#traceIdSelectModal').modal({});
			});
		},
		
		add : function(data_to_add, filter) {
			filter = filter || function(data){ return data; };

			var data = this.data;
			this.data = data = data.concat(data_to_add);

			this.data = data = filter(data);

			if (data.length < 1) {
				bar_width = 0;
				xScale = d3.time.scale().domain([new Date(option.chart.x.start), new Date(option.chart.x.end)]).range([left_padding, left_padding + axis_width]);
				yScale = d3.scale.linear().domain([0, 1]).rangeRound([top_padding + axis_height, top_padding]);
			} else {
				chart.selectAll("text.label.nodata").remove();
				if (data.length === 1) {
					var extent = d3.extent(data, function(d) { return d.timestamp; });
					extent[0] -= 1000;
					extent[1] += 1000;
					extent = extent.map(function(t){return new Date(t);});
					xScale = xScale.domain(extent);

					extent = d3.extent(data, function(d) { return d.executionTime; });
					extent[0] -= 2;
					extent[1] += 2;
					yScale = yScale.domain(extent);
				} else {
//					xScale = xScale.domain(d3.extent(data, function(d) { return d.timestamp; }).map(function(t){return new Date(t);}));
					xScale = d3.time.scale().domain([new Date(option.chart.x.start), new Date(option.chart.x.end)]).range([left_padding, left_padding + axis_width]);

					if (typeof option.chart.y.limit === "number" && option.chart.y.limit > 0) {
						yScale = d3.scale.linear().domain([0, option.chart.y.limit]).rangeRound([top_padding + axis_height, top_padding]);	
					} else {
						yScale = yScale.domain(d3.extent(data, function(d) { return d.executionTime; }));
					}
				}
			}
			var tick = this.autoDateTick(xScale);

			if (data.length > 0) {
				/* yAxis */
				var v_label = this.axis.selectAll("text.v.label").data(yScale.ticks(ticks));
				v_label.enter().append("text")
					.attr("class", "v label")
					.attr("x", left_padding - 10)
					.attr("text-anchor", "end")
					.style('font-size', '12px')
					.style('font-family', 'tahoma')
					.style('font-weight', 'normal');
				v_label.exit().remove();
					
				// this.axis.selectAll("text.v.label")
				v_label
					.attr("y", function (d) {
						return yScale(d) + 4 + 0.5;
					})
					.text(function(d) {
						return d;
					});
					
				// h_subline
				var h_subline = this.axis.select(".h_subline").selectAll("line.y").data(yScale.ticks(ticks));
				h_subline
					.enter().append("line")
					.attr("class", "y")
					.style("stroke", "#ddd")
					.style("stroke-dasharray", "3,3")
					.attr("x1", function (d) {
						return left_padding + 0.5;
					})
					.attr("x2", function (d) {
						return chart_width - right_padding + 0.5;
					});
				h_subline.exit().remove();
					
				h_subline
					.attr("y1", function (d) {
						return yScale(d) + 0.5;
					})
					.attr("y2", function (d) {
						return yScale(d) + 0.5;
					});
			}
			
			var v_subline = this.axis.select(".v_subline").selectAll("line.x").data(xScale.ticks(tick[0], tick[1]));
			v_subline
				.enter().append("line")
				.style("stroke", function(d) {
					if (d === 0) {
						return "#000";
					}
					return "#ddd";
				})
				.style("stroke-dasharray", "3,3")
				.attr("class", "x")
				.attr("y1", function (d) {
					return top_padding + 0.5;
				})
				.attr("y2", function (d) {
					return chart_height - bottom_padding + 0.5;
				});
			v_subline.exit().remove();
				
			v_subline
				.attr("x1", function (d) {
					return Math.floor(xScale(d) + bar_width / 2) + 0.5;
				})
				.attr("x2", function (d) {
					return Math.floor(xScale(d) + bar_width / 2) + 0.5;
				});
				
			var h_label = this.axis.selectAll("text.h.label").data(xScale.ticks(tick[0], tick[1]));
			h_label
				.enter().append("text")
				.attr("class", "h label")
				.attr("y", top_padding + axis_height + 20)
				.attr("text-anchor", "middle")
				.style('font-size', '12px')
				.style('font-family', 'tahoma')
				.style('font-weight', 'normal');
				
			h_label.exit().remove();
			
			h_label
				.attr("x", function (d) {
					return xScale(d) + bar_width / 2 + 0.5;
				})
				.text(function(d) {
					return d3.time.format(tick[2])(new Date(d));
				});
			

			var dot_green = [],
				dot_red = [];

			for (var i = 0; i < data.length; i++) {
				if (data[i].resultCode == 1) {
					dot_red.push(data[i]);
					continue;
				}
				dot_green.push(data[i]);
			}

			var green = this.dot_green.selectAll(".dot")
				.data(dot_green);
			green.exit().remove();
			green.enter().append("circle")
				.attr("class", "dot green")
				.attr("r", 3)
				.on("click", function(d) {
					self._openTrace(d.traceId);
				});
			
			this.dot_green.selectAll(".dot")
				// .transition()
				.style("fill", COLOR_GREEN)
				// .duration(1500)
				.attr("cx", function(d) { return xScale(d.timestamp); })
				.attr("cy", function(d) { 
					if (typeof option.chart.y.limit === "number" && option.chart.y.limit > 0) {
						return yScale(Math.min(option.chart.y.limit, d.executionTime));
					}
					return yScale(d.executionTime); 
				});

			var red = this.dot_red.selectAll(".dot")
				.data(dot_red);
			red.exit().remove();
			red.enter().append("circle")
				.attr("class", "dot red")
				.attr("r", 3)
				.on("click", function(d) {
					self._openTrace(d.traceId);
				});
			
			this.dot_red.selectAll(".dot")
				.style("fill", COLOR_RED)
				.attr("cx", function(d) { return xScale(d.timestamp); })
				.attr("cy", function(d) { 
					if (typeof option.chart.y.limit === "number" && option.chart.y.limit > 0) {
						return yScale(Math.min(option.chart.y.limit, d.executionTime));
					}
					return yScale(d.executionTime); 
				});

			brush.clear();
			this.brush.call(brush.x(xScale).y(yScale)); 
		},

		_draw : function() {
			if (!Array.isArray(this.option.data) || this.option.data.length < 1) {
				bar_width = 0;
				xScale = d3.time.scale().domain([new Date(option.chart.x.start), new Date(option.chart.x.end)]).range([left_padding, left_padding + axis_width]);
				yScale = d3.scale.linear().domain([0, 1]).rangeRound([top_padding + axis_height, top_padding]);
			}

			if (typeof option.chart.y.limit === "number" && option.chart.y.limit > 0) {
				yScale = d3.scale.linear().domain([0, option.chart.y.limit]).rangeRound([top_padding + axis_height, top_padding]);	
			}

			this._prepare();
			this._draw_desc();	
			this._draw_axis();
			this._draw_bottom_line();
			this._draw_chart();
			this._setEventHandler();
		}
	};
};