(function(){ 
	'use strict';
	/**
	 * (en)한국어 Tooltip 
	 * @ko 한국어 Tooltip
	 * @group Config
	 * @name pinpointApp#helpContent-ko
	 */
	var oHelp = {
		configuration: {
			general: {
				warning: "* 설정 정보는 브라우저 캐쉬에 저장합니다. 서버 측 저장은 추후 지원 할 예정입니다.",
				empty: "등록된 목록이 없습니다."
			},
			alarmRules: {
				mainStyle: "",
				title: "알람 룰의 종류",
				desc: "Pinpoint에서 지원하는 Alarm rule의 종류는 아래와 같습니다.",
				category: [{
					title: "[항목]",
					items: [{
						name: "SLOW COUNT",
						desc: "application 내에서 외부서버를 호출한 요청 중 slow 호출의 개수가 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "SLOW RATE",
						desc: "application 내에서 외부서버를 호출한 요청 중 slow 호출의 비율(%)이 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "ERROR COUNT",
						desc: "application 내에서 외부서버를 호출한 요청 중 error 가 발생한 호출의 개수가 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "ERROR RATE",
						desc: "application 내에서 외부서버를 호출한 요청 중 error 가 발생한 호출의 비율이 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "TOTAL COUNT",
						desc: "application 내에서 외부서버를 호출한 요청의 개수가 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "SLOW COUNT TO CALLEE",
						desc: "외부에서 application을 호출한 요청 중에 외부서버로 응답을 늦게 준 요청의 개수가 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "SLOW RATE TO CALLEE",
						desc: "외부에서 application을 호출한 요청 중에 외부서버로 응답을 늦게 준 요청의 비율(%)이 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "ERROR COUNT TO CALLEE",
						desc: "외부에서 application을 호출한 요청 중에 에러가 발생한 요청의 개수가 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "ERROR RATE TO CALLEE",
						desc: "외부에서 application을 호출한 요청 중에 에러가 발생한 요청의 비율(%)이 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "TOTAL COUNT TO CALLEE",
						desc: "외부에서 application을 호출한 요청 개수가 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "HEAP USAGE RATE",
						desc: "heap의 사용률이 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "JVM CPU USAGE RATE",
						desc: "applicaiton의 CPU 사용률이 임계치를 초과한 경우 알람이 전송된다."
					},{
						name: "DATASOURCE CONNECTION USAGE RATE",
						desc: "applicaiton의 DataSource내의 Connection 사용률이 임계치를 초과한 경우 알람이 전송된다."
					}, {
						name: "DEADLOCK OCCURRENCE",
						desc: "applicaiton에서 데드락 상태가 탐지되면 알람이 전송된다."
					}]
				}]
			}
		},
		navbar : {
			searchPeriod : {
				guide: "한번에 검색 할 수 있는 최대 기간은 {{day}}일 입니다."
			},
			applicationSelector: {
				mainStyle: "",
				title: "응용프로그램 목록",
				desc: "핀포인트가 설치된 응용프로그램 목록입니다.",
				category : [{
					title: "[범례]",
					items: [{
						name: "아이콘",
						desc: "응용프로그램의 종류"
					}, {
						name: "텍스트",
						desc: "응용프로그램의 이름입니다. Pinpoint agent 설정에서 applicationName에 지정한 값입니다."
					}]
				}]
			},
			depth : {
				mainStyle: "",
				title: '<img src="images/inbound.png" width="22px" height="22px" style="margin-top:-4px;"> Inbound 와 <img src="images/outbound.png" width="22px" height="22px" style="margin-top:-4px"> Outbound',
				desc: "서버맵의 탐색 깊이를 설정합니다.",
				category : [{
					title: "[범례]",
					items: [{
						name: "Inbound",
						desc: "선택된 노드를 기준으로 들어오는 탐색깊이"
					}, {
						name: "Outbound",
						desc: "선택된 노드를 기준으로 나가는 탐색 깊이"
					}]
				}]
			},
			bidirectional : {
				mainStyle: "",
				title: '<img src="images/bidirect_on.png" width="22px" height="22px" style="margin-top:-4px;"> Bidirectional Search',
				desc: "서버맵의 탐색 방법을 설정합니다.",
				category : [{
					title: "[범례]",
					items: [{
						name: "Bidirectional",
						desc: "모든 노드들에 대해 양방향 탐색을 하여 선택된 노드와 직접적인 연관이 없는 노드들도 탐색됩니다.<br>주의 : 이 옵션을 선택하시면 필요 이상으로 복잡한 서버맵이 조회될 수 있습니다."
					}]
				}]
			},
			periodSelector: {
				mainStyle: "",
				title: "조회 시간 설정",
				desc: "데이터 조회 시간을 선택합니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "<button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-th-list'></span></button>",
						desc: "현재 시간을 기준으로 선택한 시간 이전부터 현재시간 사이에 수집된 데이터를 조회합니다.<br/>최근 5m, 10m, 3h조회는 자동 새로고침 기능을 지원합니다."
					},{
						name: "<button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-calendar'></span></button>",
						desc: "지정된 시간 사이에 수집된 데이터를 조회합니다. 조회 시간은 분단위로 최대 48시간을 지정할 수 있습니다."
					}]
				}]
			}
		},
		servermap : {
			"default": {
				mainStyle: "width:560px;",
				title: "서버맵",
				desc: "분산된 서버를 도식화 한 지도 입니다.",
				category: [{
					title: "[박스]",
					list: [
				       "박스는 응용프로그램 그룹을 나타냅니다.",
				       "우측의 숫자는 응용프로그램 그룹에 속한 서버 인스턴스의 개수입니다.(한 개일 때에는 숫자를 보여주지 않습니다.)",
				       "좌측 빨간 알람은 임계값을 초과한 모니터링 항목이 있을 때 나타납니다."
					]
				},{
					title: "[화살표]",
					list: [
						"화살표는 트랜잭션의 흐름을 나타냅니다.",
						"화살표의 숫자는 호출 수 입니다. 임계치 이상의 에러를 포함하면 빨간색으로 보여집니다.",
						"<span class='glyphicon glyphicon-filter' style='color:green;'></span> : 필터가 적용되면 아이콘이 표시됩니다."
				    ]
				},{
					title: "[박스의 기능]",
					list: [ "박스를 선택하면 어플리케이션으로 유입된 트랜잭션 정보를 화면 우측에 보여줍니다." ]
				},{
					title: "[화살표의 기능]",
					list: [
				        "화살표를 선택하여 선택된 구간을 통과하는 트랜잭션의 정보를 화면 우측에 보여줍니다.",
				        "Context menu의 Filter는 선택된 구간을 통과하는 트랜잭션만 모아서 보여줍니다.",
				        "Filter wizard는 보다 상세한 필터 설정을 할 수 있습니다.",
				        "필터가 적용되면 화살표에 <span class='glyphicon glyphicon-filter' style='color:green;'></span>아이콘이 표시됩니다."
					]
				},{
					title: "[차트 설정]",
					list: [
				        "비어있는 부분을 마우스 오른쪽 클릭하여 context menu를 열면 차트 설정메뉴가 보입니다.",
				        "Node Setting / Merge Unknown : agent가 설치되어있지 않은 응용프로그램을 하나의 박스로 보여줍니다.",
				        "비어있는 부분 더블클릭 : 줌을 초기화 합니다."
					]
				}]
			} 
		},
		realtime: {
			"default": {
				mainStyle: "",
				title: "Realtime Active Thread Chart",
				desc: "각 Agent의 Active Thread 갯수를 실시간으로 보여줍니다.",
				category: [{
					title: "[에러 메시지 설명]",
					items: [{
						name: "UNSUPPORTED VERSION",
						desc: "해당 에이전트의 버전에서는 지원하지 않는 기능입니다. (1.5.0 이상 버전으로 업그레이드하세요.)",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "CLUSTER OPTION NOTSET",
						desc: "해당 에이전트의 설정에서 기능이 비활성화되어 있습니다. (pinpoint 설정 파일에서 profiler.pinpoint.activethread 항목을 true로 변경하세요.)",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "TIMEOUT",
						desc: "해당 에이전트에서 일시적으로 활성화 된 스레드 개수를 받지 못하였습니다.(오래 지속될 경우 담당자에게 문의하세요.)",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "NOT FOUND",
						desc: "해당 에이전트를 찾을 수 없습니다.(에이전트가 활성화 되어 있는 경우에 해당 메시지가 발생한다면 pinpoint 설정 파일에서 profiler.tcpdatasender.command.accept.enable 항목을 true로 변경하세요.)",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "CLUSTER CHANNEL CLOSED",
						desc: "해당 에이전트와의 세션이 종료되었습니다.",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "PINPOINT INTERNAL ERROR",
						desc: "핀포인트 내부 에러가 발생하였습니다.(담당자에게 문의하세요.)",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "No Active Thread",
						desc: "현재 해당 에이전트는 활성화된 스레드가 존재하지 않습니다.",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					},{
						name: "No Response",
						desc: "핀포인트 웹 서버로부터의 응답을 받지 못하였습니다.(담당자에게 문의하세요.)",
						nameStyle: "width:120px;border-bottom:1px solid gray",
						descStyle: "border-bottom:1px solid gray"
					}]
				}]
			}
		},		
		scatter : {
			"default": {
				mainStyle: "",
				title: "Response Time Scatter Chart",
				desc: "수집된 트랜잭션의 응답시간 분포도입니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "<span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span>",
						desc: "에러가 없는 트랜잭션 (Success)"
					},{
						name: "<span class='glyphicon glyphicon-stop' style='color:#f53034'></span>",
						desc: "에러를 포함한 트랜잭션 (Failed)"
					},{
						name: "X축",
						desc: "트랜잭션이 실행된 시간 (시:분)"
					},{
						name: "Y축",
						desc: "트랜잭션의 응답 속도 (ms)"
					}]
				},{
					title: "[기능]",
					image: "<img src='images/help/scatter_01.png' width='200px' height='125px'>",
					items: [{
						name: "<span class='glyphicon glyphicon-plus'></span>",
						desc: "마우스로 영역을 드래그하여 드래그 된 영역에 속한 트랜잭션의 상세정보를 조회할 수 있습니다."
					},{
						name: "<span class='glyphicon glyphicon-cog'></span>",
						desc: "에러를 포함한 트랜잭션 (Failed)"
					},{
						name: "X축",
						desc: "응답시간(Y축)의 최대 또는 최소값을 변경할 수 있습니다."
					},{
						name: "<span class='glyphicon glyphicon-download-alt'></span>",
						desc: "차트를 새창으로 크게 보여줍니다."
					}]
				}]
			}
		},
		nodeInfoDetails: {
			responseSummary: {
				mainStyle: "",
				title: "Response Summary Chart",
				desc: "응답결과 요약 입니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "X축",
						desc: "트랜잭션 응답시간 요약 단위"
					},{
						name: "Y축",
						desc: "트랜잭션의 개수"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "<span class='label label-info'>0초 <= 응답시간 < 1초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "<span class='label label-info'>1초 <= 응답시간 < 3초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "<span class='label label-info'>3초 <= 응답시간 < 5초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "<span class='label label-info'>5초 <= 응답시간</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "응답시간과 무관하게 실패한 트랜잭션의 수"
					}]
				}]
			},
			load: {
				mainStyle: "",
				title: "Load Chart",
				desc: "시간별 트랜잭션의 응답 결과입니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "X축",
						desc: "트랜잭션이 실행된 시간 (분단위)"
					},{
						name: "Y축",
						desc: "트랜잭션의 개수"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "<span class='label label-info'>0초 <= 응답시간 < 1초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "<span class='label label-info'>1초 <= 응답시간 < 3초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "<span class='label label-info'>3초 <= 응답시간 < 5초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "<span class='label label-info'>5초 <= 응답시간</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "응답시간과 무관하게 실패한 트랜잭션의 수"
					}]
				},{
					title: "[기능]",
					list: [
				        "범례를 클릭하여 해당 응답시간에 속한 트랜잭션을 차트에서 제외하거나 포함 할 수 있습니다.",
				        "마우스로 드래그하여 드래그 한 범위를 확대할 수 있습니다."
					]
				}]
			},
			nodeServers: {
				mainStyle: "width:400px;",
				title: "Server Information",
				desc: "물리서버와 해당 서버에서 동작중인 서버 인스턴스의 정보를 보여줍니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "<span class='glyphicon glyphicon-home'></span>",
						desc: "물리서버의 호스트이름입니다."
					},{
						name: "<span class='glyphicon glyphicon-hdd'></span>",
						desc: "물리서버에 설치된 서버 인스턴스에서 동작중인 Pinpoint의 agentId입니다."
					}]
				},{
					title: "[기능]",
					items: [{
						name: "<button type='button' class='btn btn-default btn-xs'>Inspector</button>",
						desc: "Pinpoint가 설치된 WAS의 상세한 정보를 보여줍니다."
					},{
						name: "<span class='glyphicon glyphicon-record' style='color:#3B99FC'></span>",
						desc: "해당 인스턴스에서 처리된 트랜잭션 통계를 조회할 수 있습니다."
					},{
						name: "<span class='glyphicon glyphicon-hdd' style='color:red'></span>",
						desc: "에러를 발생시킨 트랜잭션이 포함되어있다는 의미입니다."
					}]
				}]
			},
			unknownList: {
				mainStyle: "",
				title: "UnknownList",
				desc: "차트 오른쪽 상단의 아이콘부터",
				category: [{
					title: "[기능]",
					items: [{
						name: "첫번째",
						desc: "Response Summary"
					},{
						name: "두번째",
						desc: "해당 노드 상세보기"
					}]
				}]
			},
			searchAndOrder: {
				mainStyle: "",
				title: "검색과 필터링",
				desc: "서버 이름과 Count로 검색이 가능합니다.",
				category: [{
					title: "[기능]",
					items: [{
						name: "Name",
						desc: "이름을 오름/내림차순 정렬 합니다."
					},{
						name: "Count",
						desc: "갯수를 오름/내림차순 정렬 합니다."
					}]
				}]
			}
		},
		linkInfoDetails: {
			responseSummary: {
				mainStyle: "",
				title: "Response Summary Chart",
				desc: "응답결과 요약 입니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "X축",
						desc: "트랜잭션 응답시간 요약 단위"
					},{
						name: "Y축",
						desc: "트랜잭션의 개수"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "<span class='label label-info'>0초 <= 응답시간 < 1초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "<span class='label label-info'>1초 <= 응답시간 < 3초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "<span class='label label-info'>3초 <= 응답시간 < 5초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "<span class='label label-info'>5초 <= 응답시간</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "응답시간과 무관하게 실패한 트랜잭션의 수"
					}]
				},{
					title: "[기능]",
					list: ["바(bar)를 클릭하면 해당 응답시간에 속한 트랜잭션의 목록을 조회합니다."]
				}]
			},
			load: {
				mainStyle: "",
				title: "Load Chart",
				desc: "시간별 트랜잭션의 응답 결과입니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "X축",
						desc: "트랜잭션이 실행된 시간 (분단위)"
					},{
						name: "Y축",
						desc: "트랜잭션의 개수"
					},{
						name: "<spanstyle='color:#2ca02c'>1s</span>",
						desc: "<span class='label label-info'>0초 <= 응답시간 < 1초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#3c81fa'>3s</span>",
						desc: "<span class='label label-info'>1초 <= 응답시간 < 3초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f8c731'>5s</span>",
						desc: "<span class='label label-info'>3초 <= 응답시간 < 5초</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f69124'>Slow</span>",
						desc: "<span class='label label-info'>5초 <= 응답시간</span> 에 해당하는 성공한 트랜잭션의 수"
					},{
						name: "<span style='color:#f53034'>Error</span>",
						desc: "응답시간과 무관하게 실패한 트랜잭션의 수"
					}]
				},{
					title: "[기능]",
					list: [
				       "범례를 클릭하여 해당 응답시간에 속한 트랜잭션을 차트에서 제외하거나 포함 할 수 있습니다.",
				       "마우스로 드래그하여 드래그 한 범위를 확대할 수 있습니다."
					]
				}]
			},
			linkServers: {
				mainStyle: "width:350px;",
				title: "Server Information",
				desc: "해당 구간을 통과하는 트랜잭션을 호출한 서버 인스턴스의 정보입니다. (호출자)",
				category: [{
					title: "[범례]",
					items: [{
						name: "<span class='glyphicon glyphicon-hdd'></span>",
						desc: "물리서버에 설치된 서버 인스턴스에서 동작중인 Pinpoint의 agentId입니다."
					}]
				},{
					title: "[기능]",
					items: [{
						name: "<button type='button' class='btn btn-default btn-xs'>Inspector</button>",
						desc: "Pinpoint가 설치된 WAS의 상세한 정보를 보여줍니다."
					},{
						name: "<button type='button' class='btn btn-default btn-xs'><span class='glyphicon glyphicon-plus'></span></button>",
						desc: "해당 인스턴스에서 처리된 트랜잭션 통계를 조회할 수 있습니다."
					},{
						name: "<button type='button' class='btn btn-danger btn-xs'><span class='glyphicon glyphicon-plus'></span></button>",
						desc: "에러를 발생시킨 트랜잭션이 포함되어있다는 의미입니다."
					}]
				}]
			},
			unknownList: {
				mainStyle: "",
				title: "UnknownList",
				desc: "차트 오른쪽 상단의 아이콘부터",
				category: [{
					title: "[기능]",
					items: [{
						name: "첫번째",
						desc: "Response Summary"
					},{
						name: "두번째",
						desc: "해당 노드 상세보기"
					}]
				}]
			},
			searchAndOrder: {
				mainStyle: "",
				title: "검색과 필터링",
				desc: "서버 이름과 Count로 검색이 가능합니다.",
				category: [{
					title: "[기능]",
					items: [{
						name: "Name",
						desc: "이름을 오름/내림차순 정렬 합니다."
					},{
						name: "Count",
						desc: "갯수를 오름/내림차순 정렬 합니다."
					}]
				}]
			}
		},
		inspector: {
			list: {
				mainStyle: "",
				title: "Agent 리스트",
				desc: "현 Appliation Name에 등록된 agent 리스트입니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "<span class='glyphicon glyphicon-home'></span>",
						desc: "Agent가 설치된 장비의 호스트 이름"
					},{
						name: "<span class='glyphicon glyphicon-hdd'></span>",
						desc: "설치된 agent의 agent-id"
					},{
						name: "<span class='glyphicon glyphicon-ok-sign' style='color:#40E340'></span>",
						desc: "정상적으로 실행중인 agent 상태 표시"
					},{
						name: "<span class='glyphicon glyphicon-minus-sign' style='color:#F00'></span>",
						desc: "Shutdown 된 agent 상태 표시"
					},{
						name: "<span class='glyphicon glyphicon-remove-sign' style='color:#AAA'></span>",
						desc: "연결이 끊긴 agent 상태 표시"
					},{
						name: "<span class='glyphicon glyphicon-question-sign' style='color:#AAA'></span>",
						desc: "알수 없는 상태의 agent 상태 표시"
					}]
				}]
			},
			heap: {
				mainStyle: "",
				title: "Heap",
				desc: "JVM의 heap 정보와 full garbage collection 소요 시간",
				category: [{
					title: "[범례]",
					items: [{
						name: "Max",
						desc: "최대 heap 사이즈"
					},{
						name: "Used",
						desc: "현재 사용 중인 heap 사이즈"
					},{
						name: "FGC",
						desc: "Full garbage collection의 총 소요 시간(2번 이상 발생 시, 괄호 안에 발생 횟수 표시)"
					}]
				}]
			},
			permGen: {
				mainStyle: "",
				title: "PermGen",
				desc: "JVM의 PermGen 정보와 full garbage collection 소요 시간",
				category: [{
					title: "[범례]",
					items: [{
						name: "Max",
						desc: "최대 heap 사이즈"
					},{
						name: "Used",
						desc: "현재 사용 중인 heap 사이즈"
					},{
						name: "FGC",
						desc: "Full garbage collection의 총 소요 시간(2번 이상 발생 시, 괄호 안에 발생 횟수 표시)"
					}]
				}]
			},
			cpuUsage: {
				mainStyle: "",
				title: "Cpu Usage",
				desc: "JVM과 시스템의 CPU 사용량 - 멀티코어 CPU의 경우, 전체 코어 사용량의 평균입니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "Java 1.6",
						desc: "JVM의 CPU 사용량만 수집됩니다."
					},{
						name: "Java 1.7+",
						desc: "JVM과 전체 시스템의 CPU 사용량 모두 수집됩니다."
					}]
				}]
			},
			tps: {
                mainStyle: "",
                title: "TPS",
                desc: "서버로 인입된 초당 트랜잭션 수",
                category: [{
                    title: "[범례]",
                    items: [{
                        name: "Sampled New (S.N)",
                        desc: "선택된 agent에서 시작한 샘플링된 트랜잭션"
                    },{
                        name: "Sampled Continuation (S.C)",
                        desc: "다른 agent에서 시작한 샘플링된 트랜잭션"
                    },{
                        name: "Unsampled New (U.N)",
                        desc: "선택된 agent에서 시작한 샘플링되지 않은 트랜잭션"
                    },{
                        name: "Unsampled Continuation (U.C)",
                        desc: "다른 agent에서 시작한 샘플링되지 않은 트랜잭션"
                    },{
                        name: "Total",
                        desc: "모든 트랜잭션"
                    }]
                }]
            },
			activeThread: {
				mainStyle: "",
				title: "Active Thread",
				desc: "사용자 request를 처리하는 agent의 active thread 현황을 보여줍니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "Fast (1s)",
						desc: "현재 소요시간이 1초 이하인 thread 갯수"
					},{
						name: "Normal (3s)",
						desc: "현재 소요시간이 1초 초과, 3초 이하인 thread 갯수"
					},{
						name: "Slow (5s)",
						desc: "현재 소요시간이 3초 초과, 5초 이하인 thread 갯수"
					},{
						name: "Very Slow (slow)",
						desc: "현재 소요시간이 5초를 넘고 있는 thread 갯수"
					}]
				}]
			},
			dataSource: {
				mainStyle: "",
				title: "Data Source",
				desc: "에이전트의 DataSource 현황을 보여줍니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "Active Avg",
						desc: "사용한 Connection의 평균 갯수"
					},{
						name: "Active Max",
						desc: "사용한 Connection의 최대 갯수"
					},{
						name: "Total Max",
						desc: "사용이 가능한 Connection의 최대 갯수"
					},{
						name: "Type",
						desc: "DB Connection Pool 종류"
					}]
				}]
			},
			responseTime: {
				mainStyle: "",
				title: "Response time",
				desc: "에이전트의 Response Time의 현황을 보여줍니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "Avg",
						desc: "평균 Response Time (단위 millisecond)"
					}]
				}]
			},
			wrongApp: [
				"<div style='font-size:12px'>해당 agent는 {{application1}}이 아닌 {{application2}}에 포함되어 있습니다.<br>",
				"원인은 다음 중 하나입니다.<hr>",
				"1. 해당 agent가 {{application1}}에서 {{application2}}으로 이동한 경우<br>",
				"2.{{agentId}}의 agent가 {{application2}}에도 등록 된 경우<hr>",
				"1의 경우 {{application1}}과 {{agentId}}간의 매핑 저보를 삭제해야 합니다<br>",
				"2의 경우 중복 등록 된 agent의 id를 변경해야 합니다.</div>"
			].join(""),
			statHeap: {
				mainStyle: "",
				title: "Heap",
				desc: "Agent들이 사용하는 JVM Heap 사이즈 정보",
				category: [{
					title: "[범례]",
					items: [{
						name: "MAX",
						desc: "Agent들이  사용하는  Heap 중 가장 큰 값"
					},{
						name: "AVG",
						desc: "Agent들이 사용하는 Heap의 평균값"
					},{
						name: "MIN",
						desc: "Agent들이 사용하는 Heap 중 가장 작은 값"
					}]
				}]
			},
			statPermGen: {
				mainStyle: "",
				title: "PermGen",
				desc: "Agent들이 사용하는 JVM Permgen 사이즈 정보",
				category: [{
					title: "[범례]",
					items: [{
						name: "MAX",
						desc: "Agent들이 사용하는 perm 중 가장 큰 값"
					},{
						name: "AVG",
						desc: "Agent들이 사용하는 perm의 평균값"
					},{
						name: "MIN",
						desc: "Agent들이 사용하는 perm 중 가장 작은 값"
					}]
				}]
			},
			statJVMCpu: {
				mainStyle: "",
				title: "JVM Cpu Usage",
				desc: "Agent들이 사용하는 JVM cpu 사용량 - 멀티코어 CPU의 경우, 전체 코어 사용량의 평균입니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "MAX",
						desc: "Agent들이 사용하는 JVM cpu 사용량 중 가장 큰 값"
					},{
						name: "AVG",
						desc: "Agent들이 사용하는 JVM cpu 사용량의 평균값"
					},{
						name: "MIN",
						desc: "Agent들이 사용하는 JVM cpu 사용량 중 가장 작은 값"
					}]
				}]
			},
			statSystemCpu: {
				mainStyle: "",
				title: "System Cpu Usage",
				desc: "Agent 서버들의 시스템 cpu 사용량 - 멀티코어 CPU의 경우, 전체 코어 사용량의 평균입니다.",
				category: [{
					title: "[범례]",
					items: [{
						name: "MAX",
						desc: "Agent 서버들의 시스템 cpu 사용량 중 가장 큰 값"
					},{
						name: "AVG",
						desc: "Agent 서버들의 시스템 cpu 사용량 평균값"
					},{
						name: "MIN",
						desc: "Agent 서버들의 시스템 cpu 사용량 중 가장 작은 값"
					}]
				},{
					title: "[참고]",
					items: [{
						name: "Java 1.6",
						desc: "시스템 CPU 사용량은 수집되지 않습니다."
					},{
						name: "Java 1.7+",
						desc: "Java1.7+ 시스템 CPU 사용량이 수집됩니다."
					}]
				}]
			},
			statTPS: {
				mainStyle: "",
				title: "TPS",
				desc: "Agent들에 인입된 초당 트랜잭션 수",
				category: [{
					title: "[범례]",
					items: [{
						name: "MAX",
						desc: "Agent들의 트랜잭션 수 중 가장 큰 값"
					},{
						name: "AVG",
						desc: "Agent들의 트랙잭션 수의 평균값"
					},{
						name: "MIN",
						desc: "Agent들의 트랜잭션 수 중 가장 작은 값"
					}]
				}]
			}
		},
		callTree: {
			column: {
				mainStyle: "",
				title: "Call Tree",
				desc: "Call Tree의 컬럼명을 설명합니다.",
				category: [{
					title: "[컬럼]",
					items: [{
						name: "Gap",
						desc: "이전 메소드가 시작된 후 현재 메소드를 실행하기 까지의 지연 시간"
					},{
						name: "Exec",
						desc: "메소드 시작부터 종료까지의 시간"
					},{
						name: "Exec(%)",
						desc: "<img src='images/help/callTree_01.png'/>"
					},{
						name: "",
						desc: "<span style='background-color:#FFFFFF;color:#5bc0de'>옅은 파란색</span><br/>트랜잭션 전체 실행 시간 대 exec 시간의 비율"
					},{
						name: "",
						desc: "<span style='background-color:#FFFFFF;color:#4343C8'>진한 파란색</span><br/>self 시간 비율"
					},{
						name: "Self",
						desc: "메소드 자체의 시작부터 종료까지의 시간으로 하위의 메소드가 실행된 시간을 제외한 값"
					}]
				}]
			}
		},
		transactionTable: {
			log: {}
		},
		transactionList: {
			openError: {
				noParent: "부모 윈도우의 scatter chart 정보가 변경되어 더 이상 transaction 정보를 표시할 수 없습니다.",
				noData: "부모 윈도우에 {{application}} scatter chart 정보가 없습니다."
			}
		}
	};
	pinpointApp.constant('helpContent-ko', oHelp );
})();