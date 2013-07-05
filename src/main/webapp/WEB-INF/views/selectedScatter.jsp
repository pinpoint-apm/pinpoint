<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type='text/javascript'>
	var minimized = false;
	function isMinimized() {
		return minimized;
	}
	function resizeFrame() {
		if (minimized) {
			restoreFrame();
		} else {
			minimizeFrame();
		}
	}
	function restoreFrame() {
		var fs = document.getElementById('frameset');
		if (fs) {
			fs.rows = '200,*';
			minimized = false;
		}
	}
	function minimizeFrame() {
		var fs = document.getElementById('frameset');
		fs.rows = '0, *';
		minimized = true;
	}
</script>
<frameset id="frameset" rows="200,*" border="3">
	<frame name="selectedScatterList" id="selectedScatterList" src="/selectedScatterList.pinpoint">
	<frame name="transactionView" id="transationView" src="">
</frameset>