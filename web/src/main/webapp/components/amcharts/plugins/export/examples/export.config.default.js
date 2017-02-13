/**
 * This is a sample chart export config file. It is provided as a reference on
 * how miscelaneous items in export menu can be used and set up.
 *
 * You do not need to use this file. It contains default export menu options 
 * that will be shown if you do not provide any "menu" in your export config.
 *
 * Please refer to README.md for more information.
 */


/**
 * PDF-specfic configuration
 */
AmCharts.exportPDF = {
	"format": "PDF",
	"content": [ "Saved from:", window.location.href, {
		"image": "reference",
		"fit": [ 523.28, 769.89 ] // fit image to A4
	} ]
};

/**
 * Print-specfic configuration
 */
AmCharts.exportPrint = {
	"format": "PRINT",
	"label": "Print"
};

/**
 * Define main universal config
 */
AmCharts.exportCFG = {
	"enabled": true,
	"menu": [ {
		"class": "export-main",
		"label": "Export",
		"menu": [ {
			"label": "Download as ...",
			"menu": [ "PNG", "JPG", "SVG", AmCharts.exportPDF ]
		}, {
			"label": "Save data ...",
			"menu": [ "CSV", "XLSX", "JSON" ]
		}, {
			"label": "Annotate",
			"action": "draw"
		}, AmCharts.exportPrint ]
	} ],

	"drawing": {
		"menu": [ {
			"class": "export-drawing",
			"menu": [ {
				"label": "Add ...",
				"menu": [ {
					"label": "Shape ...",
					"action": "draw.shapes"
				}, {
					"label": "Text",
					"action": "text"
				} ]
			}, {
				"label": "Change ...",
				"menu": [ {
					"label": "Mode ...",
					"action": "draw.modes"
				}, {
					"label": "Color ...",
					"action": "draw.colors"
				}, {
					"label": "Size ...",
					"action": "draw.widths"
				}, {
					"label": "Opactiy ...",
					"action": "draw.opacities"
				}, "UNDO", "REDO" ]
			}, {
				"label": "Download as...",
				"menu": [ "PNG", "JPG", "SVG", "PDF" ]
			}, "PRINT", "CANCEL" ]
		} ]
	}
};