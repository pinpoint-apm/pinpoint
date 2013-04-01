var helptext = {
		// Focus on passing transactions
		1001 : "선택한 구간이나 리소스를 사용하는 요청으로 서버 지도를 다시 그립니다.",
		// Response scatter chart
		1002 : "응답시간에 대한 scatter chart를 조회합니다.",
		// Show Requests
		1003 : "선택한 구간이나 리소스를 통과하는 요청들의 목록을 조회합니다.",
		// response statistics 
		1004 : "선택한 구간을 지나는 요청의 응답시간 분포입니다.",
		
		// scatter chart란?
		2001 : "",
		// request list
		2002 : ""
}

function help(id) {
	alert(helptext[id]);
}