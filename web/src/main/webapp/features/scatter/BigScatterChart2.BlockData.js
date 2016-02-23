(function(global, $) {
	'use strict';
	function BlockData( oData, oPropertyIndex, oTypeInfo ) {
		this._initData( oData );
		this._splitDataByAgent( oPropertyIndex, oTypeInfo );
	}
	BlockData.prototype._initData = function( oData ) {
		this._resultFrom = oData.resultFrom;
		this._resultTo = oData.resultTo;
		this._from = oData.from;
		this._to = oData.to;
		this._oAgentMetaInfo = oData.scatter.metadata;
		this._aAllData = oData.scatter.dotList;
	};
	BlockData.prototype._splitDataByAgent = function( oPropertyIndex, oTypeInfo ) {
		var self = this;
		this._oPropertyIndex = oPropertyIndex;
		this._oAgentData = {};
		this._oCountOfType = {};
		$.each( this._oAgentMetaInfo, function( key, oValue ) {
			var agentName = oValue[1];
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
		this._minX = this._aAllData[0][oPropertyIndex.x];
		this._maxX = this._aAllData[this._aAllData.length - 1][oPropertyIndex.x];
		this._minY = minY;
		this._maxY = maxY;
	};
	BlockData.prototype._getAgentName = function( key ) {
		return this._oAgentMetaInfo[ key ][1];
	};
	BlockData.prototype.getDataByAgent = function( agent, index ) {
		return this._oAgentData[agent][index];
	};
	BlockData.prototype.getData = function( index ) {
		return this._aAllData[index];
	};
	BlockData.prototype.count = function() {
		return this._aAllData.length;
	};
	BlockData.prototype.countByAgent = function( agent ) {
		return this._oAgentData[agent].length;
	};
	BlockData.prototype.getCountByType = function( type ) {
		var sum = 0;
		$.each( this._oCountOfType, function( agentName, oCountData ) {
			sum += oCountData[type];
		});
		return sum;
	};
	BlockData.prototype.getCount = function( agentName, type ) {
		return this._oCountOfType[agentName][type];
	};
	BlockData.prototype.isSameAgent = function( aData, agentName ) {
		return this._oAgentMetaInfo[ aData[this._oPropertyIndex.meta + ""][1] ] === agentName;
	};
	BlockData.prototype.getTransactionID = function( aBlockData ) {
		var oMeta = this._oAgentMetaInfo[ aBlockData[this._oPropertyIndex.meta] + "" ];
		return oMeta[1] + "^" + oMeta[2] + "^" + aBlockData[this._oPropertyIndex.transactionId];
	};
	BlockData.prototype.getAgentName = function( aBlockData ) {
		var oMeta = this._oAgentMetaInfo[ aBlockData[this._oPropertyIndex.meta] + "" ];
		return oMeta[1];
	};

	global.BigScatterChart2.BlockData = BlockData;
})(window, jQuery);