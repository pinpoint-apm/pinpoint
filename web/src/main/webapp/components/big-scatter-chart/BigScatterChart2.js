(function(global, $) {
	'use strict';
	function BigScatterChart2( htOption, aAgents, aPlugins, oExternal, agentAllStr ) {
		this._oExternal = oExternal;
		this._AGENT_ALL = this._currentAgent = agentAllStr;
		this._aAgentList = aAgents;

		this._setOption( htOption );
		this._initVariables();
		this._initManager();
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
			"containerId": "",
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
			"fnXAxisFormat": function ( tickX, i, minX ) {
				var oMoment = moment( tickX * i + minX );
				return oMoment.format( "MM-DD" ) + "<br>" + oMoment.format( "HH:mm:ss" );
			},
			"fnYAxisFormat": function ( tickY, i, minY, maxY ) {
				return BigScatterChart2.Util.addComma(( maxY + minY ) - (( tickY * i ) + minY ));
			},
			"fOnSelect": function() {}
		});
		this.option(htOption);
		this.option( "minY", this._oExternal.loadFromStorage( "scatter-y-min" ) || this.option("minY") );
		this.option( "maxY", this._oExternal.loadFromStorage( "scatter-y-max" ) || this.option("maxY") );
	};
	BigScatterChart2.prototype._initVariables = function() {
		this._aBubbles = [];
		this._oSCManager = new BigScatterChart2.SizeCoordinateManager( this.option() );
		this._bPause = false;
		this._bDestroied = false;
		this._bRequesting = false;
		this._getContainer();
	};
	BigScatterChart2.prototype._getContainer = function() {
		var vContainer = this.option( "containerId" );
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
		this._$elContainer.attr( "id", this.option("sPrefix") );
		this._$elContainer.css({
			"width": this._oSCManager.getWidth(),
			"height": this._oSCManager.getHeight(),
			"position": "relative"
		}).addClass( this.option( "containerClass" ) );
	};
	BigScatterChart2.prototype._initManager = function() {
		var self = this;

		this._oBubbleTypeManager = new BigScatterChart2.BubbleTypeManager( this.option(), this._oSCManager, this._$elContainer, {
			"onChange": function( type ) {
				self._oRendererManager.toggle( self._isAll( self.getCurrentAgent() ), self.getCurrentAgent(), type );
			},
			"onSend": function( type, bChecked ) {
				self._oExternal.sendAnalytics( type, bChecked );
			}
		});
		this._oRendererManager = new BigScatterChart2.RendererManager( this.option(), this._oSCManager, this._aAgentList, this._$elContainer );
		this._oDragManager = new BigScatterChart2.DragManager( this.option(), this._oSCManager, this._$elContainer, {
			"onSelect": function( oDragAreaPosition, oDragXY ) {
				if ( self.hasDataByXY( oDragXY.fromX, oDragXY.toX, oDragXY.fromY, oDragXY.toY ) ) {
					self._oExternal.onSelect( oDragAreaPosition, oDragXY, self._currentAgent, self._oBubbleTypeManager.getVisibleType().join(",") );
				}
			}
		});
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
	BigScatterChart2.prototype._initPlugin = function( aPluginList ) {
		var self = this;
		$.each( aPluginList, function( index, oPlugin ) {
			oPlugin.initElement( self._$elContainer, self._$elPluginArea, self.option() ).initEvent( self );
		});
	};
	BigScatterChart2.prototype._initInnerPlugin = function() {
		this._oMessage = new BigScatterChart2.MessagePlugin();
		this._oMessage.initElement( this._$elContainer, this._$elPluginArea, this.option() ).initEvent( self );
	};
	BigScatterChart2.prototype.fireDragEvent = function( oParam ) {
		this._oDragManager.triggerDrag( oParam );
	};
	BigScatterChart2.prototype.selectArea = function( type, min, max ) {
		this._oExternal.onSelect( type, min, max, this._currentAgent, this._oBubbleTypeManager.getVisibleType() );
	};
	BigScatterChart2.prototype.selectType = function( type ) {
		this._oBubbleTypeManager.selectType( type );
		this._oRendererManager.selectType( this._currentAgent, type );
		return this;
	};
	BigScatterChart2.prototype.addBubbles = function( oDataBlock ) {
		this._aBubbles.push( oDataBlock );
	};
	BigScatterChart2.prototype._getSumCountByType = function( bRealtime ) {
		var self = this;
		var oSum = {};
		var oRangeX = this._oSCManager.getX();
		$.each( this.option( "typeInfo" ), function( key, oValue ) {
			oSum[oValue[0]] = 0;
		});
		for( var i = 0 ; i < this._aBubbles.length ; i++ ) {
			var oDataBlock = this._aBubbles[i];
			$.each( this._aAgentList, function( index,  agentName ) {
				if ( self._currentAgent === self._AGENT_ALL || self._currentAgent === agentName ) {
					$.each( oSum, function( type ) {
						if ( bRealtime === true ) {
							oSum[type] += oDataBlock.getCount( agentName, type, oRangeX.min, oRangeX.max );
						} else {
							oSum[type] += oDataBlock.getCount( agentName, type );
						}
					});
				}
			});
		}
		return oSum;
	};
	BigScatterChart2.prototype.redrawBubbles = function() {
		this._oBubbleTypeManager.showTypeCount( this._getSumCountByType() );

		if (this._aBubbles.length > 0) {
			this._oMessage.hide();
		}
		for (var i = 0, nLen = this._aBubbles.length; i < nLen; i++) {
			this._drawBubbles( this._aBubbles[i] );
		}
	};
	BigScatterChart2.prototype.clear = function() {
		this._oRendererManager.clear();
		this._aBubbles = [];
		this._oBubbleTypeManager.showTypeCount( this._getSumCountByType() );
		this._oDragManager.hide();
		this._oMessage.show( this.option( "noDataStr" ) );
	};
	BigScatterChart2.prototype._drawBubbles = function( oDataBlock ) {
		var self = this;
		var oTypeInfo = this.option("typeInfo");
		var oPropertyIndex = this.option( "propertyIndex" );
		var sPrefix = this.option("sPrefix");
		$.each(self._aAgentList, function (index, agentName) {
			for (var i = 0, nLen = oDataBlock.countByAgent( agentName ); i < nLen && !self._bDestroied; i++) {
				var aAgentBubbleData = oDataBlock.getDataByAgent(agentName, i);
				var groupCount = aAgentBubbleData[oPropertyIndex.groupCount];
				if ( groupCount !== 0 ) {
					var aBubbleType = oTypeInfo[aAgentBubbleData[oPropertyIndex.type]];
					self._oRendererManager.drawBubble( BigScatterChart2.Util.makeKey( agentName, sPrefix, aBubbleType[0] ), aBubbleType[1], aAgentBubbleData );
				}
			}

		});
	};
	BigScatterChart2.prototype.addBubbleAndMoveAndDraw = function( oDataBlock, bRealtime, nextRequestTime ) {
		if ( bRealtime === false ) {
			if ( oDataBlock.isEmpty() ) {
				return;
			} else {
				this._oMessage.hide();
			}
		}
		this.addBubbles( oDataBlock );
		if( this._bPause === true ) return;
		this._oBubbleTypeManager.showTypeCount( this._getSumCountByType( bRealtime ) );
		this._drawBubbles( oDataBlock );
		this._moveChart( oDataBlock, nextRequestTime );
		this._oRendererManager.updateXYAxis();
		this._removeBubble();
	};
	BigScatterChart2.prototype._moveChart = function( oDataBlock, nextRequestTime ) {
		var oRangeX = this._oSCManager.getX();
		var oDataBlockRangeX = oDataBlock.getX();
		var animationTime = this.option( "chartAnimationTime" );

		if ( oDataBlockRangeX.min >= oRangeX.max ) {
			var moveXTime = oDataBlockRangeX.max - oRangeX.max;
			var moveXValue = moveXTime * this._oSCManager.getPixelPerTime();
			this._oSCManager.setX( oRangeX.min + moveXTime, oRangeX.max + moveXTime );
			this._oRendererManager.moveChart( parseInt( moveXValue, 10 ), nextRequestTime < animationTime ? 0 : animationTime ); // 300 or 0
		}
	};
	BigScatterChart2.prototype._removeBubble = function() {
		var minX = this._oSCManager.getX().min;

		for( var i = 0 ; i < this._aBubbles.length ; i++ ) {
			var oDataBlock = this._aBubbles[i];
			if ( oDataBlock.getX().max < minX ) {
				this._aBubbles.shift();
				i--;
			} else {
				break;
			}
		}
	};
	BigScatterChart2.prototype.createDataBlock = function( oData ) {
		return new BigScatterChart2.DataBlock( oData, this.option( "propertyIndex" ), this.option( "typeInfo" ) );
	};
	BigScatterChart2.prototype.getDataByXY = function( fromX, toX, fromY, toY, selectedAgent, visibleType ) {
		var aData = [];
		var oTypeInfo = this.option( "typeInfo" );
		var oPropertyIndex = this.option( "propertyIndex" );

		fromX = parseInt(fromX, 10);
		toX = parseInt(toX, 10);
		fromY = parseInt(fromY, 10);
		toY = parseInt(toY, 10);

		var oRangeY = this._oSCManager.getY();
		var aVisibleType = visibleType.split(",");

		for (var i = 0, nLen = this._aBubbles.length; i < nLen; i++) {
			var oDataBlock = this._aBubbles[i];
			for (var j = 0, nLen2 = oDataBlock.count() ; j < nLen2; j++ ) {
				var aBubbleData = oDataBlock.getData( j );
				var agentName = oDataBlock.getAgentName( aBubbleData );
				if ( selectedAgent === this._AGENT_ALL || selectedAgent === agentName  ) {
					var bubbleX = aBubbleData[oPropertyIndex.x];
					var bubbleType = oTypeInfo[aBubbleData[oPropertyIndex.type]][0];
					if ( BigScatterChart2.Util.isInRange( fromX, toX, bubbleX ) && BigScatterChart2.Util.indexOf( aVisibleType, bubbleType ) >= 0) {
						var bubbleY = aBubbleData[oPropertyIndex.y];
						if ( BigScatterChart2.Util.isInRange( fromY, toY, bubbleY ) || toY === oRangeY.max && toY < bubbleY ) {
							aData.push( [
								oDataBlock.getTransactionID( aBubbleData ),
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
	BigScatterChart2.prototype.getDataByRange = function( type, fromY, toY, selectedAgent, visibleType ) {
		var aData = [];
		var oTypeInfo = this.option( "typeInfo" );
		var oPropertyIndex = this.option( "propertyIndex" );
		var aVisibleType = visibleType.split(",");

		fromY = parseInt(fromY, 10);
		toY = parseInt(toY, 10);

		for (var i = 0, nLen = this._aBubbles.length; i < nLen; i++) {
			var oDataBlock = this._aBubbles[i];
			for (var j = 0, nLen2 = oDataBlock.count() ; j < nLen2; j++ ) {
				var aBubbleData = oDataBlock.getData( j );
				var agentName = oDataBlock.getAgentName( aBubbleData );
				if ( selectedAgent === this._AGENT_ALL || selectedAgent === agentName  ) {
					var bubbleType = oTypeInfo[aBubbleData[oPropertyIndex.type]][0];
					if ( type === bubbleType ) {
						var bubbleY = aBubbleData[oPropertyIndex.y];
						if ( type === "Failed" || BigScatterChart2.Util.isInRange( fromY, toY, bubbleY ) ) {
							aData.push( [
								oDataBlock.getTransactionID( aBubbleData ),
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
		var aVisibleType = this._oBubbleTypeManager.getVisibleType();

		for (var i = 0, nLen = this._aBubbles.length; i < nLen; i++) {
			var oDataBlock = this._aBubbles[i];
			for (var j = 0, nLen2 = oDataBlock.count() ; j < nLen2; j++ ) {
				var aBubbleData = oDataBlock.getData( j );
				var agentName = oDataBlock.getAgentName( aBubbleData );
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
		return this._oRendererManager.getChartAsImage( type, this._oBubbleTypeManager.getElementLI() );
	};
	BigScatterChart2.prototype.drawWithDataSource = function( oDataLoadManager ) {
		this._oDataLoadManager = oDataLoadManager;
		this._oDataLoadManager.setTimeManager( this._oSCManager );
		this._oDataLoadManager.abort();
		this._oDataLoadManager.initCallCount();
		this._drawWithDataSource();
		if ( this.option( "realtime" ) ) {
			this._invokeLoadRealtimeData();
		}
	};
	BigScatterChart2.prototype._invokeLoadRealtimeData = function() {
		var self = this;
		setTimeout(function() {
			self._drawWithRealtimeDataSource();
		}, this._oDataLoadManager.getRealtimeInterval() );
	};
	BigScatterChart2.prototype._drawWithDataSource = function() {
		var self = this;
		if ( typeof this._oDataLoadManager === "undefined" ) {
			return;
		}
		if (this._bPause || this._bRequesting || this._oDataLoadManager.isCompleted() ) {
			return;
		}
		if ( this._oDataLoadManager.isFirstRequest() ) {
			this._oMessage.show( this.option( "loadingStr" ) );
		}
		this._oDataLoadManager.loadData( function() {
			self._bRequesting = false;
		}, function( oResultData, bHasNextData, intervalTime ) {

			var oDataBlock = self.createDataBlock( oResultData );
			if ( oDataBlock.isEmpty() === false ) {
				self.addBubbleAndMoveAndDraw( oDataBlock, false, 0 );
			}
			if ( bHasNextData === true ) {
				setTimeout(function () {
					self._drawWithDataSource();
				}, intervalTime );
			} else if ( self._aBubbles && self._aBubbles.length === 0 ) {
				self._oMessage.show( self.option( "noDataStr" ) );
			}
		}, function() {
			self._showServerError();
			self._oExternal.onError();
		}, this._oSCManager.getXOfPixel(), this._oSCManager.getYOfPixel()
		);
		this._bRequesting = true;
	};
	BigScatterChart2.prototype._drawWithRealtimeDataSource = function() {
		var self = this;
		this._oDataLoadManager.loadRealtimeData( function( oResultData, nextRequestTime, bResetRealtime, currentServerTime ) {
			self._oMessage.hide();
			self._hideServerError();
			if ( bResetRealtime ) {
				self.pause();
				self.resume( currentServerTime - self._oSCManager.getGapX(), currentServerTime );
			} else {
				self.addBubbleAndMoveAndDraw( self.createDataBlock( oResultData ), true, nextRequestTime );
				setTimeout(function () {
					if( self._bPause === false ) {
						self._drawWithRealtimeDataSource();
					}
				}, nextRequestTime );
			}
		}, function() {
			self._showServerError();
			self._oExternal.onError();
			setTimeout(function () {
				if( self._bPause === false ) {
					self._drawWithRealtimeDataSource();
				}
			}, 0 );
		}, this._oSCManager.getXOfPixel(), this._oSCManager.getYOfPixel() );
	};
	BigScatterChart2.prototype._hideServerError = function() {
		this._$elContainer.css({
			"backgroundImage": "none"
		});
	};
	BigScatterChart2.prototype._showServerError = function() {
		this._$elContainer.css({
			"backgroundImage": "url(" + this.option( "errorImage" ) + ")",
			"backgroundRepeat": "no-repeat",
			"backgroundPosition": "88% 21%",
			"backgroundSize": "30px 30px"
		});
	};
	BigScatterChart2.prototype.redraw = function() {
		this._oRendererManager.clear();
		this._oRendererManager.updateXYAxis();
		this.redrawBubbles();
	};
	BigScatterChart2.prototype.abort = function() {
		this._bPause = true;
		if ( this._oDataLoadManager ) {
			this._oDataLoadManager.abort();
		}
	};
	BigScatterChart2.prototype.pause = function() {
		this._bPause = true;
		if ( this.option( "realtime" ) ) {
			this._oRendererManager.reset();
		}
	};
	BigScatterChart2.prototype.resume = function( from, to ) {
		this._bPause = false;
		if ( this.option( "realtime" ) ) {
			this._aBubbles = [];
			this._oSCManager.setX( from, to, true );
			this._oDataLoadManager.reset();
			this._oDataLoadManager.setRealtimeFrom( to );
			this._oRendererManager.updateXYAxis();
			this._drawWithDataSource();
			this._invokeLoadRealtimeData();
		} else {
			this._drawWithDataSource();
		}
	};
	BigScatterChart2.prototype.changeRangeOfY = function( oNewRangeY ) {
		this._oSCManager.setY( oNewRangeY.min, oNewRangeY.max );
	};
	BigScatterChart2.prototype.selectAgent = function( agentName, bInitCheck ) {
		var self = this;
		var aBubbleTypeInfo = this.option("typeInfo");
		var oTypeCheckInfo = {};

		if ( bInitCheck === true) {
			this._oBubbleTypeManager.selectAll();
			$.each( aBubbleTypeInfo, function( key, aValue ) {
				oTypeCheckInfo[ aValue[0] ] = true;
			});
		} else {
			$.each( aBubbleTypeInfo, function( index, aValue ) {
				oTypeCheckInfo[ aValue[0] ] = self._oBubbleTypeManager.isChecked( aValue[0] );
			});
		}
		this._currentAgent = agentName;
		this._oRendererManager.showSelectedAgent( this._isAll( agentName ), agentName, oTypeCheckInfo );
		this._oBubbleTypeManager.showTypeCount( this._getSumCountByType() );
	};
	BigScatterChart2.prototype.getCurrentAgent = function() {
		return this._currentAgent;
	};
	BigScatterChart2.prototype._isAll = function( agentName ) {
		return agentName === this._AGENT_ALL;
	};


	global.BigScatterChart2 = BigScatterChart2;
})(window, jQuery);