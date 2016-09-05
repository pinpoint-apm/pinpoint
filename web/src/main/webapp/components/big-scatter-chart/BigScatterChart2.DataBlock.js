(function(global, $) {
	'use strict';
	function DataBlock( oData, oPropertyIndex, oTypeInfo ) {
		this._initData( oData );
		this._splitDataByAgent( oPropertyIndex, oTypeInfo );
	}
	DataBlock.prototype._initData = function( oData ) {
		this._from = oData.from;
		this._to = oData.to;
		this._resultFrom = oData.complete ? oData.from : oData.resultFrom;
		this._resultTo = oData.complete ? oData.to : oData.resultTo;
		this._oAgentMetaInfo = oData.scatter.metadata;
		this._aAllData = oData.scatter.dotList;
		this._bLoadComplete = oData.complete;

		this._oAgentData = {};
		this._oCountOfType = {};
	};
	DataBlock.prototype._splitDataByAgent = function( oPropertyIndex, oTypeInfo ) {
		var self = this;
		this._oPropertyIndex = oPropertyIndex;
		this._oTypeInfo = oTypeInfo;
		$.each( this._oAgentMetaInfo, function( key, oValue ) {
			var agentName = oValue[0];
			self._oAgentData[agentName] = [];
			self._oCountOfType[agentName] = {};
			$.each( oTypeInfo, function( key, aValue ) {
				self._oCountOfType[agentName][aValue[0]] = 0;
			});
		});
		var minY = Number.MAX_VALUE;
		var maxY = 0;

		$.each( this._aAllData, function( index, aValue ) {
			var agentName = self._getAgentName( aValue[2] + "" );
			minY = Math.min( aValue[1], minY );
			maxY = Math.max( aValue[1], maxY );
			aValue[0] += self._from;
			self._oAgentData[ agentName ].push( aValue );
			self._oCountOfType[agentName][ oTypeInfo[aValue[oPropertyIndex.type] + "" ][0] ]++;

		});
		this._minX = this._bLoadComplete ? this._from : this._resultFrom;
		this._maxX = this._bLoadComplete ? this._to : this._resultTo;
		this._minY = minY;
		this._maxY = maxY;
	};
	DataBlock.prototype._getAgentName = function( key ) {
		return this._oAgentMetaInfo[ key ][0];
	};
	DataBlock.prototype.getDataByAgent = function( agent, index ) {
		return this._oAgentData[agent][index];
	};
	DataBlock.prototype.getData = function( index ) {
		return this._aAllData[index];
	};
	DataBlock.prototype.count = function() {
		return this._aAllData.length;
	};
	DataBlock.prototype.countByAgent = function( agent ) {
		if ( this._oAgentData[agent] ) {
			return this._oAgentData[agent].length;
		} else {
			return 0;
		}
	};
	DataBlock.prototype.getCount = function( agentName, type, minX, maxX ) {
		if ( arguments.length === 2 ) {
			if (this._oCountOfType[agentName]) {
				return this._oCountOfType[agentName][type];
			} else {
				return 0;
			}
		} else {
			return this._getRealtimeCount( agentName, type, minX, maxX );
		}
	};
	DataBlock.prototype._getRealtimeCount = function( agentName, type, minX, maxX ) {
		var sum = 0;
		var metaIndex = this._oPropertyIndex.meta;
		var typeIndex = this._oPropertyIndex.type;
		var xIndex = this._oPropertyIndex.x;
		var length = this._aAllData.length;

		if ( this._from >= maxX || this._to <= minX || length === 0 || typeof this._oCountOfType[agentName] === "undefined" ) {
			return sum;
		}
		if ( this._from >= minX && this._to <= maxX ) {
			return this._oCountOfType[agentName][type];
		}

		for( var i = 0; i < length ; i++ ) {
			var aValue = this._aAllData[i];
			if ( aValue[ xIndex ] < minX ) {
				break;
			}
			if ( agentName === this._getAgentName( aValue[ metaIndex ] + "" ) ) {
				if (type === this._oTypeInfo[ aValue[ typeIndex ] + "" ][0]) {
					if ( aValue[ xIndex ] <= maxX ) {
						sum++;
					}
				}
			}
		}
		return sum;
	};
	DataBlock.prototype.getTransactionID = function( aDataBlock ) {
		var oMeta = this._oAgentMetaInfo[ aDataBlock[this._oPropertyIndex.meta] + "" ];
		return oMeta[1] + "^" + oMeta[2] + "^" + aDataBlock[this._oPropertyIndex.transactionId];
	};
	DataBlock.prototype.getAgentName = function( aDataBlock ) {
		var oMeta = this._oAgentMetaInfo[ aDataBlock[this._oPropertyIndex.meta] + "" ];
		return oMeta[0];
	};
	DataBlock.prototype.getX = function() {
		return {
			"min": this._minX,
			"max": this._maxX
		};
	};
	DataBlock.prototype.isEmpty = function() {
		return this._aAllData.length === 0;
	};

	global.BigScatterChart2.DataBlock = DataBlock;
})(window, jQuery);