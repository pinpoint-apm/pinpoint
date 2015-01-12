AmCharts.AmExport = AmCharts.Class({
	construct: function(chart, cfg, init ) {
		var _this					= this;
		_this.DEBUG					= false;
		_this.chart					= chart;
		_this.canvas				= null;
		_this.svgs					= [];
		_this.userCFG				= cfg;
		_this.buttonIcon			= 'export.png';
		_this.exportPNG				= true;
		_this.exportPDF				= false;
		_this.exportJPG				= false;
		_this.exportSVG				= false;
		//_this.left;
		_this.right					= 0;
		//_this.bottom;
		_this.top					= 0;
		//_this.color;
		_this.buttonRollOverColor	= "#EFEFEF";
		//_this.buttonColor			= "#FFFFFF";
		//_this.buttonRollOverAlpha	= 0.5;
		_this.textRollOverColor		= "#CC0000";
		_this.buttonTitle			= "Save chart as an image";
		_this.buttonAlpha			= 0.75;
		_this.imageFileName			= "amChart";
		_this.imageBackgroundColor	= "#FFFFFF";

		if (init) {
			_this.init();
		}
	},

	toCoordinate:function(value){
		if(value === undefined){
			return "auto";
		}
		if(String(value).indexOf("%") != -1){
			return value;
		}
		else{
			return value + "px";
		}
	},

	init: function(){
		var _this = this;

		var formats = [];
		if (_this.exportPNG) {
			formats.push("png");
		}
		if (_this.exportPDF) {
			formats.push("pdf");
		}
		if (_this.exportJPG) {
			formats.push("jpg");
		}
		if (_this.exportSVG) {
			formats.push("svg");
		}

		var menuItems = [];
		if(formats.length == 1){
			var format = formats[0];
			menuItems.push({format:format, iconTitle:_this.buttonTitle, icon:_this.chart.pathToImages + _this.buttonIcon})
		}
		else if(formats.length > 1){
			var subItems = [];
			for(var i = 0; i < formats.length; i++){
				subItems.push({format:formats[i], title:formats[i].toUpperCase()});
			}
			menuItems.push({onclick: function() {}, icon:_this.chart.pathToImages + _this.buttonIcon, items:subItems})
		}

		var color = _this.color;
		if(color === undefined){
			color = _this.chart.color;
		}

		var buttonColor = _this.buttonColor;
		if(buttonColor === undefined){
			buttonColor = "transparent";
		}

		_this.cfg = {
			menuTop		: _this.toCoordinate(_this.top),
			menuLeft	: _this.toCoordinate(_this.left),
			menuRight	: _this.toCoordinate(_this.right),
			menuBottom	: _this.toCoordinate(_this.bottom),
			menuItems	: menuItems,
			menuItemStyle: {
				backgroundColor			: buttonColor,
				opacity					:_this.buttonAlpha,
				rollOverBackgroundColor	: _this.buttonRollOverColor,
				color					: color,
				rollOverColor			: _this.textRollOverColor,
				paddingTop				: '6px',
				paddingRight			: '6px',
				paddingBottom			: '6px',
				paddingLeft				: '6px',
				marginTop				: '0px',
				marginRight				: '0px',
				marginBottom			: '0px',
				marginLeft				: '0px',
				textAlign				: 'left',
				textDecoration			: 'none',
				fontFamily				: _this.chart.fontFamily,
				fontSize				: _this.chart.fontSize + 'px'
			},
			menuItemOutput: {
				backgroundColor	: _this.imageBackgroundColor,
				fileName		: _this.imageFileName,
				format			: 'png',
				output			: 'dataurlnewwindow',
				render			: 'browser',
				dpi				: 90,
				onclick			: function(instance, config, event) {
					event.preventDefault();
					instance.output(config);
				}
			},
			removeImagery: true
		};

		_this.processing = {
			buffer: [],
			drawn: 0,
			timer: 0
		};

		// Config dependency adaption
		if (typeof(window.canvg) != 'undefined' && typeof(window.RGBColor) != 'undefined') {
			_this.cfg.menuItemOutput.render = 'canvg';
		}
		if (typeof(window.saveAs) != 'undefined') {
			_this.cfg.menuItemOutput.output = 'save';
		}
		if (AmCharts.isIE && AmCharts.IEversion < 10) {
			_this.cfg.menuItemOutput.output = 'dataurlnewwindow';
		}

		// Merge given configs
		var cfg = _this.userCFG;
		if (cfg) {
			cfg.menuItemOutput = AmCharts.extend(_this.cfg.menuItemOutput, cfg.menuItemOutput || {});
			cfg.menuItemStyle = AmCharts.extend(_this.cfg.menuItemStyle, cfg.menuItemStyle || {});
			_this.cfg = AmCharts.extend(_this.cfg, cfg);
		}

		// Add reference to chart
		_this.chart.AmExport = _this;

		// Listen to the drawer
		_this.chart.addListener('rendered', function() {
			_this.setup();
		});

		// DEBUG; Public reference
		if (_this.DEBUG) {
			window.AmExport = _this;
		}
	},

	/*
	Simple log function for internal purpose
	@param **args
	*/
	log: function() {
		console.log('AmExport: ', arguments);
	},

	/* PUBLIC
	Prepares everything to get exported
	@param none
	*/
	setup: function() {
		var _this = this;

		if (!AmCharts.isIE || (AmCharts.isIE && AmCharts.IEversion > 9)) {
			_this.generateButtons();
		}
	},

	/* PUBLIC
	Decodes base64 string to binary array
	@param base64_string
	@copyright Eli Grey, http://eligrey.com and Devin Samarin, https://github.com/eboyjr
	*/
	generateBinaryArray: function(base64_string) {
		var
		len = base64_string.length,
			buffer = new Uint8Array(len / 4 * 3 | 0),
			i = 0,
			outptr = 0,
			last = [0, 0],
			state = 0,
			save = 0,
			rank, code, undef, base64_ranks = new Uint8Array([
				62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, 0, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
			]);
		while (len--) {
			code = base64_string.charCodeAt(i++);
			rank = base64_ranks[code - 43];
			if (rank !== 255 && rank !== undef) {
				last[1] = last[0];
				last[0] = code;
				save = (save << 6) | rank;
				state++;
				if (state === 4) {
					buffer[outptr++] = save >>> 16;
					if (last[1] !== 61 /* padding character */ ) {
						buffer[outptr++] = save >>> 8;
					}
					if (last[0] !== 61 /* padding character */ ) {
						buffer[outptr++] = save;
					}
					state = 0;
				}
			}
		}
		// 2/3 chance there's going to be some null bytes at the end, but that
		// doesn't really matter with most image formats.
		// If it somehow matters for you, truncate the buffer up outptr.
		return buffer;
	},

	/*
	Creates blob object
	@param base64_datastring string
	@param type string
	*/
	generateBlob: function(datastring, type) {
		var _this	= this,
		header_end	= type!='image/svg+xml'?datastring.indexOf(',') + 1:0,
		header		= datastring.substring(0, header_end),
		data		= datastring,
		blob		= new Blob();

		if (header.indexOf('base64') != -1) {
			data = _this.generateBinaryArray(datastring.substring(header_end));
		}

		// Fake blob for IE
		if (AmCharts.isIE && AmCharts.IEversion < 10) {
			blob.data = data;
			blob.size = data.length;
			blob.type = type;
			blob.encoding = 'base64';
		} else {
			blob = new Blob([data], {
				type: type
			});
		}
		return blob;
	},

	/*
	Creates PDF object
	@param config object
	*/
	generatePDF: function(cfg) {
		var _this = this,
			pdf = {
				output: function() {
					return '';
				}
			},
			data = _this.canvas.toDataURL('image/jpeg'), // JSPDF ONLY SUPPORTS JPG
			width = (_this.canvas.width * 25.4) / cfg.dpi,
			height = (_this.canvas.height * 25.4) / cfg.dpi;

		// Check
		if (window.jsPDF) {
			pdf = new jsPDF();
			if (pdf.addImage) {
				pdf.addImage(data, 'JPEG', 0, 0, width, height);
			} else {
				alert("Missing jsPDF plugin; Please add the 'addImage' plugin.");
			}
		} else {
			alert("Missing jsPDF lib; Don't forget to add the addImage plugin.");
		}

		return pdf;
	},

	/*
	Creates the CANVAS to receive the image data
	@param format void()
	@param callback; given callback function which returns the blob or datastring of the configured ouput type
	*/
	output: function(cfg, externalCallback) {
		var _this = this;
		cfg = AmCharts.extend(AmCharts.extend({}, _this.cfg.menuItemOutput), cfg || {});

		// Prepare chart
		if(_this.chart.prepareForExport){
			_this.chart.prepareForExport();
		}

		/* PRIVATE
		Callback function which gets called after the drawing process is done
		@param none
		*/
		function internalCallback() {
			var data = null;
			var blob;

			// SVG
			if (cfg.format == 'image/svg+xml' || cfg.format == 'svg') {
				data = _this.generateSVG();
				blob = _this.generateBlob(data, 'image/svg+xml');

				if (cfg.output == 'save') {
					saveAs(blob, cfg.fileName + '.svg');
				} else if (cfg.output == 'datastring' || cfg.output == 'datauristring' || cfg.output == 'dataurlstring') {
					blob = 'data:image/svg+xml;base64,' + btoa(data);
				} else if (cfg.output == 'dataurlnewwindow') {
					window.open('data:image/svg+xml;base64,' + btoa(data));
				} else if (cfg.output == 'datauri' || cfg.output == 'dataurl') {
					location.href = 'data:image/svg+xml;base64,' + btoa(data);
				} else if (cfg.output == 'datastream') {
					location.href = 'data:image/octet-stream;base64,' + data;
				}

				if (externalCallback) {
					externalCallback.apply(_this, [blob]);
				}

				// PDF
			} else if (cfg.format == 'application/pdf' || cfg.format == 'pdf') {
				data = _this.generatePDF(cfg).output('dataurlstring');
				blob = _this.generateBlob(data, 'application/pdf');

				if (cfg.output == 'save') {
					saveAs(blob, cfg.fileName + '.pdf');
				} else if (cfg.output == 'datastring' || cfg.output == 'datauristring' || cfg.output == 'dataurlstring') {
					blob = data;
				} else if (cfg.output == 'dataurlnewwindow') {
					window.open(data);
				} else if (cfg.output == 'datauri' || cfg.output == 'dataurl') {
					location.href = data;
				} else if (cfg.output == 'datastream') {
					location.href = data.replace('application/pdf', 'application/octet-stream');
				}

				if (externalCallback) {
					externalCallback.apply(_this, [blob]);
				}

				// PNG
			} else if (cfg.format == 'image/png' || cfg.format == 'png') {
				data = _this.canvas.toDataURL('image/png');
				blob = _this.generateBlob(data, 'image/png');

				if (cfg.output == 'save') {
					saveAs(blob, cfg.fileName + '.png');
				} else if (cfg.output == 'datastring' || cfg.output == 'datauristring' || cfg.output == 'dataurlstring') {
					blob = data;
				} else if (cfg.output == 'dataurlnewwindow') {
					window.open(data);
				} else if (cfg.output == 'datauri' || cfg.output == 'dataurl') {
					location.href = data;
				} else if (cfg.output == 'datastream') {
					location.href = data.replace('image/png', 'image/octet-stream');
				}

				if (externalCallback) {
					externalCallback.apply(_this, [blob]);
				}

				// JPG
			} else if (cfg.format == 'image/jpeg' || cfg.format == 'jpeg' || cfg.format == 'jpg') {
				data = _this.canvas.toDataURL('image/jpeg');
				blob = _this.generateBlob(data, 'image/jpeg');

				if (cfg.output == 'save') {
					saveAs(blob, cfg.fileName + '.jpg');
				} else if (cfg.output == 'datastring' || cfg.output == 'datauristring' || cfg.output == 'dataurlstring') {
					blob = data;
				} else if (cfg.output == 'dataurlnewwindow') {
					window.open(data);
				} else if (cfg.output == 'datauri' || cfg.output == 'dataurl') {
					location.href = data;
				} else if (cfg.output == 'datastream') {
					location.href = data.replace('image/jpeg', 'image/octet-stream');
				}

				if (externalCallback) {
					externalCallback.apply(_this, [blob]);
				}
			}

		}

		return _this.generateOutput(cfg, internalCallback);
	},

	/* PUBLIC
	Polifies missing attributes to the SVG and replaces images to embedded base64 images
	@param none
	*/
	polifySVG: function(svg) {
		var _this	= this;

		// Recursive function to force the attributes
		function recursiveChange(svg, tag) {
			var items	= svg.getElementsByTagName(tag);
			var i		= items.length;

			while(i--) {
				if (_this.cfg.removeImagery) {
					items[i].parentNode.removeChild(items[i]);

				} else {
					var image		= document.createElement('img');
					var canvas		= document.createElement('canvas');
					var ctx			= canvas.getContext('2d');

					canvas.width	= items[i].getAttribute('width');
					canvas.height	= items[i].getAttribute('height');
					image.src		= items[i].getAttribute('xlink:href');
					image.width		= items[i].getAttribute('width');
					image.height	= items[i].getAttribute('height');

					try {
						ctx.drawImage(image, 0, 0, image.width, image.height);
						datastring = canvas.toDataURL(); // image.src; // canvas.toDataURL(); //
					} catch (err) {
						datastring = image.src; // image.src; // canvas.toDataURL(); //

						_this.log('Tainted canvas, reached browser CORS security; origin from imagery must be equal to the server!');
						throw new Error(err);
					}

					items[i].setAttribute('xlink:href', datastring);
				}

			}
		}

		// Put some attrs to it; fixed 20/03/14 xmlns is required to produce a valid svg file
		if (AmCharts.IEversion == 0) {
			svg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
			if ( !_this.cfg.removeImagery ) {
				svg.setAttribute('xmlns:xlink','http://www.w3.org/1999/xlink');
			}
		}

		// Force link adaption
		recursiveChange(svg, 'pattern');
		recursiveChange(svg, 'image');
		_this.svgs.push(svg);

		return svg;
	},

	/* PUBLIC
	Stacks multiple SVGs into one
	@param none
	*/
	generateSVG: function() {
		var _this	= this;
		var context	= document.createElement('svg');
		context.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
		context.setAttribute('xmlns:xlink','http://www.w3.org/1999/xlink');

		for (var i = 0; i < _this.processing.buffer.length; i++) {
			var group	= document.createElement('g'),
				data	= _this.processing.buffer[i];

			data[0].setAttribute('xmlns', 'http://www.w3.org/2000/svg');
			data[0].setAttribute('xmlns:xlink','http://www.w3.org/1999/xlink');

			group.setAttribute('transform', 'translate('+data[1].x+','+data[1].y+')');
			group.appendChild(data[0]);
			context.appendChild(group);
		}

		return new XMLSerializer().serializeToString(context);
	},

	/* PUBLIC
	Generates the canvas with the given SVGs and configured renderer
	@param callback; function(); gets called after drawing process on the canvas has been finished
	*/
	generateOutput: function(cfg, callback) {
		var _this	= this,
		coll		= [],
		svgs		= [],
		canvas		= document.createElement('canvas'),
		context		= canvas.getContext('2d'),
		offset		= {
			y: 0,
			x: 0
		},

		// Push svgs into array
		coll = _this.chart.div.getElementsByTagName('svg');
		for ( var i = 0; i < coll.length; i++ ) svgs.push(coll[i]);

		// Add external legend
		if ( _this.chart.legend && _this.chart.legend.position == 'outside' ) {
			_this.chart.legend.container.container.externalLegend = true
			svgs.push(_this.chart.legend.container.container);

			// Add offset
			if ( _this.cfg.legendPosition == 'left' ) {
				offset.x = _this.chart.legend.div.offsetWidth;
			} else if ( _this.cfg.legendPosition == 'top' ) {
				offset.y = _this.chart.legend.div.offsetHeight;
			} else if ( typeof _this.cfg.legendPosition == 'object' ) {
				offset.y = _this.cfg.legendPosition.chartTop;
				offset.x = _this.cfg.legendPosition.chartLeft;
			}
		}

		// Reset
		_this.processing.buffer	= [];
		_this.processing.drawn	= 0;
		_this.canvas			= canvas;
		_this.svgs				= [];
		var remember = {
			x: 0,
			y: 0
		}

		for (var i = 0; i < svgs.length; i++) {
			var parent    = svgs[i].parentNode,
			svgX          = Number(parent.style.left.slice(0, -2)),
			svgY          = Number(parent.style.top.slice(0, -2)),
			svgClone      = _this.polifySVG(svgs[i].cloneNode(true)),
			tmp           = AmCharts.extend({}, offset);

			// Add external legend
			if ( svgs[i].externalLegend ) {
				if ( _this.cfg.legendPosition == 'right' ) {
					offset.y = 0;
					offset.x = _this.chart.divRealWidth;

				} else if ( _this.cfg.legendPosition == 'bottom' ) {
					offset.y = svgY ? svgY : offset.y;

				} else if ( typeof _this.cfg.legendPosition == 'object' ) {
					offset.x = _this.cfg.legendPosition.left;
					offset.y = _this.cfg.legendPosition.top;

				} else {
					offset.x = 0;
					offset.y = 0;
				}

			// Overtake parent position if given; fixed 20/03/14 distinguish between relativ and others
			} else {
				if ( parent.style.position == 'relative' ) {
					offset.x = svgX ? svgX : offset.x;
					offset.y = svgY ? svgY : offset.y;
				} else {
					offset.x = svgX + remember.x;
					offset.y = svgY + remember.y;
				}
			}

			_this.processing.buffer.push([svgClone, AmCharts.extend({}, offset)]);

			// Put back from "cache"
			if ( svgY && svgX ) {
				offset = tmp;

			// New offset for next one
			} else {
				offset.y += svgY ? 0 : parent.offsetHeight;
			}

			// HOTFIX 25/10/14
			if ( parent.style.position == "absolute" && parent.getAttribute("class") == "amChartsLegend" ) {
				remember.y += parent.parentNode.offsetHeight;
			}
		}

		canvas.id		= AmCharts.getUniqueId();
		canvas.width	= _this.chart.divRealWidth;
		canvas.height	= _this.chart.divRealHeight;

		// External legend exception
		if ( _this.chart.legend && _this.chart.legend.position == "outside" ) {
			if ( ['left','right'].indexOf(_this.cfg.legendPosition) != -1 ) {
				canvas.width	+= _this.chart.legend.div.offsetWidth;

			} else if ( typeof _this.cfg.legendPosition == 'object' ) {
				canvas.width	+= _this.cfg.legendPosition.width;
				canvas.height	+= _this.cfg.legendPosition.height;

			} else {
				canvas.height	+= _this.chart.legend.div.offsetHeight;
			}
		}

		// Stockchart exception
		var adapted = {
			width: false,
			height: false
		};
		if ( _this.chart.periodSelector ) {
			if ( ['left','right'].indexOf(_this.chart.periodSelector.position) != -1 ) {
				canvas.width -= _this.chart.periodSelector.div.offsetWidth + 16;
				adapted.width = true;
			} else {
				canvas.height -= _this.chart.periodSelector.div.offsetHeight;
				adapted.height = true;
			}
		}

		if ( _this.chart.dataSetSelector ) {
			if ( ['left','right'].indexOf(_this.chart.dataSetSelector.position) != -1 ) {
				if ( !adapted.width ) {
					canvas.width -= _this.chart.dataSetSelector.div.offsetWidth + 16;
				}
			} else {
				canvas.height -= _this.chart.dataSetSelector.div.offsetHeight;
			}
		}

		// Set given background; jpeg default
		if (cfg.backgroundColor || cfg.format == 'image/jpeg') {
			context.fillStyle = cfg.backgroundColor || '#FFFFFF';
			context.fillRect(0, 0, canvas.width, canvas.height);
		}

		/* PRIVATE
		Recursive function to draw the images to the canvas;
		@param none;
		*/
		function drawItWhenItsLoaded() {
			var img, buffer, offset, source;

			// DRAWING PROCESS DONE
			if (_this.processing.buffer.length == _this.processing.drawn || cfg.format == 'svg' ) {
				return callback();

			// LOOPING LUI
			} else {
				buffer = _this.processing.buffer[_this.processing.drawn];
				source = new XMLSerializer().serializeToString(buffer[0]); //source = 'data:image/svg+xml;base64,' + btoa();
				offset = buffer[1];

				// NATIVE
				if (cfg.render == 'browser') {
					img		= new Image();
					img.id	= AmCharts.getUniqueId();
					source	= 'data:image/svg+xml;base64,' + btoa(source);

					//img.crossOrigin	= "Anonymous";
					img.onload = function() {
						context.drawImage(this, buffer[1].x, buffer[1].y);
						_this.processing.drawn++;

						drawItWhenItsLoaded();
					};
					img.onerror = function() {
						context.drawImage(this, buffer[1].x, buffer[1].y);
						_this.processing.drawn++;
						drawItWhenItsLoaded();
					};
					img.src = source;

					if (img.complete || typeof(img.complete) == 'undefined' || img.complete === undefined) {
						img.src = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==";
						img.src = source;
					}

				// CANVG
				} else if (cfg.render == 'canvg') {
					canvg(canvas, source, {
						offsetX: offset.x,
						offsetY: offset.y,
						ignoreMouse: true,
						ignoreAnimation: true,
						ignoreDimensions: true,
						ignoreClear: true,
						renderCallback: function() {
							_this.processing.drawn++;
							drawItWhenItsLoaded();
						}
					});
				}
			}
		}
		return drawItWhenItsLoaded();
	},

	/*
	Generates the export menu to trigger the exportation
	@param none;
	*/
	generateButtons: function() {
		var _this = this,lvl = 0;
		var div;
		if(_this.div){
			div = _this.div;
			div.innerHTML = "";
		}
		else{
			div = document.createElement('div'),
			_this.div = div;
		}

		// Push sublings
		function createList(items) {
			var ul = document.createElement('ul');

			ul.setAttribute('style', 'list-style: none; margin: 0; padding: 0;');

			// Walkthrough items
			for (var i = 0; i < items.length; i++) {
				var li = document.createElement('li'),
					img = document.createElement('img'),
					a = document.createElement('a'),
					item = items[i],
					children = null,
					itemStyle = AmCharts.extend(AmCharts.extend({}, _this.cfg.menuItemStyle), items[i]);

				// MERGE CFG
				item = AmCharts.extend(AmCharts.extend({}, _this.cfg.menuItemOutput), item);

				// ICON
				if (item['icon']) {
					img.alt = '';
					img.src = item['icon'];
					img.setAttribute('style', 'margin: 0 auto;border: none;outline: none');
					if (item['iconTitle']) {
						img.title = item['iconTitle'];
					}
					a.appendChild(img);
				}

				// TITLE; STYLING
				a.href = '#';
				if (item['title']) {
					img.setAttribute('style', 'margin: 0px 5px;');
					a.innerHTML += item.title;
				}
				a.setAttribute('style', 'display: block;');
				AmCharts.extend(a.style, itemStyle);

				// ONCLICK
				a.onclick = item.onclick.bind(a, _this, item);
				li.appendChild(a);

				// APPEND SIBLINGS
				if (item.items) {
					children = createList(item.items);
					li.appendChild(children);

					li.onmouseover = function() {
						children.style.display = 'block';
					};
					li.onmouseout = function() {
						children.style.display = 'none';
					};
					children.style.display = 'none';
				}

				// Append to parent
				ul.appendChild(li);

				// Apply hover
				a.onmouseover = function() {
					this.style.backgroundColor = itemStyle.rollOverBackgroundColor;
					this.style.color = itemStyle.rollOverColor;
					this.style.borderColor = itemStyle.rollOverBorderColor;
				};
				a.onmouseout = function() {
					this.style.backgroundColor = itemStyle.backgroundColor;
					this.style.color = itemStyle.color;
					this.style.borderColor = itemStyle.borderColor;
				};
			}
			lvl++;

			return ul;
		}

		// Style wrapper; Push into chart div
		div.setAttribute('style', 'position: absolute;top:' + _this.cfg.menuTop + ';right:' + _this.cfg.menuRight + ';bottom:' + _this.cfg.menuBottom + ';left:' + _this.cfg.menuLeft + ';');
		div.setAttribute('class', 'amExportButton');
		div.appendChild(createList(_this.cfg.menuItems));
		_this.chart.containerDiv.appendChild(div);
	}
});