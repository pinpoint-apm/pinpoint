nv.models.pie = function() {

  //============================================================
  // Public Variables with Default Settings
  //------------------------------------------------------------

  var margin = {top: 0, right: 0, bottom: 0, left: 0}
    , width = 500
    , height = 500
    , getValues = function(d) { return d.values }
    , getX = function(d) { return d.x }
    , getY = function(d) { return d.y }
    , getDescription = function(d) { return d.description }
    , id = Math.floor(Math.random() * 10000) //Create semi-unique ID in case user doesn't select one
    , color = nv.utils.defaultColor()
    , valueFormat = d3.format(',.2f')
    , showLabels = true
    , pieLabelsOutside = true
    , donutLabelsOutside = false
    , labelThreshold = .02 //if slice percentage is under this, don't show label
    , donut = false
    , labelSunbeamLayout = false
    , startAngle = false
    , endAngle = false
    , donutRatio = 0.5
    , dispatch = d3.dispatch('chartClick', 'elementClick', 'elementDblClick', 'elementMouseover', 'elementMouseout')
    ;

  //============================================================


  function chart(selection) {
    selection.each(function(data) {
      var availableWidth = width - margin.left - margin.right,
          availableHeight = height - margin.top - margin.bottom,
          radius = Math.min(availableWidth, availableHeight) / 2,
          arcRadius = radius-(radius / 5),
          container = d3.select(this);


      //------------------------------------------------------------
      // Setup containers and skeleton of chart

      //var wrap = container.selectAll('.nv-wrap.nv-pie').data([data]);
      var wrap = container.selectAll('.nv-wrap.nv-pie').data([getValues(data[0])]);
      var wrapEnter = wrap.enter().append('g').attr('class','nvd3 nv-wrap nv-pie nv-chart-' + id);
      var gEnter = wrapEnter.append('g');
      var g = wrap.select('g');

      gEnter.append('g').attr('class', 'nv-pie');

      wrap.attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
      g.select('.nv-pie').attr('transform', 'translate(' + availableWidth / 2 + ',' + availableHeight / 2 + ')');

      //------------------------------------------------------------


      container
          .on('click', function(d,i) {
              dispatch.chartClick({
                  data: d,
                  index: i,
                  pos: d3.event,
                  id: id
              });
          });


      var arc = d3.svg.arc()
                  .outerRadius(arcRadius);

      if (startAngle) arc.startAngle(startAngle)
      if (endAngle) arc.endAngle(endAngle);
      if (donut) arc.innerRadius(radius * donutRatio);

      // Setup the Pie chart and choose the data element
      var pie = d3.layout.pie()
          .sort(null)
          .value(function(d) { return d.disabled ? 0 : getY(d) });

      var slices = wrap.select('.nv-pie').selectAll('.nv-slice')
          .data(pie);

      slices.exit().remove();

      var ae = slices.enter().append('g')
              .attr('class', 'nv-slice')
              .on('mouseover', function(d,i){
                d3.select(this).classed('hover', true);
                dispatch.elementMouseover({
                    label: getX(d.data),
                    value: getY(d.data),
                    point: d.data,
                    pointIndex: i,
                    pos: [d3.event.pageX, d3.event.pageY],
                    id: id
                });
              })
              .on('mouseout', function(d,i){
                d3.select(this).classed('hover', false);
                dispatch.elementMouseout({
                    label: getX(d.data),
                    value: getY(d.data),
                    point: d.data,
                    index: i,
                    id: id
                });
              })
              .on('click', function(d,i) {
                dispatch.elementClick({
                    label: getX(d.data),
                    value: getY(d.data),
                    point: d.data,
                    index: i,
                    pos: d3.event,
                    id: id
                });
                d3.event.stopPropagation();
              })
              .on('dblclick', function(d,i) {
                dispatch.elementDblClick({
                    label: getX(d.data),
                    value: getY(d.data),
                    point: d.data,
                    index: i,
                    pos: d3.event,
                    id: id
                });
                d3.event.stopPropagation();
              });

        slices
            .attr('fill', function(d,i) { return color(d, i); })
            .attr('stroke', function(d,i) { return color(d, i); });

        var paths = ae.append('path')
            .each(function(d) { this._current = d; });
            //.attr('d', arc);

        d3.transition(slices.select('path'))
            .attr('d', arc)
            .attrTween('d', arcTween);

        if (showLabels) {
          // This does the normal label
          var labelsArc = d3.svg.arc().innerRadius(0);
          
          if (pieLabelsOutside){ labelsArc = arc; }

          if (donutLabelsOutside) { labelsArc = d3.svg.arc().outerRadius(arc.outerRadius()); }

          ae.append("g").classed("nv-label", true)
            .each(function(d, i) {
              var group = d3.select(this);

              group
                .attr('transform', function(d) {
                     if (labelSunbeamLayout) {
                       d.outerRadius = arcRadius + 10; // Set Outer Coordinate
                       d.innerRadius = arcRadius + 15; // Set Inner Coordinate
                       var rotateAngle = (d.startAngle + d.endAngle) / 2 * (180 / Math.PI);
                       if ((d.startAngle+d.endAngle)/2 < Math.PI) {
                       	 rotateAngle -= 90;
                       } else {
                       	 rotateAngle += 90;
                       }
                       return 'translate(' + labelsArc.centroid(d) + ') rotate(' + rotateAngle + ')';
                     } else {
                       d.outerRadius = radius + 10; // Set Outer Coordinate
                       d.innerRadius = radius + 15; // Set Inner Coordinate
                       return 'translate(' + labelsArc.centroid(d) + ')'
                     }
                });

              group.append('rect')
                  .style('stroke', '#fff')
                  .style('fill', '#fff')
                  .attr("rx", 3)
                  .attr("ry", 3);

              group.append('text')
                  .style('text-anchor', labelSunbeamLayout ? ((d.startAngle + d.endAngle) / 2 < Math.PI ? 'start' : 'end') : 'middle') //center the text on it's origin or begin/end if orthogonal aligned
                  .style('fill', '#000')


          });

          slices.select(".nv-label").transition()
            .attr('transform', function(d) {
            	if (labelSunbeamLayout) {
                  d.outerRadius = arcRadius + 10; // Set Outer Coordinate
                  d.innerRadius = arcRadius + 15; // Set Inner Coordinate
                  var rotateAngle = (d.startAngle + d.endAngle) / 2 * (180 / Math.PI);
                  if ((d.startAngle+d.endAngle)/2 < Math.PI) {
                    rotateAngle -= 90;
                  } else {
                    rotateAngle += 90;
                  }
                  return 'translate(' + labelsArc.centroid(d) + ') rotate(' + rotateAngle + ')';
                } else {
                  d.outerRadius = radius + 10; // Set Outer Coordinate
                  d.innerRadius = radius + 15; // Set Inner Coordinate
                  return 'translate(' + labelsArc.centroid(d) + ')'
                }
            });

          slices.each(function(d, i) {
            var slice = d3.select(this);

            slice
              .select(".nv-label text")
                .style('text-anchor', labelSunbeamLayout ? ((d.startAngle + d.endAngle) / 2 < Math.PI ? 'start' : 'end') : 'middle') //center the text on it's origin or begin/end if orthogonal aligned
                .text(function(d, i) {
                  var percent = (d.endAngle - d.startAngle) / (2 * Math.PI);
                  return (d.value && percent > labelThreshold) ? getX(d.data) : '';
                });

            var textBox = slice.select('text').node().getBBox();
            slice.select(".nv-label rect")
              .attr("width", textBox.width + 10)
              .attr("height", textBox.height + 10)
              .attr("transform", function() {
                return "translate(" + [textBox.x - 5, textBox.y - 5] + ")";
              });
          });
        }


        // Computes the angle of an arc, converting from radians to degrees.
        function angle(d) {
          var a = (d.startAngle + d.endAngle) * 90 / Math.PI - 90;
          return a > 90 ? a - 180 : a;
        }

        function arcTween(a) {
          a.endAngle = isNaN(a.endAngle) ? 0 : a.endAngle;
          a.startAngle = isNaN(a.startAngle) ? 0 : a.startAngle;
          if (!donut) a.innerRadius = 0;
          var i = d3.interpolate(this._current, a);
          this._current = i(0);
          return function(t) {
            return arc(i(t));
          };
        }

        function tweenPie(b) {
          b.innerRadius = 0;
          var i = d3.interpolate({startAngle: 0, endAngle: 0}, b);
          return function(t) {
              return arc(i(t));
          };
        }

    });

    return chart;
  }


  //============================================================
  // Expose Public Variables
  //------------------------------------------------------------

  chart.dispatch = dispatch;

  chart.margin = function(_) {
    if (!arguments.length) return margin;
    margin.top    = typeof _.top    != 'undefined' ? _.top    : margin.top;
    margin.right  = typeof _.right  != 'undefined' ? _.right  : margin.right;
    margin.bottom = typeof _.bottom != 'undefined' ? _.bottom : margin.bottom;
    margin.left   = typeof _.left   != 'undefined' ? _.left   : margin.left;
    return chart;
  };

  chart.width = function(_) {
    if (!arguments.length) return width;
    width = _;
    return chart;
  };

  chart.height = function(_) {
    if (!arguments.length) return height;
    height = _;
    return chart;
  };

  chart.values = function(_) {
    if (!arguments.length) return getValues;
    getValues = _;
    return chart;
  };

  chart.x = function(_) {
    if (!arguments.length) return getX;
    getX = _;
    return chart;
  };

  chart.y = function(_) {
    if (!arguments.length) return getY;
    getY = d3.functor(_);
    return chart;
  };
  
  chart.description = function(_) {
    if (!arguments.length) return getDescription;
    getDescription = _;
    return chart;
  };

  chart.showLabels = function(_) {
    if (!arguments.length) return showLabels;
    showLabels = _;
    return chart;
  };
  
  chart.labelSunbeamLayout = function(_) {
    if (!arguments.length) return labelSunbeamLayout;
    labelSunbeamLayout = _;
    return chart;
  };

  chart.donutLabelsOutside = function(_) {
    if (!arguments.length) return donutLabelsOutside;
    donutLabelsOutside = _;
    return chart;
  };
  
  chart.pieLabelsOutside = function(_) {
    if (!arguments.length) return pieLabelsOutside;
    pieLabelsOutside = _;
    return chart;
  };

  chart.donut = function(_) {
    if (!arguments.length) return donut;
    donut = _;
    return chart;
  };
  
  chart.donutRatio = function(_) {
    if (!arguments.length) return donutRatio;
    donutRatio = _;
    return chart;
  };

  chart.startAngle = function(_) {
    if (!arguments.length) return startAngle;
    startAngle = _;
    return chart;
  };

  chart.endAngle = function(_) {
    if (!arguments.length) return endAngle;
    endAngle = _;
    return chart;
  };

  chart.id = function(_) {
    if (!arguments.length) return id;
    id = _;
    return chart;
  };

  chart.color = function(_) {
    if (!arguments.length) return color;
    color = nv.utils.getColor(_);
    return chart;
  };

  chart.valueFormat = function(_) {
    if (!arguments.length) return valueFormat;
    valueFormat = _;
    return chart;
  };

  chart.labelThreshold = function(_) {
    if (!arguments.length) return labelThreshold;
    labelThreshold = _;
    return chart;
  };
  //============================================================


  return chart;
}
