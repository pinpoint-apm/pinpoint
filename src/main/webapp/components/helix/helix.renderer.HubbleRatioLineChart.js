helix.extendRenderer('ratio_line', 'HubbleRatioLineChart', 'HubbleLineChart', function() {
    var container = d3.select(this.option.target),
        range = this.option.chart.y.range,
        allkeys = this.getKeys(),
        color = d3.scale.category20b().domain([0, allkeys.length - 1]);

    return {
        _getColor : function(index) {
            //return "#666"
            return color(index);
        },
        _getAllData : function(keys) {
            var allData = [];
            for (var i = 0, len = this.data.time.length; i < len; i++) {
                allData[i] = 0;
            }

            // the number of keys should be 2
            if (keys.length != 2) {
                console.log('[ratio_line] number of keys should be 2', keys);
                return allData;
            }

            var tdata1 = this.getData(keys[0].trim(), 'value');
            var tdata2 = this.getData(keys[1].trim(), 'value');
            var len = Math.min(tdata1.length, tdata2.length);
            for (var i = 0; i < len; i++) {
                // e.g. hit ratio = hit / hit + miss
                var ratio = (tdata1[i] / (tdata1[i] + tdata2[i])) * 100;
                if (! isNaN(ratio)) {
                    allData[i] = ratio;
                }
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