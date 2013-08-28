//TODO: consider deprecating and using multibar with single series for this
nv.models.historicalBar = function() {

  //============================================================
  // Public Variables with Default Settings
  //------------------------------------------------------------

  var margin = {top: 0, right: 0, bottom: 0, left: 0}
    , width = 960
    , height = 500
    , id = Math.floor(Math.random() * 10000) //Create semi-unique ID in case user doesn't select one
    , x = d3.scale.linear()
    , y = d3.scale.linear()
    , getX = function(d) { return d.x }
    , getY = function(d) { return d.y }
    , forceX = []
    , forceY = [0]
    , padData = false
    , clipEdge = true
    , color = nv.utils.defaultColor()
    , xDomain
    , yDomain
    , dispatch = d3.dispatch('chartClick', 'elementClick', 'elementDblClick', 'elementMouseover', 'elementMouseout')
    ;

  //============================================================


  function chart(selection) {
    selection.each(function(data) {
      var availableWidth = width - margin.left - margin.right,
          availableHeight = height - margin.top - margin.bottom,
          container = d3.select(this);


      //------------------------------------------------------------
      // Setup Scales

      x   .domain(xDomain || d3.extent(data[0].values.map(getX).concat(forceX) ))

      if (padData)
        x.range([availableWidth * .5 / data[0].values.length, availableWidth * (data[0].values.length - .5)  / data[0].values.length ]);
      else
        x.range([0, availableWidth]);

      y   .domain(yDomain || d3.extent(data[0].values.map(getY).concat(forceY) ))
          .range([availableHeight, 0]);

      // If scale's domain don't have a range, slightly adjust to make one... so a chart can show a single data point
      if (x.domain()[0] === x.domain()[1] || y.domain()[0] === y.domain()[1]) singlePoint = true;
      if (x.domain()[0] === x.domain()[1])
        x.domain()[0] ?
            x.domain([x.domain()[0] - x.domain()[0] * 0.01, x.domain()[1] + x.domain()[1] * 0.01])
          : x.domain([-1,1]);

      if (y.domain()[0] === y.domain()[1])
        y.domain()[0] ?
            y.domain([y.domain()[0] + y.domain()[0] * 0.01, y.domain()[1] - y.domain()[1] * 0.01])
          : y.domain([-1,1]);

      //------------------------------------------------------------


      //------------------------------------------------------------
      // Setup containers and skeleton of chart

      var wrap = container.selectAll('g.nv-wrap.nv-bar').data([data[0].values]);
      var wrapEnter = wrap.enter().append('g').attr('class', 'nvd3 nv-wrap nv-bar');
      var defsEnter = wrapEnter.append('defs');
      var gEnter = wrapEnter.append('g');
      var g = wrap.select('g');

      gEnter.append('g').attr('class', 'nv-bars');

      wrap.attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

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


      defsEnter.append('clipPath')
          .attr('id', 'nv-chart-clip-path-' + id)
        .append('rect');

      wrap.select('#nv-chart-clip-path-' + id + ' rect')
          .attr('width', availableWidth)
          .attr('height', availableHeight);

      g   .attr('clip-path', clipEdge ? 'url(#nv-chart-clip-path-' + id + ')' : '');



      var bars = wrap.select('.nv-bars').selectAll('.nv-bar')
          .data(function(d) { return d });

      bars.exit().remove();


      var barsEnter = bars.enter().append('rect')
          //.attr('class', function(d,i,j) { return (getY(d,i) < 0 ? 'nv-bar negative' : 'nv-bar positive') + ' nv-bar-' + j + '-' + i })
          .attr('x', 0 )
          .attr('y', function(d,i) {  return y(Math.max(0, getY(d,i))) })
          .attr('height', function(d,i) { return Math.abs(y(getY(d,i)) - y(0)) })
          .on('mouseover', function(d,i) {
            d3.select(this).classed('hover', true);
            dispatch.elementMouseover({
                point: d,
                series: data[0],
                pos: [x(getX(d,i)), y(getY(d,i))],  // TODO: Figure out why the value appears to be shifted
                pointIndex: i,
                seriesIndex: 0,
                e: d3.event
            });

          })
          .on('mouseout', function(d,i) {
                d3.select(this).classed('hover', false);
                dispatch.elementMouseout({
                    point: d,
                    series: data[0],
                    pointIndex: i,
                    seriesIndex: 0,
                    e: d3.event
                });
          })
          .on('click', function(d,i) {
                dispatch.elementClick({
                    //label: d[label],
                    value: getY(d,i),
                    data: d,
                    index: i,
                    pos: [x(getX(d,i)), y(getY(d,i))],
                    e: d3.event,
                    id: id
                });
              d3.event.stopPropagation();
          })
          .on('dblclick', function(d,i) {
              dispatch.elementDblClick({
                  //label: d[label],
                  value: getY(d,i),
                  data: d,
                  index: i,
                  pos: [x(getX(d,i)), y(getY(d,i))],
                  e: d3.event,
                  id: id
              });
              d3.event.stopPropagation();
          });

      bars
          .attr('fill', function(d,i) { return color(d, i); })
          .attr('class', function(d,i,j) { return (getY(d,i) < 0 ? 'nv-bar negative' : 'nv-bar positive') + ' nv-bar-' + j + '-' + i })
          .attr('transform', function(d,i) { return 'translate(' + (x(getX(d,i)) - availableWidth / data[0].values.length * .45) + ',0)'; })  //TODO: better width calculations that don't assume always uniform data spacing;w
          .attr('width', (availableWidth / data[0].values.length) * .9 )


      d3.transition(bars)
          //.attr('y', function(d,i) {  return y(Math.max(0, getY(d,i))) })
          .attr('y', function(d,i) {
            return getY(d,i) < 0 ?
                    y(0) :
                    y(0) - y(getY(d,i)) < 1 ?
                      y(0) - 1 :
                      y(getY(d,i))
          })
          .attr('height', function(d,i) { return Math.max(Math.abs(y(getY(d,i)) - y(0)),1) });
          //.order();  // not sure if this makes any sense for this model

    });

    return chart;
  }


  //============================================================
  // Expose Public Variables
  //------------------------------------------------------------

  chart.dispatch = dispatch;

  chart.x = function(_) {
    if (!arguments.length) return getX;
    getX = _;
    return chart;
  };

  chart.y = function(_) {
    if (!arguments.length) return getY;
    getY = _;
    return chart;
  };

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

  chart.xScale = function(_) {
    if (!arguments.length) return x;
    x = _;
    return chart;
  };

  chart.yScale = function(_) {
    if (!arguments.length) return y;
    y = _;
    return chart;
  };

  chart.xDomain = function(_) {
    if (!arguments.length) return xDomain;
    xDomain = _;
    return chart;
  };

  chart.yDomain = function(_) {
    if (!arguments.length) return yDomain;
    yDomain = _;
    return chart;
  };

  chart.forceX = function(_) {
    if (!arguments.length) return forceX;
    forceX = _;
    return chart;
  };

  chart.forceY = function(_) {
    if (!arguments.length) return forceY;
    forceY = _;
    return chart;
  };

  chart.padData = function(_) {
    if (!arguments.length) return padData;
    padData = _;
    return chart;
  };

  chart.clipEdge = function(_) {
    if (!arguments.length) return clipEdge;
    clipEdge = _;
    return chart;
  };

  chart.color = function(_) {
    if (!arguments.length) return color;
    color = nv.utils.getColor(_);
    return chart;
  };

  chart.id = function(_) {
    if (!arguments.length) return id;
    id = _;
    return chart;
  };

  //============================================================


  return chart;
}
