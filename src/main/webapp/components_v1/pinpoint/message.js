function addWarning(title, msg) {
	var html = [];
	html.push('<div class="alert">');
	html.push('<button type="button" class="close" data-dismiss="alert">&times;</button>');
	html.push('<strong>' + title + '</strong>');
	html.push('<span>' + msg + '</span>');
	html.push('</div>');
	
	$("#warningMessage").append(html.join(''));
}

function warning(title, msg) {
	$("#warningMessage").empty();
	addWarning(title, msg);
}

function clearAllWarnings() {
	$("#warningMessage").empty();
}