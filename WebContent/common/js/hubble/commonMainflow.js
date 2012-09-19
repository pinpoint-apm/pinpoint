/*
 coded by minseok 2010.04.
 changes in 'jquery.flot_noconflict.js' at 2709 line  :  $J.plot.formatDate UTC time to local timezone by minseok
 */
var commonMainflow = {
    isDashboard: false,
    isMonitorDashboard: false, // if false, doesn't create fieldset because of UI space.
    DELAYDRAWGRAPH_INTERVAL: 100,
    // fetched json data's resolution(step)
    GRAPHDATA_RESOLUTION_BASIC: 300,
    GRAPHDATA_RESOLUTION_DETAIL: 5,
    gGraphDataResolution: this.GRAPHDATA_RESOLUTION_BASIC,
    ERROR_PREFIX: "ERROR",
    INFORM_PREFIX: "INFORM",
    gRefreshChartMsgDiv: null,
    gMergedGraphTypeS: "S",
    gMergedGraphTypeM: "M",
    gMergedGraphTypeMS: "MS",
    gMergedGraphTypePA: "PA",	
	DRAW_TOGETHER_TYPE_AVG:1,
	DRAW_TOGETHER_TYPE_MAX:2,
	DRAW_TOGETHER_TYPE_MAXAVG:3,
	
    
    // whole data from /hubble/monitor/getHostCateFactorList
    // stored hostFactor data, reused when graphing
    // commonCode, it is setted in commonCode.js
    gFactorCateDataJson: null,
    gSelFactorCateList: null,
    gTimeParamsStr: null,
    // for delay loading.
    gDrawEachGraphFromQueue_EmptyQueueFoundCount: 0,
    gDrawEachGraphFromQueueTimeout: null,
    // drawing graph status
    gLoadingComplete: true,
    
    //do beginning search Process
    doPreparingProcess: function(){
    
        commonMainflow.initAllFlags();
        
        UIControl.displayTimePeriodGap();
        UIControl.saveTimePeriodToGlobal();
        
        // show progress animation.
        flotControl.gProgressAnimationShowAllTimeout = setTimeout(flotControl.progressAnimationShowAll, 100); // this has a bugs that not hided progress images after loading completes at first loading page.
        // reset zoomed graphs.
        flotControl.gResetZoomActionTimeout = setTimeout(flotControl.resetZoomLabelClickEventAction_allgraphs, 100); // not works when the graph excanvas is invisable
        //flotControl.resetZoomLabelClickEventAction_allgraphs();
        //flotControl.progressAnimationShowAll(); // if this is activated, malfunctioning bugs exists in dashboard delete.(because  gVisibleHostObj is empty initially.)
        UIControl.notifyLoadingMsgInProgress(); // loading in progress.
    },
    
    initAllFlags: function(){
    
        commonMainflow.gLoadingComplete = true;
        flotControl.gVisibleFactorObjs = null;
        flotControl.gVisibleFactorHostObjs = null;
        commonMainflow.clearGraphDataToDelayDrawQueue();
    },
    
    clearGraphDataToDelayDrawQueue: function(){
    
        // remove remains in queue
        while (gGraphDataToDelayDrawQueue.getSize() > 0) {
            gGraphDataToDelayDrawQueue.dequeue();
        }
        clearInterval(commonMainflow.gDrawEachGraphFromQueueTimeout);
        commonMainflow.gDrawEachGraphFromQueueTimeout = null;
        commonMainflow.gDrawEachGraphFromQueue_EmptyQueueFoundCount = 0;
        if (DEBUG) console.log("clearGraphDataToDelayDrawQueue() empty, cleared setInterval :", commonMainflow.gDrawEachGraphFromQueueTimeout);
        
        
    },
    
    
    //when complete init all flags.
    checkDelayedDrawQueueAndComplete: function(){
        // even though queue is empty, check two times more to complete finally.
		// gItemsCurrentlyFetching.isEmpty is to check an item which is ajax requested but not responded yet.
        if (gItemsCurrentlyFetching.isEmpty() && gGraphDataToDelayDrawQueue.isEmpty() && commonMainflow.gDrawEachGraphFromQueue_EmptyQueueFoundCount > 2) {
            if (DEBUG) console.log("doEndingProcess() starting... ");
            commonMainflow.gLoadingComplete = true;
            commonMainflow.clearGraphDataToDelayDrawQueue();
            // hides all remains progress images.
            flotControl.gProgressAnimationHideAllTimeout = setTimeout(flotControl.progressAnimationHideAll, 100);
            
            var date = new Date();
            UIControl.refreshChartMsg("last loaded:" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
            UIControl.notifyLoadingMsgComplete();
            return true;
        } else {
            commonMainflow.gLoadingComplete = false;
            UIControl.notifyLoadingMsgInProgress();
            return false;
        }
        
    },
    
    determinToDrawGraph: function(graphUid){
        var drawGraph = true;
        // first load or click by search button, then draw all graphs.
        if (!UIControl.gBoolAutoRefreshChart) {
            if (DEBUG) console.log("first or search button, drawing graph ", graphUid)
            drawGraph = true;
        } else {// when auto refreshing, check visibility.
            if (!flotControl.isGraphVisible(graphUid)) {
                if (DEBUG) console.log("auto refreshing, notvisible ", graphUid)
                drawGraph = false;
            } else {
                if (DEBUG) console.log("auto refreshing , visible graph ", graphUid)
                drawGraph = true;
            }
        }
        return drawGraph;
    },
    //decide to expand the accordian(hostFieldSetId) and to request graph data via ajax
    determinToExpand: function(hostFieldSetId, isFirstDiv, isAutoRefresh){
    
        if (DEBUG) console.log('#commonMainflow.determinToExpand>', UIControl.gCurrentExpandHostFieldSetId);
        if (DEBUG) console.log('#commonMainflow.determinToExpand>', UIControl.gCurrentExpandHostFieldSetId_hostName);
        
        
        if (UIControl.gCurrentExpandHostFieldSetId != null) {
            if (UIControl.gCurrentExpandHostFieldSetId == hostFieldSetId) {
                if (DEBUG) console.log('#commonMainflow.determinToExpand>result: true, UIControl.gCurrentExpandHostFieldSetId==', hostFieldSetId);
                return true;
            }
        }
        
        if (UIControl.gCurrentExpandHostFieldSetId_hostName != null) {
            if (gfnEndsWith(hostFieldSetId, UIControl.gCurrentExpandHostFieldSetId_hostName)) {
                if (DEBUG) console.log('#commonMainflow.determinToExpand>result: true, UIControl.gCurrentExpandHostFieldSetId_hostName==', hostFieldSetId);
                return true;
            } else {
                if (DEBUG) console.log('#commonMainflow.determinToExpand>result: false, UIControl.gCurrentExpandHostFieldSetId_hostName exists');
                return false;
            }
        }
        
        var hostFieldSetDiv = jQuery("#" + hostFieldSetId);
        var isExist = hostFieldSetDiv != undefined && hostFieldSetDiv.length >= 1;
        
        var result = false;
        if (isExist) {
            var hostFieldSetDiv_a = jQuery("a[id='" + makeHostFieldsetId_a(hostFieldSetId) + "']");
            var expand = hostFieldSetDiv_a.attr("class");
            if (expand == "expand") {
                result = true;
            } else {
            
                if (isFirstDiv) {
                    if (isAutoRefresh) {
                        result = false;
                    } else {
                        result = true;
                    }
                } else {
                    result = false;
                }
            }
        } else {
            result = false;
        }
        if (DEBUG) console.log('#commonMainflow.determinToExpand>result:', result, ', hostFieldSetId:', hostFieldSetId, ',isExist(', isExist, '),expand(', expand, '),isFirstDiv(', isFirstDiv, '),isAutoRefresh(', isAutoRefresh, ')');
        return result;
    },
    checkHostListExist: function(){
        if (!isValidParentWindow()) return true; // TODO 
        var hostList = parent.topframe.gHostList;
        
        if (hostList == null || hostList.length <= 0) {
            //UIControl.notifyMsg("No host list found, see server settings");
            UIControl.displayMessageGraphTopDiv("No host list found <br> check server settings");
            return false;
        }
        return true;
    },
    checkToContinueSearch: function(){
        if (!commonMainflow.checkHostListExist()) return false;
        
        
        if (!commonMainflow.gLoadingComplete) { // ##############
            if (DEBUG) console.log("can't start update graph, previous loading progress");
            return false;
        }
        return true;
    },
    //http://oss.oetiker.ch/rrdtool/doc/rrdfetch.en.html
    determinGraphDataResolution: function(){
        // first use global variable;
        var step = commonMainflow.gGraphDataResolution;
        
        timeControl.getDate();
        var interval = timeControl.toTimeValue - timeControl.fromTimeValue;
        var xSec = 1000;
        var xMin = xSec * 60;
        var intervalMin = Math.abs(Math.floor(interval / xMin));
        
        if (intervalMin <= 60) {//less than 1hour
            step = commonMainflow.GRAPHDATA_RESOLUTION_DETAIL;
        } else step = commonMainflow.GRAPHDATA_RESOLUTION_BASIC;
        
        if (DEBUG) console.log("determinGraphDataResolution intervalMin:" + intervalMin + " /step:" + step);
        return step;
    },
    
    //##############################################################
    // common functions after fetching data    
    //drawing graph hostname, factorname
    
    fetchGraphData: function(graphUid, dataString){
        gItemsCurrentlyFetching.add(graphUid)
        jQuery.ajax({
            async: true,
            url: "/hubble/monitor/getGraphData/",
            method: 'POST',
            data: dataString,
            dataType: 'json',
            success: commonMainflow.onSuccessFetchData,
            error: commonMainflow.onErrorFetchData
        });
    },
    
    
    
    // processing fetchedData
    storeFetchedData: function(series, graphUid){
        gGraphDataMap.put(graphUid, series.rrdDataList);
        gGraphDataStepIntervalMap.put(graphUid, series.stepInterval);
        gGraphDataMergedGraphTypeMap.put(graphUid, series.mergeGraphType);
    },
    
    
    // then fetch the data using jQuery ajax
    onSuccessFetchData: function(series){
    
        gItemsCurrentlyFetching.del(graphUid)
        
        var graphUid = series.graphUid;
        var metaDataObj = gGraphMetaDataMap.get(graphUid);
        var hostName = metaDataObj['hostName'];
        var factorCateName = metaDataObj['factorCateName'];
        var factorDirName = metaDataObj['factorDirName'];
        var fileName = metaDataObj['fileName'];
        var graphGroupDivId = metaDataObj['graphGroupDivId'];
        
        
        if (series == null) {
            var msg = commonMainflow.ERROR_PREFIX + "Ok(200) Response. but, Fetching failed";
            msg += "<br>folder:<b>" + hostName + "/" + factorDirName + "</b>";
            gGraphDataMap.put(graphUid, msg);
        } else if (series.rrdDataList.length <= 0) {
            var msg = commonMainflow.ERROR_PREFIX + "Ok(200) Response. but, Fetched data is empty.";
            msg += "<br>check the folder existance in server.";
            msg += "<br>folder:<b>" + hostName + "/" + factorDirName + "</b>";
            gGraphDataMap.put(graphUid, msg);
        } else {
            commonMainflow.storeFetchedData(series, graphUid);
        }
        
        commonMainflow.drawGraph(graphUid, hostName, factorCateName, factorDirName, fileName, graphGroupDivId);
        
    },
    
    validateFetchedData: function(series){
        if (series.rrdDataList.length > 0) {
            return true;
        }
        return false;
    },
    
    onErrorFetchData: function(xhr, textevent, thrownError){
    
        gItemsCurrentlyFetching.del(graphUid)
        // get graphUid
        var reqUrl = this.url;
        var graphUid = reqUrl.substr(reqUrl.indexOf("graphUid=") + "graphUid=".length);
        if (graphUid.indexOf("&") > 0) {
            graphUid = graphUid.substring(0, graphUid.indexOf("&"));
        }
        var metaDataObj = gGraphMetaDataMap.get(graphUid);
        var hostName = metaDataObj['hostName'];
        var factorCateName = metaDataObj['factorCateName'];
        var factorDirName = metaDataObj['factorDirName'];
        var fileName = metaDataObj['fileName'];
        var graphGroupDivId = metaDataObj['graphGroupDivId'];
        
        //var msg = commonMainflow.ERROR_PREFIX + "Failure while fetching data:";
		var msg = commonMainflow.ERROR_PREFIX + "unable to fetch data:";
        msg += "<br>" + xhr.responseText;
        gGraphDataMap.put(graphUid, msg);
        commonMainflow.drawGraph(graphUid, hostName, factorCateName, factorDirName, fileName, graphGroupDivId);
        
    },
    
    //activate draw function.
    activateDrawGraphInterval: function(){
    
        if (!commonMainflow.gDrawEachGraphFromQueueTimeout) {
            commonMainflow.gDrawEachGraphFromQueueTimeout = setInterval(commonMainflow.drawEachGraphFromQueue, commonMainflow.DELAYDRAWGRAPH_INTERVAL);
            if (DEBUG) console.log("activateDrawGraphInterval() activating ", commonMainflow.gDrawEachGraphFromQueueTimeout);
        } else {
            if (DEBUG) console.log("activateDrawGraphInterval() already started. ", commonMainflow.gDrawEachGraphFromQueueTimeout);
        }
    },
    
    gBoolFirstDrawGraphGroupRequest: true,
    //drawing graph hostname, factorname
    //if fail  display error and return.
    drawGraph: function(graphUid, hostName, factorCateName, factorDirName, fileName, graphGroupDivId){
    
        if (commonMainflow.gBoolFirstDrawGraphGroupRequest) {// first draw request, draw.
            if (DEBUG) console.log("drawGraph() first draw request, drawing ");
            commonMainflow.gBoolFirstDrawGraphGroupRequest = false;
            commonMainflow.drawGraphAction(graphUid, hostName, factorCateName, factorDirName, fileName, graphGroupDivId);
        } else {
            commonMainflow.drawGraph_enqueue(graphUid, hostName, factorCateName, factorDirName, fileName, graphGroupDivId);
        }
        // if there is no enque graphs, dequeue progress not started. so starts here forcely.
        commonMainflow.activateDrawGraphInterval();
    },
    
    drawGraph_enqueue: function(graphUid, hostName, factorCateName, factorDirName, fileName, graphGroupDivId){
        var factorFieldsetId = makeFactorFieldsetId(factorDirName, hostName);
        
        var metaDataObj = new Object();
        metaDataObj['graphUid'] = graphUid;
        metaDataObj['fileName'] = fileName;
        metaDataObj['hostName'] = hostName;
        metaDataObj['factorCateName'] = factorCateName;
        metaDataObj['factorDirName'] = factorDirName;
        metaDataObj['graphGroupDivId'] = graphGroupDivId;
        
        if (DEBUG && (graphGroupDivId == null || graphGroupDivId == undefined)) console.trace()
        
        gGraphDataToDelayDrawQueue.enqueue(metaDataObj);
        
        if (DEBUG) console.log("drawGraph_enqueue(), queue count", gGraphDataToDelayDrawQueue.getSize());
        commonMainflow.activateDrawGraphInterval();
    },
    
    
    //not drawing right now, but when clicked factorgroup, then dall darwGraph();
    drawEachGraphFromQueue: function(){
    
        if (DEBUG) console.log("drawEachGraphFromQueue() starting ....., queue count ", gGraphDataToDelayDrawQueue.getSize());
        
        if (gGraphDataToDelayDrawQueue.isEmpty()) {
            commonMainflow.gDrawEachGraphFromQueue_EmptyQueueFoundCount++;
            commonMainflow.checkDelayedDrawQueueAndComplete();
            return;
        }
        
        UIControl.notifyLoadingMsgInProgress();
        
        var metaDataObj = gGraphDataToDelayDrawQueue.dequeue();
        var graphUid = metaDataObj['graphUid'];
        var fileName = metaDataObj['fileName'];
        var hostName = metaDataObj['hostName'];
        var factorCateName = metaDataObj['factorCateName'];
        var factorDirName = metaDataObj['factorDirName'];
        var graphGroupDivId = metaDataObj['graphGroupDivId'];
        commonMainflow.drawGraphAction(graphUid, hostName, factorCateName, factorDirName, fileName, graphGroupDivId);
        
        
        if (DEBUG) console.log("drawEachGraphFromQueue() deque() done, queue count", gGraphDataToDelayDrawQueue.getSize());
        
    },
    
    
    drawGraphAction: function(graphUid, hostName, factorCateName, factorDirName, fileName, graphGroupDivId){
        if (DEBUG) console.log("drawGraphAction() starting.... ", graphUid);
        var graphUid_DEBUG = graphUid;
        var graphGroupDiv = jQuery("#" + graphGroupDivId);
        var dataset = gGraphDataMap.get(graphUid);
        var atLeastOneGraphForHost = false;
        
        if (gGraphDataStepIntervalMap.get(graphUid) != 'none') UIControl.stepIntervalMsg("interval " + gGraphDataStepIntervalMap.get(graphUid) + " sec");
        
        if (commonMainflow.isDashboard) {
            var atLeastOneGraph = false;
            if (isBarGraphUid(graphUid)) {// prefix aggregate bar chart.
                if (commonMainflow.drawGraphActionPrefixAggregate(hostName, factorCateName, factorDirName, graphGroupDivId, graphGroupDiv, graphUid, dataset)) atLeastOneGraph = true;
            } else {
                // in dashboard view, just using stoard information to display graphs.
                var boolMergedGraph = isMergedGraphUid(graphUid);//###############################################
                if (boolMergedGraph) {
                    if (commonMainflow.drawGraphActionMerged(hostName, factorCateName, factorDirName, graphGroupDivId, graphGroupDiv, graphUid, dataset)) atLeastOneGraph = true;
                } else {
                    if (commonMainflow.drawGraphActionSplited(hostName, factorCateName, factorDirName, graphGroupDivId, graphGroupDiv, graphUid, dataset)) atLeastOneGraph = true;
                }
            }
            atLeastOneGraphForHost = atLeastOneGraph;
        } else {
            var atLeastOneGraph = false;
            var mergedGraphType = getMergedGraphType(graphUid);
            
            // if split graph
            if (mergedGraphType == commonMainflow.gMergedGraphTypeM) {
                if (commonMainflow.drawGraphActionMerged(hostName, factorCateName, factorDirName, graphGroupDivId, graphGroupDiv, graphUid, dataset)) atLeastOneGraph = true;
            } else if (mergedGraphType == commonMainflow.gMergedGraphTypeMS) {
                if (commonMainflow.drawGraphActionMerged(hostName, factorCateName, factorDirName, graphGroupDivId, graphGroupDiv, graphUid, dataset)) atLeastOneGraph = true;
                if (drawGraphActionSplited(hostName, factorCateName, factorDirName, graphGroupDivId, graphGroupDiv, graphUid, dataset)) atLeastOneGraph = true;
            } else if (mergedGraphType == commonMainflow.gMergedGraphTypePA) {// prefix aggregate bar chart.
                if (commonMainflow.drawGraphActionPrefixAggregate(hostName, factorCateName, factorDirName, graphGroupDivId, graphGroupDiv, graphUid, dataset)) atLeastOneGraph = true;
            } else {
                if (commonMainflow.drawGraphActionSplited(hostName, factorCateName, factorDirName, graphGroupDivId, graphGroupDiv, graphUid, dataset)) atLeastOneGraph = true;
            }
            atLeastOneGraphForHost = atLeastOneGraph;
        }
        
        // notify if there is no graph for this host.
        if (!atLeastOneGraphForHost) {
        
            var msg = commonMainflow.ERROR_PREFIX + "No matching graph data received for graphUid";
            msg += "<br>graphUid:" + graphUid;
            msg += "<br>host:" + hostName;
            msg += "<br>factor Category:" + factorCateName;
            msg += "<br>factor Directory:" + factorDirName;
            msg += "<br>factor filename:" + fileName;
            
            if (commonMainflow.checkAndDisplayMessage(graphUid, msg, graphGroupDiv)) return true;
            
        }
        
    },
    
    drawGraphActionPrefixAggregate: function(hostName, factorCateName, factorDirName, graphGroupDivId, graphGroupDiv, graphUid, dataset){
    
        var factorFieldsetDiv = commonMainflow.prepareFactorFieldsetDiv(hostName, factorDirName, graphGroupDiv);
        if (commonMainflow.checkAndDisplayMessage(graphUid, dataset, factorFieldsetDiv)) {// return when displaying msg.
            return true;
        }
        var paGraphUid = makeGraphUidPrefixAggregate(hostName, factorCateName, factorDirName);
        gGraphDataMap.put(paGraphUid, dataset);
        var graphGroupDivIdNew = commonMainflow.appendGraphGroupDivHtml(paGraphUid, factorFieldsetDiv)
        var graphGroupDivNew = jQuery("#" + graphGroupDivIdNew);
        return commonMainflow.drawPrefixAggregateGraph(paGraphUid, hostName, factorCateName, factorDirName, graphGroupDivNew, dataset);
        
    },
    
    
    // return: false if only display message. true if successfully finished.
    drawGraphActionMerged: function(hostName, factorCateName, factorDirName, graphGroupDivId, graphGroupDiv, graphUid, dataset){
        var factorFieldsetDiv = commonMainflow.prepareFactorFieldsetDiv(hostName, factorDirName, graphGroupDiv);
        if (commonMainflow.checkAndDisplayMessage(graphUid, dataset, factorFieldsetDiv)) {// return when displaying msg.
            return true;
        }
        
        var mergedGraphUid = makeGraphUidMerged(hostName, factorCateName, factorDirName);
        gGraphDataMap.put(mergedGraphUid, dataset);
        var graphGroupDivIdMerged = commonMainflow.appendGraphGroupDivHtml(mergedGraphUid, factorFieldsetDiv)
        var graphGroupDivMerged = jQuery("#" + graphGroupDivIdMerged);
        return commonMainflow.drawMergedGraph(mergedGraphUid, hostName, factorCateName, factorDirName, graphGroupDivMerged, dataset);
        
        
    },
    
    prepareFactorFieldsetDiv: function(hostName, factorDirName, graphGroupDiv){
    
    
        if (commonMainflow.isMonitorDashboard) {// if monitor dashboard, don't create fieldset.because of UI space.
            return graphGroupDiv;
        }
        var factorFieldsetId = makeFactorFieldsetId(factorDirName, hostName);
        // reusing old message div.
        if (!isElementExistInContainer(graphGroupDiv, "#" + factorFieldsetId)) {
            graphGroupDiv.append(commonMainflow.makeFieldSetFactorHtml(hostName, factorDirName, factorFieldsetId));
        }
        var factorFieldsetDiv = jQuery("#" + factorFieldsetId);
        return factorFieldsetDiv;
        
    },
    
    //return: false if only display message. true if successfully finished.
    // return true if displayed something.
    drawGraphActionSplited: function(hostName, factorCateName, factorDirName, graphGroupDivId, graphGroupDiv, graphUid, dataset){
    
        var factorFieldsetDiv = commonMainflow.prepareFactorFieldsetDiv(hostName, factorDirName, graphGroupDiv);
        if (commonMainflow.checkAndDisplayMessage(graphUid, dataset, factorFieldsetDiv)) return true;
        
        var atLeastOneGraphForHost = false;
        var lineDataset = null;
        var newFactorDirName = null;
        var newSplitedGraphUid = null;
        var newDataset = null;
        // need to hold max line in one dataset.
        var rrdFetchLevel = UIControl.getSelectedRrdFetchLevel();
        var drawTogetherType = 1;// 1=avg, 2=max, 3=maxavg
        if (rrdFetchLevel == 'MAXAVG' && factorCateName != 'QoS') { //# QoS has only MAX  
            drawTogetherType = 3;
        } else if (rrdFetchLevel == 'MAX') {
            drawTogetherType = 2;
        }
        
        for (var i = 0; i < dataset.length; i++) {
            // actually, response is directory base if filename is not specified, so needs to pick out dataset to draw.
            if (commonMainflow.checkOperationMessageBeforeGraphing(graphUid, dataset, factorFieldsetDiv)) return true;// displayed charts.		
            newSplitedGraphUid = makeGraphUidSingleLine(graphUid, hostName, factorCateName, factorDirName, dataset[i].label);
            if (commonMainflow.isDashboard && newSplitedGraphUid != graphUid) { //######################################
                if (DEBUG) console.log("graphUid comparison, not matching  graphUid(requested):", graphUid, ", newSplitedGraphUid(new by label):", newSplitedGraphUid)
                continue;
            }
            // deep copy	    		
            var lineDataset = Array();//flot libray requires array object.
            // AVG dataset will be followed by MAX dataset.		
            if (drawTogetherType == 2 || drawTogetherType == 3) {
                var lineDatasetCopyMax = ObjectHandler.getCloneOfObject(dataset[i]);
                if (lineDatasetCopyMax != null) {
                    lineDatasetCopyMax = flotControl.setFlotOptionToDataForMaxLine(lineDatasetCopyMax);
                    lineDataset.push(lineDatasetCopyMax);
                }
            }
            if (drawTogetherType == 3) i++;
            
            if (drawTogetherType == 1 || drawTogetherType == 3) {
                var lineDatasetCopyAvg = ObjectHandler.getCloneOfObject(dataset[i]);
                if (lineDatasetCopyAvg != null) {
                    lineDatasetCopyAvg = flotControl.setFlotOptionToDataForAvgLine(lineDatasetCopyAvg);
                    lineDataset.push(lineDatasetCopyAvg);
                }
            }
            
            gGraphDataMap.put(newSplitedGraphUid, lineDataset);
            gGraphDataMergedGraphTypeMap.put(newSplitedGraphUid, commonMainflow.gMergedGraphTypeS);
            
             
			currentDrawResult= commonMainflow.drawSingleLineGraph(newSplitedGraphUid, hostName, factorCateName, lineDataset[0].label, factorFieldsetDiv, lineDataset);
			atLeastOneGraphForHost = atLeastOneGraphForHost || currentDrawResult;
        }
        return atLeastOneGraphForHost;
    },
    
    // return true if displayed something.
    checkAndDisplayMessage: function(graphUid, dataset, graphGroupDiv){
    
        // fetching and checking data
        // if fail  display error and return.
        if (commonMainflow.checkDataInformBeforeGraphing(graphUid, dataset, graphGroupDiv)) return true;
        if (commonMainflow.checkDataErrorBeforeGraphing(graphUid, dataset, graphGroupDiv)) return true;
        
        return false;
    },
    
    // return true if displayed something.
    checkDataInformBeforeGraphing: function(graphUid, dataset, graphGroupDiv){
        if (typeof dataset == "string" && gfnStartsWith(dataset, commonMainflow.INFORM_PREFIX)) {
            prefixlen = commonMainflow.INFORM_PREFIX.length
            message = dataset.substring(prefixlen, dataset.length)
            displayMessage(graphUid, message, graphGroupDiv);
            return true;
        }
        return false;
    },
    
    
    // return true if displayed something.
    checkDataErrorBeforeGraphing: function(graphUid, dataset, graphGroupDiv){
        if (typeof dataset == "string" && gfnStartsWith(dataset, commonMainflow.ERROR_PREFIX)) {
            message = dataset.split(commonMainflow.ERROR_PREFIX).join('');
            displayMessage(graphUid, message, graphGroupDiv);
            return true;
        }
        return false;
    },
    
    
    // return true if displayed something.
    checkOperationMessageBeforeGraphing: function(graphUid, dataset, graphDiv){
    
        if (typeof dataset == "object" && dataset.length > 0) {
            // if error message in dataset, display message.
            var message = dataset[0].operationMessage + ""; // to string.
            //if( message!='' && message.startsWith(commonMainflow.ERROR_PREFIX)){
            if (message != '' && gfnStartsWith(message, commonMainflow.ERROR_PREFIX)) {
                message = commonMainflow.ERROR_PREFIX + "<br>" + message;
                if (commonMainflow.checkAndDisplayMessage(graphUid, message, graphDiv)) {// return when displaying msg.
                    return true;
                }
            }
        }
        return false;
    },
    
    //remove graph div and create one
    appendFactorGraphDiv: function(factorGraphDivId, selFactorCate, clear){
    
        var factorGraphDivId_a = makeFactorGraphDivId_a(factorGraphDivId);
        if (clear != undefined && clear) {
            //removeElement("factorGraphDivGroupClass"+factorGraphDivId);
            var obj = jQuery("div").find(".factorGraphDivGroupClass");
            if (obj.length > 0) obj.remove();
        }
        
        if (!isElementExist(factorGraphDivId)) {
            // add div into 'graph_top_div'
            var graphTopTiv = jQuery("#graph_top_div");
            var onclickScript = 'javascript:UIControl.divToggle(jQuery(\'div[id=' + factorGraphDivId + ']\')[0],this);'; // for jQuery put [0] at the end !!!
            //	graphTopTiv.unbind();
            //	graphTopTiv.empty();
            //graphTopTiv.html("");
            // leak 발생 //graphTopTiv.prepend('<h4><A class="expand" onclick="'+onclickScript+'" id="'+makeFactorGraphDivId_a(factorGraphDivId)+'">'+selFactorCate.toUpperCase()+'</A></h4><div id="'+factorGraphDivId+'"></div>');
            if (selFactorCate != null) selFactorCate = selFactorCate.toUpperCase();
            if (commonMainflow.isDashboard) { // float for factor label.
                graphTopTiv.append('<div class="factorGraphDivGroupClassForDashboard" > <h4><A class="expand" onclick="' + onclickScript + '" id="' + makeFactorGraphDivId_a(factorGraphDivId) + '">' + selFactorCate + '</A></h4><div id="' + factorGraphDivId + '" style="display: inline;" ></div></div>'); //###################
            } else {
                graphTopTiv.append('<div class="factorGraphDivGroupClass" id="factorGraphDivGroupClass' + factorGraphDivId + '"> <h4><A class="expand" onclick="' + onclickScript + '" id="' + makeFactorGraphDivId_a(factorGraphDivId) + '">' + selFactorCate + '</A></h4><div id="' + factorGraphDivId + '"></div></div>');
            }
        }
        return jQuery("#" + factorGraphDivId);
    },
    
    
    hideAllAndShowCurrentFactorGraphDiv: function(factorGraphDivId){
        // hide
        var targetObjs = jQuery("div").find(".factorGraphDivGroupClass");
        targetObjs.css("display", "none");//hide
        // and collapse
        UIControl.collapseAll();
        
        // show
        var objToshow = jQuery('#factorGraphDivGroupClass' + factorGraphDivId)
        objToshow.css("display", "");//show
        // and expand.
        UIControl.factorGraphDiv_toggleById(factorGraphDivId, true)
    },
    
    
    appendGraphGroupDivHtml_sub: function(graphUid, fieldSetDiv, isDivClassInline){
        var graphGroupDivId = makeGraphDivId(graphUid);
        if (!isElementExist(graphGroupDivId)) {
            if (DEBUG) console.log("not exist in the container, adding " + graphGroupDivId);
            fieldSetDiv.append(commonMainflow.makeGraphGroupDivHtml(graphGroupDivId, isDivClassInline));
        }
        return graphGroupDivId;
    },
    
    appendGraphGroupDivHtml2: function(graphUid, fieldSetDiv, isDivClassInline){
        return commonMainflow.appendGraphGroupDivHtml_sub(graphUid, fieldSetDiv, isDivClassInline)
    },
    
    appendGraphGroupDivHtml: function(graphUid, fieldSetDiv){
        var isDivClassInline = UIControl.isDisplayHostAlignhorizontal();
        return commonMainflow.appendGraphGroupDivHtml_sub(graphUid, fieldSetDiv, isDivClassInline)
    },
    
    
    
    appendGraphDivHtml: function(graphUid, fieldSetDiv){
        var graphDivId = makeGraphDivId(graphUid);
        if (!isElementExist(graphDivId)) {
            if (DEBUG) console.log("not exist in the container, adding " + graphDivId);
            fieldSetDiv.append(commonMainflow.makeGraphDivHtml(graphDivId));
        }
        return graphDivId;
        
    },
    // may not used.
    removeHostFieldsetHtmlCollapsable: function(fieldsetId){
        removeElement(commonMainflow.makeHostFieldsetId_upperFieldsetId('graph_fieldset_host_id'));
    },
    
    removeHostFieldsetHtmlCollapsable_upper: function(fieldsetId){
        removeElement(commonMainflow.makeHostFieldsetId_upperFieldsetId(fieldsetId));
    },
    
    makeHostFieldsetId_upperFieldsetId: function(fieldsetId){
        return "upper_" + fieldsetId;
    },
    makeHostFieldsetHtmlCollapsable: function(hostName, fieldsetId){
        var style = 'display:inline';
        if (UIControl.isDisplayHostAlignhorizontal()) {
            style = '';
        }
		 // remove 'all' prefix
        hostNameToShow = commonUtils.trimTextMiddlefix(hostName, 'all_');
        
        var hostNameTobeComparedLater = replaceSpecialCharactorsAsaID(hostName);// will be compared with fieldsetid, see determinExpand()    
        var onclickScript = 'javascript:UIControl.collapseExpandAllHost(false);UIControl.hostFieldsetDiv_toggleById(\'' + fieldsetId + '\',true); UIControl.hostFieldsetDiv_rememberHostname(\'' + hostNameTobeComparedLater + '\');fetchAndDrawForSelectedHostForLazyAjax(\'' + fieldsetId + '\')';
        var str = '<fieldset id="' + commonMainflow.makeHostFieldsetId_upperFieldsetId(fieldsetId) + '" class="graph_fieldset_host" style="' + style + '" >' +
        '<legend ><a class="collapse" onclick="' +
        onclickScript +
        '" id="' +
        makeHostFieldsetId_a(fieldsetId) +
        '">' +
        '<b>' +
        hostNameToShow +
        '</b>' +
        '</a></legend> <div id ="' +
        fieldsetId +
        '" class=\'hostFieldSetDivClass\' /></fieldset>';
        
        return str;
    },
    
    makeFieldSetFactorHtml: function(hostName, factorDirName, fieldsetId){
        var style = 'display:inline';
        if (UIControl.isDisplayHostAlignhorizontal()) {
            style = '';
        }
        // remove 'all' prefix
        factorDirName = commonUtils.trimTextMiddlefix(factorDirName, flotControl.KEYWORDS_TO_TRIM_FIELDSET_NAME_IN_ALL);
        // replace special charter to space.
        factorDirName = replaceEscapeChars(factorDirName);
		 // replace '_' to space.
        factorDirName = replaceUnderScore(factorDirName);
		
        
        
        return '<fieldset id="graph_fieldset_factor_id" class="graph_fieldset_factor" style="' + style +
        '" ><legend><b class="graph_fieldset_factor_legend" >' +
        factorDirName +
        '</b></legend><div id ="' +
        fieldsetId +
        '" /></fieldset>';
    },
    
    
    makeGraphGroupDivHtml: function(graphDivId, isDivClassInline){
        var classdiv = "graph_group_div";
        if (isDivClassInline) {
            classdiv = "graph_group_div_inline";
        }
        return '<div  class="' + classdiv + '" id="' + graphDivId + '"></div>';
        
    },
    
    
    makeGraphDivHtml: function(graphDivId){
        var classdiv = "graph_div";
        if (UIControl.isDisplayHostAlignhorizontal()) {
            classdiv = "graph_div_inline";
        }
        return '<div  class="' + classdiv + '" id="' + graphDivId + '"></div>';
        
    },
    
    makeGraphSelectionCheckboxHtml: function(graphUid){
        return '<div class="selectGraphDiv" style="display:none" ><input type="checkbox" id="' + makeChartSelectionCheckboxId(graphUid) + '" class="selectGraphChkbox" /></div>';
    },
    
    makeProgressAnimationHtml: function(graphUid){
        var progressUid = makeProgressUid(graphUid);
        return '&nbsp;<img id="' + progressUid + '" src="/common/images/ajax-loader_small.gif">';
    },
    
    // progress animation is added besides to title using jquery. the function is in flotControl.plotGraphs().
    makeGraphTitleCanvasDivHtml: function(title, graphUid, boolMagnify){
        var height = eval(UIControl.getCheckedChartHeight());	
        var width = calcGraphWidth(height, boolMagnify);
        height = calcGraphHeight(height, boolMagnify);
        
        var cssPostfixTobe = boolMagnify ? '_m' : '';
        var titleStyle = 'width:' + width + 'px;'
        var graphStyle = 'width:' + width + 'px; height:' + height + 'px;'
        var textToInsert = '';
        textToInsert += commonMainflow.makeGraphSelectionCheckboxHtml(graphUid);
        textToInsert += '<div id="' + makeGraphTitleId(graphUid) + '" class="graph_title' + cssPostfixTobe + '" style="' + titleStyle + '" >' + title + '</div>';
        textToInsert += '<div id="' + makeMessageDivId(graphUid) + '" class="graph_message"></div>';
        textToInsert += '<div id="' + graphUid + '" class="graph_canvas' + cssPostfixTobe + '" style="' + graphStyle + '"/>';
        
        return textToInsert;
    },
    
    
    makeLegendHtml: function(graphUid){
        return '<p id="' + makeLegendId(graphUid) + '" class="graph_legend_m" >';
    },
    
    makeGraphCanvasHtml: function(graphUid, boolMagnify){
        var height = eval(UIControl.getCheckedChartHeight());
        var width = calcGraphWidth(height, boolMagnify);
        height = calcGraphHeight(height, boolMagnify);
        
        var cssPostfixTobe = boolMagnify ? '_m' : '';
        var titleStyle = 'width:' + width + 'px;'
        var graphStyle = 'width:' + width + 'px; height:' + height + 'px;'
        var textToInsert = '<div id="' + graphUid + '" class="graph_canvas' + cssPostfixTobe + '" style="' + graphStyle + '"/>';
        return textToInsert;
    },
    
    makeGraphTitleHtml: function(title, graphUid, boolMagnify){
        var height = eval(UIControl.getCheckedChartHeight());
        var width = calcGraphWidth(height, boolMagnify);
        height = calcGraphHeight(height, boolMagnify);
        
        var cssPostfixTobe = boolMagnify ? '_m' : '';
        var titleStyle = 'width:' + width + 'px;'
        var graphStyle = 'width:' + width + 'px; height:' + height + 'px;'
        var textToInsert = '<div id="' + makeGraphTitleId(graphUid) + '" class="graph_title' + cssPostfixTobe + '" style="' + titleStyle + '" >' + title + '</div>';
        return textToInsert;
    },
    
    
    
    makeResetZoomHtml: function(graphUid, currentZoomLabelClassName){
        var cssClass = 'graph_zoomlabel';
        return '<div  id="' + makeResetZoomId(graphUid) + '" class="' + cssClass + '">reset zoom</div>';
    },
    
    
    //no legend for single graph
    drawSingleLineGraph: function(graphUid, hostName, factorCateName, labelName, factorFieldsetDiv, dataset){
    
        if (!commonMainflow.determinToDrawGraph(graphUid)) return true;
        
        var newDrawing = !isElementExist(graphUid);
        var graphGroupDivId = commonMainflow.appendGraphDivHtml(graphUid, factorFieldsetDiv)
        var graphGroupDiv = jQuery("#" + graphGroupDivId);
        var legendId = makeLegendId(graphUid);
        var flotOptionlocal = gGraphFlotOptionMap.get(graphUid);
        
        if (flotOptionlocal == null || flotOptionlocal == undefined) {
            flotOptionlocal = flotControl.getNewFlotOption(factorCateName);
            flotOptionlocal.legend.show = false;// has to false  	
            gGraphFlotOptionMap.put(graphUid, flotOptionlocal);
        }
        
        // if cpu and sum, y scale should be cpucount*100
        if (factorCateName == 'cpu' && gfnStartsWith(labelName, "sum_")) {
            cpuCount = dataset[0].cpuCount;
            if (DEBUG) console.log(eval(cpuCount * 100));
            flotOptionlocal.yaxis.max = eval(cpuCount * 100);
        }
		
		if( factorCateName='memcached_prefix' && (gfnEndsWith(labelName, "_gauge") || gfnEndsWith(labelName, "_gg"))){
			if (DEBUG) console.log("don't draw 'memcached_prefix' and 'xxx_gauge' chart ");			
			return false;
		}
		
        
        var labelPrefix2Trim = "";// prepare lable
        var rrdFileName = dataset[0].fileName;
        if (flotOptionlocal.series.labelPrefix2Trim != undefined) labelPrefix2Trim = flotOptionlocal.series.labelPrefix2Trim;
        var title = flotControl.makeGraphTitleFromDataLabel(labelName, rrdFileName, labelPrefix2Trim);
        
        if (newDrawing) {
            commonMainflow.appendGraphDiv(graphGroupDiv, title, graphUid, false);
        } else {
            var isCurrentGraphMagnified = jQuery("#" + graphUid)[0].className == "graph_canvas_m" ? true : false;
            commonMainflow.appendGraphTitleDiv(title, graphUid, isCurrentGraphMagnified);
            commonMainflow.appendGraphCanvasDiv(graphUid, isCurrentGraphMagnified);
        }
        if (commonMainflow.checkOperationMessageBeforeGraphing(graphUid, dataset, graphGroupDiv)) return true;
        
        // set datapoints if number of data is under some limits.
        flotOptionlocal = flotControl.setToshowDatapoints(flotOptionlocal, dataset);
        
        // set color by  all zero data and threshhold
        dataset = flotControl.setGraphColorAndCheckDataForSingleLineChart(graphUid, flotOptionlocal, dataset);
        
        // threshhold function moved to commonFlocontrol.js	        
        flotControl.plotGraphs(dataset, graphUid, flotOptionlocal);
        
        //        newDrawing = null;
        //        graphGroupDivId = null;
        //        graphGroupDiv = null;
        //        legendId = null;
        //        flotOptionlocal = null;
        //        dataset = null;
        //        hostFieldSetDiv = null;
        
        return true;
        
    },
    
    drawMergedGraph: function(graphUid, hostName, factorCateName, factorDirName, graphDiv, dataset){
    
        if (!commonMainflow.determinToDrawGraph(graphUid)) return true;
        
        var newDrawing = !isElementExist(graphUid);
        // fixing color
        var colorIndex = 0;
        jQuery.each(dataset, function(key, val){
            val.color = colorIndex;
            colorIndex++;
        });
        
        
        var flotOptionlocal = gGraphFlotOptionMap.get(graphUid);
        
        if (flotOptionlocal == undefined || flotOptionlocal == null) {
            flotOptionlocal = flotControl.getNewFlotOption(factorCateName);
            flotOptionlocal.legend.show = false;// has to false
            flotOptionlocal.legend.noColumns = 5;
            flotOptionlocal.series.lines.fill = true;
            gGraphFlotOptionMap.put(graphUid, flotOptionlocal);
        }
        
        var labelPrefix2Trim = "";// prepare lable
        var rrdFileName = dataset[0].fileName;
        if (flotOptionlocal.series.labelPrefix2Trim != undefined) labelPrefix2Trim = flotOptionlocal.series.labelPrefix2Trim;
        var title = flotControl.makeGraphTitleFromDataLabel(factorDirName, rrdFileName, labelPrefix2Trim);
        try {
            if (flotOptionlocal.series.stack) title += "(stacked)";
        } 
        catch (e) {
        }
        
        if (newDrawing) {
            commonMainflow.appendGraphDiv(graphDiv, title, graphUid, false);
        } else {
            var isCurrentGraphMagnified = jQuery("#" + graphUid)[0].className == "graph_canvas_m" ? true : false;
            commonMainflow.appendGraphTitleDiv(title, graphUid, isCurrentGraphMagnified);
            commonMainflow.appendGraphCanvasDiv(graphUid, isCurrentGraphMagnified);
        }
        
        if (commonMainflow.checkOperationMessageBeforeGraphing(graphUid, dataset, graphDiv)) return true;
        
        // set datapoints if number of data is under some limits.
        flotOptionlocal = flotControl.setToshowDatapoints(flotOptionlocal, dataset);
        
        flotControl.plotGraphs(dataset, graphUid, flotOptionlocal);
        
        //    if (newDrawing) {
        //        graphDiv.append(makeLegendHtml(graphUid));
        //        // legend is drawn separately at first time.
        //        flotControl.drawLegend(graphUid, flotOptionlocal, rrdFileName);
        //    }
        return true;
    },
    
    
    drawPrefixAggregateGraph: function(graphUid, hostName, factorCateName, factorDirName, graphDiv, dataset){
    
        if (!commonMainflow.determinToDrawGraph(graphUid)) return true;
        
        var newDrawing = !isElementExist(graphUid);
        // fixing color
        var colorIndex = 0;
        jQuery.each(dataset, function(key, val){
            val.color = colorIndex;
            colorIndex++;
        });
        
        
        var flotOptionlocal = gGraphFlotOptionMap.get(graphUid);
        if (flotOptionlocal == undefined || flotOptionlocal == null) {
			
            flotOptionlocal = flotControl.getNewFlotOptionForPrefixAggregate(factorCateName);
			var rrdFileName = dataset[0].fileName;
			if (gfnStartsWith(rrdFileName,'memcached_prefix_list')){				
				flotOptionlocal.xaxis.ticks=[[0, ''], [1, "Get"], [2, "Insert"], [3, "Delete"], [4, "GetHit"]]
			}else if (gfnStartsWith(rrdFileName,'memcached_prefix_set')){				
				flotOptionlocal.xaxis.ticks=[[0, ''], [1, "Get"], [2, "Insert"], [3, "Delete"], [4, "GetHit"], [5,"Exists"]]
			}else if (gfnStartsWith(rrdFileName,'memcached_prefix_B_plus_tree')){	 			
				flotOptionlocal.xaxis.ticks=[[0, ''], [1, "Get"], [2, "Insert"], [3, "Delete"], [4, "GetHit"]]
			}
            gGraphFlotOptionMap.put(graphUid, flotOptionlocal);
        }
        
        var labelPrefix2Trim = "";// prepare lable
        var rrdFileName = dataset[0].fileName;
        if (flotOptionlocal.series.labelPrefix2Trim != undefined) labelPrefix2Trim = flotOptionlocal.series.labelPrefix2Trim;
        var title = flotControl.makeGraphTitleFromDataLabel(factorDirName, rrdFileName, labelPrefix2Trim);
        try {
            if (flotOptionlocal.series.stack) title += "(stacked)";
        } 
        catch (e) {
        }
        
        if (newDrawing) {
            commonMainflow.appendGraphDiv(graphDiv, title, graphUid, false);
        } else {
            var isCurrentGraphMagnified = jQuery("#" + graphUid)[0].className == "graph_canvas_m" ? true : false;
            commonMainflow.appendGraphTitleDiv(title, graphUid, isCurrentGraphMagnified);
            commonMainflow.appendGraphCanvasDiv(graphUid, isCurrentGraphMagnified);
        }
        
        if (commonMainflow.checkOperationMessageBeforeGraphing(graphUid, dataset, graphDiv)) return true;
        
        
        flotControl.plotGraphs(dataset, graphUid, flotOptionlocal);
        
        return true;
    },
    
    appendGraphDiv: function(graphDiv, factorDirName, graphUid, boolTobeMagnify){
        var graphHtmlText = commonMainflow.makeGraphTitleCanvasDivHtml(factorDirName, graphUid, boolTobeMagnify);
        graphDiv.append(graphHtmlText);
    },
    
    appendGraphTitleDiv: function(title, graphUid, boolTobeMagnify){
    
        var graphTitleDiv = jQuery("#" + makeGraphTitleId(graphUid));
        //		graphTitleDiv.unbind();
        //		 jQuery(graphTitleDiv).unbind();
        //		 jQuery.event.remove(graphTitleDiv);
        //	 	 jQuery.removeData(graphTitleDiv);
        //		jQuery.removeData(graphTitleDiv);
        graphTitleDiv.empty();
        //graphTitleDiv.remove();
        graphTitleDiv.replaceWith(commonMainflow.makeGraphTitleHtml(title, graphUid, boolTobeMagnify));
        
    },
    
    appendGraphCanvasDiv: function(graphUid, boolTobeMagnify){
    
        var graphDiv = jQuery("#" + graphUid);
        //		graphDiv.unbind();
        //		 jQuery(graphDiv).unbind();
        //		 jQuery.event.remove(graphDiv);
        //	 	 jQuery.removeData(graphDiv);
        graphDiv.empty();
        //graphDiv.detach();
        //graphDiv.remove();
        graphDiv.replaceWith(commonMainflow.makeGraphCanvasHtml(graphUid, boolTobeMagnify));
    }
};


var guidSeparator = '_-_';

var guidPostfix = guidSeparator + "graphUid";
var guidPrefix_Schart = commonMainflow.gMergedGraphTypeS + guidSeparator;
var guidPrefix_Mchart = commonMainflow.gMergedGraphTypeM + guidSeparator;
var guidPrefix_MSchart = commonMainflow.gMergedGraphTypeMS + guidSeparator;
var guidPrefix_PAchart = commonMainflow.gMergedGraphTypePA + guidSeparator;

//input: id_11_perf_nhnsystem_com__cpu__cpu-0
//return : cpu
function makeFactorCateNameFromGraphUid(graphUid){
    var lists = graphUid.split(guidSeperator);
    return lists[2];
}


function getMergedGraphType(graphUid){
    var mergedGraph = gGraphDataMergedGraphTypeMap.get(graphUid);
    if (mergedGraph == undefined || mergedGraph == null) return commonMainflow.gMergedGraphTypeS;
    else         
        return mergedGraph;
}





// return :all_cloud_-_memcached_prefix_-_all_hitRatio_of_Cafe_comment
function makeGraphUidCore(hostName, factorCateName, factorDirName){
    return hostName + guidSeparator + factorCateName + guidSeparator + factorDirName; 
}
// return :all_cloud_-_memcached_prefix_-_all_hitRatio_of_Cafe_comment_-_graphUid
function makeGraphUid(hostName, factorCateName, factorDirName){
    var graphUid = makeGraphUidCore(hostName, factorCateName, factorDirName) + guidPostfix;	
    return removeSpecialCharacters(graphUid);
}

// @deprecated: rebuilding guid is not fit to various case using makeGraphUidCore()
// return :S_-_all_cloud_-_memcached_prefix_-_all_hitRatio_of_Cafe_comment_-_ratio_-_graphUid"
function makeGraphUidSingleLine_old(origGraphUid, hostName, factorCateName, factorDirName, labelName){
    var graphUid = guidPrefix_Schart + makeGraphUidCore(hostName, factorCateName, factorDirName) + guidSeparator + labelName + guidPostfix;	
    return removeSpecialCharacters(graphUid);    
}

// origGraphUid: all_cloud_-_memcached_prefix_-_all_hitRatio_of_Cafe_comment_-_graphUid
// return :  S_-_all_cloud_-_memcached_prefix_-_all_hitRatio_of_Cafe_comment_-_ratio_-_graphUid"
function makeGraphUidSingleLine(origGraphUid, hostName, factorCateName, factorDirName, labelName){  
    var graphUid =origGraphUid;
    if (gfnStartsWith(origGraphUid, guidPrefix_Schart)) {	
	    origGraphUid = getOrigGraphUidFromFinalGraphUid(origGraphUid);	   
	}
	graphUid = guidPrefix_Schart + origGraphUid + guidSeparator + labelName + guidPostfix;
    return removeSpecialCharacters(graphUid);    
}

// @deprecated: rebuilding guid is not fit to various case, use getOrigGraphUidFromFinalGraphUid() instead
//get graph uid from single or merged graphuid.
//S_-_all_cloud_-_memcached_prefix_-_all_hitRatio_of_Cafe_comment_-_ratio_-_graphUid"
// return :all_cloud_-_memcached_prefix_-_all_hitRatio_of_Cafe_comment_-_graphUid
function getGraphUidFrom_old(gFinalUid){
    var lists = gFinalUid.split(guidSeparator);
    return makeGraphUid(lists[1], lists[2], lists[3]);
}

//get graph uid from single or merged graphuid.
//     S_-_all_cloud_-_memcached_prefix_-_all_hitRatio_of_Cafe_comment_-_ratio_-_graphUid"
// return :all_cloud_-_memcached_prefix_-_all_hitRatio_of_Cafe_comment_-_graphUid
function getOrigGraphUidFromFinalGraphUid(gFinalUid){
    var lists = gFinalUid.split(guidPostfix);	
	return lists[0].replace(guidPrefix_Schart,"")	 +guidPostfix
    
}

function makeGraphUidMerged(hostName, factorCateName, factorDirName){
    var graphUid = guidPrefix_Mchart + makeGraphUidCore(hostName, factorCateName, factorDirName) + guidPostfix;
    return removeSpecialCharacters(graphUid);
    
}

function makeGraphUidPrefixAggregate(hostName, factorCateName, factorDirName){
    var graphUid = guidPrefix_PAchart + makeGraphUidCore(hostName, factorCateName, factorDirName) + guidPostfix;
    return removeSpecialCharacters(graphUid);
}


function isLineGraphUid(graphUid){
    // if (graphUid.match(/^M__/) || graphUid.match(/^S__/)) return true;
    if (gfnStartsWith(graphUid, guidPrefix_Mchart) || gfnStartsWith(graphUid, guidPrefix_Schart)) {
        return true;
    } else {
        return false;
    }
}

function isBarGraphUid(graphUid){
    if (gfnStartsWith(graphUid, guidPrefix_PAchart)) {
        return true;
    } else {
        return false;
    }
}

function isMergedGraphUid(graphUid){
    //if (graphUid.match(/^m___/)) return true;
    if (gfnStartsWith(graphUid, guidPrefix_Mchart)) return true;
    else         
        return false;
}

function makeGraphDivId(graphUid){
    return graphUid + '_graphdiv';
}

function makeGraphUidFromGraphdivid(src){
    return trimPostfix(src, '_graphdiv');
}

function makeGraphTitleId(graphUid){
    return graphUid + '_title';
}

function getGraphUidFromGraphTitleId(src){
    return trimPostfix(src, '_title');
}

function makeLegendId(graphUid){
    return graphUid + '_legendLabel';
}

function getGraphUidFromLegendId(src){
    return trimPostfix(src, '_legendLabel');
}

function makeTooltipId(graphUid){
    return graphUid + '_tooltip';
}

function makeDataLabelId(graphUid){
    return graphUid + '_datalabelbar';
}

function getGraphUidFromTooltipId(src){
    return trimPostfix(src, '_tooltip');
}

function makeResetZoomId(graphUid){
    return graphUid + '_resetZoom';
}

function getGraphUidFromResetZoomId(src){
    return trimPostfix(src, '_resetZoom');
}

function makeMinMaxButtonId(graphUid){
    return graphUid + '_minmaxButton';
}

function makeMessageDivId(graphUid){
    return graphUid + '_message';
}

function makeChartSelectionCheckboxId(graphUid){
    return graphUid + '_selectCheckbox';
}

function makeProgressUid(graphUid){
    return graphUid + '_progressUid';
}

//factorCategoryName: ex) cpu
//return div_cpu_info
function makeFactorGraphDivId(factorCategoryName){
    return 'factorGraphDiv_' + factorCategoryName;//div_cpu_info';
}

//factorCategoryName: ex) cpu
//return div_cpu_info
function makeFactorGraphDivId_a(factorGraphDivId){
    return factorGraphDivId + "_a";//div_cpu_info';
}



function makeHostFieldsetId(factorGraphDivId, hostName){
    var fieldsetId = 'hostFieldset_' + factorGraphDivId + '_' + hostName;
    return replaceSpecialCharactorsAsaID(fieldsetId);
}

function makeHostFieldsetId_a(fieldsetId){
    return fieldsetId + "_a";//div_cpu_info';
}

function makeFactorFieldsetId(factorGraphDivId, hostName){
    var fieldsetId = 'factorFieldset_' + factorGraphDivId + '_' + hostName;
    return replaceSpecialCharactorsAsaID(fieldsetId);
}


