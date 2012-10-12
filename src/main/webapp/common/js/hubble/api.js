var hubble = {
    protocol:"http://",
    host:"localhost:8080",

    getGaugeData:function (query) {
        var url = this.protocol + this.host + "/getgaugedata.hubble?"
            + $.param(query);
        var result;
        try {
            $.ajax({
                async:false,
                url:url,
                cache:false,
                success:function (data, textStatus, xhr) {
                    result = $.parseJSON(data);
                },
                error:this.ajaxErrorHandler
            });
        } catch (e) {
            alert("[error] " + e);
        }
        return result;
    },

    getCountData:function (query) {
        var url = this.protocol + this.host + "/getcountdata.hubble?"
            + $.param(query);
        var result;
        try {
            $.ajax({
                async:false,
                url:url,
                cache:false,
                success:function (data, textStatus, xhr) {
                    result = $.parseJSON(data);
                },
                error:this.ajaxErrorHandler
            });
        } catch (e) {
            alert("[error] " + e);
        }
        return result;
    },

    chartData:function (data, placeholder, options, cb) {
        var plot = $.plot($(placeholder), [ data ], options);
    },

    createCount:function (uid, stat, cb) {
        var url = this.protocol + this.host + "/createcount.hubble?uid=" + uid
            + "&stat=" + stat;
        try {
            $.ajax({
                async:false,
                url:url,
                cache:false,
                success:function (data, textStatus, xhr) {
                    if (cb != undefined) {
                        cb($.parseJSON(data), textStatus, xhr);
                    } else {
                        alert($.parseJSON(data).message);
                    }
                },
                error:this.ajaxErrorHandler
            });
        } catch (e) {
            alert("[error] " + e);
        }
    },

    createGauge:function (uid, stat, cb) {
        var url = this.protocol + this.host + "/creategauge.hubble?uid=" + uid
            + "&stat=" + stat;
        try {
            $.ajax({
                async:false,
                url:url,
                cache:false,
                success:function (data, textStatus, xhr) {
                    if (cb != undefined) {
                        cb($.parseJSON(data), textStatus, xhr);
                    } else {
                        alert($.parseJSON(data).message);
                    }
                },
                error:this.ajaxErrorHandler
            });
        } catch (e) {
            alert("[error] " + e);
        }
    },

    putValue:function (uid, stat, value, cb) {
        var url = "/putvalue.hubble?uid=" + uid + "&stat=" + stat + "&value="
            + value;

//		if (tags != null && tags != undefined) {
//			url += $.param(query);
//		}

        $.ajax({
            async:false,
            url:url,
            cache:false,
            success:function (data, textStatus, xhr) {
                if (cb != undefined) {
                    cb($.parseJSON(data), textStatus, xhr);
                } else {
                    var result = $.parseJSON(data);
                    alert(result.message);
                }
            },
            error:this.ajaxErrorHandler
        });
    },

    ajaxErrorHandler:function (xhr, ajaxOptions, thrownError) {
        try {
            alert("[ERROR] " + xhr.statusText);
        } catch (e) {
            alert(e);
        }
    }
};
