function showRequests(applicationName, from, to, period, usePeriod, filter) {
	console.log("showRequests", applicationName, from, to, period, usePeriod);
    if (usePeriod) {
    	window.open("/lastTransactionList.pinpoint?application=" + applicationName + "&period=" + period + ((filter) ? "&filter=" + filter : ""));
    } else {
    	window.open("/transactionList.pinpoint?application=" + applicationName + "&from=" + from + "&to=" + to + ((filter) ? "&filter=" + filter : ""));
    }
}