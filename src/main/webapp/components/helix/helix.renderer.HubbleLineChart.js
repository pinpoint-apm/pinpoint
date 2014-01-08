//defineRenderer(chart_type, constructor_name, draw_function);
helix.defineRenderer('line', 'HubbleLineChart', function(){
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
        range = option.chart.y.range,
        ticks = option.chart.y.ticks,
        data_count = data.time.length,
        bar_width = axis_width / data_count,
        xScale = d3.time.scale().domain([data.time[0], data.time[data_count - 1]]).range([left_padding, left_padding + axis_width - bar_width]),
        yScale = d3.scale.linear().rangeRound([top_padding + axis_height, top_padding]),
        xIndex = d3.scale.linear().domain([0, axis_width]).range([0, data_count]),
        xPosition = d3.scale.linear().domain([0, data_count]).rangeRound([left_padding, left_padding + axis_width]),
        container = d3.select(option.target),
        chart = container.append("svg").attr("width", chart_width).attr("height", chart_height).style("margin", option.chart.margin.map(function(s){return s+"px"}).join(" ")),
        symbol_type = ['circle', 'cross', 'diamond', 'square', 'triangle-down', 'triangle-up'],
        allkeys = this.getKeys(),
        color = d3.scale.category10().domain([0, allkeys.length - 1]),
        last_x = -1,
        min = null,
        max = null,
        allData = null,
        convertTo = function(v, kind) {
            if (v >= 1000000000) {
                return (v/1000000000).toFixed(2) + "G"
            } else if (v >= 1000000) {
                return (v/1000000).toFixed(2) + "M"
            } else if (v >= 1000) {
                return (v/1000).toFixed(2) + "K"
            }
            return v;
        },
        pointing = function(x, y, x1, x2, y1, y2) {
            return (x >= x1 && x <= x2 && y >= y1 && y <= y2);
        },
        highlight = function(x) {
            x = Math.max(0, Math.min(data_count - 1, Math.round(x - 0.5)));
            if (last_x === x) return;
            var v = allData[x];

            //highlighted data
            var highlight = chart.selectAll("line.highlight");
            highlight.data([x])
                .enter().append("line")
                .attr("class", "highlight")
                .attr("x1", function (d) {
                    return xPosition(d) + (bar_width / 2)
                })
                .attr("x2", function (d) {
                    return xPosition(d) + (bar_width / 2)
                })
                .attr("y1", top_padding + 0.5)
                .attr("y2", chart_height - bottom_padding + 0.5)
                .style("stroke", "red")

            chart.selectAll("line.highlight")
                // .transition().ease('cubic-out').duration(50)
                .attr("x1", function (d) {
                    return xPosition(d) + (bar_width / 2)
                })
                .attr("x2", function (d) {
                    return xPosition(d) + (bar_width / 2)
                })

            // console.log(x, v)
            // var tick = yScale.ticks(ticks);
            // var tick_interval = tick[1] - tick[0];
            // console.log(tick_interval);
            // console.log()
            var symbols = chart.selectAll("path.symbol.highlight");
            symbols.data([v])
                .enter().append("path")
                .attr("class", "symbol highlight")
                .attr("transform", "translate(-100, -100)")
                .attr("d", d3.svg.symbol().type('circle').size(50))
                .style("fill", "white")
                .style("stroke", 'red')
                .style("stroke-width", "2px")

            chart.selectAll("path.symbol.highlight")
                .attr("transform", function(d) {
                    return "translate(" + (xPosition(x) + (bar_width / 2)) + "," + yScale(d) + ")";
                });

            // console.log(x, v)
            // console.log(x, v)
            var textrect = chart.selectAll("rect.highlight-value");
            textrect.data([v])
                .enter().append("rect")
                .attr("class", "highlight-value")
                .attr("height", 15)
                .style("fill", "white")

            var text = chart.selectAll("text.highlight-value");
            text.data([v])
                .enter().append("text")
                .attr("class", "highlight-value")
                .attr("text-anchor", "middle")
                .style('font-size', '12px')
                .style('font-family', 'tahoma')
                .style('font-weight', 'bold')

            var texttime = chart.selectAll("text.highlight-time");
            texttime.data([v])
                .enter().append("text")
                .attr("class", "highlight-time")
                .attr("text-anchor", "middle")
                .style('font-size', '12px')
                .style('font-family', 'tahoma')
                .style('font-weight', 'bold')

            text = chart.selectAll("text.highlight-value")
                .attr("x", xPosition(x) + (bar_width / 2))
                .attr("y", function(d){
                    return yScale(d) - 10;
                })
                // FIX
                .text(convertTo(v.toFixed(5) * 1, 'size'));
            //.text(v.toFixed(5) * 1)

            var tw = text[0][0].getBBox().width + 10;

            texttime = chart.selectAll("text.highlight-time")
                .attr("x", xPosition(x) + (bar_width / 2))
                .attr("y", top_padding + axis_height + 35)
                .text(d3.time.format("%m/%d %H:%M")(data.time[x]))
            //.text(d3.time.format(option.chart.desc.tick_format)(data.time[x]))

            textrect = chart.selectAll("rect.highlight-value")
                .attr("x", (xPosition(x) + (bar_width / 2) - Math.floor(tw/2)))
                .attr("y", function(d){
                    return yScale(d) - 22;
                })
                .attr("width", tw)

            last_x = x;
        },
        dehighlight = function() {
            // console.log("dehighlight")
            if (last_x === -1) {
                return;
            }
            last_x = -1;

            chart.selectAll("line.highlight").remove();
            chart.selectAll("path.symbol.highlight").remove();
            chart.selectAll("text.highlight-time").remove();
            chart.selectAll("text.highlight-value").remove();
            chart.selectAll("rect.highlight-value").remove();
            chart.selectAll("path.highlight-max").remove();
            chart.selectAll("text.highlight-maxvalue").remove();

        };

    this.svg = chart[0][0];

    return {
        _getColor : function(index) {
            return color(index);
        },
        _getRange : function(keys) {
            return (Array.isArray(range)) ? range : [0, d3.max(this._getAllData(keys)) * 1.1 || 1]
        },
        _getAllData : function(keys) {
            var allData = [];
            for (var i = 0, len = keys.length; i < len; i++) {
                allData = allData.concat(self.getData(keys[i].trim(), 'value'));
            }
            return allData;
        },
        _onMouseMove : (function(e) {
            if (this._et) {
                clearTimeout(this._et);
            }
            // return;
            // var e = d3.event;
            this._et = setTimeout(function(){
                var offset = $(container[0]).offset(),
                    x = e.pageX - offset.left - left_margin,
                    y = e.pageY - offset.top - top_margin;


                if (pointing(x, y, left_padding, left_padding+axis_width, top_padding, top_padding+axis_height)) {
                    var symbols = chart.selectAll("path.symbol.highlight-max");
                    symbols.data([max.value])
                        .enter().append("path")
                        .attr("class", "symbol highlight-max")
                        .attr("transform", "translate(-100, -100)")
                        .attr("d", d3.svg.symbol().type('circle').size(50))
                        .style("fill", "white")
                        .style("stroke", 'red')
                        .style("stroke-width", "2px")

                    symbols.attr("transform", function(d) {
                        return "translate(" + (xPosition(max.index) + (bar_width / 2)) + "," + yScale(d) + ")";
                    });

                    var text = chart.selectAll("text.highlight-maxvalue");
                    text.data([max.value])
                        .enter().append("text")
                        .attr("class", "highlight-maxvalue")
                        .attr("x", Math.max(left_padding + 20, xPosition(max.index) + (bar_width / 2)))
                        .attr("y", top_padding + axis_height + 35)
                        .attr("text-anchor", "start")
                        .style('font-size', '12px')
                        .style('font-family', 'tahoma')
                        .style('font-weight', 'bold')

                    text
                        .attr("x", left_padding + 5)
                        .attr("y", top_padding - 10)
                        // FIX
                        .text(convertTo(max.value.toFixed(5) * 1, 'size') + " max");
                    //.text(max.value.toFixed(5) * 1 + " max")
                    //event broadcast
                    helix.broadcast("highlight", option.group, {
                        origin : chart,
                        x : x,
                        y : y
                    });
                } else {
                    helix.broadcast("dehighlight", option.group, {});
                }

                this._et = null;
            }, 0);
        }).bind(this),
        _onMouseLeave : (function() {
            //event broadcast
            helix.broadcast("dehighlight", option.group, {
                origin : chart[0]
            });
        }).bind(this),
        _onBroadcast : (function(e){
            switch (e.name) {
                case 'highlight':
                    highlight(xIndex(e.x - left_padding));
                    break;
                case 'dehighlight':
                    dehighlight();
                    break;
            }
        }).bind(this),
        _setEventHandler : function() {
            /* event handling */
            $(chart[0]).bind("mousemove", this._onMouseMove);
            $(chart[0]).bind("mouseleave", this._onMouseLeave);
            $(chart[0]).bind("broadcast", this._onBroadcast);
        },
        _destroy : function() {
            $(chart[0]).unbind("mousemove", this._onMouseMove);
            $(chart[0]).unbind("mouseleave", this._onMouseLeave);
            $(chart[0]).unbind("broadcast", this._onBroadcast);
            destroyNode(this.svg);
            destroyObject(this);
            // destroy(this);
            // console.log(this)
            self = option = data = chart_width = chart_height = top_margin = right_margin = bottom_margin = left_margin = top_padding = right_padding = bottom_padding = left_padding = axis_width = axis_height = range = ticks = data_count = bar_width = xScale = yScale = xIndex = xPosition = container = chart = symbol_type = allkeys = color = last_x = min = max = allData = pointing = highlight = dehighlight = null;
        },
        _prepare : function() {
            allData = this._getAllData(allkeys, 'value');
            var minv = d3.min(allData),
                maxv = d3.max(allData);
            min = {
                index : allData.indexOf(minv),
                value : minv
            };
            max = {
                index : allData.indexOf(maxv),
                value : maxv
            };

            // title
            this.title = chart.append("text")
                .attr("class", "title")
                .attr("x", left_padding + (axis_width) / 2)
                .attr("y", top_padding - 15)
                .attr("text-anchor", "middle")
                .style('font-size', '12px')
                .style('font-family', 'tahoma')

            // sub_title
            // this.sub_title = chart.append("text")
            // .attr("class", "sub_title")
            // .attr("x", left_padding + (axis_width) / 2)
            // .attr("y", top_padding - 20)
            // .attr("text-anchor", "middle")
            // .style('font-size', '10px')
            // .style('font-family', 'tahoma')

            // from
            // this.from = chart.append("text")
            // .attr("class", "from")
            // .attr("x", left_padding)
            // .attr("y", top_padding - 15)
            // .style('font-size', '12px')
            // .style('font-family', 'tahoma')
//
            // // until
            // this.until = chart.append("text")
            // .attr("class", "until")
            // .attr("x", chart_width - right_padding)
            // .attr("y", top_padding - 15)
            // .attr("text-anchor", "end")
            // .style('font-size', '12px')
            // .style('font-family', 'tahoma')
        },
        _draw_desc : function(keys) {
            /* title */
            this.title.text(option.chart.desc.title || keys.join(", "))
            /* sub_title */
            // this.sub_title.text(option.chart.desc.sub_title)
            /* from */
            // this.from.text(d3.time.format(option.chart.desc.period_format)(data.time[0]))
            // /* until */
            // this.until.text(d3.time.format(option.chart.desc.period_format)(data.time[data_count - 1]))
        },
        _draw_bottom_line : function() {
            chart.selectAll("line.bottom_line").remove();
            chart.append("line")
                .attr("class", "bottom_line")
                .attr("y1", top_padding + axis_height + 1)
                .attr("y2", top_padding + axis_height + 1)
                .attr("x1", left_padding)
                .attr("x2", left_padding + axis_width)
                .style("stroke", "#666")
                .style("stroke-width", "1px")
        },

        _draw_axis : function(keys) {
            yScale.domain(this._getRange(keys));

            var v_subline = chart.append("g").attr("class", "v_subline");
            v_subline.selectAll("line.x").data(xScale.ticks(d3.time[option.chart.x.tick], option.chart.x.tick_interval))
                .enter().append("line")
                .style("stroke", "#ddd")
                .attr("class", "x")
                .attr("x1", function (d, i) {
                    return Math.floor(xScale(d) + bar_width / 2) + .5
                })
                .attr("x2", function (d) {
                    return Math.floor(xScale(d) + bar_width / 2) + .5
                })
                .attr("y1", top_padding + .5)
                .attr("y2", chart_height - bottom_padding + .5)
            var h_label = chart.selectAll("text.h_label").data(xScale.ticks(d3.time[option.chart.x.tick], option.chart.x.tick_interval))
            h_label.enter().append("text")
                .attr("class", "h_label")
                .attr("x", function (d) {
                    return Math.floor(xScale(d) + bar_width / 2) + .5
                })
                .attr("y", top_padding + axis_height + 20)
                .attr("text-anchor", "middle")
                .style('font-size', '12px')
                .style('font-family', 'tahoma')
                .text(function(d) {
                    return d3.time.format(option.chart.x.tick_format)(d);
                });

            /* yAxis */
            chart.selectAll("text.v_label").data(yScale.ticks(ticks))
                .enter().append("text")
                .attr("class", "v_label")
                .attr("x", left_padding - 10)
                .attr("y", function (d) {
                    return yScale(d) + 4 + .5
                })
                .attr("text-anchor", "end")
                .style('font-size', '12px')
                .style('font-family', 'tahoma')
                .text(option.chart.y.label);

            // h_subline
            var h_subline = chart.append("g").attr("class", "h_subline");
            h_subline.selectAll("line.y").data(yScale.ticks(ticks))
                .enter().append("line").attr("class", "y").style("stroke", "#ddd")
                .attr("x1", left_padding + .5)
                .attr("x2", chart_width - right_padding + .5)
                .attr("y1", function (d) {
                    return yScale(d) + .5
                })
                .attr("y2", function (d) {
                    return yScale(d) + .5
                })

            chart.selectAll('.axis text')
                .style('font-size', '12px')
                .style('font-family', 'tahoma')
            chart.selectAll('.axis path, .axis line')
                .style('fill', 'none')
                .style('shape-rendering', 'crispEdges')
        },

        _draw_chart : function(value) {
            /* area */
            var area = d3.svg.area()
                .x(function (d, i) {
                    return xPosition(i) + (bar_width / 2)
                })
                .y0(top_padding + axis_height)
                .y1(function (d) {
                    return yScale(d) + .5
                })
            chart.append("path")
                .attr("class", "area data")
                .attr("d", area(value))
                .style("stroke", "none")
                .style("fill", this._getColor(0))
                .style("fill-opacity", "0.2")

            /* line */
            var line = d3.svg.line()
                .x(function (d, i) {
                    return xPosition(i) + (bar_width / 2)
                })
                .y(function (d) {
                    return yScale(d) + .5
                })
            chart.append("path")
                .attr("class", "line data")
                .attr("d", line(value))
                .style("stroke", this._getColor(0))
                .style("fill", "none")
                .style("stroke-width", "0.5px")
        },

        _draw : function(keys) {
            keys = keys || allkeys;
            this._prepare();
            this._draw_desc(keys);
            this._draw_axis(keys);
            this._draw_chart(allData);
            this._draw_bottom_line();
            this._setEventHandler();

            return container.html();
        },

        _redraw : function(keys) {
            keys = keys || allkeys;
            this._draw_desc(keys);
            yScale.domain(this._getRange(keys));
            // v_subline
            var v_subline = chart.select("g.v_subline")
            v_subline = chart.selectAll("line.x").data(xScale.ticks(d3.time[option.chart.x.tick], option.chart.x.tick_interval));
            v_subline.enter().append("line")
                .style("stroke", "#ddd")
                .attr("class", "x")
                .attr("y1", top_padding + .5)
                .attr("y2", chart_height - bottom_padding + .5)
                .attr("x1", Math.floor(left_padding + axis_width + bar_width / 2) + .5)
                .attr("x2", Math.floor(left_padding + axis_width + bar_width / 2) + .5)
            v_subline.exit().remove()
            v_subline.transition().duration(1000)
                .attr("x1", function (d) {
                    return Math.floor(xScale(d) + bar_width / 2) + .5
                })
                .attr("x2", function (d) {
                    return Math.floor(xScale(d) + bar_width / 2) + .5
                })

            //h_label
            var h_label = chart.selectAll("text.h_label").data(xScale.ticks(d3.time[option.chart.x.tick], option.chart.x.tick_interval))
            h_label.exit().remove()
            h_label.enter().append("text")
                .attr("class", "h_label")
                .attr("x", left_padding + axis_width)
                .attr("y", top_padding + axis_height + 20)
                .attr("text-anchor", "middle")
                .style('font-size', '12px')
                .style('font-family', 'tahoma')
            h_label.text(function(d) {
                return d3.time.format(option.chart.x.tick_format)(d);
            });
            h_label.transition().duration(1000)
                .attr("x", function (d) {
                    return xScale(d) + 5 + .5
                });

            chart.selectAll("g.axis.y").remove();

            // h_subline
            var h_subline = chart.select("g.h_subline")
            h_subline = h_subline.selectAll("line.y").data(yScale.ticks(ticks));
            h_subline.enter().insert("line").attr("class", "y").style("stroke", "#ddd")
                .attr("x1", left_padding + .5)
                .attr("x2", chart_width - right_padding + .5)
                .attr("y1", top_padding + .5)
                .attr("y2", top_padding + .5)
            h_subline.exit().remove()
            h_subline.transition().duration(1000)
                .attr("y1", function (d) {
                    return yScale(d) + .5
                })
                .attr("y2", function (d) {
                    return yScale(d) + .5
                })

            //v_label
            var v_label = chart.selectAll("text.v_label").data(yScale.ticks(ticks))
            v_label.exit().remove()
            v_label.enter().append("text")
                .attr("class", "v_label")
                .attr("x", left_padding - 20)
                .attr("y", top_padding)
                .attr("text-anchor", "middle")
                .style('font-size', '12px')
                .style('font-family', 'tahoma')
            v_label.text(function(d) {
                return d
            });
            v_label.transition().duration(1000)
                .attr("y", function (d) {
                    return yScale(d) + 4 + .5
                });

            // styling axis text
            chart.selectAll('.axis text')
                .style('font-size', '12px')
                .style('font-family', 'tahoma')
            chart.selectAll('.axis path, .axis line')
                .style('fill', 'none')
                .style('shape-rendering', 'crispEdges')

            var index = 0;
            var drawData = function(key) {
                var value = self.getData(key, 'value');
                if (!value) return;

                /* area */
                var area = d3.svg.area()
                    .x(function (d, i) {
                        return xPosition(i) + (bar_width / 2)
                    })
                    .y0(top_padding + axis_height)
                    .y1(function (d) {
                        return yScale(d) + .5
                    })

                chart.selectAll("path.area.data-" +index).transition().duration(1000)
                    .attr("d", area(value))

                /* line */
                var line = d3.svg.line()
                    .x(function (d, i) {
                        return xPosition(i) + (bar_width / 2)
                    })
                    .y(function (d) {
                        return yScale(d) + .5
                    })
                chart.selectAll("path.line.data-" +index).transition().duration(1000)
                    .attr("d", line(value))

                index++;
            };

            for (var i = 0, len = keys.length; i < len; i++) {
                drawData(keys[i]);
            }

            this._draw_bottom_line();
        }
    }
});