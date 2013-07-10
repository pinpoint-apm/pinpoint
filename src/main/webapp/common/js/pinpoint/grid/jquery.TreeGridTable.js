(function(window){
	"use strict";

	/**
	 * Tree Table
	 * @class TreeGridTable 
	 * @version 0.0.1
	 * @since July, 2013
	 * @author Denny Lim<hello@iamdenny.com, iamdenny@nhn.com>
	 * @license MIT License
	 * @copyright 2013 NHN Corp.
	 */
	window.TreeGridTable = $.Class({
		
		/**
		* constructor  
		* @constructor
		* @method $init
		* @param {Hash Table} options
		*/
		$init : function(options){
			this.option({
				tableId : '',
				height : 'auto'
			});
			this.option(options);
				
			this._init();
		},
		
		/**
		 * internal constructor
		 * 
		 * @method _init
		 */
		_init : function(){
			var self = this,
				flexiGridReady = false,
				tableId = this.option('tableId');
			this._table = $('#' + tableId);
			if(typeof this._table === 'undefined' || typeof this._table.get(0) === 'undefined' || this._table.get(0).nodeName !== 'TABLE'){
				console.error('Id of TreeGridTable is undefined.');
				return;
			}
			
			this._table.treetable({
				expandable : true,
				clickableNodeNames : true,
				indent : 10,
				onNodeExpanded : function(){
					if(flexiGridReady){
						self._table.flexHeight();
					}
				},
				onNodeCollapsed : function(){
					if(flexiGridReady){
						self._table.flexHeight();
					}
				}
			});
			this.expandAll();
			this._table.flexigrid({
				height: this.option('height')
			});
			flexiGridReady = true;
		},
		
		/**
		 * expand all nodes
		 * 
		 * @method expandAll
		 */
		expandAll : function(){
			this._table.treetable('expandAll');
		},
		
		/**
		 * collapse all nodes
		 * 
		 * @method collapseAll
		 */
		collapseAll : function(){
			this._table.treetable('collapseAll');
		}
	});

})(window);