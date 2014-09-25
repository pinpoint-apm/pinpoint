(function(window){
	"use strict";

	/**
	 * Tree Grid Table
	 * @class TreeGridTable 
	 * @version 0.0.2
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
				height : 'auto',
				intent : 15,
				dblClickResize : true,
				singleSelect : true
			});
			this.option(options);
				
			this._executeVendors();
		},
		
		/**
		 * internal constructor
		 * 
		 * @method _executeVendors
		 */
		_executeVendors : function(){
			var self = this,
				tableId = this.option('tableId');
			this._table = $('#' + tableId);
			
			if(typeof this._table === 'undefined' || typeof this._table.get(0) === 'undefined' || this._table.get(0).nodeName !== 'TABLE'){
				console.error('Id of TreeGridTable is undefined.');
				return;
			}
			
			this._table.flexigrid({
				height: this.option('height'),
				dblClickResize : this.option('dblClickResize'),
				singleSelect : this.option('singleSelect'),
				onClick : function(tr, grid, option, selected){
					var ttId = $(tr).data('tt-id'),
						node = self._table.treetable('node', ttId),
						ancestors = node.ancestors();
					self._updateAncestorsClass(ancestors, selected);
				}
			});
			this._table.treetable({
				expandable : true,
				clickableNodeNames : false,
				indent : this.option('intent'),
				columnInnerElType : 'div',
				onNodeExpanded : function(){
					self._table.flexHeight();
				},
				onNodeCollapsed : function(){
					self._table.flexHeight();
				}
			});
			this.expandAll();
			
			this._table.flexHeight();
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
		},
		
		/**
		 * 조상 노드의 클래스 추가
		 * 
		 * @method _updateAncestorsClass
		 * @param {Array} ancestors nodes of treetable
		 * @param {Boolean} selected 
		 */
		_updateAncestorsClass : function(ancestors, selected){
			$('tr', this._table).removeClass('ancestorSelected');
			if(!ancestors && ancestors.length < 1) {
				return;
			}
			if(!selected) {
				return;
			}
			for(var i=0; i<ancestors.length; i+=1){
				ancestors[i].row.addClass('ancestorSelected');
			}
		}
	});

})(window);