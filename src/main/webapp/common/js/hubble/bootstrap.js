(function () {
    var scriptList = [ "/common/js/flot/jquery.flot.min.js",
        "/common/js/flot/jquery.flot.crosshair.min.js",
        "/common/js/flot/jquery.flot.selection.min.js",
        "/common/js/flot/jquery.flot.stack.min.js",
        "/common/js/flot/jquery.flot.threshold.min.js",
        "/common/js/hubble/api.js", "/common/js/hubble/chart.js" ];

    var hostAndPort = "localhost:8080";

    for (var i = 0; i < scriptList.length; i++) {
        var script = document.createElement('script');
        script.type = 'text/javascript';
        script.async = true;

        if ('https:' == document.location.protocol) {
            script.src = 'https://' + hostAndPort + scriptList[i];
        } else if ('http:' == document.location.protocol) {
            script.src = 'http://' + hostAndPort + scriptList[i];
        } else if ('file:' == document.location.protocol) {
            script.src = scriptList[i];
        }

        var s = document.getElementsByTagName('script')[0];
        s.parentNode.insertBefore(script, s);
    }
})();
