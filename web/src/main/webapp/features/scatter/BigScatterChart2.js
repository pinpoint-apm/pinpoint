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
			"fXAxisFormat": function (tickX, i) {
				var oMoment = moment(tickX * i + this._minX);
				return oMoment.format("MM-DD") + "<br>" + oMoment.format("HH:mm:ss");
			},
			"fYAxisFormat": function (tickY, i) {
				return BigScatterChart2.Util.addComma((this._maxY + this._minY) - ((tickY * i) + this._minY));
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
		var oPadding = this.option("padding");
		var	bubbleSize = this.option("bubbleSize");
		var width = this.option("width");
		var height = this.option("height");

		this._widthOfWork = ( this.option("width") - ( oPadding.left + oPadding.right ) ) - bubbleSize * 2;
		this._heightOfWork = ( this.option("height") - ( oPadding.top + oPadding.bottom ) ) - bubbleSize * 2;

		this._maxX = this.option("maxX");
		this._minX = this.option("minX");

		this._maxY = this.option("maxY");
		this._minY = this.option("minY");

		this._maxZ = this.option("maxZ");
		this._minZ = this.option("minZ");

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
			"width": this.option("width"),
			"height": this.option("height"),
			"position": "relative"
		}).addClass("bigscatterchart");
	};
	BigScatterChart2.prototype._initElements = function() {

		var oPadding = this.option("padding");
		this._$elPluginArea = $("<div>").css({
			"top": "0px",
			"left": ( this.option("width") - oPadding.right ) + "px",
			"width": oPadding.right + "px",
			"height": this.option("height") + "px",
			"z-index": 510,
			"position": "absolute"
		}).appendTo( this._$elContainer );
	};
	BigScatterChart2.prototype._initInnerFeature = function() {
		var self = this;
		this._oTypeManager = new BigScatterChart2.TypeManager( this.option(), this._$elContainer, {
			"onChange": function( type ) {
				self._oCanvasManager.toggle( type );
			},
			"onSend": function( type, bChecked ) {
				self._oExternal.sendAnalytics( type, bChecked );
			}
		});
		this._oCanvasManager = new BigScatterChart2.CanvasManager( this.option(), this._aAgentList, this._$elContainer );
		this._oDragManager = new BigScatterChart2.DragManager( this.option(), this._$elContainer, {
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
		$.each( this.option( "typeInfo" ), function( key, oValue ) {
			oSum[oValue[0]] = 0;
		});
		for( var i = 0 ; i < this._aBubbles.length ; i++ ) {
			var oBlockData = this._aBubbles[i];
			$.each( this._aAgentList, function( index,  agentName ) {
				if ( self._currentAgent === self._AGENT_ALL || self._currentAgent === agentName ) {
					$.each( oSum, function( type ) {
						//if ( self._oTypeManager.isChecked( type ) ) {
							oSum[type] += oBlockData.getCount( agentName, type );
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
	BigScatterChart2.prototype.addBubbleAndDraw = function( aBubbles ) {
		if ($.isArray(aBubbles) === false || aBubbles.length === 0) {
			return;
		}
		if (aBubbles.length > 0) {
			this._oMessage.hide();
		}

		this.addBubbles(aBubbles);
		this._oTypeManager.showTypeCount( this._getSumCountByType() );
		this._drawBubbles(aBubbles);
	};
	BigScatterChart2.prototype._drawBubbles = function() {
		var self = this;
		var oTypeInfo = this.option("typeInfo");
		var bubbleRadius = this.option("bubbleRadius");
		var oPropertyIndex = this.option( "propertyIndex" );
		var sPrefix = this.option("sPrefix");
		var oBlockData = this._aBubbles[ this._aBubbles.length - 1 ];

		setTimeout(function () {
			$.each(self._aAgentList, function (index, agentName) {
				for (var i = 0, nLen = oBlockData.countByAgent( agentName ); i < nLen && !self._bDestroied; i++) {
					var aAgentBubbleData = oBlockData.getDataByAgent(agentName, i);
					var groupCount = aAgentBubbleData[oPropertyIndex.groupCount];
					if ( groupCount !== 0 ) {
						var x = self._parseXDataToXChart(BigScatterChart2.Util.getBoundaryValue(self._maxX, self._minX, aAgentBubbleData[oPropertyIndex.x]));
						var y = self._parseYDataToYChart(BigScatterChart2.Util.getBoundaryValue(self._maxY, self._minY, aAgentBubbleData[oPropertyIndex.y]));
						var r = self._parseZDataToZChart(aAgentBubbleData.r || bubbleRadius);
						var a = aAgentBubbleData[oPropertyIndex.y] / self._maxY * 0.7 * groupCount;
						var aBubbleType = oTypeInfo[aAgentBubbleData[oPropertyIndex.type]];
						var key = BigScatterChart2.Util.makeKey(agentName, sPrefix, aBubbleType[0]);

						self._oCanvasManager.drawBubble(key, aBubbleType[1], x, y, r, a);
						//aAgentBubbleData[i].realx = x;
						//aAgentBubbleData[i].realy = y;
						//aAgentBubbleData[i].realz = r;
					}
				}

			});
		}, 0);
	};
	BigScatterChart2.prototype._parseXDataToXChart = function( x ) {
		return Math.round(((x - this._minX) / (this._maxX - this._minX)) * this._widthOfWork) + this.option("padding").left + this.option( "bubbleSize" );
	};
	BigScatterChart2.prototype._parseYDataToYChart = function( y ) {
		return Math.round(this._heightOfWork - (((y - this._minY) / (this._maxY - this._minY)) * this._heightOfWork)) + this.option("padding").top + this.option( "bubbleSize" );
	};
	BigScatterChart2.prototype._parseZDataToZChart = function( nZ ) {
		return Math.round(((nZ - this._minZ) / (this._maxZ - this._minZ)) * this.option("bubbleSize"));
	};
	BigScatterChart2.prototype.addBubbleAndMoveAndDraw = function( oBubbleData ) { //oBubbles, maxX ) {
		if ( BigScatterChart2.Util.isEmpty( oBubbleData.scatter.dotList ) ) {
			return;
		} else {
			this._oMessage.hide();
		}

		var	width = this.option("width");
		var height = this.option("height");
		var maxX = oBubbleData.resultFrom;
		if (maxX > this._maxX) {
			var gapX = maxX - this._maxX;
			var x = gapX + this._minX;
			var widthX = Math.round( ( ( x - this._minX ) / ( this._maxX - this._minX ) ) * this._widthOfWork );
			this._oDragManager.moveDragArea( gapX );
			this._oCanvasManager.moveChartLeftwardly( this.option("padding").left + this.option("bubbleSize") + widthX, 0, width - widthX, height);
			this._maxX = maxX;
			this._minX += gapX;
			this._removeOldDataLessThan(this._minX);
		}
		this.addBubbles( oBubbleData );
		this._oTypeManager.showTypeCount( this._getSumCountByType() );
		this._drawBubbles(); // takes on average 33 ~ 45 ms
		this._oCanvasManager.updateXYAxis();
	};
	BigScatterChart2.prototype._removeOldDataLessThan = function( nX ) {
		// may cause some slowdowns, but it won"t affect rendering much
		// takes a long time sending/receiving arrays, even when using walker
		//var self = this;
		//var aBubbles = this._aBubbles || [],
		//    aIndexToBeRemoved = [],
		//    htType = this.option("typeInfo"),
		//    htDataSource = this.option("htDataSource"),
		//    htDataIndex = htDataSource.index,
		//    htDataType = htDataSource.type;
		//
		//outerLoop:
		//    for (var i = 0, nLen = aBubbles.length; i < nLen; i++) {
		//        var htTypeCountToBeRemoved = {};
		//        $.each(htType, function (sKey, sVal) {
		//            htTypeCountToBeRemoved[sKey] = 0;
		//        });
		//
		//        if (this._aBubbleStep[i].nXMin <= nX) {
		//            for (var j = 0, nLen2 = aBubbles[i].length; j < nLen2; j++) {
		//                htTypeCountToBeRemoved[aBubbles[i][j][htDataType[htDataIndex.type]]] += 1;
		//                if (aBubbles[i][j].x > nX || j === nLen2 - 1) {
		//                    aBubbles[i].splice(0, j + 1);
		//                    this._aBubbleStep[i].nXMin = nX;
		//                    this._aBubbleStep[i].nLength = aBubbles[i].length;
		//
		//                    $.each(htTypeCountToBeRemoved, function (sKey, sVal) {
		//					self._aBubbleStep[i]["htTypeCount"][sKey] -= sVal;
		//					self._oCountByType[sKey] -= sVal;
		//                    });
		//
		//                    if (aBubbles[i].length === 0) {
		//                        aIndexToBeRemoved.push(i);
		//                    }
		//                    break outerLoop;
		//                }
		//            }
		//        }
		//    }
		//for (var i = 0, nLen = aIndexToBeRemoved.length; i < nLen; i++) {
		//    aBubbles.splice(aIndexToBeRemoved[i], 1);
		//    this._aBubbleStep.splice(aIndexToBeRemoved[i], 1);
		//}
		//return;
	};
	BigScatterChart2.prototype.getDataByXY = function( fromX, toX, fromY, toY ) {

		var aData = [];
		var oTypeInfo = this.option( "typeInfo" );
		var oPropertyIndex = this.option( "propertyIndex" );

		fromX = parseInt(fromX, 10);
		toX = parseInt(toX, 10);
		fromY = parseInt(fromY, 10);
		toY = parseInt(toY, 10);

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
						if ( BigScatterChart2.Util.isInRange( fromY, toY, bubbleY ) || toY === this._maxY && toY < bubbleY ) {
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
		console.log( "count : ", aData.length );
		return aData;
	};
	BigScatterChart2.prototype.hasDataByXY = function( fromX, toX, fromY, toY ) {
		var oTypeInfo = this.option( "typeInfo" );
		var oPropertyIndex = this.option( "propertyIndex" );

		fromX = parseInt(fromX, 10);
		toX = parseInt(toX, 10);
		fromY = parseInt(fromY, 10);
		toY = parseInt(toY, 10);

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
						if ( BigScatterChart2.Util.isInRange( fromY, toY, bubbleY ) || toY === this._maxY && toY < bubbleY ) {
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
		//this.clear();
		this._oDataManager = oDataManager;
		this._oDataManager.abort();
		this._oDataManager.initCallCount();
		this._drawWithDataSource();
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
		});
		this._bRequesting = true;
	};
	BigScatterChart2.prototype.redraw = function() {
		this._oCanvasManager.clear();
		this._oCanvasManager.updateXYAxis( this.option( "minX" ), this.option( "maxX" ), this.option( "minY" ), this.option( "maxY" ) );
		this.redrawBubbles();
	};
	BigScatterChart2.prototype.pause = function() {
		this._bPause = true;
	};
	BigScatterChart2.prototype.resume = function() {
		this._bPause = false;
		this._drawWithDataSource();
	};
	BigScatterChart2.prototype.changeRangeOfY = function( oNewRangeY ) {
		this.option("minY", this._minY = oNewRangeY.min );
		this.option("maxY", this._maxY = oNewRangeY.max );
		this._oDragManager.setRangeOfY( oNewRangeY.min, oNewRangeY.max );
	};
	BigScatterChart2.prototype.selectAgent = function( agentName, bInitCheck ) {
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
				oTypeCheckInfo[ aValue[0] ] = this._oTypeManager.isChecked( aValue[0] );
			});
		}
		this._currentAgent = agentName;
		this._oCanvasManager.showSelectedAgent( bIsAll, agentName, oTypeCheckInfo );
		this._oTypeManager.showTypeCount( this._getSumCountByType() );
	};

	global.BigScatterChart2 = BigScatterChart2;
})(window, jQuery);