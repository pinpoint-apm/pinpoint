(function(){ 
	'use strict';
	/**
	 * (en)한국어 Tooltip 
	 * @ko 한국어 Tooltip
	 * @group Config
	 * @name pinpointApp#helpContent-ko
	 */
	var oHelp = {
		navbar : {
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
				title: "<span class='glyphicon glyphicon-map-marker' aria-hidden='true'></span> Depth",
				desc: "서버맵의 탐색 깊이를 설정합니다."
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
					image: "<img src='/images/help/scatter_01.png' width='200px' height='125px'>",
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
				title: "Server Instance",
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
						name: "FCG",
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
						name: "FCG",
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
						desc: "<img src='/images/help/callTree_01.png'/>"
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
		}
	};
	pinpointApp.constant('helpContent-ko', oHelp );
})();