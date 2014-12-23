'use strict';

pinpointApp
  .constant('helpContent', {
        "navbar": {
            "applicationSelector": "<div style='width:400px;'><strong>Application List</strong><br/>Shows the list of applications with Pinpoint installed.<br/><br/>" +
                    "[Legend]<br/>" +
                    "<ul>" +
                    "<li>Icon : Application Type</li>" +
                    "<li>Text : Application Name. The value set using <code>-Dpinpoint.applicationName</code> when launching Pinpoint agent.</li>" +
                    "</ul>" +
                    "</div>",
                    "periodSelector": "<strong>Period Selector</strong><br/>Selects the time period for querying data.<br/><br/>" +
                    "[Usage]<br/>" +
                    "<ul>" +
                    "<li><button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-th-list'></span></button> : Query for data traced during the most recent selected time-period.<br/>Auto-refresh is supported for 5m, 10m, 3h time-period.</li>" +
                    "<li><button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-calendar'></span></button> : Query for data traced between the two selected times for a maximum of 48 hours.</li>" +
                    "</ul>"
        },
        "servermap": {
            "default": "<div style='width:300px;'><strong>Server Map</strong><br/>Displays a topological view of the distributed server map.<br/><br/>" +
                    "[Node]<br/>" +
                    "<ul>" +
                    "<li>Each node is a logical unit of application.</li>" +
                    "<li>The value on the top-right corner represents the number of server instances assigned to that application. (Not shown when there is only one such instance)</li>" +
                    "<li>An alarm icon is displayed on the top-left corner if an error/exception is detected in one of the server instances.</li>" +
                    "<li>Clicking a node shows information on all incoming transactions on the right-hand side of the screen.</li>" +
                    "</ul>" +
                    "[Arrow]<br/>" +
                    "<ul>" +
                    "<li>Each arrow represents a transaction flow.</li>" +
                    "<li>The number shows the transaction count and is displayed in red for transactions with error.</li>" +
                    "<li><span class='glyphicon glyphicon-filter'></span> is shown when a filter is applied.</li>" +
                    "<li>Clicking an arrow shows information on all transactions passing through the selected section on the right-hand side of the screen.</li>" +
                    "</ul>" +
                    "[Applying Filter]<br/>" +
                    "<ul>" +
                    "<li>Right-clicking on an arrow displays a filter menu.</li>" +
                    "<li>'Filter' filters the server map to only show transactions that has passed through the selected section.</li>" +
                    "<li>'Filter Wizard' allows additional filter configurations.</li>" +
                    "</ul>" +
                    "[Chart Configuration]<br/>" +
                    "<ul>" +
                    "<li>Right-clicking on an empty area displays a chart configuration menu.</li>" +
                    "<li>Node Setting / Merge Unknown : Groups all agent-less applications into a single node.</li>" +
                    "<li>Double-clicking on an empty resets the zoom level of the server map.</li>" +
                    "</ul>" +
                    "</div>"
        },
        "scatter": {
            "default": "<div style='width:400px'><strong>Response Time Scatter Chart</strong><br/><br/>" +
                    "[Legend]<br/>" +
                    "<ul>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span> : Successful Transaction</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f53034'></span> : Failed Transaction</li>" +
                    "<li>X-axis : Transaction Timestamp (hh:mm)</li>" +
                    "<li>Y-axis : Response Time (ms)</li>" +
                    "</ul>" +
                    "[Usage]<br/>" +
                    "<ul>" +
                    "<li><span class='glyphicon glyphicon-plus'></span> : Drag on the scatter chart to show detailed information on selected transactions.</li>" +
                    "<li><span class='glyphicon glyphicon-cog'></span> : Set the min/max value of the Y-axis (Response Time).</li>" +
                    "<li><span class='glyphicon glyphicon-download-alt'></span> : Download the chart as an image file.</li>" +
                    "<li><span class='glyphicon glyphicon-fullscreen'></span> : Open the chart in a new window.</li>" +
                    "</ul></div>"
        },
        "nodeInfoDetails": {
            "responseSummary": "<div style='width:400px'><strong>Response Summary Chart</strong><br/><br/>" +
                    "[Legend]<br/>" +
                    "<ul>" +
                    "<li>X-Axis : Response Time</li>" +
                    "<li>Y-Axis : Transaction Count</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span> : No. of Successful transactions (less than 1 second)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#3c81fa'></span> : No. of Successful transactions (1 ~ 3 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f8c731'></span> : No. of Successful transactions (3 ~ 5 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f69124'></span> : No. of Successful transactions (greater than 5 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f53034'></span> : No. of Failed transactions regardless of response time</li>" +
                    "</ul>" +
                    "</div>",
            "load": "<div style='width:400px'><strong>Load Chart</strong><br/><br/>" +
                    "[Legend]<br/>" +
                    "<ul>" +
                    "<li>X-Axis : Transaction Timestamp (in minutes)</li>" +
                    "<li>Y-Axis : Transaction Count</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span> : No. of Successful transactions (less than 1 second)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#3c81fa'></span> : No. of Successful transactions (1 ~ 3 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f8c731'></span> : No. of Successful transactions (3 ~ 5 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f69124'></span> : No. of Successful transactions (greater than 5 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f53034'></span> : No. of Failed transactions regardless of response time</li>" +
                    "</ul>" +
                    "[Usage]<br/>" +
                    "<ul>" +
                    "<li>Clicking on a legend item shows/hides all transactions within the selected group.</li>" +
                    "<li>Dragging on the chart zooms in to the dragged area.</li>" +
                    "</ul>" +
                    "</div>",
            "nodeServers": "<div style='width:350px'><strong>Server Instances</strong><br/>List of physical servers and their server instances.<br/><br/>" +
                    "[Legend]<br/>" +
                    "<ul>" +
                    "<li><span class='glyphicon glyphicon-home'></span> : Hostname of the physical server</li>" +
                    "<li><span class='glyphicon glyphicon-hdd'></span> : AgentId of the Pinpoint agent installed on the server instance running on the physical server</li>" +
                    "</ul>" +
                    "[Usage]<br/>" +
                    "<ul>" +
                    "<li><button type='button' class='btn btn-default btn-xs'>Inspector</button> : Open a new window with detailed information on the WAS with Pinpoint installed.</li>" +
                    "<li><button type='button' class='btn btn-default btn-xs'><span class='glyphicon glyphicon-plus'></span></button> : Display statistics on transactions carried out by the server instance.</li>" +
                    "<li><button type='button' class='btn btn-danger btn-xs'><span class='glyphicon glyphicon-plus'></span></button> : Display statistics on transactions (with error) carried out by the server instance.</li>" +
                    "</ul>" +
                    "</div>",
            "unknownList": "<div style='width:400px'>From the chart's top-right icon,<br/>1st : Toggle between Response Summary Chart / Load Chart<br/>2nd : Show Node Details</div>",
            "searchAndOrder": "<div style='width:400px'>Filter by server name or total count.<br/>Clicking Name or Count sorts the list in ascending/descending order.</div>"
        },
        "linkInfoDetails": {
            "responseSummary": "<div style='width:400px'><strong>Response Summary Chart</strong><br/><br/>" +
                    "[Legend]<br/>" +
                    "<ul>" +
                    "<li>X-Axis : Response Time</li>" +
                    "<li>Y-Axis : Transaction Count</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span> : No. of Successful transactions (less than 1 second)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#3c81fa'></span> : No. of Successful transactions (1 ~ 3 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f8c731'></span> : No. of Successful transactions (3 ~ 5 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f69124'></span> : No. of Successful transactions (greater than 5 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f53034'></span> : No. of Failed transactions regardless of response time</li>" +
                    "</ul>" +
                    "[Usage]<br/>" +
                    "<ul>" +
                    "<li>Click on the bar to query for transactions within the selected response time.</li>" +
                    "</ul>" +
                    "</div>",
            "load": "<div style='width:400px'><strong>Load Chart</strong><br/><br/>" +
                    "[Legend]<br/>" +
                    "<ul>" +
                    "<li>X-Axis : Transaction Timestamp (in minutes)</li>" +
                    "<li>Y-Axis : Transaction Count</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span> : No. of Successful transactions (less than 1 second)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#3c81fa'></span> : No. of Successful transactions (1 ~ 3 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f8c731'></span> : No. of Successful transactions (3 ~ 5 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f69124'></span> : No. of Successful transactions (greater than 5 seconds)</li>" +
                    "<li><span class='glyphicon glyphicon-stop' style='color:#f53034'></span> : No. of Failed transactions regardless of response time</li>" +
                    "</ul>" +
                    "[Usage]<br/>" +
                    "<ul>" +
                    "<li>Clicking on a legend item shows/hides all transactions within the selected group.</li>" +
                    "<li>Dragging on the chart zooms in to the dragged area.</li>" +
                    "</ul>" +
                    "</div>",
            "linkServers": "<div style='width:400px'><strong>Server Instances</strong><br/>List of physical servers and their server instances.<br/><br/>" +
                    "[Legend]<br/>" +
                    "<ul>" +
                    "<li><span class='glyphicon glyphicon-home'></span> : Hostname of the physical server</li>" +
                    "<li><span class='glyphicon glyphicon-hdd'></span> : AgentId of the Pinpoint agent installed on the server instance running on the physical server</li>" +
                    "</ul>" +
                    "[Usage]<br/>" +
                    "<ul>" +
                    "<li><button type='button' class='btn btn-default btn-xs'>Inspector</button> : Open a new window with detailed information on the WAS with Pinpoint installed.</li>" +
                    "<li><button type='button' class='btn btn-default btn-xs'><span class='glyphicon glyphicon-plus'></span></button> : Display statistics on transactions carried out by the server instance.</li>" +
                    "<li><button type='button' class='btn btn-danger btn-xs'><span class='glyphicon glyphicon-plus'></span></button> : Display statistics on transactions (with error) carried out by the server instance.</li>" +
                    "</ul>" +
                    "</div>",
            "unknownList": "<div style='width:400px'>From the chart's top-right icon,<br/>1st : Toggle between Response Summary Chart / Load Chart<br/>2nd : Show Node Details</div>",
            "searchAndOrder": "<div style='width:400px'>Filter by server name or total count.<br/>Clicking Name or Count sorts the list in ascending/descending order.</div>"
        }
});
