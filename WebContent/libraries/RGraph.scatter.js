    /**
    * o------------------------------------------------------------------------------o
    * | This file is part of the RGraph package - you can learn more at:             |
    * |                                                                              |
    * |                          http://www.rgraph.net                               |
    * |                                                                              |
    * | This package is licensed under the RGraph license. For all kinds of business |
    * | purposes there is a small one-time licensing fee to pay and for non          |
    * | commercial  purposes it is free to use. You can read the full license here:  |
    * |                                                                              |
    * |                      http://www.rgraph.net/LICENSE.txt                       |
    * o------------------------------------------------------------------------------o
    */

    if (typeof(RGraph) == 'undefined') RGraph = {};

    /**
    * The scatter graph constructor
    * 
    * @param object canvas The cxanvas object
    * @param array  data   The chart data
    */
    RGraph.Scatter = function (id, data)
    {
        // Get the canvas and context objects
        this.id                = id;
        this.canvas            = document.getElementById(id);
        this.canvas.__object__ = this;
        this.context           = this.canvas.getContext ? this.canvas.getContext("2d") : null;
        this.max               = 0;
        this.coords            = [];
        this.data              = [];
        this.type              = 'scatter';
        this.isRGraph          = true;
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();


        /**
        * Compatibility with older browsers
        */
        RGraph.OldBrowserCompat(this.context);


        // Various config properties
        this.properties = {
            'chart.background.barcolor1':   'rgba(0,0,0,0)',
            'chart.background.barcolor2':   'rgba(0,0,0,0)',
            'chart.background.grid':        true,
            'chart.background.grid.width':  1,
            'chart.background.grid.color':  '#ddd',
            'chart.background.grid.hsize':  20,
            'chart.background.grid.vsize':  20,
            'chart.background.hbars':       null,
            'chart.background.vbars':       null,
            'chart.background.grid.vlines': true,
            'chart.background.grid.hlines': true,
            'chart.background.grid.border': true,
            'chart.background.grid.autofit':true,
            'chart.background.grid.autofit.numhlines': 5,
            'chart.background.grid.autofit.numvlines': 20,
            'chart.background.image':       null,
            'chart.background.image.stretch': true,
            'chart.background.image.x':     null,
            'chart.background.image.y':     null,
            'chart.background.image.w':     null,
            'chart.background.image.h':     null,
            'chart.background.image.align': null,
            'chart.text.size':              10,
            'chart.text.angle':             0,
            'chart.text.color':             'black',
            'chart.text.font':              'Arial',
            'chart.tooltips':               [], // Default must be an empty array
            'chart.tooltips.effect':         'fade',
            'chart.tooltips.event':          'onmousemove',
            'chart.tooltips.hotspot':        3,
            'chart.tooltips.css.class':      'RGraph_tooltip',
            'chart.tooltips.highlight':      true,
            'chart.tooltips.coords.page':   false,
            'chart.units.pre':              '',
            'chart.units.post':             '',
            'chart.numyticks':              10,
            'chart.tickmarks':              'cross',
            'chart.ticksize':               5,
            'chart.xticks':                 true,
            'chart.xaxis':                  true,
            'chart.gutter.left':            25,
            'chart.gutter.right':           25,
            'chart.gutter.top':             25,
            'chart.gutter.bottom':          25,
            'chart.xmin':                   0,
            'chart.xmax':                   0,
            'chart.ymax':                   null,
            'chart.ymin':                   null,
            'chart.scale.decimals':         null,
            'chart.scale.point':            '.',
            'chart.scale.thousand':         ',',
            'chart.title':                  '',
            'chart.title.background':       null,
            'chart.title.hpos':             null,
            'chart.title.vpos':             null,
            'chart.title.bold':             true,
            'chart.title.font':             null,
            'chart.title.xaxis':            '',
            'chart.title.xaxis.bold':       true,
            'chart.title.xaxis.size':       null,
            'chart.title.xaxis.font':       null,
            'chart.title.yaxis':            '',
            'chart.title.yaxis.bold':       true,
            'chart.title.yaxis.size':       null,
            'chart.title.yaxis.font':       null,
            'chart.title.xaxis.pos':        null,
            'chart.title.yaxis.pos':        null,
            'chart.labels':                 [],
            'chart.labels.ingraph':         null,
            'chart.labels.above':           false,
            'chart.labels.above.size':      8,
            'chart.labels.above.decimals':  0,
            'chart.ylabels':                true,
            'chart.ylabels.count':          5,
            'chart.ylabels.invert':         false,
            'chart.ylabels.specific':       null,
            'chart.ylabels.inside':         false,
            'chart.contextmenu':            null,
            'chart.defaultcolor':           'black',
            'chart.xaxispos':               'bottom',
            'chart.yaxispos':               'left',
            'chart.crosshairs':             false,
            'chart.crosshairs.color':       '#333',
            'chart.crosshairs.linewidth':   1,
            'chart.crosshairs.coords':      false,
            'chart.crosshairs.coords.fixed':true,
            'chart.crosshairs.coords.fadeout':false,
            'chart.crosshairs.coords.labels.x': 'X',
            'chart.crosshairs.coords.labels.y': 'Y',
            'chart.crosshairs.hline':       true,
            'chart.crosshairs.vline':       true,
            'chart.annotatable':            false,
            'chart.annotate.color':         'black',
            'chart.line':                   false,
            'chart.line.linewidth':         1,
            'chart.line.colors':            ['green', 'red'],
            'chart.line.shadow.color':      'rgba(0,0,0,0)',
            'chart.line.shadow.blur':       2,
            'chart.line.shadow.offsetx':    3,
            'chart.line.shadow.offsety':    3,
            'chart.line.stepped':           false,
            'chart.noaxes':                 false,
            'chart.key':                    null,
            'chart.key.background':         'white',
            'chart.key.position':           'graph',
            'chart.key.halign':             'right',
            'chart.key.shadow':             false,
            'chart.key.shadow.color':       '#666',
            'chart.key.shadow.blur':        3,
            'chart.key.shadow.offsetx':     2,
            'chart.key.shadow.offsety':     2,
            'chart.key.position.gutter.boxed': true,
            'chart.key.position.x':         null,
            'chart.key.position.y':         null,
            'chart.key.color.shape':        'square',
            'chart.key.rounded':            true,
            'chart.key.linewidth':          1,
            'chart.key.colors':             null,
            'chart.axis.color':             'black',
            'chart.zoom.factor':            1.5,
            'chart.zoom.fade.in':           true,
            'chart.zoom.fade.out':          true,
            'chart.zoom.hdir':              'right',
            'chart.zoom.vdir':              'down',
            'chart.zoom.frames':            25,
            'chart.zoom.delay':             16.666,
            'chart.zoom.shadow':            true,
            'chart.zoom.background':        true,
            'chart.zoom.action':            'zoom',
            'chart.boxplot.width':          1,
            'chart.boxplot.capped':         true,
            'chart.resizable':              false,
            'chart.resize.handle.background': null,
            'chart.xmin':                   0,
            'chart.labels.specific.align':  'left',
            'chart.xscale':                 false,
            'chart.xscale.units.pre':       '',
            'chart.xscale.units.post':      '',
            'chart.xscale.numlabels':       10,
            'chart.xscale.formatter':       null,
            'chart.noendxtick':             false,
            'chart.noendytick':             true,
            'chart.events.mousemove':       null,
            'chart.events.click':           null,
            'chart.highlight.stroke':       'rgba(0,0,0,0)',
            'chart.highlight.fill':         'rgba(255,255,255,0.7)'
        }

        // Handle multiple datasets being given as one argument
        if (arguments[1][0] && arguments[1][0][0] && typeof(arguments[1][0][0][0]) == 'number') {
            // Store the data set(s)
            for (var i=0; i<arguments[1].length; ++i) {
                this.data[i] = arguments[1][i];
            }

        // Handle multiple data sets being supplied as seperate arguments
        } else {
            // Store the data set(s)
            for (var i=1; i<arguments.length; ++i) {
                this.data[i - 1] = arguments[i];
            }
        }



        /**
        * Now make the data_arr array - all the data as one big array
        */
        this.data_arr = [];

        for (var i=0; i<this.data.length; ++i) {
            for (var j=0; j<this.data[i].length; ++j) {
                this.data_arr.push(this.data[i][j]);
            }
        }



        // Check for support
        if (!this.canvas) {
            alert('[SCATTER] No canvas support');
            return;
        }


        /**
        * Register the object
        */
        RGraph.Register(this);
    }


    /**
    * A simple setter
    * 
    * @param string name  The name of the property to set
    * @param string value The value of the property
    */
    RGraph.Scatter.prototype.Set = function (name, value)
    {
        /**
        * This is here because the key expects a name of "chart.colors"
        */
        if (name == 'chart.line.colors') {
            this.properties['chart.colors'] = value;
        }
        
        /**
        * Allow compatibility with older property names
        */
        if (name == 'chart.tooltip.hotspot') {
            name = 'chart.tooltips.hotspot';
        }
        
        /**
        * chart.yaxispos should be left or right
        */
        if (name == 'chart.yaxispos' && value != 'left' && value != 'right') {
            alert("[SCATTER] chart.yaxispos should be left or right. You've set it to: '" + value + "' Changing it to left");
            value = 'left';
        }
        
        /**
        * Check for xaxispos
        */
        if (name == 'chart.xaxispos' ) {
            if (value != 'bottom' && value != 'center') {
                alert('[SCATTER] (' + this.id + ') chart.xaxispos should be center or bottom. Tried to set it to: ' + value + ' Changing it to center');
                value = 'center';
            }
        }

        this.properties[name.toLowerCase()] = value;
    }


    /**
    * A simple getter
    * 
    * @param string name  The name of the property to set
    */
    RGraph.Scatter.prototype.Get = function (name)
    {
        return this.properties[name];
    }


    /**
    * The function you call to draw the line chart
    */
    RGraph.Scatter.prototype.Draw = function ()
    {
        // MUST be the first thing done!
        if (typeof(this.Get('chart.background.image')) == 'string') {
            RGraph.DrawBackgroundImage(this);
        }


        /**
        * Fire the onbeforedraw event
        */
        RGraph.FireCustomEvent(this, 'onbeforedraw');


        
        /**
        * This is new in May 2011 and facilitates indiviual gutter settings,
        * eg chart.gutter.left
        */
        this.gutterLeft   = this.Get('chart.gutter.left');
        this.gutterRight  = this.Get('chart.gutter.right');
        this.gutterTop    = this.Get('chart.gutter.top');
        this.gutterBottom = this.Get('chart.gutter.bottom');

        // Go through all the data points and see if a tooltip has been given
        this.hasTooltips = false;
        var overHotspot  = false;

        // Reset the coords array
        this.coords = [];

        /**
        * Look for tooltips and populate chart.tooltips
        * 
        * NB 26/01/2011 Updated so that chart.tooltips is ALWAYS populated
        */
        if (!RGraph.isOld()) {
            this.Set('chart.tooltips', []);
            for (var i=0; i<this.data.length; ++i) {
                for (var j =0;j<this.data[i].length; ++j) {

                    if (this.data[i][j] && this.data[i][j][3]) {
                        this.Get('chart.tooltips').push(this.data[i][j][3]);
                        this.hasTooltips = true;
                    } else {
                        this.Get('chart.tooltips').push(null);
                    }
                }
            }
        }

        // Reset the maximum value
        this.max = 0;

        // Work out the maximum Y value
        if (this.Get('chart.ymax') && this.Get('chart.ymax') > 0) {

            this.scale = [];
            this.max   = this.Get('chart.ymax');
            this.min   = this.Get('chart.ymin') ? this.Get('chart.ymin') : 0;

            this.scale[0] = ((this.max - this.min) * (1/5)) + this.min;
            this.scale[1] = ((this.max - this.min) * (2/5)) + this.min;
            this.scale[2] = ((this.max - this.min) * (3/5)) + this.min;
            this.scale[3] = ((this.max - this.min) * (4/5)) + this.min;
            this.scale[4] = ((this.max - this.min) * (5/5)) + this.min;

            var decimals = this.Get('chart.scale.decimals');

            this.scale = [
                          Number(this.scale[0]).toFixed(decimals),
                          Number(this.scale[1]).toFixed(decimals),
                          Number(this.scale[2]).toFixed(decimals),
                          Number(this.scale[3]).toFixed(decimals),
                          Number(this.scale[4]).toFixed(decimals)
                         ];

        } else {

            var i = 0;
            var j = 0;

            for (i=0; i<this.data.length; ++i) {
                for (j=0; j<this.data[i].length; ++j) {
                    this.max = Math.max(this.max, typeof(this.data[i][j][1]) == 'object' ? RGraph.array_max(this.data[i][j][1]) : Math.abs(this.data[i][j][1]));
                }
            }

            this.scale = RGraph.getScale(this.max, this);

            this.max   = this.scale[4];
            this.min   = this.Get('chart.ymin') ? this.Get('chart.ymin') : 0;

            if (this.min) {
                this.scale[0] = ((this.max - this.min) * (1/5)) + this.min;
                this.scale[1] = ((this.max - this.min) * (2/5)) + this.min;
                this.scale[2] = ((this.max - this.min) * (3/5)) + this.min;
                this.scale[3] = ((this.max - this.min) * (4/5)) + this.min;
                this.scale[4] = ((this.max - this.min) * (5/5)) + this.min;
            }


            if (typeof(this.Get('chart.scale.decimals')) == 'number') {
                var decimals = this.Get('chart.scale.decimals');
    
                this.scale = [
                              Number(this.scale[0]).toFixed(decimals),
                              Number(this.scale[1]).toFixed(decimals),
                              Number(this.scale[2]).toFixed(decimals),
                              Number(this.scale[3]).toFixed(decimals),
                              Number(this.scale[4]).toFixed(decimals)
                             ];
            }
        }

        this.grapharea = this.canvas.height - this.gutterTop - this.gutterBottom;



        // Progressively Draw the chart
        RGraph.background.Draw(this);

        /**
        * Draw any horizontal bars that have been specified
        */
        if (this.Get('chart.background.hbars') && this.Get('chart.background.hbars').length) {
            RGraph.DrawBars(this);
        }

        /**
        * Draw any vertical bars that have been specified
        */
        if (this.Get('chart.background.vbars') && this.Get('chart.background.vbars').length) {
            this.DrawVBars();
        }

        if (!this.Get('chart.noaxes')) {
            this.DrawAxes();
        }

        this.DrawLabels();

        i = 0;
        for(i=0; i<this.data.length; ++i) {
            this.DrawMarks(i);

            // Set the shadow
            this.context.shadowColor   = this.Get('chart.line.shadow.color');
            this.context.shadowOffsetX = this.Get('chart.line.shadow.offsetx');
            this.context.shadowOffsetY = this.Get('chart.line.shadow.offsety');
            this.context.shadowBlur    = this.Get('chart.line.shadow.blur');
            
            this.DrawLine(i);

            // Turn the shadow off
            RGraph.NoShadow(this);
        }


        if (this.Get('chart.line')) {
            for (var i=0;i<this.data.length; ++i) {
                this.DrawMarks(i); // Call this again so the tickmarks appear over the line
            }
        }



        /**
        * Setup the context menu if required
        */
        if (this.Get('chart.contextmenu')) {
            RGraph.ShowContext(this);
        }

        
        
        /**
        * Draw the key if necessary
        */
        if (this.Get('chart.key') && this.Get('chart.key').length) {
            RGraph.DrawKey(this, this.Get('chart.key'), this.Get('chart.line.colors'));
        }


        /**
        * Draw " above" labels if enabled
        */
        if (this.Get('chart.labels.above')) {
            this.DrawAboveLabels();
        }

        /**
        * Draw the "in graph" labels, using the member function, NOT the shared function in RGraph.common.core.js
        */
        this.DrawInGraphLabels(this);

        
        /**
        * This function enables resizing
        */
        if (this.Get('chart.resizable')) {
            RGraph.AllowResizing(this);
        }


        /**
        * This installs the event listeners
        */
        RGraph.InstallEventListeners(this);
        
        /**
        * Fire the RGraph ondraw event
        */
        RGraph.FireCustomEvent(this, 'ondraw');
    }


    /**
    * Draws the axes of the scatter graph
    */
    RGraph.Scatter.prototype.DrawAxes = function ()
    {
        var canvas      = this.canvas;
        var context     = this.context;
        var graphHeight = this.canvas.height - this.gutterTop - this.gutterBottom;

        context.beginPath();
        context.strokeStyle = this.Get('chart.axis.color');
        context.lineWidth   = 1;

        // Draw the Y axis
        if (this.Get('chart.yaxispos') == 'left') {
            context.moveTo(AA(this, this.gutterLeft), this.gutterTop);
            context.lineTo(AA(this, this.gutterLeft), this.canvas.height - this.gutterBottom);
        } else {
            context.moveTo(AA(this, RGraph.GetWidth(this) - this.gutterRight), this.gutterTop);
            context.lineTo(AA(this, RGraph.GetWidth(this) - this.gutterRight), RGraph.GetHeight(this) - this.gutterBottom);
        }


        // Draw the X axis
        if (this.Get('chart.xaxis')) {
            if (this.Get('chart.xaxispos') == 'center') {
                context.moveTo(this.gutterLeft, AA(this, this.gutterTop + ((RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) / 2)));
                context.lineTo(this.canvas.width - this.gutterRight, AA(this, this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2)));
            } else {
                context.moveTo(this.gutterLeft, AA(this, this.canvas.height - this.gutterBottom));
                context.lineTo(this.canvas.width - this.gutterRight, AA(this, this.canvas.height - this.gutterBottom));
            }
        }

        // Draw the Y tickmarks
        var numyticks = this.Get('chart.numyticks');

        //for (y=this.gutterTop; y < this.canvas.height - this.gutterBottom + (this.Get('chart.xaxispos') == 'center' ? 1 : 0) ; y+=(graphHeight / numyticks)) {
        for (i=0; i<numyticks; ++i) {

            var y = ((this.canvas.height - this.gutterTop - this.gutterBottom) / numyticks) * i;
                y = y + this.gutterTop;
            
            if (this.Get('chart.xaxispos') == 'center' && i == (numyticks / 2)) {
                continue;
            }

            if (this.Get('chart.yaxispos') == 'left') {
                context.moveTo(this.gutterLeft, AA(this, y));
                context.lineTo(this.gutterLeft - 3, AA(this, y));
            } else {
                context.moveTo(this.canvas.width - this.gutterRight +3, AA(this, y));
                context.lineTo(this.canvas.width - this.gutterRight, AA(this, y));
            }
        }
        
        /**
        * Draw the end Y tickmark if the X axis is in the centre
        */
        if (this.Get('chart.xaxispos') == 'center' && this.Get('chart.yaxispos') == 'left') {
            context.moveTo(this.gutterLeft, AA(this, this.canvas.height - this.gutterBottom));
            context.lineTo(this.gutterLeft - 3, AA(this, this.canvas.height - this.gutterBottom));
        } else if (this.Get('chart.xaxispos') == 'center') {
            context.moveTo(this.canvas.width - this.gutterRight + 3, AA(this, this.canvas.height - this.gutterBottom));
            context.lineTo(this.canvas.width - this.gutterRight, AA(this, this.canvas.height - this.gutterBottom));
        }

        /**
        * Draw an extra tick if the X axis isn't being shown
        */
        if (this.Get('chart.xaxis') == false && this.Get('chart.yaxispos') == 'left') {
            this.context.moveTo(this.gutterLeft + 1, AA(this, this.canvas.height - this.gutterBottom));
            this.context.lineTo(this.gutterLeft - 3, AA(this, this.canvas.height - this.gutterBottom));
        } else if (this.Get('chart.xaxis') == false && this.Get('chart.yaxispos') == 'right') {
            this.context.moveTo(this.canvas.width - this.gutterRight, AA(this, this.canvas.height - this.gutterBottom));
            this.context.lineTo(this.canvas.width - this.gutterRight + 3, AA(this, this.canvas.height - this.gutterBottom));
        }


        /**
        * Draw the X tickmarks
        */
        if (this.Get('chart.xticks') && this.Get('chart.xaxis')) {
            var x  = 0;
            var y  =  (this.Get('chart.xaxispos') == 'center') ? this.gutterTop + (this.grapharea / 2): (this.canvas.height - this.gutterBottom);
            this.xTickGap = (this.Get('chart.labels') && this.Get('chart.labels').length) ? ((this.canvas.width - this.gutterLeft - this.gutterRight ) / this.Get('chart.labels').length) : (this.canvas.width - this.gutterLeft - this.gutterRight) / 10;


            for (x =  (this.gutterLeft + (this.Get('chart.yaxispos') == 'left' ? this.xTickGap : 0) );
                 x <= (this.canvas.width - this.gutterRight - (this.Get('chart.yaxispos') == 'left' ? -1 : 1));
                 x += this.xTickGap) {

                if (this.Get('chart.yaxispos') == 'left' && this.Get('chart.noendxtick') == true && x == (this.canvas.width - this.gutterRight) ) {
                    continue;
                } else if (this.Get('chart.yaxispos') == 'right' && this.Get('chart.noendxtick') == true && x == this.gutterLeft) {
                    continue;
                }

                context.moveTo(AA(this, x), y - (this.Get('chart.xaxispos') == 'center' ? 3 : 0));
                context.lineTo(AA(this, x), y + 3);
            }

        }

        context.stroke();
    }











    /**
    * Draws the labels on the scatter graph
    */
    RGraph.Scatter.prototype.DrawLabels = function ()
    {
        this.context.fillStyle = this.Get('chart.text.color');
        var font       = this.Get('chart.text.font');
        var xMin       = this.Get('chart.xmin');
        var xMax       = this.Get('chart.xmax');
        var yMax       = this.scale[4];
        var yMin       = this.Get('chart.ymin');
        var text_size  = this.Get('chart.text.size');
        var units_pre  = this.Get('chart.units.pre');
        var units_post = this.Get('chart.units.post');
        var numYLabels = this.Get('chart.ylabels.count');
        var invert     = this.Get('chart.ylabels.invert');
        var inside     = this.Get('chart.ylabels.inside');
        var context    = this.context;
        var canvas     = this.canvas;
        var boxed      = false;

        this.halfTextHeight = text_size / 2;

            
        this.halfGraphHeight = (this.canvas.height - this.gutterTop - this.gutterBottom) / 2;

        /**
        * Draw the Y yaxis labels, be it at the top or center
        */
        if (this.Get('chart.ylabels')) {

            var xPos  = this.Get('chart.yaxispos') == 'left' ? this.gutterLeft - 5 : RGraph.GetWidth(this) - this.gutterRight + 5;
            var align = this.Get('chart.yaxispos') == 'right' ? 'left' : 'right';
            
            /**
            * Now change the two things above if chart.ylabels.inside is specified
            */
            if (inside) {
                if (this.Get('chart.yaxispos') == 'left') {
                    xPos  = this.Get('chart.gutter.left') + 5;
                    align = 'left';
                    boxed = true;
                } else {
                    xPos  = this.canvas.width - this.Get('chart.gutter.right') - 5;
                    align = 'right';
                    boxed = true;
                }
            }

            if (this.Get('chart.xaxispos') == 'center') {


                /**
                * Specific Y labels
                */
                if (typeof(this.Get('chart.ylabels.specific')) == 'object' && this.Get('chart.ylabels.specific') != null && this.Get('chart.ylabels.specific').length) {

                    var labels = this.Get('chart.ylabels.specific');
                    
                    if (this.Get('chart.ymin') > 0) {
                        labels = [];
                        for (var i=0; i<(this.Get('chart.ylabels.specific').length - 1); ++i) {
                            labels.push(this.Get('chart.ylabels.specific')[i]);
                        }
                    }

                    for (var i=0; i<labels.length; ++i) {
                        var y = this.gutterTop + (i * (this.grapharea / (labels.length * 2) ) );
                        RGraph.Text(context, font, text_size, xPos, y, labels[i], 'center', align, boxed);
                    }
                    
                    var reversed_labels = RGraph.array_reverse(labels);
                
                    for (var i=0; i<reversed_labels.length; ++i) {
                        var y = this.gutterTop + (this.grapharea / 2) + ((i+1) * (this.grapharea / (labels.length * 2) ) );
                        RGraph.Text(context,font, text_size, xPos, y, reversed_labels[i], 'center', align, boxed);
                    }
                    
                    /**
                    * Draw the center label if chart.ymin is specified
                    */
                    if (this.Get('chart.ymin') > 0) {
                        RGraph.Text(context, font, text_size, xPos, (this.grapharea / 2) + this.Get('chart.gutter.top'), this.Get('chart.ylabels.specific')[this.Get('chart.ylabels.specific').length - 1], 'center', align, boxed);
                    }

                    return;
                }


                if (numYLabels == 1 || numYLabels == 3 || numYLabels == 5) {
                    // Draw the top halves labels
                    RGraph.Text(context, font, text_size, xPos, this.gutterTop, RGraph.number_format(this, this.scale[4], units_pre, units_post), 'center', align, boxed);
                    
                    
                    if (numYLabels >= 5) {
                        RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) * (1/10) ), RGraph.number_format(this, this.scale[3], units_pre, units_post), 'center', align, boxed);
                        RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) * (3/10) ), RGraph.number_format(this, this.scale[1], units_pre, units_post), 'center', align, boxed);
                    }
        
                    if (numYLabels >= 3) {
                        RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) * (2/10) ), RGraph.number_format(this, this.scale[2], units_pre, units_post), 'center', align, boxed);
                        RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) * (4/10) ), RGraph.number_format(this, this.scale[0], units_pre, units_post), 'center', align, boxed);
                    }
                    
                    // Draw the bottom halves labels
                    if (numYLabels >= 3) {
                        RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) * (1/10) ) + this.halfGraphHeight, '-' + RGraph.number_format(this, this.scale[0], units_pre, units_post), 'center', align, boxed);
                        RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) * (3/10) ) + this.halfGraphHeight, '-' + RGraph.number_format(this, this.scale[2], units_pre, units_post), 'center', align, boxed);
                    }
        
                    if (numYLabels == 5) {
                        RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) * (2/10) ) + this.halfGraphHeight, '-' + RGraph.number_format(this, this.scale[1], units_pre, units_post), 'center', align, boxed);
                        RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) * (4/10) ) + this.halfGraphHeight, '-' + RGraph.number_format(this, this.scale[3], units_pre, units_post), 'center', align, boxed);
                    }
        
                    RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (5/10) ) + this.halfGraphHeight, '-' + RGraph.number_format(this, this.scale[4], units_pre, units_post), 'center', align, boxed);
                
                } else if (numYLabels == 10) {
                    // 10 Y labels
                    var interval = (this.grapharea / numYLabels) / 2;
                
                    for (var i=0; i<numYLabels; ++i) {
                        RGraph.Text(context, font, text_size, xPos,this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (i/20) ),RGraph.number_format(this,(this.max - (this.max * (i/10))).toFixed(this.Get('chart.scale.decimals')),units_pre, units_post),'center', align, boxed);
                        RGraph.Text(context, font, text_size, xPos,this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (i/20) ) + (this.grapharea / 2) + (this.grapharea / 20),'-' + RGraph.number_format(this, ((this.max * (i/10)) + (this.max * (1/10))).toFixed((this.Get('chart.scale.decimals'))), units_pre, units_post), 'center', align, boxed);
                    }

                } else {
                    alert('[SCATTER SCALE] Number of Y labels can be 1/3/5/10 only');
                }
    
            } else {
                
                var xPos  = this.Get('chart.yaxispos') == 'left' ? this.gutterLeft - 5 : this.canvas.width - this.gutterRight + 5;
                var align = this.Get('chart.yaxispos') == 'right' ? 'left' : 'right';

                if (inside) {
                    if (this.Get('chart.yaxispos') == 'left') {
                        xPos  = this.Get('chart.gutter.left') + 5;
                        align = 'left';
                        boxed = true;
                    } else {
                        xPos  = this.canvas.width - this.Get('chart.gutter.right') - 5;
                        align = 'right';
                        boxed = true;
                    }
                }
                /**
                * Specific Y labels
                */
                if (typeof(this.Get('chart.ylabels.specific')) == 'object' && this.Get('chart.ylabels.specific')) {

                    var labels = this.Get('chart.ylabels.specific');
                    
                    // Lose the last label
                    if (this.Get('chart.ymin') > 0) {
                        labels = [];
                        for (var i=0; i<(this.Get('chart.ylabels.specific').length - 1); ++i) {
                            labels.push(this.Get('chart.ylabels.specific')[i]);
                        }
                    }

                    for (var i=0; i<labels.length; ++i) {
                        var y = this.gutterTop + (i * (this.grapharea / labels.length) );
                        
                        RGraph.Text(context, font, text_size, xPos, y, labels[i], 'center', align, boxed);
                    }

                    /**
                    * Draw the center label if chart.ymin is specified
                    */
                    if (this.Get('chart.ymin') > 0) {
                        RGraph.Text(context, font, text_size, xPos, this.canvas.height - this.Get('chart.gutter.bottom'), this.Get('chart.ylabels.specific')[this.Get('chart.ylabels.specific').length - 1], 'center', align, boxed);
                    }

                    //return;
                } else {

                    if (numYLabels == 1 || numYLabels == 3 || numYLabels == 5) {
                        if (invert) {
                            RGraph.Text(context, font, text_size, xPos, this.gutterTop, RGraph.number_format(this, 0, units_pre, units_post), 'center', align, boxed);
                            RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) * (5/5) ), RGraph.number_format(this, this.scale[4], units_pre, units_post), 'center', align, boxed);
            
                            if (numYLabels >= 5) {
                                RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (2/5) ), RGraph.number_format(this, this.scale[1], units_pre, units_post), 'center', align, boxed);
                                RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (4/5) ), RGraph.number_format(this, this.scale[3], units_pre, units_post), 'center', align, boxed);
                            }
            
                            if (numYLabels >= 3) {
                                RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (3/5) ), RGraph.number_format(this, this.scale[2], units_pre, units_post), 'center', align, boxed);
                                RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (1/5) ), RGraph.number_format(this, this.scale[0], units_pre, units_post), 'center', align, boxed);
                            }
    
            
                            if (!this.Get('chart.xaxis')) {
                                RGraph.Text(context,
                                            font,
                                            text_size,
                                            xPos,
                                            this.canvas.height - this.gutterBottom,
                                            RGraph.number_format(this, '0', units_pre, units_post),
                                            'center',
                                            align,
                                            boxed);
                            }
    
                        } else {
                            RGraph.Text(context, font, text_size, xPos, this.gutterTop, RGraph.number_format(this, this.scale[4], units_pre, units_post), 'center', align, boxed);
            
                            if (numYLabels >= 5) {
                                RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (1/5) ), RGraph.number_format(this, this.scale[3], units_pre, units_post), 'center', align, boxed);
                                RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (3/5) ), RGraph.number_format(this, this.scale[1], units_pre, units_post), 'center', align, boxed);
                            }
            
                            if (numYLabels >= 3) {
                                RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (2/5) ), RGraph.number_format(this, this.scale[2], units_pre, units_post), 'center', align, boxed);
                                RGraph.Text(context, font, text_size, xPos, this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (4/5) ), RGraph.number_format(this, this.scale[0], units_pre, units_post), 'center', align, boxed);
                            }
            
                            if (!this.Get('chart.xaxis')) {
                                RGraph.Text(context,
                                            font,
                                            text_size,
                                            xPos,
                                            this.canvas.height - this.gutterBottom,
                                            RGraph.number_format(this, '0', units_pre, units_post),
                                            'center',
                                            align,
                                            boxed);
                            }
                        }
                    } else if (numYLabels == 10) {
                        var interval = (this.grapharea / numYLabels) / 2;
                        if (invert) {
                            for (var i=numYLabels; i>=0; --i) {
                                RGraph.Text(context, font, text_size, xPos,this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * ((10-i)/10) ),RGraph.number_format(this,(this.max - (this.max * (i/10))).toFixed((this.Get('chart.scale.decimals'))), units_pre, units_post),'center', align, boxed);
                            }
                        } else {
                            // 10 Y labels
                            for (var i=0; i<numYLabels; ++i) {
    
                                RGraph.Text(context, font, text_size, xPos,this.gutterTop + ((this.canvas.height - this.gutterTop - this.gutterBottom) * (i/10) ),RGraph.number_format(this, (this.max - ((this.max - this.min) * (i/10))).toFixed((this.Get('chart.scale.decimals'))), units_pre, units_post),'center', align, boxed);
                            }
                        }
                    } else {
                        alert('[SCATTER SCALE] Number of Y labels can be 1/3/5/10 only');
                    }
                    
                    if (this.Get('chart.ymin')) {
                        RGraph.Text(context, font, text_size, xPos, this.canvas.height - this.gutterBottom,RGraph.number_format(this, this.Get('chart.ymin').toFixed(this.Get('chart.scale.decimals')), units_pre, units_post),'center', align, boxed);
                    }
                }
            }
        }




        /**
        * Draw an X scale
        */
        if (this.Get('chart.xscale')) {

            var numXLabels   = this.Get('chart.xscale.numlabels');
            var interval     = (this.canvas.width - this.gutterLeft - this.gutterRight) / numXLabels;
            var y            = this.canvas.height - this.gutterBottom + 5 + (text_size / 2);
            var units_pre_x  = this.Get('chart.xscale.units.pre');
            var units_post_x = this.Get('chart.xscale.units.post');


            if (!this.Get('chart.xmax')) {
                var xmax = 0;
                
                for (var ds=0; ds<this.data.length; ++ds) {
                    for (var point=0; point<this.data[ds].length; ++point) {
                        xmax = Math.max(xmax, this.data[ds][point][0]);
                    }
                }

                this.Set('chart.xmax', RGraph.getScale(xmax)[4]);
            }


            for (var i=0; i<numXLabels; ++i) {
            
                var num  = ( (this.Get('chart.xmax') - this.Get('chart.xmin')) * ((i+1) / numXLabels)) + this.Get('chart.xmin');
                var x    = this.gutterLeft + ((i+1) * interval);

                if (typeof(this.Get('chart.xscale.formatter')) == 'function') {
                    var text = this.Get('chart.xscale.formatter')(this, num);
                } else {
                    var text = RGraph.number_format(this,
                                                    num.toFixed(this.Get('chart.scale.decimals')),
                                                    units_pre_x,
                                                    units_post_x);
                }

                RGraph.Text(context, font, text_size, x, y, text, 'center', 'center');
            }

        /**
        * Draw X labels
        */
        } else {

            // Put the text on the X axis
            var graphArea = this.canvas.width - this.gutterLeft - this.gutterRight;
            var xInterval = graphArea / this.Get('chart.labels').length;
            var xPos      = this.gutterLeft;
            var yPos      = (this.canvas.height - this.gutterBottom) + 15;
            var labels    = this.Get('chart.labels');

            /**
            * Text angle
            */
            var angle  = 0;
            var valign = null;
            var halign = 'center';
    
            if (this.Get('chart.text.angle') > 0) {
                angle  = -1 * this.Get('chart.text.angle');
                valign = 'center';
                halign = 'right';
                yPos -= 10;
            }

            for (i=0; i<labels.length; ++i) {

                if (typeof(labels[i]) == 'object') {
                
                    if (this.Get('chart.labels.specific.align') == 'center') {
                        var rightEdge = 0;
    
                        if (labels[i+1] && labels[i+1][1]) {
                            rightEdge = labels[i+1][1];
                        } else {
                            rightEdge = this.Get('chart.xmax');
                        }
                        
                        var offset = (rightEdge - labels[i][1]) / 2;
    
                    } else {
                        var offset = 0;
                    }
                
    
                    RGraph.Text(context,
                                font,
                                this.Get('chart.text.size'),
                                this.gutterLeft + (graphArea * ((labels[i][1] - xMin + offset) / (this.Get('chart.xmax') - xMin))) + 5,
                                yPos,
                                String(labels[i][0]),
                                valign,
                                angle != 0 ? 'right' : (this.Get('chart.labels.specific.align') == 'center' ? 'center' : 'left'),
                                null,
                                angle
                               );
                    
                    /**
                    * Draw the gray indicator line
                    */
                    this.context.beginPath();
                        this.context.strokeStyle = '#bbb';
                        this.context.moveTo(AA(this, this.gutterLeft + (graphArea * ((labels[i][1] - xMin)/ (this.Get('chart.xmax') - xMin)))), RGraph.GetHeight(this) - this.gutterBottom);
                        this.context.lineTo(AA(this, this.gutterLeft + (graphArea * ((labels[i][1] - xMin)/ (this.Get('chart.xmax') - xMin)))), RGraph.GetHeight(this) - this.gutterBottom + 20);
                    this.context.stroke();
                
                } else {

                    RGraph.Text(context,
                                font,
                                this.Get('chart.text.size'),
                                xPos + (xInterval / 2),
                                yPos,
                                String(labels[i]),
                                valign,
                                halign,
                                null,
                                angle);
                }
                
                // Do this for the next time around
                xPos += xInterval;
            }
    
            /**
            * Draw the final indicator line
            */
            if (typeof(labels[0]) == 'object') {
                this.context.beginPath();
                    this.context.strokeStyle = '#bbb';
                    this.context.moveTo(this.gutterLeft + graphArea, RGraph.GetHeight(this) - this.gutterBottom);
                    this.context.lineTo(this.gutterLeft + graphArea, RGraph.GetHeight(this) - this.gutterBottom + 20);
                this.context.stroke();
            }
        }
    }














    /**
    * Draws the actual scatter graph marks
    * 
    * @param i integer The dataset index
    */
    RGraph.Scatter.prototype.DrawMarks = function (i)
    {
        /**
        *  Reset the coords array
        */
        this.coords[i] = [];

        /**
        * Plot the values
        */
        var xmax          = this.Get('chart.xmax');
        var default_color = this.Get('chart.defaultcolor');

        for (var j=0; j<this.data[i].length; ++j) {
            /**
            * This is here because tooltips are optional
            */
            var data_point = this.data[i];

            var xCoord = data_point[j][0];
            var yCoord = data_point[j][1];
            var color  = data_point[j][2] ? data_point[j][2] : default_color;
            var tooltip = (data_point[j] && data_point[j][3]) ? data_point[j][3] : null;

            
            this.DrawMark(
                          i,
                          xCoord,
                          yCoord,
                          xmax,
                          this.scale[4],
                          color,
                          tooltip,
                          this.coords[i],
                          data_point
                         );
        }
    }


    /**
    * Draws a single scatter mark
    */
    RGraph.Scatter.prototype.DrawMark = function (index, x, y, xMax, yMax, color, tooltip, coords, data)
    {
        /**
        * Inverted Y scale handling
        */
        if (this.Get('chart.ylabels.invert')) {
            if (typeof(y) == 'number') {
                y = yMax - y;
            }
        }

        var tickmarks = this.Get('chart.tickmarks');
        var tickSize  = this.Get('chart.ticksize');
        var xMin      = this.Get('chart.xmin');
        var x = ((x - xMin) / (xMax - xMin)) * (this.canvas.width - this.gutterLeft - this.gutterRight);
        var originalX = x;
        var originalY = y;
        
        
        /**
        * This allows chart.tickmarks to be an array
        */

        if (tickmarks && typeof(tickmarks) == 'object') {
            tickmarks = tickmarks[index];
        }


        /**
        * This allows chart.ticksize to be an array
        */
        if (typeof(tickSize) == 'object') {
            var tickSize     = tickSize[index];
            var halfTickSize = tickSize / 2;
        } else {
            var halfTickSize = tickSize / 2;
        }


        /**
        * This bit is for boxplots only
        */
        if (   typeof(y) == 'object'
            && typeof(y[0]) == 'number'
            && typeof(y[1]) == 'number'
            && typeof(y[2]) == 'number'
            && typeof(y[3]) == 'number'
            && typeof(y[4]) == 'number'
           ) {

            var yMin = this.Get('chart.ymin') ? this.Get('chart.ymin') : 0;
            this.Set('chart.boxplot', true);
            this.graphheight = this.canvas.height - this.gutterTop - this.gutterBottom;
            
            if (this.Get('chart.xaxispos') == 'center') {
                this.graphheight /= 2;
            }

            var y0 = (this.graphheight) - ((y[4] - yMin) / (yMax - yMin)) * (this.graphheight);
            var y1 = (this.graphheight) - ((y[3] - yMin) / (yMax - yMin)) * (this.graphheight);
            var y2 = (this.graphheight) - ((y[2] - yMin) / (yMax - yMin)) * (this.graphheight);
            var y3 = (this.graphheight) - ((y[1] - yMin) / (yMax - yMin)) * (this.graphheight);
            var y4 = (this.graphheight) - ((y[0] - yMin) / (yMax - yMin)) * (this.graphheight);
            
            /**
            * Inverted labels
            */
            if (this.Get('chart.ylabels.invert')) {
                y0 = this.graphheight - y0;
                y1 = this.graphheight - y1;
                y2 = this.graphheight - y2;
                y3 = this.graphheight - y3;
                y4 = this.graphheight - y4;
            }

            var col1  = y[5];
            var col2  = y[6];

            // Override the boxWidth
            if (typeof(y[7]) == 'number') {
                var boxWidth = y[7];
            }

            var y = this.graphheight - y2;

        } else {
            var yMin = this.Get('chart.ymin') ? this.Get('chart.ymin') : 0;
            var y = (( (y - yMin) / (yMax - yMin)) * (RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom));
        }

        /**
        * Account for the X axis being at the centre
        */
        if (this.Get('chart.xaxispos') == 'center') {
            y /= 2;
            y += this.halfGraphHeight;
        }

        // This is so that points are on the graph, and not the gutter
        x += this.gutterLeft;
        y = this.canvas.height - this.gutterBottom - y;

        this.context.beginPath();
        
        // Color
        this.context.strokeStyle = color;

        /**
        * Boxplots
        */
        if (   this.Get('chart.boxplot')
            && typeof(y0) == 'number'
            && typeof(y1) == 'number'
            && typeof(y2) == 'number'
            && typeof(y3) == 'number'
            && typeof(y4) == 'number'
           ) {

            var boxWidth = boxWidth ? boxWidth : this.Get('chart.boxplot.width');

            // boxWidth is now a scale value, so convert it to a pixel vlue
            boxWidth = (boxWidth / this.Get('chart.xmax')) * (this.canvas.width -this.gutterLeft - this.gutterRight);

            var halfBoxWidth = boxWidth / 2;

            this.context.beginPath();
                this.context.strokeRect(x - halfBoxWidth, y1 + this.gutterTop, boxWidth, y3 - y1);
    
                // Draw the upper coloured box if a value is specified
                if (col1) {
                    this.context.fillStyle = col1;
                    this.context.fillRect(x - halfBoxWidth, y1 + this.gutterTop, boxWidth, y2 - y1);
                }
    
                // Draw the lower coloured box if a value is specified
                if (col2) {
                    this.context.fillStyle = col2;
                    this.context.fillRect(x - halfBoxWidth, y2 + this.gutterTop, boxWidth, y3 - y2);
                }
            this.context.stroke();

            // Now draw the whiskers
            this.context.beginPath();
            if (this.Get('chart.boxplot.capped')) {
                this.context.moveTo(x - halfBoxWidth, AA(this, y0 + this.gutterTop));
                this.context.lineTo(x + halfBoxWidth, AA(this, y0 + this.gutterTop));
            }

            this.context.moveTo(AA(this, x), y0 + this.gutterTop);
            this.context.lineTo(AA(this, x), y1 + this.gutterTop);

            if (this.Get('chart.boxplot.capped')) {
                this.context.moveTo(x - halfBoxWidth, AA(this, y4 + this.gutterTop));
                this.context.lineTo(x + halfBoxWidth, AA(this, y4 + this.gutterTop));
            }

            this.context.moveTo(AA(this, x), y4 + this.gutterTop);
            this.context.lineTo(AA(this, x), y3 + this.gutterTop);

            this.context.stroke();
        }


        /**
        * Draw the tickmark, but not for boxplots
        */
        if (!y0 && !y1 && !y2 && !y3 && !y4) {
            
            this.graphheight = this.canvas.height - this.gutterTop - this.gutterBottom;


            
            if (tickmarks == 'circle') {
                this.context.arc(x, y, halfTickSize, 0, 6.28, 0);
                this.context.fillStyle = color;
                this.context.fill();
            
            } else if (tickmarks == 'plus') {

                this.context.moveTo(x, y - halfTickSize);
                this.context.lineTo(x, y + halfTickSize);
                this.context.moveTo(x - halfTickSize, y);
                this.context.lineTo(x + halfTickSize, y);
                this.context.stroke();
            
            } else if (tickmarks == 'square') {
                this.context.strokeStyle = color;
                this.context.fillStyle = color;
                this.context.fillRect(
                                      x - halfTickSize,
                                      y - halfTickSize,
                                      tickSize,
                                      tickSize
                                     );
                //this.context.fill();

            } else if (tickmarks == 'cross') {

                this.context.moveTo(x - halfTickSize, y - halfTickSize);
                this.context.lineTo(x + halfTickSize, y + halfTickSize);
                this.context.moveTo(x + halfTickSize, y - halfTickSize);
                this.context.lineTo(x - halfTickSize, y + halfTickSize);
                
                this.context.stroke();
            
            /**
            * Diamond shape tickmarks
            */
            } else if (tickmarks == 'diamond') {
                this.context.fillStyle = this.context.strokeStyle;

                this.context.moveTo(x, y - halfTickSize);
                this.context.lineTo(x + halfTickSize, y);
                this.context.lineTo(x, y + halfTickSize);
                this.context.lineTo(x - halfTickSize, y);
                this.context.lineTo(x, y - halfTickSize);
                
                this.context.fill();
                this.context.stroke();

            /**
            * Custom tickmark style
            */
            } else if (typeof(tickmarks) == 'function') {

                var graphWidth = RGraph.GetWidth(this) - this.gutterLeft - this.gutterRight
                var xVal = ((x - this.gutterLeft) / graphWidth) * xMax;
                var yVal = ((this.graphheight - (y - this.gutterTop)) / this.graphheight) * yMax;

                tickmarks(this, data, x, y, xVal, yVal, xMax, yMax, color)

            /**
            * No tickmarks
            */
            } else if (tickmarks == null) {
    
            /**
            * Unknown tickmark type
            */
            } else {
                alert('[SCATTER] (' + this.id + ') Unknown tickmark style: ' + tickmarks );
            }
        }

        /**
        * Add the tickmark to the coords array
        */
        if (   this.Get('chart.boxplot')
            && typeof(y0) == 'number'
            && typeof(y1) == 'number'
            && typeof(y2) == 'number'
            && typeof(y3) == 'number'
            && typeof(y4) == 'number') {

            x = [x - halfBoxWidth, x + halfBoxWidth];
            y = [y0 + this.gutterTop, y1 + this.gutterTop, y2 + this.gutterTop, y3 + this.gutterTop, y4 + this.gutterTop];

        }

        coords.push([x, y, tooltip]);
    }
    
    
    /**
    * Draws an optional line connecting the tick marks.
    * 
    * @param i The index of the dataset to use
    */
    RGraph.Scatter.prototype.DrawLine = function (i)
    {
        if (this.Get('chart.line') && this.coords[i].length >= 2) {

            this.context.lineCap     = 'round';
            this.context.lineJoin    = 'round';
            this.context.lineWidth   = this.GetLineWidth(i);// i is the index of the set of coordinates
            this.context.strokeStyle = this.Get('chart.line.colors')[i];
            
            this.context.beginPath();
                var len = this.coords[i].length;
    
                for (var j=0; j<this.coords[i].length; ++j) {
    
                    var xPos = this.coords[i][j][0];
                    var yPos = this.coords[i][j][1];
    
                    if (j == 0) {
                        this.context.moveTo(xPos, yPos);
                    } else {
                    
                        // Stepped?
                        var stepped = this.Get('chart.line.stepped');
    
                        if (   (typeof(stepped) == 'boolean' && stepped)
                            || (typeof(stepped) == 'object' && stepped[i])
                           ) {
                            this.context.lineTo(this.coords[i][j][0], this.coords[i][j - 1][1]);
                        }
    
                        this.context.lineTo(xPos, yPos);
                    }
                }
            this.context.stroke();
        }
        
        /**
        * Set the linewidth back to 1
        */
        this.context.lineWidth = 1;
    }


    /**
    * Returns the linewidth
    * 
    * @param number i The index of the "line" (/set of coordinates)
    */
    RGraph.Scatter.prototype.GetLineWidth = function (i)
    {
        var linewidth = this.Get('chart.line.linewidth');
        
        if (typeof(linewidth) == 'number') {
            return linewidth;
        
        } else if (typeof(linewidth) == 'object') {
            if (linewidth[i]) {
                return linewidth[i];
            } else {
                return linewidth[0];
            }

            alert('[SCATTER] Error! chart.linewidth should be a single number or an array of one or more numbers');
        }
    }


    /**
    * Draws vertical bars. Line chart doesn't use a horizontal scale, hence this function
    * is not common
    */
    RGraph.Scatter.prototype.DrawVBars = function ()
    {
        var canvas  = this.canvas;
        var context = this.context;
        var vbars = this.Get('chart.background.vbars');
        var graphWidth = RGraph.GetWidth(this) - this.gutterLeft - this.gutterRight;
        
        if (vbars) {
        
            var xmax = this.Get('chart.xmax');

            for (var i=0; i<vbars.length; ++i) {
                var startX = ((vbars[i][0] / xmax) * graphWidth) + this.gutterLeft;
                var width  = (vbars[i][1] / xmax) * graphWidth;

                context.beginPath();
                    context.fillStyle = vbars[i][2];
                    context.fillRect(startX, this.gutterTop, width, (RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom));
                context.fill();
            }
        }
    }





    /**
    * Draws in-graph labels.
    * 
    * @param object obj The graph object
    */
    RGraph.Scatter.prototype.DrawInGraphLabels = function (obj)
    {
        var canvas  = obj.canvas;
        var context = obj.context;
        var labels  = obj.Get('chart.labels.ingraph');
        var labels_processed = [];

        // Defaults
        var fgcolor   = 'black';
        var bgcolor   = 'white';
        var direction = 1;

        if (!labels) {
            return;
        }

        /**
        * Preprocess the labels array. Numbers are expanded
        */
        for (var i=0; i<labels.length; ++i) {
            if (typeof(labels[i]) == 'number') {
                for (var j=0; j<labels[i]; ++j) {
                    labels_processed.push(null);
                }
            } else if (typeof(labels[i]) == 'string' || typeof(labels[i]) == 'object') {
                labels_processed.push(labels[i]);
            
            } else {
                labels_processed.push('');
            }
        }

        /**
        * Turn off any shadow
        */
        RGraph.NoShadow(obj);

        if (labels_processed && labels_processed.length > 0) {

            var i=0;

            for (var set=0; set<obj.coords.length; ++set) {
                for (var point = 0; point<obj.coords[set].length; ++point) {
                    if (labels_processed[i]) {
                        var x = obj.coords[set][point][0];
                        var y = obj.coords[set][point][1];
                        var length = typeof(labels_processed[i][4]) == 'number' ? labels_processed[i][4] : 25;
                            
                        var text_x = x;
                        var text_y = y - 5 - length;

                        context.moveTo(x, y - 5);
                        context.lineTo(x, y - 5 - length);
                        
                        context.stroke();
                        context.beginPath();
                        
                        // This draws the arrow
                        context.moveTo(x, y - 5);
                        context.lineTo(x - 3, y - 10);
                        context.lineTo(x + 3, y - 10);
                        context.closePath();


                        context.beginPath();
                            
                            // Fore ground color
                            context.fillStyle = (typeof(labels_processed[i]) == 'object' && typeof(labels_processed[i][1]) == 'string') ? labels_processed[i][1] : 'black';

                            RGraph.Text(context,
                                        obj.Get('chart.text.font'),
                                        obj.Get('chart.text.size'),
                                        text_x,
                                        text_y,
                                        (typeof(labels_processed[i]) == 'object' && typeof(labels_processed[i][0]) == 'string') ? labels_processed[i][0] : labels_processed[i],
                                        'bottom',
                                        'center',
                                        true,
                                        null,
                                        (typeof(labels_processed[i]) == 'object' && typeof(labels_processed[i][2]) == 'string') ? labels_processed[i][2] : 'white');
                        context.fill();
                    }
                    
                    i++;
                }
            }
        }
    }


    /**
    * This function makes it much easier to get the (if any) point that is currently being hovered over.
    * 
    * @param object e The event object
    */
    RGraph.Scatter.prototype.getShape =
    RGraph.Scatter.prototype.getPoint = function (e)
    {
        var canvas      = e.target;
        var context     = this.context;
        var mouseXY     = RGraph.getMouseXY(e);
        var mouseX      = mouseXY[0];
        var mouseY      = mouseXY[1];
        var overHotspot = false;
        var offset      = this.Get('chart.tooltips.hotspot'); // This is how far the hotspot extends

        for (var set=0; set<this.coords.length; ++set) {

            for (var i=0; i<this.coords[set].length; ++i) {

                var x = this.coords[set][i][0];
                var y = this.coords[set][i][1];
                var tooltip = this.data[set][i][3];

                if (typeof(y) == 'number') {
                    if (mouseX <= (x + offset) &&
                        mouseX >= (x - offset) &&
                        mouseY <= (y + offset) &&
                        mouseY >= (y - offset)) {

                        var tooltip = RGraph.parseTooltipText(this.data[set][i][3], 0);

                        return {
                                0: this, 1: x, 2: y, 3: set, 4: i, 5: this.data[set][i][3],
                                'object': this, 'x': x, 'y': y, 'dataset': set, 'index': i, 'tooltip': tooltip
                               };
                    }
                } else {

                    var mark = this.data[set][i];

                    /**
                    * Determine the width
                    */
                    var width = this.Get('chart.boxplot.width');
                    
                    if (typeof(mark[1][7]) == 'number') {
                        width = mark[1][7];
                    }

                    if (   typeof(x) == 'object'
                        && mouseX > x[0]
                        && mouseX < x[1]
                        && mouseY > y[1]
                        && mouseY < y[3]
                        ) {
                        
                        var tooltip = RGraph.parseTooltipText(this.data[set][i][3], 0);

                        return {
                                0: this, 1: x[0], 2: x[1] - x[0], 3: y[1], 4: y[3] - y[1], 5: set, 6: i, 7: this.data[set][i][3],
                                'object': this, 'x': x[0], 'y': y[1], 'width': x[1] - x[0], 'height': y[3] - y[1], 'dataset': set, 'index': i, 'tooltip': tooltip
                               };
                    }
                }
            }
        }
    }


    /**
    * Draws the above line labels
    */
    RGraph.Scatter.prototype.DrawAboveLabels = function ()
    {
        var context    = this.context;
        var size       = this.Get('chart.labels.above.size');
        var font       = this.Get('chart.text.font');
        var units_pre  = this.Get('chart.units.pre');
        var units_post = this.Get('chart.units.post');


        for (var set=0; set<this.coords.length; ++set) {
            for (var point=0; point<this.coords[set].length; ++point) {
                
                var x_val = this.data[set][point][0];
                var y_val = this.data[set][point][1];
                
                
                var x_pos = this.coords[set][point][0];
                var y_pos = this.coords[set][point][1];

                RGraph.Text(context,
                            font,
                            size,
                            x_pos,
                            y_pos - 5 - size,
                            x_val.toFixed(this.Get('chart.labels.above.decimals')) + ', ' + y_val.toFixed(this.Get('chart.labels.above.decimals')),
                            'center',
                            'center',
                            true,
                            null,
                            'rgba(255, 255, 255, 0.7)');
            }
        }
    }


    /**
    * When you click on the chart, this method can return the Y value at that point. It works for any point on the
    * chart (that is inside the gutters) - not just points within the Bars.
    * 
    * @param object e The event object
    */
    RGraph.Scatter.prototype.getValue = function (arg)
    {
        if (arg.length == 2) {
            var mouseX = arg[0];
            var mouseY = arg[1];
        } else {
            var mouseCoords = RGraph.getMouseXY(arg);
            var mouseX      = mouseCoords[0];
            var mouseY      = mouseCoords[1];
        }
        var obj = this;
        
        if (   mouseY < obj.Get('chart.gutter.top')
            || mouseY > (obj.canvas.height - obj.Get('chart.gutter.bottom'))
            || mouseX < obj.Get('chart.gutter.left')
            || mouseX > (obj.canvas.width - obj.Get('chart.gutter.right'))
           ) {
            return null;
        }
        
        if (obj.Get('chart.xaxispos') == 'center') {
            var value = (((obj.grapharea / 2) - (mouseY - obj.Get('chart.gutter.top'))) / obj.grapharea) * (obj.max - obj.min)
            value *= 2;
            if (value >= 0) {
                value += obj.min
            } else {
                value -= obj.min
            }
        } else {
            var value = ((obj.grapharea - (mouseY - obj.Get('chart.gutter.top'))) / obj.grapharea) * (obj.max - obj.min)
            value += obj.min;
        }

        return value;
    }



    /**
    * Each object type has its own Highlight() function which highlights the appropriate shape
    * 
    * @param object shape The shape to highlight
    */
    RGraph.Scatter.prototype.Highlight = function (shape)
    {
        // Boxplot highlight
        if (shape['height']) {
            RGraph.Highlight.Rect(this, shape);

        // Point highlight
        } else {
            RGraph.Highlight.Point(this, shape);
        }
    }



    /**
    * The getObjectByXY() worker method. Don't call this call:
    * 
    * RGraph.ObjectRegistry.getObjectByXY(e)
    * 
    * @param object e The event object
    */
    RGraph.Scatter.prototype.getObjectByXY = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e);

        if (
               mouseXY[0] > (this.Get('chart.gutter.left') - 3)
            && mouseXY[0] < (this.canvas.width - this.Get('chart.gutter.right') + 3)
            && mouseXY[1] > (this.Get('chart.gutter.top') - 3)
            && mouseXY[1] < ((this.canvas.height - this.Get('chart.gutter.bottom')) + 3)
            ) {

            return this;
        }
    }


    /**
    * This function can be used when the canvas is clicked on (or similar - depending on the event)
    * to retrieve the relevant Y coordinate for a particular value.
    * 
    * @param int value The value to get the Y coordinate for
    */
    RGraph.Scatter.prototype.getYCoord = function (value)
    {
        if (typeof(value) != 'number') {
            return null;
        }

        var y;
        var xaxispos = this.Get('chart.xaxispos');

        // Higher than max
        if (value > this.max) {
            value = this.max;
        }

        if (xaxispos == 'center') {

            if (value > 0) {
                y = ((value - this.min) / (this.max - this.min)) * (this.grapharea / 2);
            } else {
                y = ((value + this.min) / (this.max - this.min)) * (this.grapharea / 2);
            }
            y = (this.grapharea / 2) - y;

            // Inverted Y labels
            if (this.Get('chart.ylabels.invert')) {
                y = this.grapharea - y;
            }

            y += this.gutterTop;

        } else {

            if (value < this.min) value = this.min;
            if (value > this.max) value = this.max;

            y = ((value - this.min) / (this.max - this.min)) * this.grapharea;

            // Inverted Y labels
            if (this.Get('chart.ylabels.invert')) {
                y = this.grapharea - y;
            }

            y = (this.grapharea - y) + this.gutterTop;
        }
        
        return y;
    }


    /**
    * This function can be used when the canvas is clicked on (or similar - depending on the event)
    * to retrieve the relevant X coordinate for a particular value.
    * 
    * @param int value The value to get the X coordinate for
    */
    RGraph.Scatter.prototype.getXCoord = function (value)
    {
        if (typeof(value) != 'number') {
            return null;
        }
        
        var xmin = this.Get('chart.xmin');
        var xmax = this.Get('chart.xmax');
        var x;

        // Higher than max
        if (value > xmax){
            value = xmax;
        }


        if (value < xmin) value = xmin;
        if (value > xmax) value = xmax;

        if (this.Get('chart.yaxispos') == 'right') {
            x = ((value - xmin) / (xmax - xmin)) * (this.canvas.width - this.Get('chart.gutter.left') - this.Get('chart.gutter.right'));
            x = (this.canvas.width - this.Get('chart.gutter.right') - x);
        } else {
            x = ((value - xmin) / (xmax - xmin)) * (this.canvas.width - this.Get('chart.gutter.left') - this.Get('chart.gutter.right'));
            x = x + this.Get('chart.gutter.left');
        }
        
        return x;
    }



    /**
    * This function positions a tooltip when it is displayed
    * 
    * @param obj object    The chart object
    * @param int x         The X coordinate specified for the tooltip
    * @param int y         The Y coordinate specified for the tooltip
    * @param objec tooltip The tooltips DIV element
    */
    RGraph.Scatter.prototype.positionTooltip = function (obj, x, y, tooltip, idx)
    {
        var shape      = RGraph.Registry.Get('chart.tooltip.shape');
        var dataset    = shape['dataset'];
        var index      = shape['index'];
        var coordX     = obj.coords[dataset][index][0]
        var coordY     = obj.coords[dataset][index][1]
        var canvasXY   = RGraph.getCanvasXY(obj.canvas);
        var gutterLeft = obj.Get('chart.gutter.left');
        var gutterTop  = obj.Get('chart.gutter.top');
        var width      = tooltip.offsetWidth;
        var height     = tooltip.offsetHeight;
        tooltip.style.left = 0;
        tooltip.style.top  = 0;

        // Is the coord a boxplot
        var isBoxplot = typeof(coordY) == 'object' ? true : false;

        // Show any overflow (ie the arrow)
        tooltip.style.overflow = '';

        // Create the arrow
        var img = new Image();
            img.src = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAAFCAYAAACjKgd3AAAARUlEQVQYV2NkQAN79+797+RkhC4M5+/bd47B2dmZEVkBCgcmgcsgbAaA9GA1BCSBbhAuA/AagmwQPgMIGgIzCD0M0AMMAEFVIAa6UQgcAAAAAElFTkSuQmCC';
            img.style.position = 'absolute';
            img.id = '__rgraph_tooltip_pointer__';
            img.style.top = (tooltip.offsetHeight - 2) + 'px';
        tooltip.appendChild(img);
        
        // Reposition the tooltip if at the edges:
        
        // LEFT edge //////////////////////////////////////////////////////////////////

        if ((canvasXY[0] + (coordX[0] || coordX) - (width / 2)) < 10) {
            
            if (isBoxplot) {
                tooltip.style.left = canvasXY[0] + coordX[0] + ((coordX[1] - coordX[0]) / 2) - (width * 0.1) + 'px';
                tooltip.style.top  = canvasXY[1] + coordY[0] - height - 5 + 'px';
                img.style.left = ((width * 0.1) - 8.5) + 'px';

            } else {
                tooltip.style.left = (canvasXY[0] + coordX - (width * 0.1)) + 'px';
                tooltip.style.top  = canvasXY[1] + coordY - height - 9 + 'px';
                img.style.left = ((width * 0.1) - 8.5) + 'px';
            }

// RIGHT edge //////////////////////////////////////////////////////////////////

} else if ((canvasXY[0] + (coordX[0] || coordX) + (width / 2)) > document.body.offsetWidth) {
    if (isBoxplot) {
        tooltip.style.left = canvasXY[0] + coordX[0] + ((coordX[1] - coordX[0]) / 2) - (width * 0.9) + 'px';
        tooltip.style.top  = canvasXY[1] + coordY[0] - height - 5 + 'px';
        img.style.left = ((width * 0.9) - 8.5) + 'px';

    } else {
        tooltip.style.left = (canvasXY[0] + coordX - (width * 0.9)) + 'px';
        tooltip.style.top  = canvasXY[1] + coordY - height - 9 + 'px';
        img.style.left = ((width * 0.9) - 8.5) + 'px';
    }

        // Default positioning - CENTERED //////////////////////////////////////////////////////////////////

        } else {
            if (isBoxplot) {
                tooltip.style.left = canvasXY[0] + coordX[0] + ((coordX[1] - coordX[0]) / 2) - (width / 2) + 'px';
                tooltip.style.top  = canvasXY[1] + coordY[0] - height - 5 + 'px';
                img.style.left = ((width * 0.5) - 8.5) + 'px';

            } else {
                tooltip.style.left = (canvasXY[0] + coordX - (width * 0.5)) + 'px';
                tooltip.style.top  = canvasXY[1] + coordY - height - 9 + 'px';
                img.style.left = ((width * 0.5) - 8.5) + 'px';
            }
        }
    }