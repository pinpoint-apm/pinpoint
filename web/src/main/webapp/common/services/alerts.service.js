(function() {
	'use strict';
	/**
	 * (en)AlertsService 
	 * @ko AlertsService.
	 * @group Service
	 * @name AlertsService
	 * @class
	 */
	pinpointApp.factory('AlertsService', ['$timeout', function ($timeout) {
	
	    return function (parent) {
	        this.$parent = parent || null;
	
	        this.setParent = function (parent) {
	            this.$parent = parent;
	            return this;
	        }.bind(this);
	
	        this.getParent = function () {
	            return this.$parent;
	        }.bind(this);
	
	        this.showError = function (vResult) {
	            $timeout(function () {
	            	this.getElement('.error').show();
	            	if ( typeof vResult == "string" ) {
	            		this.getElement('.error .msg').text(vResult);
	            	} else {
	            		this.getElement('.error .msg').text(vResult.message);
	            		this.getElement('.error .method').text(vResult.request.method);
	            		this.getElement('.error .header').html(this._transTableFormat(vResult.request.heads));
	            		this.getElement('.error .parameters').html(this._transTableFormat(vResult.request.parameters));
	            		this.getElement('.error .url').text(vResult.request.url);
	            		this.getElement('.error .stacktrace').text(vResult.stacktrace);
	            	}
	            }.bind(this), 300);
	        }.bind(this);
	        this.hideError = function () {
	            $timeout(function () {
	                this.getElement('.error').hide();
	            }.bind(this));
	        }.bind(this);
	
	        this.showWarning = function (msg) {
	            $timeout(function () {
	                this.getElement('.warning').show();
	                this.getElement('.warning .msg').text(msg);
	            }.bind(this), 300);
	        }.bind(this);
	        this.hideWarning = function () {
	            $timeout(function () {
	                this.getElement('.warning').hide();
	            }.bind(this));
	        }.bind(this);
	
	        this.showInfo = function (msg) {
	            $timeout(function () {
	                this.getElement('.info').show();
	                this.getElement('.info .msg').html(msg);
	            }.bind(this), 300);
	        }.bind(this);
	        this.hideInfo = function () {
	            $timeout(function () {
	                this.getElement('.info').hide();
	            }.bind(this));
	        }.bind(this);
	
	        this.getElement = function (selector) {
	            return this.$parent ? $(selector, this.$parent) : $(selector);
	        }.bind(this);
	        this._transTableFormat = function( obj ) {
	        	var str = [ "<table>"];
	        	for( var p in obj ) {
	        		str.push("<tr>");
	        		str.push("<td>" + p + "</td><td style='padding-left:20px'>" + obj[p] + "</td>");
	        		str.push("</tr>");
	        	}
	        	str.push("</table>");
	        	return str.join("");
	        };
	    };
	}]);
})();