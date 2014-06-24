'use strict';

pinpointApp
  .constant('helpContent', {
        "navbar": {
            "applicationSelector": "<div style='width:400px;'>응용프로그램 목록<br/>핀포인트가 설치된 응용프로그램 목록 입니다.<br/><br/>" +
            		"[범례]<br/>" +
            		"<ul>" +
            		"<li>아이콘 : 응용프로그램의 종류</li>" +
            		"<li>텍스트 : 응용프로그램의 이름입니다. Pinpoint agent설정에서 applicationName에 지정한 값입니다.</li>" +
            		"</ul>" +
            		"</div>",
            "periodSelector": "조회 시간 설정<br/>데이터조회 시간을 선택합니다.<br/><br/>" +
            		"[기능]<br/>" +
            		"<ul>" +
            		"<li><button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-th-list'></span></button> : 현재 시간을 기준으로 선택한 시간 이전부터 현재시간 사이에 수집된 데이터를 조회합니다.<br/>최근 5m, 10m, 3h조회는 자동 새로고침 기능을 지원합니다.</li>" +
            		"<li><button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-calendar'></span></button> : 지정된 시간 사이에 수집된 데이터를 조회합니다. 조회 시간은 분단위로 최대 48시간을 지정할 수 있습니다.</li>" +
            		"</ul>"
        },
        "servermap": {
            "default": "<div style='width:300px;'>서버맵<br/>분산된 서버를 도식화한 지도 입니다.<br/><br/>" +
            		"[박스]<br/>" +
            		"<ul>" +
            		"<li>박스는 응용프로그램 그룹을 나타냅니다.</li>" +
            		"<li>우측의 숫자는 응용프로그램 그룹에 속한 서버 인스턴스의 개수입니다. (한 개일 때에는 숫자를 보여주지 않습니다.</li>" +
            		"<li>좌측 빨간 알람은 임계값을 초과한 모니터링 항목이 있을 때 나타납니다.</li>" +
            		"</ul>" +
            		"[화살표]<br/>" +
            		"<ul>" +
            		"<li>화살표는 트랜잭션의 흐름을 나타냅니다.</li>" +
            		"<li>화살표의 숫자는 호출 수 입니다. 임계치 이상의 에러를 포함하면 빨간색으로 보여집니다.</li>" +
            		"<li><span class='glyphicon glyphicon-filter'></span> : 필터가 적용되면 아이콘이 표시됩니다.</li>" +
            		"</ul>" +
            		"[박스의 기능]<br/>" +
    				"<ul>" +
            		"<li>박스를 선택하면 어플리케이션으로 유입된 트랜잭션 정보를 화면 우측에 보여줍니다.</li>" +
            		"</ul>" +
            		"[화살표의 기능]<br/>" +
            		"<ul>" +
            		"<li>화살표를 선택하여 선택된 구간을 통과하는 트랜잭션의 정보를 화면 우측에 보여줍니다.</li>" +
            		"<li>Context menu의 Filter는 선택된 구간을 통과하는 트랜잭션만 모아서 보여줍니다.</li>" +
            		"<li>Filter wizard는 보다 상세한 필터 설정을 할 수 있습니다.</li>" +
            		"<li>필터가 적용되면 화살표에 <span class='glyphicon glyphicon-filter'></span>아이콘이 표시됩니다.</li>" +
            		"</ul>" +
            		"[차트 설정]<br/>" +
            		"<ul>" +
            		"<li>비어있는 부분을 마우스 오른쪽 클릭하여 context menu를 열면 차트 설정메뉴가 보입니다.</li>" +
            		"<li>Node Setting / Merge Unknown : agent가 설치되어있지 않은 응용프로그램을 하나의 박스로 보여줍니다.</li>" +
            		"<li>비어있는 부분 더블클릭 : 줌을 초기화 합니다.</li>" +
            		"</ul>" +
            		"</div>"
        },
        "scatter": {
            "default": "<div style='width:400px'>Scatter Chart<br/>수집된 트랜잭션의 응답시간 분포도입니다.<br/><br/>" +
            		"[범례]<br/>" +
            		"<ul>" +
            		"<li><span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span> : 에러가 없는 트랜잭션 (Success)</li>" +
            		"<li><span class='glyphicon glyphicon-stop' style='color:#f53034'></span> : 에러를 포함한 트랜잭션 (Failed)</li>" +
            		"<li>X축 : 트랜잭션이 실행된 시간 (시:분)</li>" +
            		"<li>Y축 : 트랜잭션의 응답 속도 (ms)</li>" +
            		"</ul>" +
            		"[기능]<br/>" +
            		"<ul>" +
            		"<li><span class='glyphicon glyphicon-plus'></span> : 마우스로 영역을 드래그하여 드래그 된 영역에 속한 트랜잭션의 상세정보를 조회할 수 있습니다.</li>" +
            		"<li><span class='glyphicon glyphicon-cog'></span> : 응답시간(Y축)의 최대 또는 최소값을 변경할 수 있습니다.</li>" +
            		"<li><span class='glyphicon glyphicon-download-alt'></span> : 차트를 이미지파일로 다운로드합니다.</li>" +
            		"<li><span class='glyphicon glyphicon-fullscreen'></span> : 차트를 새창으로 크게 보여줍니다.</li>" +
            		"</ul></div>"
        },
        "nodeInfoDetails": {
            "responseSummary": "<div style='width:400px'>Response Summary Chart<br/>응답결과 요약 입니다.<br/><br/>" +
		            "[범례]<br/>" +
		            "<ul>" +
            		"<li>X축 : 트랜잭션 응답시간 요약 단위</li>" +
            		"<li>Y축 : 트랜잭션의 개수</li>" +
		            "<li><span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span> : '0초 <= 응답시간 < 1초'에 해당하는 성공한 트랜잭션의 수</li>" +
		            "<li><span class='glyphicon glyphicon-stop' style='color:#3c81fa'></span> : '1초 <= 응답시간 < 3초'에 해당하는 성공한 트랜잭션의 수</li>" +
		            "<li><span class='glyphicon glyphicon-stop' style='color:#f8c731'></span> : '3초 <= 응답시간 < 5초'에 해당하는 성공한 트랜잭션의 수</li>" +
		            "<li><span class='glyphicon glyphicon-stop' style='color:#f69124'></span> : '5초 <= 응답시간'에 해당하는 성공한 트랜잭션의 수</li>" +
		            "<li><span class='glyphicon glyphicon-stop' style='color:#f53034'></span> : 응답시관과 무관하게 실패한 트랜잭션의 수</li>" +
		            "</ul>" +
            		"[기능]<br/>" +
            		"<ul>" +
            		"<li>바(bar)를 클릭하면 해당 응답시간에 속한 트랜잭션의 목록을 조회합니다.</li>" +
            		"</ul>" +
            		"</div>",
            "load": "<div style='width:400px'>Load Chart<br/>시간별 트랜잭션의 응답 결과입니다.<br/><br/>" +
            		"[범례]<br/>" +
		            "<ul>" +
            		"<li>X축 : 트랜잭션이 실행된 시간 (분단위)</li>" +
            		"<li>Y축 : 트랜잭션의 개수</li>" +
		            "<li><span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span> : '0초 <= 응답시간 < 1초'에 해당하는 성공한 트랜잭션의 수</li>" +
		            "<li><span class='glyphicon glyphicon-stop' style='color:#3c81fa'></span> : '1초 <= 응답시간 < 3초'에 해당하는 성공한 트랜잭션의 수</li>" +
		            "<li><span class='glyphicon glyphicon-stop' style='color:#f8c731'></span> : '3초 <= 응답시간 < 5초'에 해당하는 성공한 트랜잭션의 수</li>" +
		            "<li><span class='glyphicon glyphicon-stop' style='color:#f69124'></span> : '5초 <= 응답시간'에 해당하는 성공한 트랜잭션의 수</li>" +
		            "<li><span class='glyphicon glyphicon-stop' style='color:#f53034'></span> : 응답시관과 무관하게 실패한 트랜잭션의 수</li>" +
		            "</ul>" +
		            "[기능]<br/>" +
            		"<ul>" +
            		"<li>범례를 클릭하여 해당 응답시간에 속한 트랜잭션을 차트에서 제외하거나 포함 할 수 있습니다.</li>" +
            		"<li>마우스로 드래그하여 드래그 한 범위를 확대할 수 있습니다.</li>" +
            		"</ul>" +
		    		"</div>",
            "nodeServers": "Server Information<br/>물리서버와 해당 서버에서 동작중인 서버 인스턴스의 정보를 보여줍니다.<br/><br/>" +
            		"[범례]<br/>" +
            		"<ul>" +
            		"<li><span class='glyphicon glyphicon-home'></span> : 물리서버의 호스트이름입니다.</li>" +
            		"<li><span class='glyphicon glyphicon-hdd'></span> : 물리서버에 설치된 서버 인스턴스에서 동작중인 Pinpoint의 agentId입니다.</li>" +
            		"</ul>" +
            		"[기능]<br/>" +
            		"<ul>" +
            		"<li><button type='button' class='btn btn-default btn-xs'>NSight</button> : nsight에서 물리서버 상태를 조회할 수 있습니다.</li>" +
            		"<li><button type='button' class='btn btn-default btn-xs'>Inspector</button> : Pinpoint가 설치된 WAS의 상세한 정보를 보여줍니다.</li>" +
            		"<li><button type='button' class='btn btn-default btn-xs'><span class='glyphicon glyphicon-plus'></span></button> : 해당 인스턴스에서 처리된 트랜잭션 통계를 조회할 수 있습니다. 빨간색 버튼은 에러를 발생시킨 트랜잭션이 포함되어있다는 의미를 갖습니다.</li>" +
            		"</ul>",
            "unknownList": '차트 오른쪽 상단의 아이콘부터,<br>첫번째 : Response Summary / Load 차트 변환<br>두번째 : 해당 노드 상세보기',
            "searchAndOrder": '서버 이름과 Count로 검색이 가능합니다.<br>Name, Count 클릭시 오름/내림차순 정렬 됩니다.'
        },
        "linkInfoDetails": {
            "responseSummary": "<div style='width:370px'>응답결과 요약 입니다.<br/>" +
                "바(bar)를 클릭하면 해당 응답시간에 속한 트랜잭션의 목록을 조회합니다.<br/>" +
                "<ul>" +
                "<li>1s : 응답시간이 1초 이내인 성공한 트랜잭션의 개수</li>" +
                "<li>3s : 응답시간이 1초 ~ 3초인 성공한 트랜잭션의 개수</li>" +
                "<li>5s : 응답시간이 3초 ~ 5초인 성공한 트랜잭션의 개수</li>" +
                "<li>slow : 응답시간이 5초를 초과한 성공한 트랜잭션의 개수</li>" +
                "<li>error : 실패한 트랜잭션의 개수 (응답시간과 무관합니다.)</li>" +
                "</ul></div>"
            ,
            "load": "<div style='width:370px'>시간별 트랜잭션의 응답 결과입니다.<br/>" +
                "범례를 클릭하여 해당 응답시간에 속한 트랜잭션을 차트에서 제외하거나 포함 할 수 있습니다.<br/>" +
                "<ul>" +
                "<li>1s : 응답시간이 1초 이내인 성공한 트랜잭션의 개수</li>" +
                "<li>3s : 응답시간이 1초 ~ 3초인 성공한 트랜잭션의 개수</li>" +
                "<li>5s : 응답시간이 3초 ~ 5초인 성공한 트랜잭션의 개수</li>" +
                "<li>slow : 응답시간이 5초를 초과한 성공한 트랜잭션의 개수</li>" +
                "<li>error : 실패한 트랜잭션의 개수 (응답시간과 무관합니다.)</li>" +
                "</ul></div>",
            "linkServers": '인스턴스 정보 입니다.',
            "unknownList": '차트 오른쪽 상단의 아이콘부터,<br>첫번째 : Response Summary / Load 차트 변환<br>두번째 : 해당 노드 상세보기',
            "searchAndOrder": '서버 이름과 Count로 검색이 가능합니다.<br>Name, Count 클릭시 오름/내림차순 정렬 됩니다.'
        }
    });
