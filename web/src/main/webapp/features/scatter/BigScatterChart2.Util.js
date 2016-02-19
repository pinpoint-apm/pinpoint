(function(global, $) {
	'use strict';

	global.BigScatterChart2.Util = {
		addComma: function( nNumber ) {
			var sNumber = nNumber + "";
			var sPattern = /(-?[0-9]+)([0-9]{3})/;
			while (sPattern.test(sNumber)) {
				sNumber = sNumber.replace(sPattern, "$1,$2");
			}
			return sNumber;
		},
		//min: function( aData, dataIndex ) {
		//	var minIndex = 0;
		//	for( var i = 1, len = aData.length ; i < len ; i++ ) {
		//		var result = a[minIndex][dataIndex] - a[i][dataIndex];
		//		if ( result > 0 ) {
		//			minIndex = i;
		//		}
		//	}
		//	return a[minIndex][dataIndex];
		//},
		//max: function( aData, dataIndex ) {
		//	var maxIndex = 0;
		//	for( var i = 1, len = aData.length ; i < len ; i++ ) {
		//		var result = a[maxIndex][dataIndex] - a[i][dataIndex];
		//		if ( result < 0 ) {
		//			maxIndex = i;
		//		}
		//	}
		//	return a[maxIndex][dataIndex];
		//},
		compare: function( aData, dataIndex, fnCompare ) {
			var targetIndex = 0;
			for( var i = 1, len = aData.length ; i < len ; i++ ) {
				var result = aData[targetIndex][dataIndex] - aData[i][dataIndex];
				if ( fnCompare( result ) ) {
					targetIndex = i;
				}
			}
			return aData[targetIndex][dataIndex];
		},
		min: function( result ) {
			return result > 0;
		},
		max: function( result ) {
			return result < 0;
		},
		indexOf: function( aData, value ) {
			for( var i = 0 ; i < aData.length ; i++ ) {
				if ( aData[i] === value ) {
					return i;
				}
			}
			return -1;
		},
		getBoundaryValue: function( max, min, value ) {
			return Math.min( max, Math.max( min, value ) );
		},
		isInRange: function( from, to, value ) {
			return value >= from && value <= to;
		},
		isEmpty: function( obj ) {
			var count = 0;
			for( var p in obj ) {
				count += obj[p].length;
			}
			return count === 0;
		},
		makeKey: function( a, b, c ) {
			return a + "-" + b + "-" + c;
		},
		isString: function( v ) {
			return typeof v === "string";
		}

	};
})(window, jQuery);