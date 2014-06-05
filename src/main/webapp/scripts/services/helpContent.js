'use strict';

pinpointApp
  .constant('helpContent', {
        "navbar": {
            "applicationSelector": "핀포인트가 설치된 어플리케이션 목록 입니다.",
            "periodSelector": "조회방법1 : 현재 시간을 기준으로 x 시간 전 조회<br>조회방법2 : 시작 ~ 끝 시간 조회"
        },
        "servermap": {
            "default": "<div style='width:300px;'>분산된 서버를 도식화한 지도 입니다.<br>사용방법1 : 왼쪽 버튼으로 노드/링크를 선택한다.<br>사용방법2 : 오른쪽 버튼으로 노드를 선택한다.<br>사용방법3 : 휠을 이용하여 확대/축소 한다.<br>사용방법4 : 배경을 더블 클릭하여 가운데로 맞춘다.</div>"
        },
        "scatter": {
            "default": '마우스로 드래그 하시면 해당 영역의 트렌젝션을 상세보기 할 수 있습니다.'
        },
        "nodeInfoDetails": {
            "responseSummary": "응답시간 요약 입니다.",
            "load": '시간별 응답 속도 입니다.',
            "nodeServers": '물리서버와 인스터스 정보 입니다.',
            "unknownList": '차트 오른쪽 상단의 아이콘부터,<br>첫번째 : Response Summary / Load 차트 변환<br>두번째 : 해당 노드 상세보기',
            "searchAndOrder": '서버 이름과 Count로 검색이 가능합니다.<br>Name, Count 클릭시 오름/내림차순 정렬 됩니다.'
        },
        "linkInfoDetails": {
            "resonseSummary": "응답시간 요약 입니다.<br>컬럼 클릭으로 필러링 됩니다.",
            "load": '시간별 응답 속도 입니다.',
            "linkServers": '인스터스 정보 입니다.',
            "unknownList": '차트 오른쪽 상단의 아이콘부터,<br>첫번째 : Response Summary / Load 차트 변환<br>두번째 : 해당 노드 상세보기',
            "searchAndOrder": '서버 이름과 Count로 검색이 가능합니다.<br>Name, Count 클릭시 오름/내림차순 정렬 됩니다.'
        }
    });
