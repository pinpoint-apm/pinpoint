var transactionsCache;
var prevDetaildRow;

function showTransactionList(transactions) {
	transactionsCache = transactions;
	var html = [];
	for (var i = 0; i < transactions.length; i++) {
	    html.push("<tr>");
	
	    html.push("<td><div onclick='openTransactionDetails(");
	    html.push(i);
	    html.push(", this); return false;' style='cursor:pointer;'>");
	    html.push(transactions[i].name);
	    html.push("</div></td>");
	
	    html.push("<td class='calls' sorttable_customkey='" + transactions[i].calls + "'>");
	    html.push(formatNumber(transactions[i].calls));
	    html.push("</td>");
	
	    html.push("<td class='time' sorttable_customkey='" + transactions[i].avgTime + "'>");
	    html.push(formatNumber(transactions[i].avgTime));
	    html.push("</td>");
	
	    html.push("<td class='time' sorttable_customkey='" + transactions[i].minTime + "'>");
	    html.push(formatNumber(transactions[i].minTime));
	    html.push("</td>");
	
	    html.push("<td class='time' sorttable_customkey='" + transactions[i].maxTime + "'>");
	    html.push(formatNumber(transactions[i].maxTime));
	    html.push("</td>");
	
	    html.push("</tr>");
	}
	$("#businessTransactions TBODY").append(html.join(''));	
}

function openTransactionDetails(index, row) {
    if (prevDetaildRow != null) {
        prevDetaildRow.css({'background-color':'#FFFFFF'});
    }
    var currentRow = $(row).parent().parent();
    currentRow.css({'background-color':'#FFFF00'});
    prevDetaildRow = currentRow;

    $("#businessTransactionsDetail TBODY").empty();

    var traces = transactionsCache[index].traces;
    var html = [];
    for (var i = 0; i < traces.length; i++) {
        html.push("<tr>");

        html.push("<td>");
        html.push(i + 1);
        html.push("</td>");
        
        html.push("<td>");
        html.push(new Date(traces[i].timestamp));
        html.push("</td>");

        html.push("<td><a href='#' onclick='openTrace(\"");
        html.push(traces[i].traceId);
        html.push("\", -1); return false;' style='cursor:pointer;'>");
        html.push(traces[i].traceId);
        html.push("</a></td>");

        html.push("<td sorttable_customkey='" + traces[i].executionTime + "'>");
        html.push(traces[i].executionTime);
        html.push("</td>");

        html.push("</tr>");
    }
    $("#businessTransactionsDetail TBODY").append(html.join(''));
}