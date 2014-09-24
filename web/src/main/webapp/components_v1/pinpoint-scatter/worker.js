onmessage = function(e) {
	var htData = e.data;
	switch(htData.sCmd){
		case 'reqRemoveOldDataLessThan' : 
			//var aBubbles = removeOldDataLessThan(htData.nX, htData.aBubbles);
			postMessage({
				sCmd : 'resRemoveOldDataLessThan',
				aBubbles : htData.aBubbles,
				nX : htData.nX,
				start : htData.start
			});
			break;
		default :
			postMessage('Unkown command : ' + htData.sCmd);
	}
}

function removeOldDataLessThan(nX, aBubbles){
	for(var i = 0, nLen = aBubbles.length; i < nLen; i++) {
		if(aBubbles[i].x > nX){
			aBubbles.splice(0,i-1);
			break;
		}
	}
	return aBubbles;
}