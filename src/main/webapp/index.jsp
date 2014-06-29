<%@ page contentType="text/html; charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pinpoint" uri="http://pinpoint.nhncorp.com/pinpoint" %>
<!doctype html>
<!--[if lt IE 7]>
<html lang="en-US" class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>
<html lang="en-US" class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>
<html lang="en-US" class="no-js lt-ie9""> <![endif]-->
<!--[if gt IE 8]><!-->
<html lang="en-US" class="no-js"> <!--<![endif]-->
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>PINPOINT</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width">
    <!-- Place favicon.ico and apple-touch-icon.png in the root directory -->
    <link rel="shortcut icon" href="favicon.png" type="image/png">
    <link rel="icon" href="favicon.png" type="image/png">

    <!--<link href="//fonts.googleapis.com/css?family=Arimo:400italic,400,700italic,700" rel="stylesheet" type="text/css">-->
    <link href='//fonts.googleapis.com/css?family=Lato' rel='stylesheet' type='text/css'>

    <!-- build:css styles/vendor.css -->
    <!-- bower:css -->
    <pinpoint:link rel="stylesheet" href="components/jquery-ui/themes/smoothness/jquery-ui.css" />
    <pinpoint:link rel="stylesheet" href="components/bootstrap/dist/css/bootstrap.min.css" />
    <pinpoint:link rel="stylesheet" href="components/jquery-timepicker-addon/jquery-ui-timepicker-addon.css" />
    <pinpoint:link rel="stylesheet" href="components/select2/select2.css" />
    <pinpoint:link rel="stylesheet" href="components/nvd3/src/nv.d3.css" />
    <pinpoint:link rel="stylesheet" href="components/jquery.layout/dist/layout-default-latest.css" />
    <pinpoint:link rel="stylesheet" href="components/pinpoint-scatter/jquery.dragToSelect.css" />
    <pinpoint:link rel="stylesheet" href="components/pinpoint-scatter/jquery.BigScatterChart.css" />
    <pinpoint:link rel="stylesheet" href="components/jquery.jslider/css/jslider.css" />
    <pinpoint:link rel="stylesheet" href="components/jquery.jslider/css/jslider.round.plastic.css" />
    <pinpoint:link rel="stylesheet" href="components/angular-slider/angular-slider.min.css" />
    <pinpoint:link rel="stylesheet" href="components/slickgrid/slick.grid.css" />
    <pinpoint:link rel="stylesheet" href="components/intro.js/minified/introjs.min.css" />
    <pinpoint:link rel="stylesheet" href="components/angular-intro-plus.js/build/angular-intro-plus.min.css" />
    <!-- endbower -->
    <!-- endbuild -->

    <!-- build:css({.tmp,app}) styles/main.css -->
    <pinpoint:link rel="stylesheet" href="styles/main.css" />
    <pinpoint:link rel="stylesheet" href="styles/inspector.css" />
    <pinpoint:link rel="stylesheet" href="styles/navbar.css" />
    <pinpoint:link rel="stylesheet" href="styles/nodeInfoDetails.css" />
    <pinpoint:link rel="stylesheet" href="styles/linkInfoDetails.css" />
    <pinpoint:link rel="stylesheet" href="styles/transactionTable.css" />
    <pinpoint:link rel="stylesheet" href="styles/callStacks.css" />
    <pinpoint:link rel="stylesheet" href="styles/timeSlider.css" />
    <pinpoint:link rel="stylesheet" href="styles/agentChartGroup.css" />
    <pinpoint:link rel="stylesheet" href="styles/sidebarTitle.css" />
    <pinpoint:link rel="stylesheet" href="styles/serverMap.css" />
    <pinpoint:link rel="stylesheet" href="styles/filterInformation.css" />
    <pinpoint:link rel="stylesheet" href="styles/distributedCallFlow.css" />
    <pinpoint:link rel="stylesheet" href="styles/loading.css" />
    <!-- endbuild -->

    <!-- build:js scripts/plugins.js -->
    <pinpoint:script src="components/jquery/dist/jquery.js" />
    <pinpoint:script src="components/jquery-ui/ui/jquery-ui.js" />
    <pinpoint:script src="components/moment/moment.js" />
    <pinpoint:script src="components/angular/angular.js" />
    <pinpoint:script src="components/modernizr/modernizr.js" />
    <pinpoint:script src="components/datejs/build/date.js" />
    <pinpoint:script src="components/underscore/underscore-min.js" />
    <pinpoint:script src="components/bootstrap/dist/js/bootstrap.min.js" />
    <pinpoint:script src="components/gojs/go.js" />
    <scirpt src="components/gojs/go-debug.js"></scirpt>
    <pinpoint:script src="components/d3/d3.min.js" />
    <pinpoint:script src="components/nvd3/nv.d3.js" />
    <pinpoint:script src="components/select2/select2.min.js" />
    <pinpoint:script src="components/select2/select2_locale_ko.js" />
    <pinpoint:script src="components/jquery-class/jquery.Class.js" />
    <pinpoint:script src="components/jquery.layout/dist/jquery.layout-latest.js" />
    <pinpoint:script src="components/pinpoint-scatter/jquery.dragToSelect.js" />
    <pinpoint:script src="components/pinpoint-scatter/jquery.BigScatterChart.js" />
    <pinpoint:script src="components/pinpoint-servermap/js/jquery.ServerMap2.js" />
    <pinpoint:script src="components/jquery-timepicker-addon/jquery-ui-timepicker-addon.js" />
    <pinpoint:script src="components/jquery.jslider/js/draggable-0.1.js" />
    <pinpoint:script src="components/jquery.jslider/js/jshashtable-2.1_src.js" />
    <pinpoint:script src="components/jquery.jslider/js/jquery.dependClass-0.1.js" />
    <pinpoint:script src="components/jquery.jslider/js/jquery.numberformatter-1.2.3.js" />
    <pinpoint:script src="components/jquery.jslider/js/tmpl.js" />
    <pinpoint:script src="components/jquery.jslider/js/jquery.jslider.js" />
    <pinpoint:script src="components/jquery-ui-tabs-paging/js/ui.tabs.paging.js" />
    <pinpoint:script src="components/angular-slider/angular-slider.min.js" />
    <pinpoint:script src="components/slickgrid/lib/jquery.event.drag-2.2.js" />
    <pinpoint:script src="components/slickgrid/slick.core.js" />
    <pinpoint:script src="components/slickgrid/slick.formatters.js" />
    <pinpoint:script src="components/slickgrid/slick.editors.js" />
    <pinpoint:script src="components/slickgrid/slick.grid.js" />
    <pinpoint:script src="components/slickgrid/slick.dataview.js" />
    <pinpoint:script src="components/angular-base64/angular-base64.min.js" />
    <pinpoint:script src="components/amcharts/amcharts.js" />
    <pinpoint:script src="components/amcharts/serial.js" />
    <pinpoint:script src="components/amcharts/themes/light.js" />
    <pinpoint:script src="components/intro.js/intro.js" />
    <!-- endbuild -->

    <!-- build:js scripts/modules.js -->
    <pinpoint:script src="components/angular-resource/angular-resource.js" />
    <pinpoint:script src="components/angular-cookies/angular-cookies.js" />
    <pinpoint:script src="components/angular-webstorage/angular-webstorage.js" />
    <pinpoint:script src="components/angular-strap/dist/angular-strap.min.js" />
    <pinpoint:script src="components/angular-strap/dist/angular-strap.tpl.min.js" />
    <pinpoint:script src="components/angular-animate/angular-animate.min.js" />
    <pinpoint:script src="components/angular-route/angular-route.js" />
    <pinpoint:script src="components/angular-sanitize/angular-sanitize.min.js" />
    <pinpoint:script src="components/angular-timer/dist/angular-timer.min.js" />
    <pinpoint:script src="components/angular-intro-plus.js/build/angular-intro-plus.min.js" />
    <!-- endbuild -->

</head>
<body ng-app="pinpointApp">
<!--[if lt IE 7]>
<p class="chromeframe">You are using an outdated browser. <a href="http://browsehappy.com/">Upgrade your browser
    today</a> or <a href="http://www.google.com/chromeframe/?redirect=true">install Google Chrome Frame</a> to better
    experience this site.</p>
<![endif]-->

<!--[if lt IE 9]>
<pinpoint:script src="bower_components/es5-shim/es5-shim.js" />
<pinpoint:script src="bower_components/json3/lib/json3.min.js" />
<![endif]-->

<div id="wrapper" ng-class="wrapperClass" ng-style="wrapperStyle" ng-view=""
     ng-intro-plus-options="IntroPlusOptions" ng-intro-plus-show="showHelpIcons"
     ng-intro-plus-on-after-overlay-creation="onAfterOverlayCreation"
     ng-intro-plus-on-before-overlay-removal="onBeforeOverlayRemoval"
     ng-intro-plus-refresh-help-icons="refreshHelpIcons">
</div>

<div id="copyright">
    <p class="bg-info">
        Copyright Â© NAVER Corp.<br>
        <a href="http://yobi.navercorp.com/Pinpoint/pinpoint-release" target="_blank">go to Yobi Repository</a>
    </p>
</div>

<!-- ng-template -->
<script id="error" type="text/ng-template">
    <div class="error" style="position:absolute;top:38%;width:90%;margin-left:5%;display:none;z-index:10">
        <div class="alert alert-danger">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
            <h4>Error!</h4>

            <div class="msg"></div>
        </div>
    </div>
</script>
<script id="warning" type="text/ng-template">
    <div class="warning" style="position:absolute;top:38%;width:90%;margin-left:5%;display:none;z-index:10">
        <div class="alert alert-warning">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
            <h4>Warning!</h4>

            <div class="msg"></div>
        </div>
    </div>
</script>
<script id="info" type="text/ng-template">
    <div class="info" style="position:absolute;top:38%;width:90%;margin-left:5%;display:none;z-index:10">
        <div class="alert alert-info">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
            <h4>Info</h4>

            <div class="msg"></div>
        </div>
    </div>
</script>
<script id="loading" type="text/ng-template">
    <div class="progress progress-striped"
         style="position:absolute;top:40%;width:90%;margin-left:5%;display:none;z-index:10">
        <div class="bar progress-bar progress-bar-info" role="progressbar" aria-valuenow="0" aria-valuemin="0"
             aria-valuemax="100" style="width: 0%">
        </div>
    </div>
</script>
<script id="loading.html" type="text/ng-template">
    <div class="cg-busy cg-busy-animation ng-hide" ng-show="!!showLoading">
        <div class="cg-busy cg-busy-backdrop"></div>
        <div class="cg-busy-default-wrapper" style="position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px;">
            <div class="cg-busy-default-sign">
                <div class="cg-busy-default-spinner">
                    <div class="bar1"></div>
                    <div class="bar2"></div>
                    <div class="bar3"></div>
                    <div class="bar4"></div>
                    <div class="bar5"></div>
                    <div class="bar6"></div>
                    <div class="bar7"></div>
                    <div class="bar8"></div>
                    <div class="bar9"></div>
                    <div class="bar10"></div>
                    <div class="bar11"></div>
                    <div class="bar12"></div>
                </div>
                <div class="cg-busy-default-text ng-binding">{{loadingMessage}}</div>
            </div>
        </div>
    </div>
</script>

<!-- Modal -->
<div id="supported-browsers" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">Ã</button>
        <h3 id="myModalLabel">Supported Browsers</h3>
    </div>
    <div class="modal-body">
        <div class="main-section--answer main-section content-container"><p>You can access PinPoint via a browser
            installed on a PC, Mac, or Linux computer.</p>

            <h4>To get the latest PinPoint features, use a supported browser</h4>

            <p>In general, PinPoint supports the current and prior major release of Chrome, Firefox, Internet Explorer
                and Safari on a rolling basis. If you use a browser other than those listed below, please install the
                one of them.</p>

            <ul>
                <li>Google Chrome <a href="http://www.google.com/chrome" target="_blank">download</a></li>
                <li>Firefox <a href="http://www.mozilla.org/firefox" target="_blank">download</a></li>
                <li>Internet Explorer <a href="http://windows.microsoft.com/en-US/internet-explorer/download-ie"
                                         target="_blank">download</a></li>
                <li>Safari <a href="http://www.apple.com/safari" target="_blank">download</a></li>
            </ul>

            <h4>To use PinPoint, enable cookies and JavaScript</h4>

            <p>Regardless of your browser type, you must have cookies enabled to use PinPoint. Also, if your browser
                supports it, enable JavaScript.</p>
        </div>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
    </div>
</div>


<!-- build:js({.tmp,app}) scripts/scripts.js -->
<pinpoint:script src="scripts/app.js" />
<pinpoint:script src="scripts/controllers/main.js" />
<pinpoint:script src="scripts/controllers/inspector.js" />
<pinpoint:script src="scripts/directives/navbar.js" />
<pinpoint:script src="scripts/directives/serverMap.js" />
<pinpoint:script src="scripts/directives/scatter.js" />
<pinpoint:script src="scripts/directives/nodeInfoDetails.js" />
<pinpoint:script src="scripts/directives/linkInfoDetails.js" />
<pinpoint:script src="scripts/directives/agentList.js" />
<pinpoint:script src="scripts/directives/agentInfo.js" />
<pinpoint:script src="scripts/filters/filterToString.js" />
<pinpoint:script src="scripts/controllers/transactionList.js" />
<pinpoint:script src="scripts/controllers/transactionDetail.js" />
<pinpoint:script src="scripts/directives/timeSlider.js" />
<pinpoint:script src="scripts/directives/transactionTable.js" />
<pinpoint:script src="scripts/directives/callStacks.js" />
<pinpoint:script src="scripts/directives/timeline.js" />
<pinpoint:script src="scripts/filters/timeline.js" />
<pinpoint:script src="scripts/filters/timelineWidth.js" />
<pinpoint:script src="scripts/filters/timelineMarginLeft.js" />
<pinpoint:script src="scripts/controllers/filteredMap.js" />
<pinpoint:script src="scripts/services/TimeSliderVo.js" />
<pinpoint:script src="scripts/services/Alerts.js" />
<pinpoint:script src="scripts/services/ProgressBar.js" />
<pinpoint:script src="scripts/filters/datejs.js" />
<pinpoint:script src="scripts/filters/icons.js" />
<pinpoint:script src="scripts/services/NavbarVo.js" />
<pinpoint:script src="scripts/filters/encodeURIComponent.js" />
<pinpoint:script src="scripts/services/WebSql.js" />
<pinpoint:script src="scripts/services/TransactionDao.js" />
<pinpoint:script src="scripts/services/WebSqlMigrator.js" />
<pinpoint:script src="scripts/services/location.js" />
<pinpoint:script src="scripts/services/IndexedDb.js" />
<pinpoint:script src="scripts/services/ServerMapDao.js" />
<pinpoint:script src="scripts/controllers/transactionView.js" />
<pinpoint:script src="scripts/services/AgentDao.js" />
<pinpoint:script src="scripts/directives/agentChartGroup.js" />
<pinpoint:script src="scripts/directives/linePlusBarChart.js" />
<pinpoint:script src="scripts/controllers/scatterFullScreenMode.js" />
<pinpoint:script src="scripts/directives/helixChart.js" />
<pinpoint:script src="scripts/services/HelixChartVo.js" />
<pinpoint:script src="scripts/directives/sidebarTitle.js" />
<pinpoint:script src="scripts/services/SidebarTitleVo.js" />
<pinpoint:script src="scripts/services/filteredMapUtil.js" />
<pinpoint:script src="scripts/services/filterConfig.js" />
<pinpoint:script src="scripts/services/ServerMapFilterVo.js" />
<pinpoint:script src="scripts/directives/filterInformation.js" />
<pinpoint:script src="scripts/directives/distributedCallFlow.js" />
<pinpoint:script src="scripts/filters/base64.js" />
<pinpoint:script src="scripts/filters/applicationNameToClassName.js" />
<pinpoint:script src="scripts/filters/decodeURIComponent.js" />
<pinpoint:script src="scripts/controllers/LoginForm.js" />
<pinpoint:script src="scripts/filters/humanReadableNumberFormat.js" />
<pinpoint:script src="scripts/services/ServerMapHintVo.js" />
<pinpoint:script src="scripts/services/isVisible.js" />
<pinpoint:script src="scripts/directives/responseTimeChart.js" />
<pinpoint:script src="scripts/directives/loadChart.js" />
<pinpoint:script src="scripts/services/helpContent.js" />
<pinpoint:script src="scripts/directives/jvmMemoryChart.js" />
<pinpoint:script src="scripts/directives/loading.js" />
<!-- endbuild -->
</body>
</html>
