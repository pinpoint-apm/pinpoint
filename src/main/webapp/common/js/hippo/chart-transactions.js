function showRequests(applicationName, from, to, period, usePeriod, filter) {
	console.log("showRequests", applicationName, from, to, period, usePeriod);
    if (usePeriod) {
    	window.open("/lastTransactionList.hippo?application=" + applicationName + "&period=" + period + ((filter) ? "&filter=" + filter : ""));
    } else {
    	window.open("/transactionList.hippo?application=" + applicationName + "&from=" + from + "&to=" + to + ((filter) ? "&filter=" + filter : ""));
    }
}