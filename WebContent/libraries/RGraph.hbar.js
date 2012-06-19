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
    * The horizontal bar chart constructor. The horizontal bar is a minor variant
    * on the bar chart. If you have big labels, this may be useful as there is usually
    * more space available for them.
    * 
    * @param object canvas The canvas object
    * @param array  data   The chart data
    */
    RGraph.HBar = function (id, data)
    {
        // Get the canvas and context objects
        this.id                = id;
        this.canvas            = document.getElementById(id);
        this.context           = this.canvas.getContext ? this.canvas.getContext("2d") : null;
        this.canvas.__object__ = this;
        this.data              = data;
        this.type              = 'hbar';
        this.coords            = [];
        this.isRGraph          = true;
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();


        /**
        * Compatibility with older browsers
        */
        RGraph.OldBrowserCompat(this.context);

        
        this.max = 0;
        this.stackedOrGrouped  = false;

        // Default properties
        this.properties = {
            'chart.gutter.left':            75,
            'chart.gutter.right':           25,
            'chart.gutter.top':             35,
            'chart.gutter.bottom':          25,
            'chart.background.grid':        true,
            'chart.background.grid.color':  '#ddd',
            'chart.background.grid.width':  1,
            'chart.background.grid.hsize':  25,
            'chart.background.grid.vsize':  25,
            'chart.background.barcolor1':   'rgba(0,0,0,0)',
            'chart.background.barcolor2':   'rgba(0,0,0,0)',
            'chart.background.grid.hlines': true,
            'chart.background.grid.vlines': true,
            'chart.background.grid.border': true,
            'chart.background.grid.autofit':true,
            'chart.background.grid.autofit.numhlines': 14,
            'chart.background.grid.autofit.numvlines': 20,
            'chart.title':                  '',
            'chart.title.background':       null,
            'chart.title.xaxis':            '',
            'chart.title.xaxis.bold':       true,
            'chart.title.xaxis.size':       null,
            'chart.title.xaxis.font':       null,
            'chart.title.yaxis':            '',
            'chart.title.yaxis.bold':       true,
            'chart.title.yaxis.size':       null,
            'chart.title.yaxis.font':       null,
            'chart.title.xaxis.pos':        null,
            'chart.title.yaxis.pos':        10,
            'chart.title.hpos':             null,
            'chart.title.vpos':             null,
            'chart.title.bold':             true,
            'chart.title.font':             null,
            'chart.text.size':              10,
            'chart.text.color':             'black',
            'chart.text.font':              'Arial',
            'chart.colors':                 ['red', 'blue', 'green', 'pink', 'yellow', 'cyan', 'navy', 'gray', 'black'],
            'chart.colors.sequential':      false,
            'chart.labels':                 [],
            'chart.labels.above':           false,
            'chart.labels.above.decimals':  0,
            'chart.xlabels':                true,
            'chart.contextmenu':            null,
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
            'chart.units.pre':              '',
            'chart.units.post':             '',
            'chart.units.ingraph':          false,
            'chart.strokestyle':            'rgba(0,0,0,0)',
            'chart.xmin':                   0,
            'chart.xmax':                   0,
            'chart.axis.color':             'black',
            'chart.shadow':                 false,
            'chart.shadow.color':           '#666',
            'chart.shadow.blur':            3,
            'chart.shadow.offsetx':         3,
            'chart.shadow.offsety':         3,
            'chart.vmargin':                3,
            'chart.grouping':               'grouped',
            'chart.tooltips':               null,
            'chart.tooltips.event':         'onclick',
            'chart.tooltips.effect':        'fade',
            'chart.tooltips.css.class':     'RGraph_tooltip',
            'chart.tooltips.highlight':     true,
            'chart.highlight.fill':         'rgba(255,255,255,0.7)',
            'chart.highlight.stroke':       'rgba(0,0,0,0)',
            'chart.annotatable':            false,
            'chart.annotate.color':         'black',
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
            'chart.resizable':              false,
            'chart.resize.handle.adjust':   [0,0],
            'chart.resize.handle.background': null,
            'chart.scale.point':            '.',
            'chart.scale.thousand':         ',',
            'chart.scale.decimals':         null,
            'chart.noredraw':               false,
            'chart.events.click':           null,
            'chart.events.mousemove':       null,
            'chart.noxaxis':                false,
            'chart.noyaxis':                false,
            'chart.noxtickmarks':           false,
            'chart.noytickmarks':           false
        }

        // Check for support
        if (!this.canvas) {
            alert('[HBAR] No canvas support');
            return;
        }

        for (i=0; i<this.data.length; ++i) {
            if (typeof(this.data[i]) == 'object') {
                this.stackedOrGrouped = true;
            }
        }



        /**
        * Create the linear data array
        */
        this.data_arr = RGraph.array_linearize(this.data);
        
        
        
        /**
        * Charts are now always registered
        */
        RGraph.Register(this);
    }


    /**
    * A setter
    * 
    * @param name  string The name of the property to set
    * @param value mixed  The value of the property
    */
    RGraph.HBar.prototype.Set = function (name, value)
    {
        if (name == 'chart.labels.abovebar') {
            name = 'chart.labels.above';
        }

        this.properties[name.toLowerCase()] = value;
    }


    /**
    * A getter
    * 
    * @param name  string The name of the property to get
    */
    RGraph.HBar.prototype.Get = function (name)
    {
        if (name == 'chart.labels.abovebar') {
            name = 'chart.labels.above';
        }

        return this.properties[name];
    }


    /**
    * The function you call to draw the bar chart
    */
    RGraph.HBar.prototype.Draw = function ()
    {
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

        /**
        * Stop the coords array from growing uncontrollably
        */
        this.coords = [];
        this.max    = 0;

        /**
        * Check for chart.xmin in stacked charts
        */
        if (this.Get('chart.xmin') > 0 && this.Get('chart.grouping') == 'stacked') {
            alert('[HBAR] Using chart.xmin is not supported with stacked charts, resetting chart.xmin to zero');
            this.Set('chart.xmin', 0);
        }

        /**
        * Work out a few things. They need to be here because they depend on things you can change before you
        * call Draw() but after you instantiate the object
        */
        this.graphwidth     = this.canvas.width - this.gutterLeft - this.gutterRight;
        this.graphheight    = this.canvas.height - this.gutterTop - this.gutterBottom;
        this.halfgrapharea  = this.grapharea / 2;
        this.halfTextHeight = this.Get('chart.text.size') / 2;


        // Progressively Draw the chart
        RGraph.background.Draw(this);

        this.Drawbars();
        this.DrawAxes();
        this.DrawLabels();


        // Draw the key if necessary
        if (this.Get('chart.key') && this.Get('chart.key').length) {
            RGraph.DrawKey(this, this.Get('chart.key'), this.Get('chart.colors'));
        }



        /**
        * Setup the context menu if required
        */
        if (this.Get('chart.contextmenu')) {
            RGraph.ShowContext(this);
        }


        /**
        * Draw "in graph" labels
        */
        RGraph.DrawInGraphLabels(this);

        
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
    * This draws the axes
    */
    RGraph.HBar.prototype.DrawAxes = function ()
    {
        var halfway = (this.graphwidth / 2) + this.gutterLeft;

        this.context.beginPath();
        this.context.lineWidth   = 1;
        this.context.strokeStyle = this.Get('chart.axis.color');

        // Draw the Y axis
        if (this.Get('chart.noyaxis') == false) {
            if (this.Get('chart.yaxispos') == 'center') {
                this.context.moveTo(AA(this, halfway), this.gutterTop);
                this.context.lineTo(AA(this, halfway), this.canvas.height - this.gutterBottom);
            } else {
                this.context.moveTo(AA(this, this.gutterLeft), this.gutterTop);
                this.context.lineTo(AA(this, this.gutterLeft), this.canvas.height - this.gutterBottom);
            }
        }

        // Draw the X axis
        if (this.Get('chart.noxaxis') == false) {
            this.context.moveTo(this.gutterLeft, AA(this, RGraph.GetHeight(this) - this.gutterBottom));
            this.context.lineTo(this.canvas.width - this.gutterRight, AA(this, this.canvas.height - this.gutterBottom));
        }

        // Draw the Y tickmarks
        if (this.Get('chart.noytickmarks') == false && this.Get('chart.noyaxis') == false) {
            
            var yTickGap = (this.canvas.height - this.gutterTop - this.gutterBottom) / this.data.length;
    
            for (y=this.gutterTop; y<(this.canvas.height - this.gutterBottom - 1); y+=yTickGap) {
                if (this.Get('chart.yaxispos') == 'center') {
                    this.context.moveTo(halfway + 3, AA(this, y));
                    this.context.lineTo(halfway  - 3, AA(this, y));
                } else {
                    this.context.moveTo(this.gutterLeft, AA(this, y));
                    this.context.lineTo( this.gutterLeft  - 3, AA(this, y));
                }
            }
            
            // If the X axis isn't being shown draw the end tick
            if (this.Get('chart.noxaxis') == true) {
                if (this.Get('chart.yaxispos') == 'center') {
                    this.context.moveTo(halfway + 3, AA(this, y));
                    this.context.lineTo(halfway  - 3, AA(this, y));
                } else {
                    this.context.moveTo(this.gutterLeft, AA(this, y));
                    this.context.lineTo( this.gutterLeft  - 3, AA(this, y));
                }
            }
        }


        // Draw the X tickmarks
        if (this.Get('chart.noxtickmarks') == false && this.Get('chart.noxaxis') == false) {
            xTickGap = (this.canvas.width - this.gutterLeft - this.gutterRight ) / 10;
            yStart   = this.canvas.height - this.gutterBottom;
            yEnd     = (this.canvas.height - this.gutterBottom) + 3;
    
            for (x=(this.canvas.width - this.gutterRight), i=0; this.Get('chart.yaxispos') == 'center' ? x>=this.gutterLeft : x>this.gutterLeft; x-=xTickGap) {
    
                if (this.Get('chart.yaxispos') != 'center' || i != 5) {
                    this.context.moveTo(AA(this, x), yStart);
                    this.context.lineTo(AA(this, x), yEnd);
                }
                i++;
            }
        }

        this.context.stroke();
    }


    /**
    * This draws the labels for the graph
    */
    RGraph.HBar.prototype.DrawLabels = function ()
    {
        var context    = this.context;
        var canvas     = this.canvas;
        var units_pre  = this.Get('chart.units.pre');
        var units_post = this.Get('chart.units.post');
        var text_size  = this.Get('chart.text.size');
        var font       = this.Get('chart.text.font');


        /**
        * Set the units to blank if they're to be used for ingraph labels only
        */
        if (this.Get('chart.units.ingraph')) {
            units_pre  = '';
            units_post = '';
        }


        /**
        * Draw the X axis labels
        */
        if (this.Get('chart.xlabels')) {

            var gap = 5;

            this.context.beginPath();
            this.context.fillStyle = this.Get('chart.text.color');

            if (this.Get('chart.yaxispos') == 'center') {
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (10/10)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, RGraph.number_format(this, Number(this.scale[4]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (9/10)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, RGraph.number_format(this, Number(this.scale[3]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (8/10)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, RGraph.number_format(this, Number(this.scale[2]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (7/10)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, RGraph.number_format(this, Number(this.scale[1]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (6/10)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, RGraph.number_format(this, Number(this.scale[0]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');

                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (4/10)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, '-' + RGraph.number_format(this, Number(this.scale[0]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (3/10)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, '-' + RGraph.number_format(this, Number(this.scale[1]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (2/10)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, '-' + RGraph.number_format(this, Number(this.scale[2]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (1/10)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, '-' + RGraph.number_format(this, Number(this.scale[3]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (0)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, '-' + RGraph.number_format(this, Number(this.scale[4]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
    
            } else {

                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (5/5)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, RGraph.number_format(this, Number(this.scale[4]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (4/5)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, RGraph.number_format(this, Number(this.scale[3]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (3/5)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, RGraph.number_format(this, Number(this.scale[2]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (2/5)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, RGraph.number_format(this, Number(this.scale[1]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                RGraph.Text(context, font, text_size, this.gutterLeft + (this.graphwidth * (1/5)), this.gutterTop + this.halfTextHeight + this.graphheight + gap, RGraph.number_format(this, Number(this.scale[0]).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post), 'center', 'center');
                
                if (this.Get('chart.xmin') > 0) {
                    RGraph.Text(context,font,text_size,this.gutterLeft,this.gutterTop + this.halfTextHeight + this.graphheight + 2,RGraph.number_format(this, this.Get('chart.xmin'), units_pre, units_post),'center','center');
                }
            }
            
            this.context.fill();
            this.context.stroke();
        }

        /**
        * The Y axis labels
        */
        if (typeof(this.Get('chart.labels')) == 'object') {
        
            var xOffset = 5;
            var font    = this.Get('chart.text.font');

            // Draw the X axis labels
            this.context.fillStyle = this.Get('chart.text.color');
            
            // How wide is each bar
            var barHeight = (RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom ) / this.Get('chart.labels').length;
            
            // Reset the xTickGap
            yTickGap = (RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) / this.Get('chart.labels').length

            // Draw the X tickmarks
            var i=0;
            for (y=this.gutterTop + (yTickGap / 2); y<=RGraph.GetHeight(this) - this.gutterBottom; y+=yTickGap) {
                RGraph.Text(this.context,
                            font,
                            this.Get('chart.text.size'),
                            this.gutterLeft - xOffset,
                            y,
                            String(this.Get('chart.labels')[i++]),
                            'center',
                            'right');
            }
        }
    }
    
    
    /**
    * This function draws the actual bars
    */
    RGraph.HBar.prototype.Drawbars = function ()
    {
        this.context.lineWidth   = 1;
        this.context.strokeStyle = this.Get('chart.strokestyle');
        this.context.fillStyle   = this.Get('chart.colors')[0];
        var prevX                = 0;
        var prevY                = 0;

        /**
        * Work out the max value
        */
        if (this.Get('chart.xmax')) {
            this.scale = [
                          (((this.Get('chart.xmax') - this.Get('chart.xmin')) * 0.2) + this.Get('chart.xmin')).toFixed(this.Get('chart.scale.decimals')),
                          (((this.Get('chart.xmax') - this.Get('chart.xmin')) * 0.4) + this.Get('chart.xmin')).toFixed(this.Get('chart.scale.decimals')),
                          (((this.Get('chart.xmax') - this.Get('chart.xmin')) * 0.6) + this.Get('chart.xmin')).toFixed(this.Get('chart.scale.decimals')),
                          (((this.Get('chart.xmax') - this.Get('chart.xmin')) * 0.8) + this.Get('chart.xmin')).toFixed(this.Get('chart.scale.decimals')),
                          (((this.Get('chart.xmax') - this.Get('chart.xmin')) + this.Get('chart.xmin'))).toFixed(this.Get('chart.scale.decimals'))
                         ];
            this.max = this.scale[4];
        
        } else {
        
            var grouping = this.Get('chart.grouping');

            for (i=0; i<this.data.length; ++i) {
                if (typeof(this.data[i]) == 'object') {
                    var value = grouping == 'grouped' ? Number(RGraph.array_max(this.data[i], true)) : Number(RGraph.array_sum(this.data[i])) ;
                } else {
                    var value = Number(Math.abs(this.data[i]));
                }

                this.max = Math.max(Math.abs(this.max), Math.abs(value));
            }

            this.scale = RGraph.getScale(this.max, this);

            /**
            * Account for chart.xmin
            */
            if (this.Get('chart.xmin') > 0) {
                this.scale[0] = Number((((this.scale[4] - this.Get('chart.xmin')) * 0.2) + this.Get('chart.xmin')).toFixed(this.Get('chart.scale.decimals')));
                this.scale[1] = Number((((this.scale[4] - this.Get('chart.xmin')) * 0.4) + this.Get('chart.xmin')).toFixed(this.Get('chart.scale.decimals')));
                this.scale[2] = Number((((this.scale[4] - this.Get('chart.xmin')) * 0.6) + this.Get('chart.xmin')).toFixed(this.Get('chart.scale.decimals')));
                this.scale[3] = Number((((this.scale[4] - this.Get('chart.xmin')) * 0.8) + this.Get('chart.xmin')).toFixed(this.Get('chart.scale.decimals')));
                this.scale[4] = Number((((this.scale[4] - this.Get('chart.xmin')) * 1.0) + this.Get('chart.xmin')).toFixed(this.Get('chart.scale.decimals')));
            }

            this.max = this.scale[4];
        }

        if (this.Get('chart.scale.decimals') == null && Number(this.max) == 1) {
            this.Set('chart.scale.decimals', 1);
        }
        
        /*******************************************************
        * This is here to facilitate sequential colors
        *******************************************************/
        var colorIdx = 0;

        /**
        * The bars are drawn HERE
        */
        var graphwidth = (this.canvas.width - this.gutterLeft - this.gutterRight);
        var halfwidth  = graphwidth / 2;

        for (i=0; i<this.data.length; ++i) {

            // Work out the width and height
            var width  = (this.data[i] / this.max) *  graphwidth;
            var height = this.graphheight / this.data.length;

            var orig_height = height;

            var x       = this.gutterLeft;
            var y       = this.gutterTop + (i * height);
            var vmargin = this.Get('chart.vmargin');

            // Account for negative lengths - Some browsers (eg Chrome) don't like a negative value
            if (width < 0) {
                x -= width;
                width = Math.abs(width);
            }

            /**
            * Turn on the shadow if need be
            */
            if (this.Get('chart.shadow')) {
                this.context.shadowColor   = this.Get('chart.shadow.color');
                this.context.shadowBlur    = this.Get('chart.shadow.blur');
                this.context.shadowOffsetX = this.Get('chart.shadow.offsetx');
                this.context.shadowOffsetY = this.Get('chart.shadow.offsety');
            }

            /**
            * Draw the bar
            */
            this.context.beginPath();
                if (typeof(this.data[i]) == 'number') {

                    var barHeight = height - (2 * vmargin);
                    var barWidth  = ((this.data[i] - this.Get('chart.xmin')) / (this.max - this.Get('chart.xmin'))) * this.graphwidth;
                    var barX      = this.gutterLeft;

                    // Account for Y axis pos
                    if (this.Get('chart.yaxispos') == 'center') {
                        barWidth /= 2;
                        barX += halfwidth;
                        
                        if (this.data[i] < 0) {
                            barWidth = (Math.abs(this.data[i]) - this.Get('chart.xmin')) / (this.max - this.Get('chart.xmin'));
                            barWidth = barWidth * (this.graphwidth / 2);
                            barX = ((this.graphwidth / 2) + this.gutterLeft) - barWidth;
                        }
                    }

                    // Set the fill color
                    this.context.strokeStyle = this.Get('chart.strokestyle');
                    this.context.fillStyle = this.Get('chart.colors')[0];
                    
                    // Sequential colors
                    if (this.Get('chart.colors.sequential')) {
                        this.context.fillStyle = this.Get('chart.colors')[colorIdx++];
                    }

                    this.context.strokeRect(barX, this.gutterTop + (i * height) + this.Get('chart.vmargin'), barWidth, barHeight);
                    this.context.fillRect(barX, this.gutterTop + (i * height) + this.Get('chart.vmargin'), barWidth, barHeight);

                    this.coords.push([barX,
                                      y + vmargin,
                                      barWidth,
                                      height - (2 * vmargin),
                                      this.context.fillStyle,
                                      this.data[i],
                                      true]);

                /**
                * Stacked bar chart
                */
                } else if (typeof(this.data[i]) == 'object' && this.Get('chart.grouping') == 'stacked') {

                    if (this.Get('chart.yaxispos') == 'center') {
                        alert('[HBAR] You can\'t have a stacked chart with the Y axis in the center, change it to grouped');
                    }

                    var barHeight = height - (2 * vmargin);

                    for (j=0; j<this.data[i].length; ++j) {
                    

                        // Set the fill/stroke colors
                        this.context.strokeStyle = this.Get('chart.strokestyle');
                        this.context.fillStyle = this.Get('chart.colors')[j];
                        

                        // Sequential colors
                        if (this.Get('chart.colors.sequential')) {
                            this.context.fillStyle = this.Get('chart.colors')[colorIdx++];
                        }
                        

                        var width = (((this.data[i][j]) / (this.max))) * this.graphwidth;
                        var totalWidth = (RGraph.array_sum(this.data[i]) / this.max) * this.graphwidth;

                        this.context.strokeRect(x, this.gutterTop + this.Get('chart.vmargin') + (this.graphheight / this.data.length) * i, width, height - (2 * vmargin) );
                        this.context.fillRect(x, this.gutterTop + this.Get('chart.vmargin') + (this.graphheight / this.data.length) * i, width, height - (2 * vmargin) );

                        /**
                        * Store the coords for tooltips
                        */

                        // The last property of this array is a boolean which tells you whether the value is the last or not
                        this.coords.push([x,
                                          y + vmargin,
                                          width,
                                          height - (2 * vmargin),
                                          this.context.fillStyle,
                                          RGraph.array_sum(this.data[i]),
                                          j == (this.data[i].length - 1)
                                         ]);

                        x += width;
                    }

                /**
                * A grouped bar chart
                */
                } else if (typeof(this.data[i]) == 'object' && this.Get('chart.grouping') == 'grouped') {

                    for (j=0; j<this.data[i].length; ++j) {

                        /**
                        * Turn on the shadow if need be
                        */
                        if (this.Get('chart.shadow')) {
                            RGraph.SetShadow(this, this.Get('chart.shadow.color'), this.Get('chart.shadow.offsetx'), this.Get('chart.shadow.offsety'), this.Get('chart.shadow.blur'));
                        }

                        // Set the fill/stroke colors
                        this.context.strokeStyle = this.Get('chart.strokestyle');
                        this.context.fillStyle = this.Get('chart.colors')[j];
                        

                        // Sequential colors
                        if (this.Get('chart.colors.sequential')) {
                            this.context.fillStyle = this.Get('chart.colors')[colorIdx++];
                        }

                        var width = ((this.data[i][j] - this.Get('chart.xmin')) / (this.max - this.Get('chart.xmin'))) * (RGraph.GetWidth(this) - this.gutterLeft - this.gutterRight );
                        var individualBarHeight = (height - (2 * vmargin)) / this.data[i].length;

                        var startX = this.gutterLeft;
                        var startY = y + vmargin + (j * individualBarHeight);

                        // Account for the Y axis being in the middle
                        if (this.Get('chart.yaxispos') == 'center') {
                            width  /= 2;
                            startX += halfwidth;
                        }
                        
                        if (width < 0) {
                            startX += width;
                            width *= -1;
                        }

                        this.context.strokeRect(startX, startY, width, individualBarHeight);
                        this.context.fillRect(startX, startY, width, individualBarHeight);

                        this.coords.push([startX,
                                          startY,
                                          width,
                                          individualBarHeight,
                                          this.context.fillStyle,
                                          this.data[i][j],
                                          true]);
                    }
                }

            this.context.closePath();
        }

        this.context.fill();
        this.context.stroke();



        /**
        * Now the bars are stroke()ed, turn off the shadow
        */
        RGraph.NoShadow(this);
        
        this.RedrawBars();
    }
    
    
    /**
    * This function goes over the bars after they been drawn, so that upwards shadows are underneath the bars
    */
    RGraph.HBar.prototype.RedrawBars = function ()
    {
        if (this.Get('chart.noredraw')) {
            return;
        }

        var coords = this.coords;

        var font   = this.Get('chart.text.font');
        var size   = this.Get('chart.text.size');
        var color  = this.Get('chart.text.color');

        RGraph.NoShadow(this);
        this.context.strokeStyle = this.Get('chart.strokestyle');

        for (var i=0; i<coords.length; ++i) {

            if (this.Get('chart.shadow')) {
                this.context.beginPath();
                    this.context.strokeStyle = this.Get('chart.strokestyle');
                    this.context.fillStyle = coords[i][4];
                    this.context.lineWidth = 1;
                    this.context.strokeRect(coords[i][0], coords[i][1], coords[i][2], coords[i][3]);
                    this.context.fillRect(coords[i][0], coords[i][1], coords[i][2], coords[i][3]);
                this.context.fill();
                this.context.stroke();
            }

            /**
            * Draw labels "above" the bar
            */
            if (this.Get('chart.labels.above') && coords[i][6]) {

                this.context.fillStyle   = color;
                this.context.strokeStyle = 'black';
                RGraph.NoShadow(this);

                var border = (coords[i][0] + coords[i][2] + 7 + this.context.measureText(this.Get('chart.units.pre') + this.coords[i][5] + this.Get('chart.units.post')).width) > this.canvas.width ? true : false;

                RGraph.Text(this.context,
                            font,
                            size,
                            coords[i][0] + coords[i][2] + 5,
                            coords[i][1] + (coords[i][3] / 2),
                            RGraph.number_format(this, (this.coords[i][5]).toFixed(this.Get('chart.labels.above.decimals')), this.Get('chart.units.pre'), this.Get('chart.units.post')),
                            'center',
                            'left');
            }
        }
    }
    
    
    /*******************************************************
    * This function can be used to get the appropriate bar information (if any)
    * 
    * @param  e Event object
    * @return   Appriate bar information (if any)
    *******************************************************/
    RGraph.HBar.prototype.getShape =
    RGraph.HBar.prototype.getBar = function (e)
    {
        var canvas      = this.canvas;
        var context     = this.context;
        var mouseCoords = RGraph.getMouseXY(e);

        /**
        * Loop through the bars determining if the mouse is over a bar
        */
        for (var i=0,len=this.coords.length; i<len; i++) {

            var mouseX = mouseCoords[0];  // In relation to the canvas
            var mouseY = mouseCoords[1];  // In relation to the canvas
            var left   = this.coords[i][0];
            var top    = this.coords[i][1];
            var width  = this.coords[i][2];
            var height = this.coords[i][3];
            var idx    = i;

            if (mouseX >= left && mouseX <= (left + width) && mouseY >= top && mouseY <= (top + height) ) {

                var tooltip = RGraph.parseTooltipText(this.Get('chart.tooltips'), i);

                return {
                        0: this,   'object': this,
                        1: left,   'x': left,
                        2: top,    'y': top,
                        3: width,  'width': width,
                        4: height, 'height': height,
                        5: idx,    'index': idx,
                                   'tooltip': tooltip
                       };
            }
        }
    }


    /**
    * When you click on the chart, this method can return the X value at that point. It works for any point on the
    * chart (that is inside the gutters) - not just points within the Bars.
    * 
    * @param object e The event object
    */
    RGraph.HBar.prototype.getValue = function (arg)
    {
        if (arg.length == 2) {
            var mouseX = arg[0];
            var mouseY = arg[1];
        } else {
            var mouseCoords = RGraph.getMouseXY(arg);
            var mouseX      = mouseCoords[0];
            var mouseY      = mouseCoords[1];
        }
        
        if (   mouseY < this.Get('chart.gutter.top')
            || mouseY > (this.canvas.height - this.Get('chart.gutter.bottom'))
            || mouseX < this.Get('chart.gutter.left')
            || mouseX > (this.canvas.width - this.Get('chart.gutter.right'))
           ) {
            return null;
        }
        
        if (this.Get('chart.yaxispos') == 'center') {
            var value = ((mouseX - this.Get('chart.gutter.left')) / (this.graphwidth / 2)) * (this.max - this.Get('chart.xmin'));
                value = value - this.max
                
                // Special case if xmin is defined
                if (this.Get('chart.xmin') > 0) {
                    value = ((mouseX - this.Get('chart.gutter.left') - (this.graphwidth / 2)) / (this.graphwidth / 2)) * (this.max - this.Get('chart.xmin'));
                    value += this.Get('chart.xmin');
                    
                    if (mouseX < (this.gutterLeft + (this.graphwidth / 2))) {
                        value -= (2 * this.Get('chart.xmin'));
                    }
                }
        } else {
            var value = ((mouseX - this.Get('chart.gutter.left')) / this.graphwidth) * (this.max - this.Get('chart.xmin'));
                value += this.Get('chart.xmin');
        }

        return value;
    }



    /**
    * Each object type has its own Highlight() function which highlights the appropriate shape
    * 
    * @param object shape The shape to highlight
    */
    RGraph.HBar.prototype.Highlight = function (shape)
    {
        // Add the new highlight
        RGraph.Highlight.Rect(this, shape);
    }



    /**
    * The getObjectByXY() worker method. Don't call this call:
    * 
    * RGraph.ObjectRegistry.getObjectByXY(e)
    * 
    * @param object e The event object
    */
    RGraph.HBar.prototype.getObjectByXY = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e);

        if (
               mouseXY[0] > this.Get('chart.gutter.left')
            && mouseXY[0] < (this.canvas.width - this.Get('chart.gutter.right'))
            && mouseXY[1] > this.Get('chart.gutter.top')
            && mouseXY[1] < (this.canvas.height - this.Get('chart.gutter.bottom'))
            ) {

            return this;
        }
    }



    /**
    * This function positions a tooltip when it is displayed
    * 
    * @param obj object    The chart object
    * @param int x         The X coordinate specified for the tooltip
    * @param int y         The Y coordinate specified for the tooltip
    * @param objec tooltip The tooltips DIV element
    */
    RGraph.HBar.prototype.positionTooltip = function (obj, x, y, tooltip, idx)
    {
        var coordX     = obj.coords[tooltip.__index__][0];
        var coordY     = obj.coords[tooltip.__index__][1];
        var coordW     = obj.coords[tooltip.__index__][2];
        var coordH     = obj.coords[tooltip.__index__][3];
        var canvasXY   = RGraph.getCanvasXY(obj.canvas);
        var gutterLeft = obj.Get('chart.gutter.left');
        var gutterTop  = obj.Get('chart.gutter.top');
        var width      = tooltip.offsetWidth;
        var height     = tooltip.offsetHeight;

        // Set the top position
        tooltip.style.left = 0;
        tooltip.style.top  = canvasXY[1] + coordY - height - 7 + (coordH / 2) + 'px';
        
        // By default any overflow is hidden
        tooltip.style.overflow = '';

        // The arrow
        var img = new Image();
            img.src = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAAFCAYAAACjKgd3AAAARUlEQVQYV2NkQAN79+797+RkhC4M5+/bd47B2dmZEVkBCgcmgcsgbAaA9GA1BCSBbhAuA/AagmwQPgMIGgIzCD0M0AMMAEFVIAa6UQgcAAAAAElFTkSuQmCC';
            img.style.position = 'absolute';
            img.id = '__rgraph_tooltip_pointer__';
            img.style.top = (tooltip.offsetHeight - 2) + 'px';
        tooltip.appendChild(img);
        
        // Reposition the tooltip if at the edges:
        
        // LEFT edge
        if ((canvasXY[0] + coordX  + (coordW / 2) - (width / 2)) < 10) {
            tooltip.style.left = (canvasXY[0] + coordX - (width * 0.1)) + (coordW / 2) + 'px';
            img.style.left = ((width * 0.1) - 8.5) + 'px';

        // RIGHT edge
        } else if ((canvasXY[0] + (coordW / 2) + coordX + (width / 2)) > document.body.offsetWidth) {
            tooltip.style.left = canvasXY[0] + coordX - (width * 0.9) + (coordW / 2) + 'px';
            img.style.left = ((width * 0.9) - 8.5) + 'px';

        // Default positioning - CENTERED
        } else {
            tooltip.style.left = (canvasXY[0] + coordX + (coordW / 2) - (width * 0.5)) + 'px';
            img.style.left = ((width * 0.5) - 8.5) + 'px';
        }
    }