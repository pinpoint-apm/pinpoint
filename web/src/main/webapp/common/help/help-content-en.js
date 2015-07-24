(function() {
	'use strict';
	/**
	 * (en)Tooltip for english 
	 * @ko 영문 Tooltip
	 * @group Config
	 * @name pinpointApp#helpContent-en
	 */
	var oHelp = {
		navbar : {
			applicationSelector: {
				mainStyle: "",
				title: "Application List",
				desc: "Shows the list of applications with Pinpoint installed.",
				category : [{
					title: "[Legend]",
					items: [{
						name: "Icon",
						desc: "Application Type"
					}, {
						name: "Text",
						desc: "Application Name. The value set using <code>-Dpinpoint.applicationName</code> when launching Pinpoint agent."
					}]
				}]
			},
			depth : {
				mainStyle: "",
				title: "<span class='glyphicon glyphicon-map-marker' aria-hidden='true'></span> Depth",
				desc: "Search-depth of server map"
			},
			periodSelector: {
				mainStyle: "",
				title: "Period Selector",
				desc: "Selects the time period for querying data.",
				category: [{
					title: "[Usage]",
					items: [{
						name: "<button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-th-list'></span></button>",
						desc: "Query for data traced during the most recent selected time-period.<br/>Auto-refresh is supported for 5m, 10m, 3h time-period."
					},{
						name: "<button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-calendar'></span></button>",
						desc: "Query for data traced between the two selected times for a maximum of 48 hours."
					}]
				}]
			}
		},
		servermap : {
			"default": {
				mainStyle: "width:560px;",
				title: "Server Map",
				desc: "Displays a topological view of the distributed server map.",
				category: [{
					title: "[Node]",
					list: [
				       "Each node is a logical unit of application.",
				       "The value on the top-right corner represents the number of server instances assigned to that application. (Not shown when there is only one such instance)",
				       "An alarm icon is displayed on the top-left corner if an error/exception is detected in one of the server instances.",
				       "Clicking a node shows information on all incoming transactions on the right-hand side of the screen."
					]
				},{
					title: "[Arrow]",
					list: [
						"Each arrow represents a transaction flow.",
						"The number shows the transaction count and is displayed in red for transactions with error.",
						"<span class='glyphicon glyphicon-filter' style='color:green;'></span> is shown when a filter is applied.",
						"Clicking an arrow shows information on all transactions passing through the selected section on the right-hand side of the screen."
				    ]
				},{
					title: "[Applying Filter]",
					list: [
				        "Right-clicking on an arrow displays a filter menu.",
				        "'Filter' filters the server map to only show transactions that has passed through the selected section.",
				        "'Filter Wizard' allows additional filter configurations."
					]
				},{
					title: "[Chart Configuration]",
					list: [
				        "Right-clicking on an empty area displays a chart configuration menu.",
				        "Node Setting / Merge Unknown : Groups all agent-less applications into a single node.",
				        "Double-clicking on an empty resets the zoom level of the server map."
					]
				}]
			} 
		},
		scatter : {
			"default": {
				mainStyle: "",
				title: "Response Time Scatter Chart",
				desc: "",
				category: [{
					title: "[Legend]",
					items: [{
						name: "<span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span>",
						desc: "Successful Transaction"
					},{
						name: "<span class='glyphicon glyphicon-stop' style='color:#f53034'></span>",
						desc: "Failed Transaction"
					},{
						name: "X-axis",
						desc: "Transaction Timestamp (hh:mm)"
					},{
						name: "Y-axis",
						desc: "Response Time (ms)"
					}]
				},{
					title: "[Usage]",
					image: "<img src='/images/help/scatter_01.png' width='200px' height='125px'>",
					items: [{
						name: "<span class='glyphicon glyphicon-plus'></span>",
						desc: "Drag on the scatter chart to show detailed information on selected transactions."
					},{
						name: "<span class='glyphicon glyphicon-cog'></span>",
						desc: "Set the min/max value of the Y-axis (Response Time)."
					},{
						name: "<span class='glyphicon glyphicon-download-alt'></span>",
						desc: "Download the chart as an image file."
					},{
						name: "<span class='glyphicon glyphicon-fullscreen'></span>",
						desc: "Open the chart in a new window."
					}]
				}]
			}
		},
		nodeInfoDetails: {
			responseSummary: {
				mainStyle: "",
				title: "Response Summary Chart",
				desc: "",
				category: [{
					title: "[Legend]",
					items: [{
						name: "X-Axis",
						desc: "Response Time"
					},{
						name: "Y-Axis",
						desc: "Transaction Count"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "No. of Successful transactions (less than 1 second)"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "No. of Successful transactions (1 ~ 3 seconds)"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "No. of Successful transactions (3 ~ 5 seconds)"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "No. of Successful transactions (greater than 5 seconds)"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "No. of Failed transactions regardless of response time"
					}]
				}]
			},
			load: {
				mainStyle: "",
				title: "Load Chart",
				desc: "",
				category: [{
					title: "[Legend]",
					items: [{
						name: "X-Axis",
						desc: "Transaction Timestamp (in minutes)"
					},{
						name: "Y-Axis",
						desc: "Transaction Count"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "No. of Successful transactions (less than 1 second)"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "No. of Successful transactions (1 ~ 3 seconds)"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "No. of Successful transactions (3 ~ 5 seconds)"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "No. of Successful transactions (greater than 5 seconds)"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "No. of Failed transactions regardless of response time"
					}]
				},{
					title: "[Usage]",
					list: [
				       "Clicking on a legend item shows/hides all transactions within the selected group.",
				       "Dragging on the chart zooms in to the dragged area."
					]
				}]
			},
			nodeServers: {
				mainStyle: "width:400px;",
				title: "Server Instances",
				desc: "List of physical servers and their server instances.",
				category: [{
					title: "[Legend]",
					items: [{
						name: "<span class='glyphicon glyphicon-home'></span>",
						desc: "Hostname of the physical server"
					},{
						name: "<span class='glyphicon glyphicon-hdd'></span>",
						desc: "AgentId of the Pinpoint agent installed on the server instance running on the physical server"
					}]
				},{
					title: "[Usage]",
					items: [{
						name: "<button type='button' class='btn btn-default btn-xs'>Inspector</button>",
						desc: "Open a new window with detailed information on the WAS with Pinpoint installed."
					},{
						name: "<span class='glyphicon glyphicon-record' style='color:#3B99FC'></span>",
						desc: "Display statistics on transactions carried out by the server instance."
					},{
						name: "<span class='glyphicon glyphicon-hdd' style='color:red'></span>",
						desc: "Display statistics on transactions (with error) carried out by the server instance."
					}]
				}]
			},
			unknownList: {
				mainStyle: "",
				title: "UnknownList",
				desc: "From the chart's top-right icon",
				category: [{
					title: "[Usage]",
					items: [{
						name: "1st",
						desc: "Toggle between Response Summary Chart / Load Chart"
					},{
						name: "2nd",
						desc: "Show Node Details"
					}]
				}]
			},
			searchAndOrder: {
				mainStyle: "",
				title: "Search and Fliter",
				desc: "Filter by server name or total count.Clicking Name or Count sorts the list in ascending/descending order."
			}			
		},
		linkInfoDetails: {
			responseSummary: {
				mainStyle: "",
				title: "Response Summary Chart",
				desc: "",
				category: [{
					title: "[Legend]",
					items: [{
						name: "X-Axis",
						desc: "Response Time"
					},{
						name: "Y-Axis",
						desc: "Transaction Count"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "No. of Successful transactions (less than 1 second)"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "No. of Successful transactions (1 ~ 3 seconds)"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "No. of Successful transactions (3 ~ 5 seconds)"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "No. of Successful transactions (greater than 5 seconds)"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "No. of Failed transactions regardless of response time"
					}]
				},{
					title: "[Usage]",
					list: ["Click on the bar to query for transactions within the selected response time."]
				}]
			},
			load: {
				mainStyle: "",
				title: "Load Chart",
				desc: "",
				category: [{
					title: "[Legend]",
					items: [{
						name: "X-Axis",
						desc: "Transaction Timestamp (in minutes)"
					},{
						name: "Y-Axis",
						desc: "Transaction Count"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "No. of Successful transactions (less than 1 second)"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "No. of Successful transactions (1 ~ 3 seconds)"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "No. of Successful transactions (3 ~ 5 seconds)"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "No. of Successful transactions (greater than 5 seconds)"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "No. of Failed transactions regardless of response time"
					}]
				},{
					title: "[Usage]",
					list: [
				       "Clicking on a legend item shows/hides all transactions within the selected group.",
				       "Dragging on the chart zooms in to the dragged area."
					]
				}]
			},
			linkServers: {
				mainStyle: "width:350px;",
				title: "Server Instance",
				desc: "List of physical servers and their server instances.",
				category: [{
					title: "[Legend]",
					items: [{
						name: "<span class='glyphicon glyphicon-home'></span>",
						desc: "Hostname of the physical server"
					},{
						name: "<span class='glyphicon glyphicon-hdd'></span>",
						desc: "AgentId of the Pinpoint agent installed on the server instance running on the physical server"
					}]
				},{
					title: "[Usage]",
					items: [{
						name: "<button type='button' class='btn btn-default btn-xs'>Inspector</button>",
						desc: "Open a new window with detailed information on the WAS with Pinpoint installed."
					},{
						name: "<button type='button' class='btn btn-default btn-xs'><span class='glyphicon glyphicon-plus'></span></button>",
						desc: "Display statistics on transactions carried out by the server instance."
					},{
						name: "<button type='button' class='btn btn-danger btn-xs'><span class='glyphicon glyphicon-plus'></span></button>",
						desc: "Display statistics on transactions (with error) carried out by the server instance."
					}]
				}]
			},
			unknownList: {
				mainStyle: "",
				title: "UnknownList",
				desc: "From the chart's top-right icon,",
				category: [{
					title: "[Usage]",
					items: [{
						name: "1st",
						desc: "Toggle between Response Summary Chart"
					},{
						name: "2dn",
						desc: "Show Node Details"
					}]
				}]
			},
			searchAndOrder: {
				mainStyle: "",
				title: "Search and Filter",
				desc: "Filter by server name or total count.<br/>Clicking Name or Count sorts the list in ascending/descending order."
			}
		},
		inspector: {
			list: {
				mainStyle: "",
				title: "Agent list",
				desc: "List of agents registered under the current Application Name",
				category: [{
					title: "[Legend]",
					items: [{
						name: "<span class='glyphicon glyphicon-home'></span>",
						desc: "Hostname of the agent's machine"
					},{
						name: "<span class='glyphicon glyphicon-hdd'></span>",
						desc: "Agent-id of the installed agent"
					}]
				}]
			},
			heap: {
				mainStyle: "",
				title: "Heap",
				desc: "JVM's heap information and full garbage collection times(if any)",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Max",
						desc: "Maximum heap size"
					},{
						name: "Used",
						desc: "Heap currently in use"
					},{
						name: "FCG",
						desc: "Full garbage collection duration (number of FGCs in parenthesis if it occurred more than once)"
					}]
				}]
			},
			permGen: {
				mainStyle: "",
				title: "PermGen",
				desc: "JVM's PermGen information and full garbage collection times(if any)",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Max",
						desc: "Maximum heap size"
					},{
						name: "Used",
						desc: "Heap currently in use"
					},{
						name: "FCG",
						desc: "Full garbage collection duration (number of FGCs in parenthesis if it occurred more than once)"
					}]
				}]
			},
			cpuUsage: {
				mainStyle: "",
				title: "Cpu Usage",
				desc: "JVM/System's CPU Usage - For multi-core CPUs, displays the average CPU usage of all the cores",
				category: [{
					title: "[Legend]",
					items: [{
						name: "Java 1.6",
						desc: "Only the JVM's CPU usage is collected"
					},{
						name: "Java 1.7+",
						desc: "Both the JVM's and the system's CPU usage are collected"
					}]
				}]
			}
		},
		callTree: {
			column: {
				mainStyle: "",
				title: "Call Tree",
				desc: "",
				category: [{
					title: "[Column]",
					items: [{
						name: "Gap",
						desc: "Time elapsed between the start of the previous method and entry of this method"
					},{
						name: "Exec",
						desc: "The overall duration of the method call from method entry until method exit"
					},{
						name: "Exec(%)",
						desc: "<img src='/images/help/callTree_01.png'/>"
					},{
						name: "",
						desc: "<span style='background-color:#FFFFFF;color:#5bc0de'>Light blue</span> The execution time of the method call as a percentage of the total execution time of the transaction"
					},{
						name: "",
						desc: "<span style='background-color:#FFFFFF;color:#4343C8'>Dark blue</span> A percentage of the self execution time"
					},{
						name: "Self",
						desc: "The time that was used for execution of this method only, excluding time consumed in nested methods call"
					}]
				}]
			}
		},
		transactionTable: {
			log: {}
		}
	};
	pinpointApp.constant('helpContent-en', oHelp );
})();