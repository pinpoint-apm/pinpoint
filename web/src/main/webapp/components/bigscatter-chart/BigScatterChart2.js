(function(global, $) {
	'use strict';
	function BigScatterChart2( htOption, aAgents, aPlugins, oExternal, agentAllStr ) {
		this._oExternal = oExternal;
		this._AGENT_ALL = this._currentAgent = agentAllStr;
		this._aAgentList = aAgents;

		this._setOption( htOption );
		this._initVariables();
		this._initInnerFeature();
		this._initElements();
		this._initPlugin( aPlugins );
		this._initInnerPlugin();
	}
	BigScatterChart2.prototype.option = function( v, v2 ) {
		var self = this;
		var argLen = arguments.length;
		if ( argLen === 2 ) {
			this._option[v] = v2;
		} else if ( argLen === 1 ) {
			if ( typeof v === "object" ) {
				this._option = this._option || {};
				for( var p in v ) {
					self._option[p] = v[p];
				}
			} else if ( typeof v === "string" ) {
				return this._option[v];
			}
		} else {
			return this._option;
		}
	};
	BigScatterChart2.prototype._setOption = function( htOption ) {
		this.option({
			"sContainerId": "",
			"sPrefix": "bigscatterchart",
			"width": 600,
			"height": 400,
			"minX": 0, "maxX": 100,
			"minY": 0, "maxY": 100,
			"minZ": 0, "maxZ": 1,
			"ticksOfX": 5,
			"ticksOfY": 5,
			"bubbleSize": 10,
			"padding" : {
				"top": 40,
				"left": 50,
				"right": 40,
				"bottom": 30
			},
			"lineColor": "#3d3d3d",
			"bubbleRadius": 3,
			"guideStyle": {
				"lineWidth": 1,
				"lineDash": [1, 0],
				"globalAlpha": 1,
				"strokeStyle" : "#e3e3e3"
			},
			//"sXLabel": "",
			//"sYLabel": "",
			"axisLabelStyle": {
				"font-size": "10px",
				"line-height": "12px",
				"height": "20px",
				"padding-top": "5px"
			},
			"loadingStr": "Loading",
			"noDataStr": "No Data",
			"noDataStyle": {
				"font-size": "15px",
				"color": "#000",
				"font-weight": "bold"
			},
			"bUseMouseGuideLine" : true,
			"checkBoxImage": {
				"checked": "data:image/gif;base64,R0lGODlhDgANANU7APf6/QBoAO31+wBEAABZABSmDfj7/kLFLM/k9CpFeCA7bBs1ZiU/cWDZQBgxYvL4/PD2/ABLAKzQ6wBTAPn8/vD3/FLPNgBzAO30/J/J6JKgu1/YPzBLfzVQhQBvAO31/EhknURgmNTn9NPm9Orz+9fc51LQNztWjfX5/e/2++72+9vh7Njp9kBbk9jp9aCvzd3r+QB3AABhAKXM6SGvFTG5IEtnoBUuXuLu+v//zP///wAAAAAAAAAAAAAAAAAAACH5BAEAADsALAAAAAAOAA0AAAaHwJ1tSCS+VrsdSMdsOmOxXUiHq1ohlFguF2vpMAYDBvYxXDaNxuWkA3gCABIgYLGYAruOTraVoWQHgTIlOxw6BDWJiIoUGjsJOg8TNJSUEw8QCTsMOiwVEQWhERUuOAw7CjoSIykDrioiEjgKOws6MzMIArsIuDgLOw4ZVsRWDkk3ycrLN0lBADs=",
				"unchecked": "data:image/gif;base64,R0lGODlhDgANAPcbANnZ2X9/f3Nzc4eHh4ODg5GRkfDw8IqKim1tbXt7e46Ojnd3d3BwcOzs7NPT09jY2O/v7+3t7eHh4eXl5dXV1enp6d3d3Wtra5OTk/Ly8v///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH/C1hNUCBEYXRhWE1QPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS4wLWMwNjEgNjQuMTQwOTQ5LCAyMDEwLzEyLzA3LTEwOjU3OjAxICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOnhtcE1NPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvbW0vIiB4bWxuczpzdFJlZj0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL3NUeXBlL1Jlc291cmNlUmVmIyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ1M1LjEgTWFjaW50b3NoIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjYxQjZBNkRCQUVEQTExRTI5Q0M1REU5NjlFRThGRDZBIiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjYxQjZBNkRDQUVEQTExRTI5Q0M1REU5NjlFRThGRDZBIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6NjFCNkE2RDlBRURBMTFFMjlDQzVERTk2OUVFOEZENkEiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6NjFCNkE2REFBRURBMTFFMjlDQzVERTk2OUVFOEZENkEiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz4B//79/Pv6+fj39vX08/Lx8O/u7ezr6uno5+bl5OPi4eDf3t3c29rZ2NfW1dTT0tHQz87NzMvKycjHxsXEw8LBwL++vby7urm4t7a1tLOysbCvrq2sq6qpqKempaSjoqGgn56dnJuamZiXlpWUk5KRkI+OjYyLiomIh4aFhIOCgYB/fn18e3p5eHd2dXRzcnFwb25tbGtqaWhnZmVkY2JhYF9eXVxbWllYV1ZVVFNSUVBPTk1MS0pJSEdGRURDQkFAPz49PDs6OTg3NjU0MzIxMC8uLSwrKikoJyYlJCMiISAfHh0cGxoZGBcWFRQTEhEQDw4NDAsKCQgHBgUEAwIBAAAh+QQBAAAbACwAAAAADgANAAAIgwA3YBhIsCCGDRsKaFjIsGGGAhsUaMhAsaJFBRsOaIDAkaMBAxwzHNgwQEOEkygbnMwwYAMBDRViypSZgcCGABom6Ny5M0OADQk0SBhKlGiGBBsWaLDAtGnTDAs2CNDw4AEAq1WvZhCwgYEGCmDDhs3AYAMCBxbTZkCA8ILbt3AvIAwIADs="
			},
			"fXAxisFormat": function ( tickX, i, minX ) {
				var oMoment = moment(tickX * i + minX);
				return oMoment.format("MM-DD") + "<br>" + oMoment.format("HH:mm:ss");
			},
			"fYAxisFormat": function ( tickY, i, minY, maxY ) {
				return BigScatterChart2.Util.addComma(( maxY + minY) - ((tickY * i) + minY));
			},
			"fOnSelect": function() {}
		});
		this.option(htOption);
		this.option( "minY", this._oExternal.loadFromStorage( "scatter-y-min" ) || this.option("minY") );
		this.option( "maxY", this._oExternal.loadFromStorage( "scatter-y-max" ) || this.option("maxY") );
	};
	BigScatterChart2.prototype._initVariables = function( bIsRedrawing ) {
		if (bIsRedrawing !== true) {
			this._aBubbles = [];
		}
		this._oSCManager = new BigScatterChart2.SizeCoordinateManager( this.option() );

		this._bDestroied = false;
		this._bPause = false;
		this._bRequesting = false;

		this._getContainer();
	};
	BigScatterChart2.prototype._getContainer = function() {
		var vContainer = this.option("sContainerId");
		switch( typeof vContainer ) {
			case "string":
				this._$elContainer = $("#" + vContainer);
				break;
			case "object":
				this._$elContainer = vContainer;
				break;
			case "function":
				this._$elContainer = vContainer();
				break;
		}
		this._$elContainer = $( this._$elContainer );
		this._$elContainer.css({
			"width": this._oSCManager.getWidth(),
			"height": this._oSCManager.getHeight(),
			"position": "relative"
		}).addClass("bigscatterchart");
	};
	BigScatterChart2.prototype._initElements = function() {

		this._$elPluginArea = $("<div>").css({
			"top": "0px",
			"left": this._oSCManager.getLeftOfPluginArea() + "px",
			"width": this._oSCManager.getPadding().right + "px",
			"height": this._oSCManager.getHeight() + "px",
			"z-index": 510,
			"position": "absolute"
		}).addClass( "plugins" ).appendTo( this._$elContainer );
	};
	BigScatterChart2.prototype._initInnerFeature = function() {
		var self = this;
		this._oTypeManager = new BigScatterChart2.TypeManager( this.option(), this._oSCManager, this._$elContainer, {
			"onChange": function( type ) {
				self._oCanvasManager.toggle( type );
			},
			"onSend": function( type, bChecked ) {
				self._oExternal.sendAnalytics( type, bChecked );
			}
		});
		this._oCanvasManager = new BigScatterChart2.CanvasManager( this.option(), this._oSCManager, this._aAgentList, this._$elContainer );
		this._oDragManager = new BigScatterChart2.DragManager( this.option(), this._oSCManager, this._$elContainer, {
			"onSelect": function( oDragAreaPosition, oDragXY ) {
				if ( self.hasDataByXY( oDragXY.fromX, oDragXY.toX, oDragXY.fromY, oDragXY.toY ) ) {
					self._oExternal.onSelect( oDragAreaPosition, oDragXY );
				}
			}
		});
	};
	BigScatterChart2.prototype._initPlugin = function( aFeatureList ) {
		var self = this;
		$.each( aFeatureList, function( index, oFeature ) {
			oFeature.initElement( self._$elContainer, self._$elPluginArea, self.option() ).initEvent( self );
		});
	};
	BigScatterChart2.prototype._initInnerPlugin = function() {
		this._oMessage = new BigScatterChart2.MessageFeature();
		this._oMessage.initElement( this._$elContainer, this._$elPluginArea, this.option() ).initEvent( self );
	};
	BigScatterChart2.prototype.fireDragEvent = function( oParam ) {
		this._oDragManager.triggerDrag( oParam );
	};
	BigScatterChart2.prototype.selectType = function( type ) {
		this._oTypeManager.selectType( type );
		this._oCanvasManager.selectType( this._currentAgent, type );
		return this;
	};
	BigScatterChart2.prototype.setBubbles = function( aBubbles ) {
		this._aBubbles = [];
		this.addBubbles(aBubbles);
	};
	BigScatterChart2.prototype.addBubbles = function( oBubbles ) {
		if ( $.isArray( this._aBubbles ) === false ) {
			return;
		}
		this._aBubbles.push( new BigScatterChart2.BlockData( oBubbles, this.option( "propertyIndex" ), this.option( "typeInfo" ) ) );
	};
	BigScatterChart2.prototype._getSumCountByType = function() {
		var self = this;
		var oSum = {};
		var oRangeX = this._oSCManager.getX();
		$.each( this.option( "typeInfo" ), function( key, oValue ) {
			oSum[oValue[0]] = 0;
		});
		for( var i = 0 ; i < this._aBubbles.length ; i++ ) {
			var oBlockData = this._aBubbles[i];
			$.each( this._aAgentList, function( index,  agentName ) {
				if ( self._currentAgent === self._AGENT_ALL || self._currentAgent === agentName ) {
					$.each( oSum, function( type ) {
						//if ( self._oTypeManager.isChecked( type ) ) {
							oSum[type] += oBlockData.getCount( agentName, type, oRangeX.min, oRangeX.max );
						//}
					});
				}
			});
		}
		return oSum;
	};
	BigScatterChart2.prototype.redrawBubbles = function() {
		this._oTypeManager.showTypeCount( this._getSumCountByType() );

		if (this._aBubbles.length > 0) {
			this._oMessage.hide();
		}
		for (var i = 0, nLen = this._aBubbles.length; i < nLen; i++) {
			this._drawBubbles( this._aBubbles[i] );
		}
	};
	BigScatterChart2.prototype.clear = function() {
		this._oCanvasManager.clear();
		this._aBubbles = [];
		this._oTypeManager.showTypeCount( this._getSumCountByType() );
		this._oDragManager.hide();
		this._oMessage.show( this.option( "noDataStr" ) );
	};
	BigScatterChart2.prototype._drawBubbles = function( oBlockData ) {
		var self = this;
		var oTypeInfo = this.option("typeInfo");
		var oPropertyIndex = this.option( "propertyIndex" );
		var sPrefix = this.option("sPrefix");

		setTimeout(function () {
			$.each(self._aAgentList, function (index, agentName) {
				for (var i = 0, nLen = oBlockData.countByAgent( agentName ); i < nLen && !self._bDestroied; i++) {
					var aAgentBubbleData = oBlockData.getDataByAgent(agentName, i);
					var groupCount = aAgentBubbleData[oPropertyIndex.groupCount];
					if ( groupCount !== 0 ) {
						var aBubbleType = oTypeInfo[aAgentBubbleData[oPropertyIndex.type]];
						self._oCanvasManager.drawBubble( BigScatterChart2.Util.makeKey( agentName, sPrefix, aBubbleType[0] ), aBubbleType[1], aAgentBubbleData );
						//aAgentBubbleData[i].realx = oPosition.x;
						//aAgentBubbleData[i].realy = oPosition.y;
						//aAgentBubbleData[i].realz = r;
					}
				}

			});
		}, 0);
	};
	BigScatterChart2.prototype.addBubbleAndMoveAndDraw = function( oBubbleData ) { //oBubbles, maxX ) {
		if ( BigScatterChart2.Util.isEmpty( oBubbleData.scatter.dotList ) ) {
			return;
		} else {
			this._oMessage.hide();
		}

		this.addBubbles( oBubbleData );
		this._oTypeManager.showTypeCount( this._getSumCountByType() );
		this._drawBubbles( this._aBubbles[ this._aBubbles.length - 1 ] ); // takes on average 33 ~ 45 ms
		this._moveChart( oBubbleData );
		this._oCanvasManager.updateXYAxis();
	};
	BigScatterChart2.prototype._moveChart = function( oBubbleData ) {
		var oRangeX = this._oSCManager.getX();
		if ( oBubbleData.resultFrom >= oRangeX.max ) {
			var moveXTime = oBubbleData.resultTo - oRangeX.max;
			var moveXValue = moveXTime * this._oSCManager.getPixelPerTime();
			this._oSCManager.setX( oRangeX.min + moveXTime, oRangeX.max + moveXTime );
			this._oCanvasManager.moveChart( moveXValue );
		}
	};

	BigScatterChart2.prototype.getDataByXY = function( fromX, toX, fromY, toY ) {
		var aData = [];
		var oTypeInfo = this.option( "typeInfo" );
		var oPropertyIndex = this.option( "propertyIndex" );

		fromX = parseInt(fromX, 10);
		toX = parseInt(toX, 10);
		fromY = parseInt(fromY, 10);
		toY = parseInt(toY, 10);

		var oRangeY = this._oSCManager.getY();
		var aVisibleType = this._oTypeManager.getVisibleType();

		for (var i = 0, nLen = this._aBubbles.length; i < nLen; i++) {
			var oBlockData = this._aBubbles[i];
			for (var j = 0, nLen2 = oBlockData.count() ; j < nLen2; j++ ) {
				var aBubbleData = oBlockData.getData( j );
				var agentName = oBlockData.getAgentName( aBubbleData );
				if ( this._currentAgent === this._AGENT_ALL || this._currentAgent === agentName  ) {
					var bubbleX = aBubbleData[oPropertyIndex.x];
					var bubbleType = oTypeInfo[aBubbleData[oPropertyIndex.type]][0];
					if ( BigScatterChart2.Util.isInRange( fromX, toX, bubbleX ) && BigScatterChart2.Util.indexOf( aVisibleType, bubbleType ) >= 0) {
						var bubbleY = aBubbleData[oPropertyIndex.y];
						if ( BigScatterChart2.Util.isInRange( fromY, toY, bubbleY ) || toY === oRangeY.max && toY < bubbleY ) {
							aData.push( [
								oBlockData.getTransactionID( aBubbleData ),
								aBubbleData[oPropertyIndex.x],
								aBubbleData[oPropertyIndex.y]
							]);
						}
					}
				}

			}
		}
		return aData;
	};
	BigScatterChart2.prototype.hasDataByXY = function( fromX, toX, fromY, toY ) {
		var oTypeInfo = this.option( "typeInfo" );
		var oPropertyIndex = this.option( "propertyIndex" );

		fromX = parseInt(fromX, 10);
		toX = parseInt(toX, 10);
		fromY = parseInt(fromY, 10);
		toY = parseInt(toY, 10);

		var oRangeY = this._oSCManager.getY();
		var aVisibleType = this._oTypeManager.getVisibleType();

		for (var i = 0, nLen = this._aBubbles.length; i < nLen; i++) {
			var oBlockData = this._aBubbles[i];
			for (var j = 0, nLen2 = oBlockData.count() ; j < nLen2; j++ ) {
				var aBubbleData = oBlockData.getData( j );
				var agentName = oBlockData.getAgentName( aBubbleData );
				if ( this._currentAgent === this._AGENT_ALL || this._currentAgent === agentName  ) {
					var bubbleX = aBubbleData[oPropertyIndex.x];
					var bubbleType = oTypeInfo[aBubbleData[oPropertyIndex.type]][0];
					if ( BigScatterChart2.Util.isInRange( fromX, toX, bubbleX ) && BigScatterChart2.Util.indexOf( aVisibleType, bubbleType ) >= 0) {
						var bubbleY = aBubbleData[oPropertyIndex.y];
						if ( BigScatterChart2.Util.isInRange( fromY, toY, bubbleY ) || toY === oRangeY.max && toY < bubbleY ) {
							return true;
						}
					}
				}
			}
		}
		return false;
	};
	//BigScatterChart2.prototype.destroy = function() {
	//	var self = this;
	//	this._unbindAllEvents();
	//	//this._empty();
	//	$.each(this, function (property, content) {
	//		delete self[property];
	//	});
	//	this._bDestroied = true;
	//
	//};
	//BigScatterChart2.prototype._empty = function() {
	//	this._$elContainer.empty();
	//};
	//BigScatterChart2.prototype._unbindAllEvents = function() {
	//	// this is for drag-selecting. it should be unbinded.
	//	jQuery(document).unbind("mousemove").unbind("mouseup");
	//};
	BigScatterChart2.prototype.getChartAsImage = function( type ) {
		return this._oCanvasManager.getChartAsImage( type, this._oTypeManager.getElementLI() );
	};
	BigScatterChart2.prototype.drawWithDataSource = function( oDataManager ) {
		this._oDataManager = oDataManager;
		this._oDataManager.abort();
		this._oDataManager.initCallCount();
		this._drawWithDataSource();
		if ( this._oDataManager.hasRealtime() ) {
			this._invokeLoadRealtimeData();
		}
	};
	BigScatterChart2.prototype._invokeLoadRealtimeData = function() {
		var self = this;
		setTimeout(function() {
			self._drawWithRealtimeDataSource();
		}, this._oDataManager.getRealtimeInterval() );
	};
	BigScatterChart2.prototype._drawWithDataSource = function() {
		var self = this;

		if (this._bPause || this._bRequesting) {
			return;
		}
		if ( this._oDataManager.isFirstRequest() ) {
			this._oMessage.show( this.option("loadingStr") );
		}
		this._oDataManager.loadData( function() {
			self._bRequesting = false;
		}, function( oResultData, hasNextData ) {

			if ( oResultData.scatter.dotList.length !== 0  ) {
				self.addBubbleAndMoveAndDraw( oResultData );
			}
			if ( hasNextData > -1 ) {
				setTimeout(function () {
					self._drawWithDataSource();
				}, hasNextData );
			} else if (!self._aBubbles || self._aBubbles.length === 0) {
				self._oMessage.show( self.option("noDataStr") );
			}
		}, this._oSCManager.getXOfPixel(), this._oSCManager.getYOfPixel() );
		this._bRequesting = true;
	};
	BigScatterChart2.prototype._drawWithRealtimeDataSource = function() {
		var self = this;
		this._oDataManager.loadRealtimeData( function( oResultData, nextRequestTime ) {
			self.addBubbleAndMoveAndDraw( oResultData );
			setTimeout(function () {
				console.log( self._aAgentList, self._bPause, oResultData );
				if( self._bPause === false ) {
					self._drawWithRealtimeDataSource();
				}
			}, nextRequestTime );
		}, this._oSCManager.getXOfPixel(), this._oSCManager.getYOfPixel() );
	};
	BigScatterChart2.prototype.redraw = function() {
		this._oCanvasManager.clear();
		this._oCanvasManager.updateXYAxis();
		this.redrawBubbles();
	};
	BigScatterChart2.prototype.abort = function() {
		this._oDataManager.abort();
	};
	BigScatterChart2.prototype.pause = function() {
		this._bPause = true;
		if ( this.option( "realtime" ) ) {
			this._oCanvasManager.reset();
		}
	};
	BigScatterChart2.prototype.resume = function( start, end ) {
		this._bPause = false;
		this._drawWithDataSource();
		if ( this.option( "realtime" ) ) {
			this._aBubbles = [];
			this._oSCManager.setX( start, end, true );
			this._oCanvasManager.updateXYAxis();
			this._invokeLoadRealtimeData();
		}
	};
	BigScatterChart2.prototype.changeRangeOfY = function( oNewRangeY ) {
		this._oSCManager.setY( oNewRangeY.min, oNewRangeY.max );
	};
	BigScatterChart2.prototype.selectAgent = function( agentName, bInitCheck ) {
		var self = this;
		var bIsAll = ( agentName === this._AGENT_ALL );
		var aBubbleTypeInfo = this.option("typeInfo");
		var oTypeCheckInfo = {};

		if ( bInitCheck === true) {
			this._oTypeManager.selectAll();
			$.each( aBubbleTypeInfo, function( key, aValue ) {
				oTypeCheckInfo[ aValue[0] ] = true;
			});
		} else {
			$.each( aBubbleTypeInfo, function( index, aValue ) {
				oTypeCheckInfo[ aValue[0] ] = self._oTypeManager.isChecked( aValue[0] );
			});
		}
		this._currentAgent = agentName;
		this._oCanvasManager.showSelectedAgent( bIsAll, agentName, oTypeCheckInfo );
		this._oTypeManager.showTypeCount( this._getSumCountByType() );
	};

	global.BigScatterChart2 = BigScatterChart2;
})(window, jQuery);