var _PinpointNavigationBar = $.Class({
	$init : function(htOption) {
		this._application = $("#application");
		this._period = $("#period");
		this._date = $("#date");
		this._time = $("#time");
	},
	isSelected : function() {
		return this._application.val().length > 0;
	},
	getApplicationName : function() {
		return this._application.val().split("@")[0];
	},
	getServiceType : function() {
		return this._application.val().split("@")[1];
	},
	getQueryPeriod : function() {
		return this._period.find("button.active").val().split(",") * 1000 * 60;
	},
	getQueryStartTime : function() {
		return this.getQueryEndTime() - this.getQueryPeriod();
	},
	getQueryEndTime : function() {
		var now = new Date();
		var input = Date.parse(this._date.val() + ' ' + this._time.val()) || now;
		if (input.getTime() > now.getTime()) {
			return now;
		}
		
		// 초단위 무시.
		// input.setSeconds(0);
		
		// 5분 단위로 조회
		// 일단 사용안함.
		// input.setMinutes(Math.floor(input.getMinutes() / 5 + 0.9) * 5);
		
		// 0초~59초 사이에 저장된 값이 조회되도록 검색 종료 시간에 1분을 더한다.
		return input.getTime(); // + 60000;
	},
	setQueryDateToNow : function() {
	   	var date = new Date();
	   	this._date.val(date.toString("yyyy/MM/dd"));
	   	this._time.val(date.toString("hh:mm"));
	},
	isQueryFromNow : function() {
		return false;
		// return $(".btn#now.active").length > 0;
	},
	isHideIndirectAccess : function() {
		return $("#hideIndirectAccess").data('selected');
	}
});