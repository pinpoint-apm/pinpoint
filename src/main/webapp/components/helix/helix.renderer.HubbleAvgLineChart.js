helix.extendRenderer('avg_line', 'HubbleAvgLineChart', 'HubbleLineChart', function() {
    var container = d3.select(this.option.target),
        range = this.option.chart.y.range,
        allkeys = this.getKeys(),
        color = d3.scale.category20b().domain([0, allkeys.length - 1]);

    return {
        _getColor : function(index) {
            return "#666" //color(index);
        },
        _getAllData : function(keys) {
            var allData = [];
            for (var i = 0, len = this.data.time.length; i < len; i++) {
                allData[i] = 0;
            }

            for (var i = 0, len = keys.length; i < len; i++) {
                var tdata = this.getData(keys[i].trim(), 'value');
                for (var j = 0, len2 = tdata.length; j < len2; j++) {
                    allData[j] += tdata[j];
                }
            }

            for (var i = 0, len = allData.length; i < len; i++) {
                allData[i] = allData[i] / keys.length;
            }

            return allData;
        },
        _draw : function(keys) {
            keys = keys || allkeys;
            this._prepare();
            this._draw_desc(keys);
            this._draw_axis(keys);
            this._draw_chart(this._getAllData(keys));
            this._draw_bottom_line();
            this._setEventHandler();

            return container.html();
        },
        _destroy : function() {
            destroyNode(this.svg);
            destroyObject(this);
        }
    }
});