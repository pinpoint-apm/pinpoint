var HubbleChartOptions = {
    getDefaultOptions:function () {
        return {
            legend:{
                show:false,
                noColumns:3
            },
            series:{
                lines:{
                    show:true,
                    steps:false,
                    fill:0.6,
                    shadow:false,
                    lineWidth:1,
                },
                shadowSize:0.7,
                points:{
                    show:true,
                    radius:1.0,
                    lineWidth:1,
                    fill:1,
                    fillColor:"rgba(100,149,237,0.3)"
                }
            },
            xaxis:{
                mode:"time"
            },
            yaxis:{
                // min : 0,
                // max : 1
                tickFormatter:function (v, axis) {
                    if (v >= 1000) {
                        return HubbleChartOptions.dataUnitFormatter(v, 0, false);
                    } else if (v <= 1) {
                        return HubbleChartOptions.dataUnitFormatter(v, 2, false);
                    } else {
                        return v;
                    }
                }
            },
            crosshair:{
                mode:"x",
                color:"rgba(0,0,0,0.5)",
                lineWidth:0.2
            },
            selection:{
                mode:"x"
            },
            grid:{
                hoverable:true,
                aboveData:false,
                clickable:true,
                autoHighlight:true,
                mouseActiveRadius:5,
                borderWidth:0.2,
                labelMargin:3,
                borderColor:null,
                markings:null,
                markingsColor:"#f4f4f4",
                markingsLineWidth:2,
                tickColor:"rgba(0,0,0,0.07)",
                backgroundColor:"rgba(0,0,0,0.03)"
            }
        };
    },

    dataUnitFormatter:function (data, belowZeroDigit, boolBytes) {
        postfix = "";
        var unit = boolBytes ? 1024 : 1000;

        if (data / (unit * unit * unit) >= 1) {
            data = data / (unit * unit * unit);
            postfix = " G";
        } else if (data / (unit * unit) >= 1) {
            data = data / (unit * unit);
            postfix = " M";
        } else if (data / unit >= 1) {
            data = data / unit;
            postfix = " K";
        }
        return data.toFixed(belowZeroDigit) + postfix; // 3 digit below zero
    }
};

/*
 * options.yaxis.tickFormatter = function(v, axis) { if (v >= 1000) { return
 * dataUnitFormatter(v, 0, false); // 0 digit // below // zero } else if (v <=
 * 1) { return dataUnitFormatter(v, 2, false); // 2 digit // below // zero }
 * else { return v; } }
 * 
 * options.series['tooltipDataFormatter'] = function(value) { if (value == null)
 * return "nodata"; return Math.round(value * 100) / 100 + "%"; }
 * 
 * options.legend.labelFormatter = function(datalabel, series,
 * urgentPrefixToTrim) { return "label"; }
 */