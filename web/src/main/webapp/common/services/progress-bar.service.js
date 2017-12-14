(function() {
	'use strict';
	/**
	 * (en)ProgressBarService 
	 * @ko ProgressBarService
	 * @group Service
	 * @name ProgressBarService
	 * @class
	 */
	pinpointApp.factory( "ProgressBarService", [ "$timeout", "$window", "$location", "CommonUtilService", "UserLocalesService", function ($timeout, $window, $location, CommonUtilService, UserLocalesService) {
		var AVAILABLE_LOCALE = [ "ko", "en" ];
		var TIP_MAX_COUNT = 5;
		var STORAGE_NAME = "__HIDE_LOADING_TIP";
		
	    return function (parent) {
	    	
	        this.$parent = parent || null;
	        this.nPercentage = 0;
	        this.bAutoIncrease = true;
	        this.nTimePromise = null;
	
	        this.setParent = function (parent) {
	            this.$parent = parent;
	            return this;
	        }.bind(this);
	
	        this.getParent = function () {
	            return this.$parent;
	        }.bind(this);
	
	        this.startLoading = function (autoIncrease) {
	        	var bShowTip = true;
	        	if ( $window.localStorage ) {
	        		var savedTipData = $window.localStorage.getItem( STORAGE_NAME ) || "-";
	        		if ( savedTipData === "-" ) {
	        			bShowTip = true;
	        		} else {
	        			bShowTip = ( new Date().valueOf() < parseInt( savedTipData ) ) ? false : true;
	        		}
	        	}
	            this.bAutoIncrease = autoIncrease || true;
	            this.setLoading(0);
	            $timeout(function () {
	            	if ( /^\/main/.test( $location.path() ) && bShowTip ) {
		            	this.showBackground();
		            	this.showTip();
	            	}
	                this.getProgress().show();
	                this.autoIncrease();
	            }.bind(this));
	        }.bind(this);
	
	        this.stopLoading = function () {
	            $timeout.cancel(this.nTimePromise);
	            $timeout(function () {
	                this.getProgress().hide();
	                this.hideTip();
	                this.hideBackground();
	            }.bind(this), 300);
	        }.bind(this);
	
	        this.setLoading = function (p) {
	            this.nPercentage = p;
	            this.getProgressBar().width(p + '%');
	            return this;
	        }.bind(this);
	
	        this.getProgress = function () {
	            return this.$parent ? $('.progress', this.$parent) : $('.progress');
	        }.bind(this);
	
	        this.getProgressBar = function () {
	            return this.$parent ? $('.progress .bar', this.$parent) : $('.progress .bar');
	        }.bind(this);
	
	        this.autoIncrease = function () {
	            if (this.bAutoIncrease === false) {
	                return;
	            }
	            var nRandom = CommonUtilService.random( 1, 4 );
	            if (this.nPercentage + nRandom <= 99) {
	                this.setLoading(this.nPercentage + nRandom);
	
	                this.nTimePromise = $timeout(function () {
	                    this.autoIncrease();
	                }.bind(this), 500);
	            }
	        }.bind(this);
	        this.showBackground = function() {
//	        	if ( this.$parent ) {
//	        		$('.progress-back', this.$parent).show();
//	        	}
	        }.bind(this);
	        this.showTip = function() {
//	        	if ( this.$parent ) {
//	        		$('.progress-tip img', this.$parent).attr( "src", "/images/tip/tip" + this._getRandomNum() + "_" + this._getLocale() + ".png");
//	        		$('.progress-tip', this.$parent).show();
//	        	}
	        }.bind(this);
	        this._getRandomNum = function() {
	        	var num = parseInt( Math.random() * (TIP_MAX_COUNT+1) );
	        	return num < 10 ? "0" + num : num + "";
	        }.bind(this);
	        this._getLocale = function() {
	        	if ( AVAILABLE_LOCALE.indexOf( UserLocalesService.userLocale ) == -1 ) {
	        		return UserLocalesService.defaultLocale;
	        	} else {
	        		return UserLocalesService.userLocale;
	        	}
	        }.bind(this);
	        this.hideBackground = function() {
	        	if ( this.$parent ) {
	        		$('.progress-back', this.$parent).hide();
	        	}        	
	        }.bind(this);
	        this.hideTip = function() {
	        	if ( this.$parent ) {
	        		$('.progress-tip', this.$parent).hide();
	        	}
	        }.bind(this);
	    };
	}]);
})();