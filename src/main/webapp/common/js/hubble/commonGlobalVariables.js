//##############################################################################
// global variable area
//##############################################################################

var DEBUG = false;

// key: hostFieldSetId map
// value: data for ajax request in host fieldset
var gHostDivAjaxRequestDataMap = {
    obj:new Object(),
    put:function (key, value) {
        this.obj[key] = value;
    },
    get:function (key) {
        return this.obj[key];
    }
}

// value: char metadata map
// key: graphUid
var gGraphMetaDataMap = {
    obj:new Object(),
    put:function (key, value) {
        this.obj[key] = value;
    },
    get:function (key) {
        return this.obj[key];
    }
}

// graph data is stored into this javascript hash map
// used when redrawing graph by clicking legend, zoom
// key: graph uid
// value: graph datalist
var gGraphDataMap = {
    obj:new Object(),
    put:function (key, value) {
        this.obj[key] = value;
    },
    get:function (key) {
        return this.obj[key];
    }
}

// value: graph data step interval (sec)
var gGraphDataStepIntervalMap = {
    obj:new Object(),
    put:function (key, value) {
        this.obj[key] = value;
    },
    get:function (key) {
        return this.obj[key];
    }
}
// key: graph uid
// value: M/S
var gGraphDataMergedGraphTypeMap = {
    obj:new Object(),
    put:function (key, value) {
        this.obj[key] = value;
    },
    get:function (key) {
        return this.obj[key];
    }
}

// key: graph uid
// value: plot object
var gGraphPlotMap = {
    obj:new Object(),
    put:function (key, value) {
        this.obj[key] = value;
    },
    get:function (key) {
        return this.obj[key];
    }
}

// key: graph uid
// value:flotoption Object
var gGraphFlotOptionMap = {
    obj:new Object(),
    put:function (key, value) {
        this.obj[key] = value;
    },
    get:function (key) {
        return this.obj[key];
    }
}

// uids which are ajax fetching in progress
// this is only for notification messages in fetching progress.
var gItemsCurrentlyFetching = {
    obj:new Queue(),
    add:function (item) {
        this.obj.enqueue(item);
    },
    del:function (item) {
        this.obj.dequeue(item);
    },
    isEmpty:function () {
        return this.obj.isEmpty();
    }
}

// value: delayDraw info
// if there are items in queue, autoRefresh skips.
// but clicking 'search' button, forcely clears all items in queue and continues
// search.
var gGraphDataToDelayDrawQueue = new Queue();
