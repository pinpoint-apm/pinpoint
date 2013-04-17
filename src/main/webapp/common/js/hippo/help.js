var helptext = {
		1001 : ["Focus on passing transactions", "선택한 구간이나 리소스를 사용하는 요청으로 서버 지도를 다시 그립니다."],
		1002 : ["Show Requests", "선택한 구간이나 리소스를 통과하는 요청들의 목록을 조회합니다."],
		1003 : ["Response scatter chart", "응답시간에 대한 scatter chart를 조회합니다."],
		1004 : ["Response statistics", "선택한 구간을 지나는 요청의 응답시간 분포입니다."],
		1005 : ["Application name", ""],
		
		
		
		2001 : ["scatter chart", ""],
		2002 : ["request list", ""]
}

function help(id, src) {
	$(src).popover({
		"title" : helptext[id][0] + " <i class='hippo-action-icon icon-remove' onclick='$(this).parent().parent().parent().remove();'></i>",
		"content" : helptext[id][1],
		"trigger" : "click",
		"html" : true
	}).popover('toggle');
}

function man(id) {
	window.open("/help/" + id + ".html", id, "width=800, height=600");
}