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
    * The bar chart constructor
    * 
    * @param object canvas The canvas object
    * @param array  data   The chart data
    */
    RGraph.Bar = function (id, data)
    {
        // Get the canvas and context objects
        this.id                = id;
        this.canvas            = document.getElementById(id);
        this.context           = this.canvas.getContext ? this.canvas.getContext("2d") : null;
        this.canvas.__object__ = this;
        this.type              = 'bar';
        this.max               = 0;
        this.stackedOrGrouped  = false;
        this.isRGraph          = true;
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();


        /**
        * Compatibility with older browsers
        */
        RGraph.OldBrowserCompat(this.context);


        // Various config type stuff
        this.properties = {
            'chart.background.barcolor1':   'rgba(0,0,0,0)',
            'chart.background.barcolor2':   'rgba(0,0,0,0)',
            'chart.background.grid':        true,
            'chart.background.grid.color':  '#ddd',
            'chart.background.grid.width':  1,
            'chart.background.grid.hsize':  20,
            'chart.background.grid.vsize':  20,
            'chart.background.grid.vlines': true,
            'chart.background.grid.hlines': true,
            'chart.background.grid.border': true,
            'chart.background.grid.autofit':true,
            'chart.background.grid.autofit.numhlines': 5,
            'chart.background.grid.autofit.numvlines': 20,
            'chart.background.image.stretch': true,
            'chart.background.image.x':     null,
            'chart.background.image.y':     null,
            'chart.background.image.w':     null,
            'chart.background.image.h':     null,
            'chart.background.image.align': null,
            'chart.ytickgap':               20,
            'chart.smallyticks':            3,
            'chart.largeyticks':            5,
            'chart.numyticks':              10,
            'chart.hmargin':                5,
            'chart.strokecolor':            'rgba(0,0,0,0)',
            'chart.axis.color':             'black',
            'chart.gutter.top':             25,
            'chart.gutter.bottom':          25,
            'chart.gutter.left':            25,
            'chart.gutter.right':           25,
            'chart.labels':                 null,
            'chart.labels.ingraph':         null,
            'chart.labels.above':           false,
            'chart.labels.above.decimals':  0,
            'chart.labels.above.size':      null,
            'chart.labels.above.angle':     null,
            'chart.ylabels':                true,
            'chart.ylabels.count':          5,
            'chart.ylabels.inside':         false,
            'chart.xlabels.offset':         0,
            'chart.xaxispos':               'bottom',
            'chart.yaxispos':               'left',
            'chart.text.color':             'black',
            'chart.text.size':              10,
            'chart.text.angle':             0,
            'chart.text.font':              'Arial',
            'chart.ymin':                   0,
            'chart.ymax':                   null,
            'chart.title':                  '',
            'chart.title.font':             null,
            'chart.title.background':       null,
            'chart.title.hpos':             null,
            'chart.title.vpos':             null,
            'chart.title.bold':             true,
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
            'chart.colors':                 ['#01B4FF', '#0f0', '#00f', '#ff0', '#0ff', '#0f0'],
            'chart.colors.sequential':      false,
            'chart.colors.reverse':         false,
            'chart.grouping':               'grouped',
            'chart.variant':                'bar',
            'chart.shadow':                 false,
            'chart.shadow.color':           '#999',
            'chart.shadow.offsetx':         3,
            'chart.shadow.offsety':         3,
            'chart.shadow.blur':            3,
            'chart.tooltips':               null,
            'chart.tooltips.effect':        'fade',
            'chart.tooltips.css.class':     'RGraph_tooltip',
            'chart.tooltips.event':         'onclick',
            'chart.tooltips.highlight':     true,
            'chart.highlight.stroke':       'rgba(0,0,0,0)',
            'chart.highlight.fill':         'rgba(255,255,255,0.7)',
            'chart.background.hbars':       null,
            'chart.key':                    null,
            'chart.key.background':         'white',
            'chart.key.position':           'graph',
            'chart.key.shadow':             false,
            'chart.key.shadow.color':       '#666',
            'chart.key.shadow.blur':        3,
            'chart.key.shadow.offsetx':     2,
            'chart.key.shadow.offsety':     2,
            'chart.key.position.gutter.boxed': true,
            'chart.key.position.x':         null,
            'chart.key.position.y':         null,
            'chart.key.halign':             'right',
            'chart.key.color.shape':        'square',
            'chart.key.rounded':            true,
            'chart.key.text.size':          10,
            'chart.key.linewidth':          1,
            'chart.key.colors':             null,
            'chart.contextmenu':            null,
            'chart.units.pre':              '',
            'chart.units.post':             '',
            'chart.scale.decimals':         0,
            'chart.scale.point':            '.',
            'chart.scale.thousand':         ',',
            'chart.crosshairs':             false,
            'chart.crosshairs.color':       '#333',
            'chart.crosshairs.hline':       true,
            'chart.crosshairs.vline':       true,
            'chart.linewidth':              1,
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
            'chart.resizable':              false,
            'chart.resize.handle.adjust':   [0,0],
            'chart.resize.handle.background': null,
            'chart.adjustable':             false,
            'chart.noaxes':                 false,
            'chart.noxaxis':                false,
            'chart.noyaxis':                false,
            'chart.events.click':           null,
            'chart.events.mousemove':       null,
            'chart.numxticks':              null
        }

        // Check for support
        if (!this.canvas) {
            alert('[BAR] No canvas support');
            return;
        }

        /**
        * Determine whether the chart will contain stacked or grouped bars
        */
        for (i=0; i<data.length; ++i) {
            if (typeof(data[i]) == 'object') {
                this.stackedOrGrouped = true;
            }
        }

        // Store the data
        this.data = data;
        
        // Used to store the coords of the bars
        this.coords = [];



        /**
        * This linearises the data. Doing so can make it easier to pull
        * out the appropriate data from tooltips
        */
        this.data_arr = RGraph.array_linearize(this.data);



        /**
        * Register the object
        */
        RGraph.Register(this);
    }


    /**
    * A setter
    * 
    * @param name  string The name of the property to set
    * @param value mixed  The value of the property
    */
    RGraph.Bar.prototype.Set = function (name, value)
    {
        name = name.toLowerCase();

        if (name == 'chart.labels.abovebar') {
            name = 'chart.labels.above';
        }
        
        if (name == 'chart.strokestyle') {
            name = 'chart.strokecolor';
        }
        
        /**
        * Check for xaxispos
        */
        if (name == 'chart.xaxispos' ) {
            if (value != 'bottom' && value != 'center' && value != 'top') {
                alert('[BAR] (' + this.id + ') chart.xaxispos should be top, center or bottom. Tried to set it to: ' + value + ' Changing it to center');
                value = 'center';
            }
            
            if (value == 'top') {
                for (var i=0; i<this.data.length; ++i) {
                    if (typeof(this.data[i]) == 'number' && this.data[i] > 0) {
                        alert('[BAR] The data element with index ' + i + ' should be negative');
                    }
                }
            }
        }
        
        /**
        * lineWidth doesn't appear to like a zero setting
        */
        if (name.toLowerCase() == 'chart.linewidth' && value == 0) {
            value = 0.0001;
        }

        this.properties[name] = value;
    }


    /**
    * A getter
    * 
    * @param name  string The name of the property to get
    */
    RGraph.Bar.prototype.Get = function (name)
    {
        if (name == 'chart.labels.abovebar') {
            name = 'chart.labels.above';
        }
        
        var value = this.properties[name];

        return value;
    }



    /**
    * The function you call to draw the bar chart
    */
    RGraph.Bar.prototype.Draw = function ()
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

        /**
        * Convert any null values to 0. Won't make any difference to the bar (as opposed to the line chart)
        */
        for (var i=0; i<this.data.length; ++i) {
            if (this.data[i] == null) {
                this.data[i] = 0;
            }
        }


        // Cache this in a class variable as it's used rather a lot

        /**
        * Check for tooltips and alert the user that they're not supported with pyramid charts
        */
        if (   (this.Get('chart.variant') == 'pyramid' || this.Get('chart.variant') == 'dot')
            && typeof(this.Get('chart.tooltips')) == 'object'
            && this.Get('chart.tooltips')
            && this.Get('chart.tooltips').length > 0) {

            alert('[BAR] (' + this.id + ') Sorry, tooltips are not supported with dot or pyramid charts');
        }

        /**
        * Stop the coords array from growing uncontrollably
        */
        this.coords = [];

        /**
        * Work out a few things. They need to be here because they depend on things you can change before you
        * call Draw() but after you instantiate the object
        */
        this.max            = 0;
        this.grapharea      = this.canvas.height - this.gutterTop - this.gutterBottom;
        this.halfgrapharea  = this.grapharea / 2;
        this.halfTextHeight = this.Get('chart.text.size') / 2;

        // Progressively Draw the chart
        RGraph.background.Draw(this);




        //If it's a sketch chart variant, draw the axes first
        if (this.Get('chart.variant') == 'sketch') {
            this.DrawAxes();
            this.Drawbars();
        } else {
            this.Drawbars();
            this.DrawAxes();
        }

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
        * Is a line is defined, draw it
        *
        var line = this.Get('chart.line');

        if (line) {
        
            line.__bar__ = this;
            
            // Check the X axis positions
            if (this.Get('chart.xaxispos') != line.Get('chart.xaxispos')) {
                alert("[BAR] Using different X axis positions when combining the Bar and Line is not advised");
            }

            line.Set('chart.gutter.left',   this.Get('chart.gutter.left'));
            line.Set('chart.gutter.right',  this.Get('chart.gutter.right'));
            line.Set('chart.gutter.top',    this.Get('chart.gutter.top'));
            line.Set('chart.gutter.bottom', this.Get('chart.gutter.bottom'));

            line.Set('chart.hmargin', (this.canvas.width - this.gutterLeft - this.gutterRight) / (line.original_data[0].length * 2));
            
            // If a BAR custom yMax is set, use that
            if (this.Get('chart.ymax') && !line.Get('chart.ymax')) {
                line.Set('chart.ymax', this.Get('chart.ymax'));
            
            } else if (line.Get('chart.ymax')) {
                line.Set('chart.ymax', line.Get('chart.ymax'));
            }

            // The boolean is used to specify that the Line chart .Draw() method is being called by the Bar chart
            line.Draw(true);
        }
*/


        /**
        * Draw "in graph" labels
        */
        if (this.Get('chart.labels.ingraph')) {
            RGraph.DrawInGraphLabels(this);
        }

        
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
    * Draws the charts axes
    */
    RGraph.Bar.prototype.DrawAxes = function ()
    {
        if (this.Get('chart.noaxes')) {
            return;
        }

        var xaxispos = this.Get('chart.xaxispos');
        var yaxispos = this.Get('chart.yaxispos');

        this.context.beginPath();
        this.context.strokeStyle = this.Get('chart.axis.color');
        this.context.lineWidth   = 1;

        // Draw the Y axis
        if (this.Get('chart.noyaxis') == false) {
            if (yaxispos == 'right') {
                this.context.moveTo(AA(this, this.canvas.width - this.gutterRight), this.gutterTop);
                this.context.lineTo(AA(this, this.canvas.width - this.gutterRight), this.canvas.height - this.gutterBottom);
            } else {
                this.context.moveTo(AA(this, this.gutterLeft), this.gutterTop);
                this.context.lineTo(AA(this, this.gutterLeft), this.canvas.height - this.gutterBottom);
            }
        }
        
        // Draw the X axis
        if (this.Get('chart.noxaxis') == false) {
            if (xaxispos == 'center') {
                this.context.moveTo(this.gutterLeft, AA(this, ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2) + this.gutterTop));
                this.context.lineTo(this.canvas.width - this.gutterRight, AA(this, ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2) + this.gutterTop));
            } else if (xaxispos == 'top') {
                this.context.moveTo(this.gutterLeft, AA(this, this.gutterTop));
                this.context.lineTo(this.canvas.width - this.gutterRight, AA(this, this.gutterTop));
            } else {
                this.context.moveTo(this.gutterLeft, AA(this, this.canvas.height - this.gutterBottom));
                this.context.lineTo(this.canvas.width - this.gutterRight, AA(this, this.canvas.height - this.gutterBottom));
            }
        }

        var numYTicks = this.Get('chart.numyticks');

        // Draw the Y tickmarks
        if (this.Get('chart.noyaxis') == false) {
            var yTickGap = (this.canvas.height - this.gutterTop - this.gutterBottom) / numYTicks;
            var xpos     = yaxispos == 'left' ? this.gutterLeft : this.canvas.width - this.gutterRight;

            if (this.Get('chart.numyticks') > 0) {
                for (y=this.gutterTop;
                     xaxispos == 'center' ? y <= (this.canvas.height - this.gutterBottom) : y < (this.canvas.height - this.gutterBottom + (xaxispos == 'top' ? 1 : 0));
                     y += yTickGap) {
        
                    if (xaxispos == 'center' && y == (this.canvas.height / 2)) continue;
                    
                    // X axis at the top
                    if (xaxispos == 'top' && y == this.gutterTop) continue;
                    
                    this.context.moveTo(xpos, AA(this, y));
                    this.context.lineTo(xpos + (yaxispos == 'left' ? -3 : 3), AA(this, y));
                }
            }

            /**
            * If the X axis is not being shown, draw an extra tick
            */
            if (this.Get('chart.noxaxis')) {
                if (xaxispos == 'center') {
                    this.context.moveTo(xpos + (yaxispos == 'left' ? -3 : 3), AA(this, this.canvas.height / 2));
                    this.context.lineTo(xpos, AA(this, this.canvas.height / 2));
                } else if (xaxispos == 'top') {
                    this.context.moveTo(xpos + (yaxispos == 'left' ? -3 : 3), AA(this, this.gutterTop));
                    this.context.lineTo(xpos, AA(this, this.gutterTop));
                } else {
                    this.context.moveTo(xpos + (yaxispos == 'left' ? -3 : 3), AA(this, this.canvas.height - this.gutterBottom));
                    this.context.lineTo(xpos, AA(this, this.canvas.height - this.gutterBottom));
                }
            }
        }


        // Draw the X tickmarks
        if (this.Get('chart.noxaxis') == false) {
            
            xTickGap = (this.canvas.width - this.gutterLeft - this.gutterRight) / this.data.length;
            
            if (typeof(this.Get('chart.numxticks')) == 'number') {
                xTickGap = (this.canvas.width - this.gutterLeft - this.gutterRight) / this.Get('chart.numxticks');
            }

            if (xaxispos == 'bottom') {
                yStart   = this.canvas.height - this.gutterBottom;
                yEnd     = (this.canvas.height - this.gutterBottom) + 3;
            } else if (xaxispos == 'top') {
                yStart = this.gutterTop - 3;
                yEnd   = this.gutterTop;
            } else if (xaxispos == 'center') {
                yStart = ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2) + this.gutterTop + 3;
                yEnd   = ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2) + this.gutterTop - 3;
            }
            
            yStart = AA(this, yStart);
            yEnd = AA(this, yEnd);
            
            //////////////// X TICKS ////////////////
            var noEndXTick = this.Get('chart.noendxtick');

            for (x=this.gutterLeft + (yaxispos == 'left' ? xTickGap : 0); x<this.canvas.width - this.gutterRight + (yaxispos == 'left' ? 5 : 0); x+=xTickGap) {

                if (yaxispos == 'left' && !noEndXTick && x > this.gutterLeft) {
                    this.context.moveTo(AA(this, x), yStart);
                    this.context.lineTo(AA(this, x), yEnd);
                
                } else if (yaxispos == 'left' && noEndXTick && x > this.gutterLeft && x < (this.canvas.width - this.gutterRight) ) {
                    this.context.moveTo(AA(this, x), yStart);
                    this.context.lineTo(AA(this, x), yEnd);
                
                } else if (yaxispos == 'right' && x < (this.canvas.width - this.gutterRight) && !noEndXTick) {
                    this.context.moveTo(AA(this, x), yStart);
                    this.context.lineTo(AA(this, x), yEnd);
                
                } else if (yaxispos == 'right' && x < (this.canvas.width - this.gutterRight) && x > (this.gutterLeft) && noEndXTick) {
                    this.context.moveTo(AA(this, x), yStart);
                    this.context.lineTo(AA(this, x), yEnd);
                }
            }
            
            if (this.Get('chart.noyaxis') || this.Get('chart.numxticks') == null) {
                if (typeof(this.Get('chart.numxticks')) == 'number' && this.Get('chart.numxticks') > 0) {
                    this.context.moveTo(AA(this, this.gutterLeft), yStart);
                    this.context.lineTo(AA(this, this.gutterLeft), yEnd);
                }
            }
    
            //////////////// X TICKS ////////////////
        }

        /**
        * If the Y axis is not being shown, draw an extra tick
        */
        if (this.Get('chart.noyaxis') && this.Get('chart.noxaxis') == false && this.Get('chart.numxticks') == null) {
            if (xaxispos == 'center') {
                this.context.moveTo(AA(this, this.gutterLeft), (this.canvas.height / 2) - 3);
                this.context.lineTo(AA(this, this.gutterLeft), (this.canvas.height / 2) + 3);
            } else {
                this.context.moveTo(AA(this, this.gutterLeft), this.canvas.height - this.gutterBottom);
                this.context.lineTo(AA(this, this.gutterLeft), this.canvas.height - this.gutterBottom + 3);
            }
        }

        this.context.stroke();
    }


    /**
    * Draws the bars
    */
    RGraph.Bar.prototype.Drawbars = function ()
    {
        this.context.lineWidth   = this.Get('chart.linewidth');
        this.context.strokeStyle = this.Get('chart.strokecolor');
        this.context.fillStyle   = this.Get('chart.colors')[0];
        var prevX                = 0;
        var prevY                = 0;
        var decimals             = this.Get('chart.scale.decimals');

        /**
        * Work out the max value
        */
        if (this.Get('chart.ymax')) {

            this.max = this.Get('chart.ymax');
            this.min = this.Get('chart.ymin');

            this.scale = [
                          (((this.max - this.min) * (1/5)) + this.min).toFixed(decimals),
                          (((this.max - this.min) * (2/5)) + this.min).toFixed(decimals),
                          (((this.max - this.min) * (3/5)) + this.min).toFixed(decimals),
                          (((this.max - this.min) * (4/5)) + this.min).toFixed(decimals),
                          (((this.max - this.min) * (5/5) + this.min)).toFixed(decimals)
                         ];
        } else {
        
            this.min = this.Get('chart.ymin');
        
            for (i=0; i<this.data.length; ++i) {
                if (typeof(this.data[i]) == 'object') {
                    var value = this.Get('chart.grouping') == 'grouped' ? Number(RGraph.array_max(this.data[i], true)) : Number(RGraph.array_sum(this.data[i])) ;

                } else {
                    var value = Number(this.data[i]);
                }

                this.max = Math.max(Math.abs(this.max), Math.abs(value));
            }

            this.scale = RGraph.getScale(this.max, this);
            this.max   = this.scale[4];

            if (this.Get('chart.ymin')) {

                    var decimals = this.Get('chart.scale.decimals');
    
                    this.scale[0] = ((Number(this.scale[4] - this.min) * 0.2) + this.min).toFixed(decimals);
                    this.scale[1] = ((Number(this.scale[4] - this.min) * 0.4) + this.min).toFixed(decimals);
                    this.scale[2] = ((Number(this.scale[4] - this.min) * 0.6) + this.min).toFixed(decimals);
                    this.scale[3] = ((Number(this.scale[4] - this.min) * 0.8) + this.min).toFixed(decimals);
                    this.scale[4] = ((Number(this.scale[4] - this.min) * 1.0) + this.min).toFixed(decimals);

            } else {
                if (this.Get('chart.scale.decimals')) {
                    
                    var decimals = this.Get('chart.scale.decimals');
    
                    this.scale[0] = Number(this.scale[0]).toFixed(decimals);
                    this.scale[1] = Number(this.scale[1]).toFixed(decimals);
                    this.scale[2] = Number(this.scale[2]).toFixed(decimals);
                    this.scale[3] = Number(this.scale[3]).toFixed(decimals);
                    this.scale[4] = Number(this.scale[4]).toFixed(decimals);
                }
            }
        }
        
        /**
        * if the chart is adjustable fix the scale so that it doesn't change.
        */
        if (this.Get('chart.adjustable') && !this.Get('chart.ymax')) {
            this.Set('chart.ymax', this.scale[4]);
        }

        /**
        * Draw horizontal bars here
        */
        if (this.Get('chart.background.hbars') && this.Get('chart.background.hbars').length > 0) {
            RGraph.DrawBars(this);
        }

        var variant = this.Get('chart.variant');
        
        /**
        * Draw the 3D axes is necessary
        */
        if (variant == '3d') {
            RGraph.Draw3DAxes(this);
        }

        /**
        * Get the variant once, and draw the bars, be they regular, stacked or grouped
        */
        
        // Get these variables outside of the loop
        var xaxispos      = this.Get('chart.xaxispos');
        var width         = (this.canvas.width - this.gutterLeft - this.gutterRight ) / this.data.length;
        var orig_height   = height;
        var hmargin       = this.Get('chart.hmargin');
        var shadow        = this.Get('chart.shadow');
        var shadowColor   = this.Get('chart.shadow.color');
        var shadowBlur    = this.Get('chart.shadow.blur');
        var shadowOffsetX = this.Get('chart.shadow.offsetx');
        var shadowOffsetY = this.Get('chart.shadow.offsety');
        var strokeStyle   = this.Get('chart.strokecolor');
        var colors        = this.Get('chart.colors');
        var sequentialColorIndex = 0;

        for (i=0; i<this.data.length; ++i) {

            // Work out the height
            //The width is up outside the loop
            var height = ((RGraph.array_sum(this.data[i]) < 0 ? RGraph.array_sum(this.data[i]) + this.min : RGraph.array_sum(this.data[i]) - this.min) / (this.max - this.min) ) * (this.canvas.height - this.gutterTop - this.gutterBottom);

            // Half the height if the Y axis is at the center
            if (xaxispos == 'center') {
                height /= 2;
            }

            var x = (i * width) + this.gutterLeft;
            var y = xaxispos == 'center' ? ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2) + this.gutterTop - height
                                         : this.canvas.height - height - this.gutterBottom;

            // xaxispos is top
            if (xaxispos == 'top') {
                y = this.gutterTop + Math.abs(height);
            }


            // Account for negative lengths - Some browsers (eg Chrome) don't like a negative value
            if (height < 0) {
                y += height;
                height = Math.abs(height);
            }

            /**
            * Turn on the shadow if need be
            */
            if (shadow) {
                this.context.shadowColor   = shadowColor;
                this.context.shadowBlur    = shadowBlur;
                this.context.shadowOffsetX = shadowOffsetX;
                this.context.shadowOffsetY = shadowOffsetY;
            }

            /**
            * Draw the bar
            */
            this.context.beginPath();
                if (typeof(this.data[i]) == 'number') {
                    
                    var barWidth = width - (2 * hmargin);
                    
                    /**
                    * Check for a negative bar width
                    */
                    if (barWidth < 0) {
                        alert('[RGRAPH] Warning: you have a negative bar width. This may be caused by the chart.hmargin being too high or the width of the canvas not being sufficient.');
                    }

                    // Set the fill color
                    this.context.strokeStyle = strokeStyle;
                    this.context.fillStyle = colors[0];
                    
                    /**
                    * Sequential colors
                    */
                    if (this.Get('chart.colors.sequential')) {
                        this.context.fillStyle = colors[i];
                    }

                    if (variant == 'sketch') {

                        this.context.lineCap = 'round';
                        
                        var sketchOffset = 3;

                        this.context.beginPath();

                        this.context.strokeStyle = colors[0];

                        /**
                        * Sequential colors
                        */
                        if (this.Get('chart.colors.sequential')) {
                            this.context.strokeStyle = colors[i];
                        }

                        // Left side
                        this.context.moveTo(x + hmargin + 2, y + height - 2);
                        this.context.lineTo(x + hmargin , y - 2);

                        // The top
                        this.context.moveTo(x + hmargin - 3, y + -2 + (this.data[i] < 0 ? height : 0));
                        this.context.bezierCurveTo(x + ((hmargin + width) * 0.33),y + 5 + (this.data[i] < 0 ? height - 10: 0),x + ((hmargin + width) * 0.66),y + 5 + (this.data[i] < 0 ? height - 10 : 0),x + hmargin + width + -1, y + 0 + (this.data[i] < 0 ? height : 0));


                        // The right side
                        this.context.moveTo(x + hmargin + width - 2, y + -2);
                        this.context.lineTo(x + hmargin + width - 3, y + height - 3);

                        for (var r=0.2; r<=0.8; r+=0.2) {
                            this.context.moveTo(x + hmargin + width + (r > 0.4 ? -1 : 3) - (r * width),y - 1);
                            this.context.lineTo(x + hmargin + width - (r > 0.4 ? 1 : -1) - (r * width), y + height + (r == 0.2 ? 1 : -2));
                        }

                        this.context.stroke();

                    // Regular bar
                    } else if (variant == 'bar' || variant == '3d' || variant == 'glass' || variant == 'bevel') {
                    
                        if (RGraph.isOld() && shadow) {
                            this.DrawIEShadow([x + hmargin, y, barWidth, height]);
                        }
                        
                        if (variant == 'glass') {
                            RGraph.filledCurvyRect(this.context, x + hmargin, y, barWidth, height, 3, this.data[i] > 0, this.data[i] > 0, this.data[i] < 0, this.data[i] < 0);
                            RGraph.strokedCurvyRect(this.context, x + hmargin, y, barWidth, height, 3, this.data[i] > 0, this.data[i] > 0, this.data[i] < 0, this.data[i] < 0);
                        } else {
                            this.context.strokeRect(x + hmargin, y, barWidth, height);
                            this.context.fillRect(x + hmargin, y, barWidth, height);
                        }

                        // 3D effect
                        if (variant == '3d') {

                            var prevStrokeStyle = this.context.strokeStyle;
                            var prevFillStyle   = this.context.fillStyle;

                            // Draw the top
                            this.context.beginPath();
                                this.context.moveTo(x + hmargin, y);
                                this.context.lineTo(x + hmargin + 10, y - 5);
                                this.context.lineTo(x + hmargin + 10 + barWidth, y - 5);
                                this.context.lineTo(x + hmargin + barWidth, y);
                            this.context.closePath();

                            this.context.stroke();
                            this.context.fill();

                            // Draw the right hand side
                            this.context.beginPath();
                                this.context.moveTo(x + hmargin + barWidth, y);
                                this.context.lineTo(x + hmargin + barWidth + 10, y - 5);
                                this.context.lineTo(x + hmargin + barWidth + 10, y + height - 5);
                                this.context.lineTo(x + hmargin + barWidth, y + height);
                            this.context.closePath();
    
                            this.context.stroke();                        
                            this.context.fill();

                            // Draw the darker top section
                            this.context.beginPath();                            
                                this.context.fillStyle = 'rgba(255,255,255,0.3)';
                                this.context.moveTo(x + hmargin, y);
                                this.context.lineTo(x + hmargin + 10, y - 5);
                                this.context.lineTo(x + hmargin + 10 + barWidth, y - 5);
                                this.context.lineTo(x + hmargin + barWidth, y);
                                this.context.lineTo(x + hmargin, y);
                            this.context.closePath();
    
                            this.context.stroke();
                            this.context.fill();

                            // Draw the darker right side section
                            this.context.beginPath();
                                this.context.fillStyle = 'rgba(0,0,0,0.4)';
                                this.context.moveTo(x + hmargin + barWidth, y);
                                this.context.lineTo(x + hmargin + barWidth + 10, y - 5);
                                this.context.lineTo(x + hmargin + barWidth + 10, y - 5 + height);
                                this.context.lineTo(x + hmargin + barWidth, y + height);
                                this.context.lineTo(x + hmargin + barWidth, y);
                            this.context.closePath();

                            this.context.stroke();
                            this.context.fill();

                            this.context.strokeStyle = prevStrokeStyle;
                            this.context.fillStyle   = prevFillStyle;
                        
                        // Glass variant
                        } else if (variant == 'glass') {
 
                            var grad = this.context.createLinearGradient(
                                                                         x + hmargin,
                                                                         y,
                                                                         x + hmargin + (barWidth / 2),
                                                                         y
                                                                        );
                            grad.addColorStop(0, 'rgba(255,255,255,0.9)');
                            grad.addColorStop(1, 'rgba(255,255,255,0.5)');

                            this.context.beginPath();
                            this.context.fillStyle = grad;
                            this.context.fillRect(x + hmargin + 2,y + (this.data[i] > 0 ? 2 : 0),(barWidth / 2) - 2,height - 2);
                            this.context.fill();
                        }

                        // This bit draws the text labels that appear above the bars if requested
                        if (this.Get('chart.labels.above')) {

                            // Turn off any shadow
                            if (shadow) {
                                RGraph.NoShadow(this);
                            }

                            var yPos = y - 3;

                            // Account for negative bars
                            if (this.data[i] < 0) {
                                yPos += height + 6 + (this.Get('chart.text.size') - 4);
                            }

                            // Account for chart.xaxispos=top
                            if (this.Get('chart.xaxispos') == 'top') {
                                yPos = this.gutterTop + height + 6 + (typeof(this.Get('chart.labels.above.size')) == 'number' ? this.Get('chart.labels.above.size') : this.Get('chart.text.size') - 4);
                            }

                            // Account for chart.variant=3d
                            if (this.Get('chart.variant') == '3d') {
                                yPos -= 5;
                            }

                            this.context.fillStyle = this.Get('chart.text.color');

                            RGraph.Text(this.context,
                                        this.Get('chart.text.font'),
                                        typeof(this.Get('chart.labels.above.size')) == 'number' ? this.Get('chart.labels.above.size') : this.Get('chart.text.size') - 3,
                                        x + hmargin + (barWidth / 2),
                                        yPos,
                                        RGraph.number_format(this, Number(this.data[i]).toFixed(this.Get('chart.labels.above.decimals')),this.Get('chart.units.pre'),this.Get('chart.units.post')),
                                        this.Get('chart.labels.above.angle') ? 'bottom' : null,
                                        this.Get('chart.labels.above.angle') ? (this.Get('chart.labels.above.angle') > 0 && (this.Get('chart.xaxispos') != 'top') ? 'right' : 'left') : 'center',
                                        null,
                                        this.Get('chart.labels.above.angle')
                                       );
                        }

                    // Dot chart
                    } else if (variant == 'dot') {

                        this.context.beginPath();
                        this.context.moveTo(x + (width / 2), y);
                        this.context.lineTo(x + (width / 2), y + height);
                        this.context.stroke();
                        
                        this.context.beginPath();
                        this.context.fillStyle = this.Get('chart.colors')[i];
                        this.context.arc(x + (width / 2), y + (this.data[i] > 0 ? 0 : height), 2, 0, 6.28, 0);
                        
                        // Set the colour for the dots
                        this.context.fillStyle = this.Get('chart.colors')[0];

                        /**
                        * Sequential colors
                        */
                        if (this.Get('chart.colors.sequential')) {
                            this.context.fillStyle = colors[i];
                        }

                        this.context.stroke();
                        this.context.fill();
                    
                    // Pyramid chart
                    } else if (variant == 'pyramid') {

                        this.context.beginPath();
                            var startY = (this.Get('chart.xaxispos') == 'center' ? (this.canvas.height / 2) : (this.canvas.height - this.gutterBottom));
                        
                            this.context.moveTo(x + hmargin, startY);
                            this.context.lineTo(
                                                x + hmargin + (barWidth / 2),
                                                y + (this.Get('chart.xaxispos') == 'center' && (this.data[i] < 0) ? height : 0)
                                               );
                            this.context.lineTo(x + hmargin + barWidth, startY);
                        
                        this.context.closePath();
                        
                        this.context.stroke();
                        this.context.fill();
                    
                    // Arrow chart
                    } else if (variant == 'arrow') {
                        
                        var startY = (this.Get('chart.xaxispos') == 'center' ? (this.canvas.height / 2) : (this.canvas.height - this.gutterBottom));

                        this.context.lineWidth = this.Get('chart.linewidth') ? this.Get('chart.linewidth') : 1;
                        this.context.lineCap = 'round';

                        this.context.beginPath();

                            this.context.moveTo(x + hmargin + (barWidth / 2), startY);
                            this.context.lineTo(x + hmargin + (barWidth / 2), y + (this.Get('chart.xaxispos') == 'center' && (this.data[i] < 0) ? height : 0));
                            this.context.arc(x + hmargin + (barWidth / 2),
                                             y + (this.Get('chart.xaxispos') == 'center' && (this.data[i] < 0) ? height : 0),
                                             5,
                                             this.data[i] > 0 ? 0.78 : 5.6,
                                             this.data[i] > 0 ? 0.79 : 5.48,
                                             this.data[i] < 0);

                            this.context.moveTo(x + hmargin + (barWidth / 2), y + (this.Get('chart.xaxispos') == 'center' && (this.data[i] < 0) ? height : 0));
                            this.context.arc(x + hmargin + (barWidth / 2),
                                             y + (this.Get('chart.xaxispos') == 'center' && (this.data[i] < 0) ? height : 0),
                                             5,
                                             this.data[i] > 0 ? 2.355 : 4,
                                             this.data[i] > 0 ? 2.4 : 3.925,
                                             this.data[i] < 0);

                        this.context.stroke();
                        
                        this.context.lineWidth = 1;

                    // Unknown variant type
                    } else {
                        alert('[BAR] Warning! Unknown chart.variant: ' + variant);
                    }

                    this.coords.push([x + hmargin, y, width - (2 * hmargin), height]);


                /**
                * Stacked bar
                */
                } else if (typeof(this.data[i]) == 'object' && this.Get('chart.grouping') == 'stacked') {
                
                    if (this.min) {
                        alert("[ERROR] Stacked Bar charts with a Y min are not supported");
                    }
                    
                    var barWidth     = width - (2 * hmargin);
                    var redrawCoords = [];// Necessary to draw if the shadow is enabled
                    var startY       = 0;
                    var dataset      = this.data[i];
                    
                    /**
                    * Check for a negative bar width
                    */
                    if (barWidth < 0) {
                        alert('[RGRAPH] Warning: you have a negative bar width. This may be caused by the chart.hmargin being too high or the width of the canvas not being sufficient.');
                    }

                    for (j=0; j<dataset.length; ++j) {

                        // Stacked bar chart and X axis pos in the middle - poitless since negative values are not permitted
                        if (xaxispos == 'center') {
                            alert("[BAR] It's pointless having the X axis position at the center on a stacked bar chart.");
                            return;
                        }

                        // Negative values not permitted for the stacked chart
                        if (this.data[i][j] < 0) {
                            alert('[BAR] Negative values are not permitted with a stacked bar chart. Try a grouped one instead.');
                            return;
                        }

                        /**
                        * Set the fill and stroke colors
                        */
                        this.context.strokeStyle = strokeStyle
                        this.context.fillStyle = colors[j];
    
                        if (this.Get('chart.colors.reverse')) {
                            this.context.fillStyle = colors[this.data[i].length - j - 1];
                        }
                        
                        if (this.Get('chart.colors.sequential') && colors[sequentialColorIndex]) {
                            this.context.fillStyle = colors[sequentialColorIndex++];
                        } else if (this.Get('chart.colors.sequential')) {
                            this.context.fillStyle = colors[sequentialColorIndex - 1];
                        }

                        var height = (dataset[j] / this.max) * (this.canvas.height - this.gutterTop - this.gutterBottom );

                        // If the X axis pos is in the center, we need to half the  height
                        if (xaxispos == 'center') {
                            height /= 2;
                        }

                        var totalHeight = (RGraph.array_sum(dataset) / this.max) * (this.canvas.height - hmargin - this.gutterTop - this.gutterBottom);

                        /**
                        * Store the coords for tooltips
                        */
                        this.coords.push([x + hmargin, y, width - (2 * hmargin), height]);

                        // MSIE shadow
                        if (RGraph.isOld() && shadow) {
                            this.DrawIEShadow([x + hmargin, y, width - (2 * hmargin), height + 1]);
                        }

                        this.context.strokeRect(x + hmargin, y, width - (2 * hmargin), height);
                        this.context.fillRect(x + hmargin, y, width - (2 * hmargin), height);

                        
                        if (j == 0) {
                            var startY = y;
                            var startX = x;
                        }

                        /**
                        * Store the redraw coords if the shadow is enabled
                        */
                        if (shadow) {
                            redrawCoords.push([x + hmargin, y, width - (2 * hmargin), height, this.context.fillStyle]);
                        }

                        /**
                        * Stacked 3D effect
                        */
                        if (variant == '3d') {

                            var prevFillStyle = this.context.fillStyle;
                            var prevStrokeStyle = this.context.strokeStyle;

    
                            // Draw the top side
                            if (j == 0) {
                                this.context.beginPath();
                                    this.context.moveTo(startX + hmargin, y);
                                    this.context.lineTo(startX + 10 + hmargin, y - 5);
                                    this.context.lineTo(startX + 10 + barWidth + hmargin, y - 5);
                                    this.context.lineTo(startX + barWidth + hmargin, y);
                                this.context.closePath();
                                
                                this.context.fill();
                                this.context.stroke();
                            }

                            // Draw the side section
                            this.context.beginPath();
                                this.context.moveTo(startX + barWidth + hmargin, y);
                                this.context.lineTo(startX + barWidth + hmargin + 10, y - 5);
                                this.context.lineTo(startX + barWidth + hmargin + 10, y - 5 + height);
                                this.context.lineTo(startX + barWidth + hmargin , y + height);
                            this.context.closePath();
                            
                            this.context.fill();
                            this.context.stroke();

                            // Draw the darker top side
                            if (j == 0) {
                                this.context.fillStyle = 'rgba(255,255,255,0.3)';
                                this.context.beginPath();
                                    this.context.moveTo(startX + hmargin, y);
                                    this.context.lineTo(startX + 10 + hmargin, y - 5);
                                    this.context.lineTo(startX + 10 + barWidth + hmargin, y - 5);
                                    this.context.lineTo(startX + barWidth + hmargin, y);
                                this.context.closePath();
                                
                                this.context.fill();
                                this.context.stroke();
                            }

                            // Draw the darker side section
                            this.context.fillStyle = 'rgba(0,0,0,0.4)';
                            this.context.beginPath();
                                this.context.moveTo(startX + barWidth + hmargin, y);
                                this.context.lineTo(startX + barWidth + hmargin + 10, y - 5);
                                this.context.lineTo(startX + barWidth + hmargin + 10, y - 5 + height);
                                this.context.lineTo(startX + barWidth + hmargin , y + height);
                            this.context.closePath();
                            
                            this.context.fill();
                            this.context.stroke();

                            this.context.strokeStyle = prevStrokeStyle;
                            this.context.fillStyle = prevFillStyle;
                        }

                        y += height;
                    }

                    // This bit draws the text labels that appear above the bars if requested
                    if (this.Get('chart.labels.above')) {

                        // Turn off any shadow
                        RGraph.NoShadow(this);

                        this.context.fillStyle = this.Get('chart.text.color');
                        RGraph.Text(this.context,
                                    this.Get('chart.text.font'),
                                    typeof(this.Get('chart.labels.above.size')) == 'number' ? this.Get('chart.labels.above.size') : this.Get('chart.text.size') - 3,
                                    startX + (barWidth / 2) + this.Get('chart.hmargin'),
                                    startY - (this.Get('chart.shadow') && this.Get('chart.shadow.offsety') < 0 ? 7 : 4) - (this.Get('chart.variant') == '3d' ? 5 : 0),
                                    String(this.Get('chart.units.pre') + RGraph.array_sum(this.data[i]).toFixed(this.Get('chart.labels.above.decimals')) + this.Get('chart.units.post')),
                                    this.Get('chart.labels.above.angle') ? 'bottom' : null,
                                    this.Get('chart.labels.above.angle') ? (this.Get('chart.labels.above.angle') > 0 ? 'right' : 'left') : 'center',
                                    null,
                                    this.Get('chart.labels.above.angle'));
                      
                        // Turn any shadow back on
                        if (shadow) {
                            this.context.shadowColor   = shadowColor;
                            this.context.shadowBlur    = shadowBlur;
                            this.context.shadowOffsetX = shadowOffsetX;
                            this.context.shadowOffsetY = shadowOffsetY;
                        }
                    }
                    

                    /**
                    * Redraw the bars if the shadow is enabled due to hem being drawn from the bottom up, and the
                    * shadow spilling over to higher up bars
                    */
                    if (shadow) {

                        RGraph.NoShadow(this);

                        for (k=0; k<redrawCoords.length; ++k) {
                            this.context.strokeStyle = strokeStyle;
                            this.context.fillStyle = redrawCoords[k][4];
                            this.context.strokeRect(redrawCoords[k][0], redrawCoords[k][1], redrawCoords[k][2], redrawCoords[k][3]);
                            this.context.fillRect(redrawCoords[k][0], redrawCoords[k][1], redrawCoords[k][2], redrawCoords[k][3]);

                            this.context.stroke();
                            this.context.fill();
                        }
                        
                        // Reset the redraw coords to be empty
                        redrawCoords = [];
                    }
                /**
                * Grouped bar
                */
                } else if (typeof(this.data[i]) == 'object' && this.Get('chart.grouping') == 'grouped') {

                    var redrawCoords = [];
                    this.context.lineWidth = this.Get('chart.linewidth');

                    for (j=0; j<this.data[i].length; ++j) {

                        // Set the fill and stroke colors
                        this.context.strokeStyle = strokeStyle;
                        this.context.fillStyle   = colors[j];
                        
                        /**
                        * Sequential colors
                        */
                        if (this.Get('chart.colors.sequential') && colors[sequentialColorIndex]) {
                            this.context.fillStyle = colors[sequentialColorIndex++];
                        } else if (this.Get('chart.colors.sequential')) {
                            this.context.fillStyle = colors[sequentialColorIndex - 1];
                        }

                        var individualBarWidth = (width - (2 * hmargin)) / this.data[i].length;
                        var height = ((this.data[i][j] + (this.data[i][j] < 0 ? this.min : (-1 * this.min) )) / (this.max - this.min) ) * (this.canvas.height - this.gutterTop - this.gutterBottom );
                    
                        /**
                        * Check for a negative bar width
                        */
                        if (individualBarWidth < 0) {
                            alert('[RGRAPH] Warning: you have a negative bar width. This may be caused by the chart.hmargin being too high or the width of the canvas not being sufficient.');
                        }

                        // If the X axis pos is in the center, we need to half the  height
                        if (xaxispos == 'center') {
                            height /= 2;
                        }

                        var startX = x + hmargin + (j * individualBarWidth);

                        /**
                        * Determine the start positioning for the bar
                        */
                        if (xaxispos == 'top') {
                            var startY = this.Get('chart.gutter.top');
                            var height = Math.abs(height);

                        } else if (xaxispos == 'center') {
                            var startY = this.gutterTop + (this.grapharea / 2) - height;

                        } else {
                            var startY = this.canvas.height - this.gutterBottom - height;
                            var height = Math.abs(height);
                        }

                        /**
                        * Draw MSIE shadow
                        */
                        if (RGraph.isOld() && shadow) {
                            this.DrawIEShadow([startX, startY, individualBarWidth, height]);
                        }

                        this.context.strokeRect(startX, startY, individualBarWidth, height);
                        this.context.fillRect(startX, startY, individualBarWidth, height);
                        y += height;



                        /**
                        * Grouped 3D effect
                        */
                        if (variant == '3d') {
                            var prevFillStyle = this.context.fillStyle;
                            var prevStrokeStyle = this.context.strokeStyle;
                            
                            // Draw the top side
                            this.context.beginPath();
                                this.context.moveTo(startX, startY);
                                this.context.lineTo(startX + 10, startY - 5);
                                this.context.lineTo(startX + 10 + individualBarWidth, startY - 5);
                                this.context.lineTo(startX + individualBarWidth, startY);
                            this.context.closePath();
                            
                            this.context.fill();
                            this.context.stroke();
                            
                            // Draw the side section
                            this.context.beginPath();
                                this.context.moveTo(startX + individualBarWidth, startY);
                                this.context.lineTo(startX + individualBarWidth + 10, startY - 5);
                                this.context.lineTo(startX + individualBarWidth + 10, startY - 5 + height);
                                this.context.lineTo(startX + individualBarWidth , startY + height);
                            this.context.closePath();
                            
                            this.context.fill();
                            this.context.stroke();


                            // Draw the darker top side
                            this.context.fillStyle = 'rgba(255,255,255,0.3)';
                            this.context.beginPath();
                                this.context.moveTo(startX, startY);
                                this.context.lineTo(startX + 10, startY - 5);
                                this.context.lineTo(startX + 10 + individualBarWidth, startY - 5);
                                this.context.lineTo(startX + individualBarWidth, startY);
                            this.context.closePath();
                            
                            this.context.fill();
                            this.context.stroke();
                            
                            // Draw the darker side section
                            this.context.fillStyle = 'rgba(0,0,0,0.4)';
                            this.context.beginPath();
                                this.context.moveTo(startX + individualBarWidth, startY);
                                this.context.lineTo(startX + individualBarWidth + 10, startY - 5);
                                this.context.lineTo(startX + individualBarWidth + 10, startY - 5 + height);
                                this.context.lineTo(startX + individualBarWidth , startY + height);
                            this.context.closePath();
                            
                            this.context.fill();
                            this.context.stroke();

                            this.context.strokeStyle = prevStrokeStyle;
                            this.context.fillStyle   = prevFillStyle;
                        }
                        
                        if (height < 0) {
                            height = Math.abs(height);
                            startY = startY - height;
                        }

                        this.coords.push([startX, startY, individualBarWidth, height]);

                        // Facilitate shadows going to the left
                        if (this.Get('chart.shadow')) {
                            redrawCoords.push([startX, startY, individualBarWidth, height, this.context.fillStyle]);
                        }
// This bit draws the text labels that appear above the bars if requested
if (this.Get('chart.labels.above')) {

    this.context.strokeStyle = 'rgba(0,0,0,0)';

    // Turn off any shadow
    if (shadow) {
        RGraph.NoShadow(this);
    }

    var yPos = y - 3;

    // Account for negative bars
    if (this.data[i][j] < 0) {
        yPos += height + 6 + (this.Get('chart.text.size') - 4);
    }

    this.context.fillStyle = this.Get('chart.text.color');
    RGraph.Text(this.context,
                this.Get('chart.text.font'),
                typeof(this.Get('chart.labels.above.size')) == 'number' ? this.Get('chart.labels.above.size') : this.Get('chart.text.size') - 3,startX + (individualBarWidth / 2),
                startY - 2 - (this.Get('chart.variant') == '3d' ? 5 : 0),
                RGraph.number_format(this, this.data[i][j].toFixed(this.Get('chart.labels.above.decimals'))),
                null,
                this.Get('chart.labels.above.angle') ? (this.Get('chart.labels.above.angle') > 0 ? 'right' : 'left') : 'center',
                null,
                this.Get('chart.labels.above.angle'));
  
    // Turn any shadow back on
    if (shadow) {
        this.context.shadowColor   = shadowColor;
        this.context.shadowBlur    = shadowBlur;
        this.context.shadowOffsetX = shadowOffsetX;
        this.context.shadowOffsetY = shadowOffsetY;
    }
}
                    }
                    
                    /**
                    * Redraw the bar if shadows are going to the left
                    */
                    if (redrawCoords.length) {

                        RGraph.NoShadow(this);
                        
                        this.context.lineWidth = this.Get('chart.linewidth');

                        this.context.beginPath();
                            for (var j=0; j<redrawCoords.length; ++j) {

                                this.context.fillStyle   = redrawCoords[j][4];
                                this.context.strokeStyle = this.Get('chart.strokecolor');

                                this.context.fillRect(redrawCoords[j][0], redrawCoords[j][1], redrawCoords[j][2], redrawCoords[j][3]);
                                this.context.strokeRect(redrawCoords[j][0], redrawCoords[j][1], redrawCoords[j][2], redrawCoords[j][3]);
                            }
                        this.context.fill();
                        this.context.stroke();

                        redrawCoords = [];
                    }
                }

            this.context.closePath();
        }

        /**
        * Turn off any shadow
        */
        RGraph.NoShadow(this);
    }



    /**
    * Draws the labels for the graph
    */
    RGraph.Bar.prototype.DrawLabels = function ()
    {
        var context    = this.context;
        var text_angle = this.Get('chart.text.angle');
        var text_size  = this.Get('chart.text.size');
        var labels     = this.Get('chart.labels');


        // Draw the Y axis labels:
        if (this.Get('chart.ylabels')) {
            this.Drawlabels_top();
            this.Drawlabels_center();
            this.Drawlabels_bottom();
        }

        /**
        * The X axis labels
        */
        if (typeof(labels) == 'object' && labels) {

            var yOffset = 13 + Number(this.Get('chart.xlabels.offset'));

            /**
            * Text angle
            */
            var angle  = 0;
            var halign = 'center';

            if (text_angle > 0) {
                angle  = -1 * text_angle;
                halign   = 'right';
                yOffset -= 5;
                
                if (this.Get('chart.xaxispos') == 'top') {
                    halign   = 'left';
                    yOffset += 5;
                }
            }

            // Draw the X axis labels
            context.fillStyle = this.Get('chart.text.color');
            
            // How wide is each bar
            var barWidth = (this.canvas.width - this.gutterRight - this.gutterLeft) / labels.length;
            
            // Reset the xTickGap
            xTickGap = (this.canvas.width - this.gutterRight - this.gutterLeft) / labels.length

            // Draw the X tickmarks
            var i=0;
            var font = this.Get('chart.text.font');

            for (x=this.gutterLeft + (xTickGap / 2); x<=this.canvas.width - this.gutterRight; x+=xTickGap) {
                RGraph.Text(context, font,
                                      text_size,
                                      x + (this.Get('chart.text.angle') == 90 ? 0 : 0),
                                      this.Get('chart.xaxispos') == 'top' ? this.gutterTop - yOffset + text_size  - 1: (this.canvas.height - this.gutterBottom) + yOffset,
                                      String(labels[i++]),
                                      (this.Get('chart.text.angle') == 90 ? 'center' : null),
                                      halign,
                                      null,
                                      angle);
            }
        }
    }

    /**
    * Draws the X axis at the top
    */
    RGraph.Bar.prototype.Drawlabels_top = function ()
    {
        this.context.beginPath();
        this.context.fillStyle = this.Get('chart.text.color');
        this.context.strokeStyle = 'black';

        if (this.Get('chart.xaxispos') == 'top') {

            var context    = this.context;
            var interval   = (this.grapharea * (1/5) );
            var text_size  = this.Get('chart.text.size');
            var units_pre  = this.Get('chart.units.pre');
            var units_post = this.Get('chart.units.post');
            var align      = this.Get('chart.yaxispos') == 'left' ? 'right' : 'left';
            var font       = this.Get('chart.text.font');
            var numYLabels = this.Get('chart.ylabels.count');

            if (this.Get('chart.ylabels.inside') == true) {
                var xpos  = this.Get('chart.yaxispos') == 'left' ? this.gutterLeft + 5 : this.canvas.width - this.gutterRight - 5;
                var align = this.Get('chart.yaxispos') == 'left' ? 'left' : 'right';
                var boxed = true;
            } else {
                var xpos  = this.Get('chart.yaxispos') == 'left' ? this.gutterLeft - 5 : this.canvas.width - this.gutterRight + 5;
                var boxed = false;
            }
            
            /**
            * Draw specific Y labels here so that the local variables can be reused
            */
            if (typeof(this.Get('chart.ylabels.specific')) == 'object' && this.Get('chart.ylabels.specific')) {
                
                var labels = RGraph.array_reverse(this.Get('chart.ylabels.specific'));
                var grapharea = this.canvas.height - this.gutterTop - this.gutterBottom;

                for (var i=0; i<labels.length; ++i) {
                    
                    var y = this.gutterTop + (grapharea * (i / labels.length)) + (grapharea / labels.length);
                    
                    RGraph.Text(context, font, text_size, xpos, y, labels[i], 'center', align, boxed);
                }

                return;
            }
            
            // One label
            if (numYLabels == 1) {
                RGraph.Text(context, font, text_size, xpos,this.canvas.height - this.gutterBottom,'-' + RGraph.number_format(this,Number(this.scale[4]).toFixed((this.Get('chart.scale.decimals'))),units_pre, units_post), 'center', align, boxed);
            }

            // 3 or 5 labels
            if (numYLabels == 3 || numYLabels == 5) {
                
                RGraph.Text(context, font, text_size, xpos, this.gutterTop + this.halfTextHeight + interval, '-' + RGraph.number_format(this, this.scale[0], units_pre, units_post), null, align, boxed);
    
                // 5 labels
                if (numYLabels == 5) {
                    RGraph.Text(context, font, text_size, xpos, (1*interval) + this.gutterTop + this.halfTextHeight + interval, '-' + RGraph.number_format(this, this.scale[1], units_pre, units_post), null, align, boxed);
                    RGraph.Text(context, font, text_size, xpos, (3*interval) + this.gutterTop + this.halfTextHeight + interval, '-' + RGraph.number_format(this, this.scale[3], units_pre, units_post), null, align, boxed);
                }
                
                // 3 labels
                if (numYLabels == 3 || numYLabels == 5) {
                    RGraph.Text(context, font, text_size, xpos, (2*interval) + this.gutterTop + this.halfTextHeight + interval, '-' + RGraph.number_format(this, this.scale[2], units_pre, units_post), null, align, boxed);
                    RGraph.Text(context, font, text_size, xpos, (4*interval) + this.gutterTop + this.halfTextHeight + interval, '-' + RGraph.number_format(this, this.scale[4], units_pre, units_post), null, align, boxed);
                }
            }
            
            // 10 Y labels
            if (numYLabels == 10) {

                interval = (this.grapharea / numYLabels );

                for (var i=10; i>0; --i) {
                    RGraph.Text(context, font, text_size, xpos,this.gutterTop + ((this.grapharea / numYLabels) * i),'-' + RGraph.number_format(this,((this.scale[4] / numYLabels) * i).toFixed((this.Get('chart.scale.decimals'))), units_pre, units_post), 'center', align, boxed);
                }
            }

            /**
            * Show the minimum value if its not zero
            */
            if (this.Get('chart.ymin') != 0) {
                RGraph.Text(context,
                            font,
                            text_size,
                            xpos,
                            this.gutterTop,
                            '-' + RGraph.number_format(this,(this.min.toFixed((this.Get('chart.scale.decimals')))), units_pre, units_post),
                            'center',
                            align,
                            boxed);
            }

        }
        
        this.context.fill();
        this.context.stroke();
    }

    /**
    * Draws the X axis in the middle
    */
    RGraph.Bar.prototype.Drawlabels_center = function ()
    {
        var font       = this.Get('chart.text.font');
        var numYLabels = this.Get('chart.ylabels.count');

        this.context.fillStyle = this.Get('chart.text.color');

        if (this.Get('chart.xaxispos') == 'center') {

            /**
            * Draw the top labels
            */
            var interval   = (this.grapharea * (1/10) );
            var text_size  = this.Get('chart.text.size');
            var units_pre  = this.Get('chart.units.pre');
            var units_post = this.Get('chart.units.post');
            var context = this.context;
            var align   = '';
            var xpos    = 0;
            var boxed   = false;

            this.context.fillStyle = this.Get('chart.text.color');
            this.context.strokeStyle = 'black';

            if (this.Get('chart.ylabels.inside') == true) {
                var xpos  = this.Get('chart.yaxispos') == 'left' ? this.gutterLeft + 5 : this.canvas.width - this.gutterRight - 5;
                var align = this.Get('chart.yaxispos') == 'left' ? 'left' : 'right';
                var boxed = true;
            } else {
                var xpos  = this.Get('chart.yaxispos') == 'left' ? this.gutterLeft - 5 : this.canvas.width - this.gutterRight + 5;
                var align = this.Get('chart.yaxispos') == 'left' ? 'right' : 'left';
                var boxed = false;
            }












            /**
            * Draw specific Y labels here so that the local variables can be reused
            */
            if (typeof(this.Get('chart.ylabels.specific')) == 'object' && this.Get('chart.ylabels.specific')) {

                var labels = this.Get('chart.ylabels.specific');
                var grapharea = this.canvas.height - this.gutterTop - this.gutterBottom;

                // Draw the top halves labels
                for (var i=0; i<labels.length; ++i) {

                    var y = this.gutterTop + ((grapharea / 2) / labels.length) * i;
                    
                    RGraph.Text(context, font, text_size, xpos, y, String(labels[i]), 'center', align, boxed);
                }

                // Draw the bottom halves labels
                for (var i=labels.length-1; i>=0; --i) {
                    var y = this.gutterTop  + (grapharea * ( (i+1) / (labels.length * 2) )) + (grapharea / 2);

                    RGraph.Text(context, font, text_size, xpos, y, labels[labels.length - i - 1], 'center', align, boxed);
                }

                return;
            }












            if (numYLabels == 1 || numYLabels == 3 || numYLabels == 5) {
                
                RGraph.Text(context, font, text_size, xpos, this.gutterTop + this.halfTextHeight, RGraph.number_format(this, this.scale[4], units_pre, units_post), null, align, boxed);
    
                if (numYLabels == 5) {
                    RGraph.Text(context, font, text_size, xpos, (1*interval) + this.gutterTop + this.halfTextHeight, RGraph.number_format(this, this.scale[3], units_pre, units_post), null, align, boxed);
                    RGraph.Text(context, font, text_size, xpos, (3*interval) + this.gutterTop + this.halfTextHeight, RGraph.number_format(this, this.scale[1], units_pre, units_post), null, align, boxed);
                }
                
                if (numYLabels == 3 || numYLabels == 5) {
                    RGraph.Text(context, font, text_size, xpos, (4*interval) + this.gutterTop + this.halfTextHeight, RGraph.number_format(this, this.scale[0], units_pre, units_post), null, align, boxed);
                    RGraph.Text(context, font, text_size, xpos, (2*interval) + this.gutterTop + this.halfTextHeight, RGraph.number_format(this, this.scale[2], units_pre, units_post), null, align, boxed);
                }
            } else if (numYLabels == 10) {
                // 10Y labels
                interval = (this.grapharea / numYLabels) / 2;
            
                for (var i=0; i<numYLabels; ++i) {
                    RGraph.Text(context, font, text_size, xpos,this.gutterTop + ((this.grapharea / (numYLabels * 2)) * i),RGraph.number_format(this, ((this.scale[4] / numYLabels) * (numYLabels - i)).toFixed((this.Get('chart.scale.decimals'))), units_pre, units_post), 'center', align, boxed);
                }
            }
            ///////////////////////////////////////////////////////////////////////////////////

            /**
            * Draw the bottom (X axis) labels
            */
            var interval = (this.grapharea) / 10;

            if (numYLabels == 1 || numYLabels == 3 || numYLabels == 5) {
                if (numYLabels == 3 || numYLabels == 5) {
                    RGraph.Text(context, font, text_size, xpos, (this.grapharea + this.gutterTop + this.halfTextHeight) - (4 * interval), '-' + RGraph.number_format(this, this.scale[0], units_pre, units_post), null, align, boxed);
                    RGraph.Text(context, font, text_size, xpos, (this.grapharea + this.gutterTop + this.halfTextHeight) - (2 * interval), '-' + RGraph.number_format(this, this.scale[2], units_pre, units_post), null, align, boxed);
                }
    
                if (numYLabels == 5) {
                    RGraph.Text(context, font, text_size, xpos, (this.grapharea + this.gutterTop + this.halfTextHeight) - (3 * interval), '-' + RGraph.number_format(this, this.scale[1], units_pre, units_post), null, align, boxed);
                    RGraph.Text(context, font, text_size, xpos, (this.grapharea + this.gutterTop + this.halfTextHeight) - interval, '-' + RGraph.number_format(this, this.scale[3], units_pre, units_post), null, align, boxed);
                }
    
                RGraph.Text(context, font, text_size, xpos,  this.grapharea + this.gutterTop + this.halfTextHeight, '-' + RGraph.number_format(this, this.scale[4], units_pre, units_post), null, align, boxed);

            } else if (numYLabels == 10) {

                // Arbitrary number of Y labels
                interval = (this.grapharea / numYLabels) / 2;
            
                for (var i=0; i<numYLabels; ++i) {
                    RGraph.Text(context, font, text_size, xpos,this.gutterTop + (this.grapharea / 2) + ((this.grapharea / (numYLabels * 2)) * i) + (this.grapharea / (numYLabels * 2)),RGraph.number_format(this, ((this.scale[4] / numYLabels) * (i+1)).toFixed((this.Get('chart.scale.decimals'))), '-' + units_pre, units_post),'center', align, boxed);
                }
            }



            /**
            * Show the minimum value if its not zero
            */
            if (this.Get('chart.ymin') != 0) {
                RGraph.Text(context,
                            font,
                            text_size,
                            xpos,
                            this.gutterTop + (this.grapharea / 2),
                            RGraph.number_format(this,(this.min.toFixed((this.Get('chart.scale.decimals')))), units_pre, units_post),
                            'center',
                            align,
                            boxed);
            }
        }
    }

    /**
    * Draws the X axdis at the bottom (the default)
    */
    RGraph.Bar.prototype.Drawlabels_bottom = function ()
    {
        this.context.beginPath();
        this.context.fillStyle = this.Get('chart.text.color');
        this.context.strokeStyle = 'black';

        if (this.Get('chart.xaxispos') != 'center' && this.Get('chart.xaxispos') != 'top') {
            
            var interval   = (this.grapharea * (1/5) );
            var text_size  = this.Get('chart.text.size');
            var units_pre  = this.Get('chart.units.pre');
            var units_post = this.Get('chart.units.post');
            var context    = this.context;
            var align      = this.Get('chart.yaxispos') == 'left' ? 'right' : 'left';
            var font       = this.Get('chart.text.font');
            var numYLabels = this.Get('chart.ylabels.count');

            if (this.Get('chart.ylabels.inside') == true) {
                var xpos  = this.Get('chart.yaxispos') == 'left' ? this.gutterLeft + 5 : this.canvas.width - this.gutterRight - 5;
                var align = this.Get('chart.yaxispos') == 'left' ? 'left' : 'right';
                var boxed = true;
            } else {
                var xpos  = this.Get('chart.yaxispos') == 'left' ? this.gutterLeft - 5 : this.canvas.width - this.gutterRight + 5;
                var boxed = false;
            }
            
            /**
            * Draw specific Y labels here so that the local variables can be reused
            */
            if (this.Get('chart.ylabels.specific') && typeof(this.Get('chart.ylabels.specific')) == 'object') {
                
                var labels = this.Get('chart.ylabels.specific');
                var grapharea = this.canvas.height - this.gutterTop - this.gutterBottom;

                for (var i=0; i<labels.length; ++i) {
                    var y = this.gutterTop + (grapharea * (i / labels.length));
                    
                    RGraph.Text(context, font, text_size, xpos, y, labels[i], 'center', align, boxed);
                }

                return;
            }

            // 1/3/5 label
            if (numYLabels == 1 || numYLabels == 3 || numYLabels == 5) {

                RGraph.Text(context, font, text_size, xpos, this.gutterTop + this.halfTextHeight, RGraph.number_format(this, this.scale[4], units_pre, units_post), null, align, boxed);

                // 5 labels
                if (numYLabels == 5) {
                    RGraph.Text(context, font, text_size, xpos, (1*interval) + this.gutterTop + this.halfTextHeight, RGraph.number_format(this, this.scale[3], units_pre, units_post), null, align, boxed);
                    RGraph.Text(context, font, text_size, xpos, (3*interval) + this.gutterTop + this.halfTextHeight, RGraph.number_format(this, this.scale[1], units_pre, units_post), null, align, boxed);
                }
                
                // 3 labels
                if (numYLabels == 3 || numYLabels == 5) {
                    RGraph.Text(context, font, text_size, xpos, (2*interval) + this.gutterTop + this.halfTextHeight, RGraph.number_format(this, this.scale[2], units_pre, units_post), null, align, boxed);
                    RGraph.Text(context, font, text_size, xpos, (4*interval) + this.gutterTop + this.halfTextHeight, RGraph.number_format(this, this.scale[0], units_pre, units_post), null, align, boxed);
                }
            }
            
            // 10 Y labels
            if (numYLabels == 10) {

                interval   = (this.grapharea / numYLabels );

                for (var i=0; i<numYLabels; ++i) {
                    RGraph.Text(context, font, text_size, xpos, this.gutterTop + ((this.grapharea / numYLabels) * i), RGraph.number_format(this,((this.scale[4] / numYLabels) * (numYLabels - i)).toFixed((this.Get('chart.scale.decimals'))), units_pre, units_post), 'center', align, boxed);
                }
            }
            
            /**
            * Show the minimum value if its not zero
            */
            if (this.Get('chart.ymin') != 0) {
                RGraph.Text(context,
                            font,
                            text_size,
                            xpos,
                            this.canvas.height - this.gutterBottom,
                            RGraph.number_format(this,(this.min.toFixed((this.Get('chart.scale.decimals')))), units_pre, units_post),
                            'center',
                            align,
                            boxed);
            }
        }
        
        this.context.fill();
        this.context.stroke();
    }


    /**
    * This function is used by MSIE only to manually draw the shadow
    * 
    * @param array coords The coords for the bar
    */
    RGraph.Bar.prototype.DrawIEShadow = function (coords)
    {
        var prevFillStyle = this.context.fillStyle;
        var offsetx       = this.Get('chart.shadow.offsetx');
        var offsety       = this.Get('chart.shadow.offsety');
        
        this.context.lineWidth = this.Get('chart.linewidth');
        this.context.fillStyle = this.Get('chart.shadow.color');
        this.context.beginPath();
        
        // Draw shadow here
        this.context.fillRect(coords[0] + offsetx, coords[1] + offsety, coords[2], coords[3]);

        this.context.fill();
        
        // Change the fillstyle back to what it was
        this.context.fillStyle = prevFillStyle;
    }


    /**
    * Not used by the class during creating the graph, but is used by event handlers
    * to get the coordinates (if any) of the selected bar
    * 
    * @param object e The event object
    * @param object   OPTIONAL You can pass in the bar object instead of the
    *                          function using "this"
    */
    RGraph.Bar.prototype.getShape = 
    RGraph.Bar.prototype.getBar = function (e)
    {
        // This facilitates you being able to pass in the bar object as a parameter instead of
        // the function getting it from itself
        var obj = arguments[1] ? arguments[1] : this;

        var mouseXY = RGraph.getMouseXY(e);
        var mouseX  = mouseXY[0];
        var mouseY  = mouseXY[1];  

        for (var i=0; i<this.coords.length; i++) {

            var left   = obj.coords[i][0];
            var top    = obj.coords[i][1];
            var width  = obj.coords[i][2];
            var height = obj.coords[i][3];

            if (mouseX >= left && mouseX <= (left + width) && mouseY >= top && mouseY <= (top + height)) {

                if (this.Get('chart.tooltips')) {
                    var tooltip = RGraph.parseTooltipText ? RGraph.parseTooltipText(this.Get('chart.tooltips'), i) : this.Get('chart.tooltips')[i];
                }

                // Work out the dataset
                var dataset = 0;
                var idx = i;
                
                while (idx >=  (typeof(this.data[dataset]) == 'object' ? this.data[dataset].length : 1)) {
                    if (typeof(this.data[dataset]) == 'number') {
                        idx -= 1;
                    } else {
                        idx -= this.data[dataset].length;
                    }
                    
                    dataset++;
                }
                
                if (typeof(this.data[dataset]) == 'number') {
                    idx = null;
                }


                return {
                        0: obj, 1: left, 2: top, 3: width, 4: height, 5: i,
                        'object': obj, 'x': left, 'y': top, 'width': width, 'height': height, 'index': i, 'tooltip': tooltip, 'index_adjusted': idx, 'dataset': dataset
                       };
            }
        }
        
        return null;
    }


    /**
    * This retrives the bar based on the X coordinate only.
    * 
    * @param object e The event object
    * @param object   OPTIONAL You can pass in the bar object instead of the
    *                          function using "this"
    */
    RGraph.Bar.prototype.getShapeByX = function (e)
    {
        var canvas      = e.target;
        var mouseCoords = RGraph.getMouseXY(e);


        // This facilitates you being able to pass in the bar object as a parameter instead of
        // the function getting it from itself
        var obj = arguments[1] ? arguments[1] : this;


        /**
        * Loop through the bars determining if the mouse is over a bar
        */
        for (var i=0; i<obj.coords.length; i++) {

            var mouseX = mouseCoords[0];
            var mouseY = mouseCoords[1];    
            var left   = obj.coords[i][0];
            var top    = obj.coords[i][1];
            var width  = obj.coords[i][2];
            var height = obj.coords[i][3];

            if (mouseX >= left && mouseX <= (left + width)) {
            
                if (this.Get('chart.tooltips')) {
                    var tooltip = RGraph.parseTooltipText ? RGraph.parseTooltipText(this.Get('chart.tooltips'), i) : this.Get('chart.tooltips')[i];
                }

                

                return {
                        0: obj, 1: left, 2: top, 3: width, 4: height, 5: i,
                        'object': obj, 'x': left, 'y': top, 'width': width, 'height': height, 'index': i, 'tooltip': tooltip
                       };
            }
        }
        
        return null;
    }


    /**
    * When you click on the chart, this method can return the Y value at that point. It works for any point on the
    * chart (that is inside the gutters) - not just points within the Bars.
    * 
    * EITHER:
    * 
    * @param object arg The event object
    * 
    * OR:
    * 
    * @param object arg A two element array containing the X and Y coordinates
    */
    RGraph.Bar.prototype.getValue = function (arg)
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
        
        if (this.Get('chart.xaxispos') == 'center') {
            var value = (((this.grapharea / 2) - (mouseY - this.Get('chart.gutter.top'))) / this.grapharea) * (this.max - this.min)
            value *= 2;
            
            if (value >= 0) {
                value += this.min;
            } else {
                value -= this.min;
            }

        } else if (this.Get('chart.xaxispos') == 'top') {
            var value = ((this.grapharea - (mouseY - this.Get('chart.gutter.top'))) / this.grapharea) * (this.max - this.min)
            value = this.max - value;
            value = Math.abs(value) * -1;
        } else {
            var value = ((this.grapharea - (mouseY - this.Get('chart.gutter.top'))) / this.grapharea) * (this.max - this.min)
            value += this.min;
        }

        return value;
    }


    /**
    * This function can be used when the canvas is clicked on (or similar - depending on the event)
    * to retrieve the relevant Y coordinate for a particular value.
    * 
    * @param int value The value to get the Y coordinate for
    */
    RGraph.Bar.prototype.getYCoord = function (value)
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

        if (xaxispos == 'top') {
        
            // Account for negative numbers
            if (value < 0) {
                value = Math.abs(value);
            }

            y = ((value - this.min) / (this.max - this.min)) * this.grapharea;
            y = y + this.gutterTop

        } else if (xaxispos == 'center') {

            y = ((value - this.min) / (this.max - this.min)) * (this.grapharea / 2);
            y = (this.grapharea / 2) - y;
            y += this.gutterTop;

        } else {

            if (value < this.min) {
                value = this.min;
            }

            y = ((value - this.min) / (this.max - this.min)) * this.grapharea;

            y = this.canvas.height - this.gutterBottom - y;
        }
        
        return y;
    }



    /**
    * Each object type has its own Highlight() function which highlights the appropriate shape
    * 
    * @param object shape The shape to highlight
    */
    RGraph.Bar.prototype.Highlight = function (shape)
    {
        // Add the new highlight
        RGraph.Highlight.Rect(this, shape);
    }



    /**
    * The getObjectByXY() worker method
    */
    RGraph.Bar.prototype.getObjectByXY = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e);

        if (
               mouseXY[0] >= this.Get('chart.gutter.left')
            && mouseXY[0] <= (this.canvas.width - this.Get('chart.gutter.right'))
            && mouseXY[1] >= this.Get('chart.gutter.top')
            && mouseXY[1] <= (this.canvas.height - this.Get('chart.gutter.bottom'))
            ) {

            return this;
        }
    }



    /**
    * This method handles the adjusting calculation for when the mouse is moved
    * 
    * @param object e The event object
    */
    RGraph.Bar.prototype.Adjusting_mousemove = function (e)
    {
        /**
        * Handle adjusting for the Bar
        */
        if (RGraph.Registry.Get('chart.adjusting') && RGraph.Registry.Get('chart.adjusting').uid == this.uid) {

            // Rounding the value to the given number of decimals make the chart step
            var value   = Number(this.getValue(e));//.toFixed(this.Get('chart.scale.decimals'));
            var shape   = this.getShapeByX(e);

            if (shape) {
                
                RGraph.Registry.Set('chart.adjusting.shape', shape);

                this.data[shape['index']] = Number(value);
                RGraph.RedrawCanvas(e.target);
                
                RGraph.FireCustomEvent(this, 'onadjust');
            }
        }
    }


    /*********************************************************************************************************
    * This is the combined bar and Line class which makes creating bar/line combo charts a little bit easier *
    /*********************************************************************************************************/



    RGraph.CombinedChart = function ()
    {
        /**
        * Create a default empty array for the objects
        */
        this.objects = [];

        for (var i=0; i<arguments.length; ++i) {
            this.objects[i] = arguments[i];
        
            /**
            * Set the Line chart gutters to match the Bar chart gutters
            */
            this.objects[i].Set('chart.gutter.left',  this.objects[0].Get('chart.gutter.left'));
            this.objects[i].Set('chart.gutter.right',  this.objects[0].Get('chart.gutter.right'));
            this.objects[i].Set('chart.gutter.top',    this.objects[0].Get('chart.gutter.top'));
            this.objects[i].Set('chart.gutter.bottom', this.objects[0].Get('chart.gutter.bottom'));
            
            if (this.objects[i].type == 'line') {
        
                /**
                * Set the line chart hmargin
                */
                this.objects[i].Set('chart.hmargin', ((this.objects[0].canvas.width - this.objects[0].Get('chart.gutter.right') - this.objects[0].Get('chart.gutter.left')) / this.objects[0].data.length) / 2 );
                
                
                /**
                * No labels, axes or grid on the Line chart
                */
                this.objects[i].Set('chart.noaxes', true);
                this.objects[i].Set('chart.background.grid', false);
                this.objects[i].Set('chart.ylabels', false);
            }
            
            /**
            * Resizing
            */
            if (this.objects[i].Get('chart.resizable')) {
                var resizable = true;
            }
        }
        
        /**
        * Resizing
        */
        if (resizable) {
            /**
            * This recalculates the Line chart hmargin when the chart is resized
            */
            function myOnresizebeforedraw (obj)
            {
                var gutterLeft = obj.Get('chart.gutter.left');
                var gutterRight = obj.Get('chart.gutter.right');
            
                obj.Set('chart.hmargin', (obj.canvas.width - gutterLeft - gutterRight) / (obj.original_data[0].length * 2));
            }

            RGraph.AddCustomEventListener(RGraph.ObjectRegistry.getFirstObjectByType(this.objects[0].canvas.id, 'line'), 'onresizebeforedraw', myOnresizebeforedraw);
        }
    }

    
    RGraph.CombinedChart.prototype.Draw = function ()
    {
        for (var i=0; i<this.objects.length; ++i) {
            if (typeof(arguments[i]) == 'function') {
                arguments[i](this.objects[i]);
            } else {
                this.objects[i].Draw();
            }
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
    RGraph.Bar.prototype.positionTooltip = function (obj, x, y, tooltip, idx)
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
        tooltip.style.top  = canvasXY[1] + coordY + (coordH / 2) - height - 7 + 'px';
        
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
        if ((canvasXY[0] + coordX + (coordW / 2) - (width / 2)) < 10) {
            tooltip.style.left = (canvasXY[0] + coordX - (width * 0.1)) + (coordW / 2) + 'px';
            img.style.left = ((width * 0.1) - 8.5) + 'px';

        // RIGHT edge
        } else if ((canvasXY[0] + coordX + (width / 2)) > document.body.offsetWidth) {
            tooltip.style.left = canvasXY[0] + coordX - (width * 0.9) + (coordW / 2) + 'px';
            img.style.left = ((width * 0.9) - 8.5) + 'px';

        // Default positioning - CENTERED
        } else {
            tooltip.style.left = (canvasXY[0] + coordX + (coordW / 2) - (width * 0.5)) + 'px';
            img.style.left = ((width * 0.5) - 8.5) + 'px';
        }
    }