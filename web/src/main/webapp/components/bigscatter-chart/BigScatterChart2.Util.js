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
		getBoundaryValue: function( oRange, value ) {
			return Math.min( oRange.max, Math.max( oRange.min, value ) );
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
		},
		endsWith: function( str, value ) {
			var index = str.indexOf( value );
			if ( index !== -1 ) {
				if ( str.length - index === value.length ) {
					return true;
				}
			}
			return false;
		},
		startsWith: function( str, value ) {
			return str.indexOf( value ) === 0;
		}

	};
})(window, jQuery);