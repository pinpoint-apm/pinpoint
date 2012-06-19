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
    * The traditional radar chart constructor
    * 
    * @param string id   The ID of the canvas
    * @param array  data An array of data to represent
    */
    RGraph.Radar = function (id, data)
    {
        this.id                = id;
        this.canvas            = document.getElementById(id);
        this.context           = this.canvas.getContext('2d');
        this.canvas.__object__ = this;
        this.type              = 'radar';
        this.coords            = [];
        this.isRGraph          = true;
        this.data              = [];
        this.max               = 0;
        this.original_data     = [];
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();

        for (var i=1; i<arguments.length; ++i) {
            this.original_data.push(RGraph.array_clone(arguments[i]));
            this.data.push(RGraph.array_clone(arguments[i]));
            this.max = Math.max(this.max, RGraph.array_max(arguments[i]));
        }

        /**
        * Compatibility with older browsers
        */
        RGraph.OldBrowserCompat(this.context);

        
        this.properties = {
            'chart.strokestyle':           '#aaa',
            'chart.gutter.left':           25,
            'chart.gutter.right':          25,
            'chart.gutter.top':            25,
            'chart.gutter.bottom':         25,
            'chart.linewidth':             1,
            'chart.colors':                ['rgba(255,0,0,0.5)', 'red', 'green', 'blue', 'pink', 'aqua','brown','orange','grey'],
            'chart.colors.alpha':          null,
            'chart.circle':                0,
            'chart.circle.fill':           'red',
            'chart.circle.stroke':         'black',
            'chart.labels':                [],
            'chart.labels.offsetx':        10,
            'chart.labels.offsety':        10,
            'chart.background.circles':    true,
            'chart.text.size':             10,
            'chart.text.font':             'Arial',
            'chart.text.color':            'black',
            'chart.title':                 '',
            'chart.title.background':      null,
            'chart.title.hpos':            null,
            'chart.title.vpos':            null,
            'chart.title.color':           'black',
            'chart.title.bold':             true,
            'chart.title.font':             null,
            'chart.linewidth':             1,
            
            'chart.key':                   null,
            'chart.key.background':        'white',
            'chart.key.shadow':            false,
            'chart.key.shadow.color':       '#666',
            'chart.key.shadow.blur':        3,
            'chart.key.shadow.offsetx':     2,
            'chart.key.shadow.offsety':     2,
            'chart.key.position':          'graph',
            'chart.key.halign':             'right',
            'chart.key.position.gutter.boxed': true,
            'chart.key.position.x':         null,
            'chart.key.position.y':         null,
            'chart.key.color.shape':        'square',
            'chart.key.rounded':            true,
            'chart.key.linewidth':          1,
            'chart.key.colors':             null,
            'chart.contextmenu':           null,
            'chart.annotatable':           false,
            'chart.annotate.color':        'black',
            'chart.zoom.factor':           1.5,
            'chart.zoom.fade.in':          true,
            'chart.zoom.fade.out':         true,
            'chart.zoom.hdir':             'right',
            'chart.zoom.vdir':             'down',
            'chart.zoom.frames':            25,
            'chart.zoom.delay':             16.666,
            'chart.zoom.shadow':           true,

            'chart.zoom.background':        true,
            'chart.zoom.action':            'zoom',
            'chart.tooltips.effect':        'fade',
            'chart.tooltips.event':         'onmousemove',
            'chart.tooltips.css.class':     'RGraph_tooltip',
            'chart.tooltips.highlight':     true,
            'chart.highlight.stroke':       'gray',
            'chart.highlight.fill':         'white',
            'chart.highlight.point.radius': 2,
            'chart.resizable':              false,
            'chart.resize.handle.adjust':   [0,0],
            'chart.resize.handle.background': null,
            'chart.labels.axes':            '',
            'chart.ymax':                   null,
            'chart.accumulative':           false,
            'chart.radius':                 null,
            'chart.events.click':           null,
            'chart.events.mousemove':       null,
            'chart.scale.decimals':         0,
            'chart.scale.point':            '.',
            'chart.scale.thousand':         ',',
            'chart.units.pre':              '',
            'chart.units.post':             '',
            'chart.tooltips':             null,
            'chart.tooltips.event':       'onmousemove',
            'chart.centerx':              null,
            'chart.centery':              null,
            'chart.radius':               null
        }
        
        // Must have at least 3 points
        for (var dataset=0; dataset<this.data.length; ++dataset) {
            if (this.data[dataset].length < 3) {
                alert('[RADAR] You must specify at least 3 data points');
                return;
            }
        }



        /**
        * Always register the object
        */
        RGraph.Register(this);
    }


    /**
    * A simple setter
    * 
    * @param string name  The name of the property to set
    * @param string value The value of the property
    */
    RGraph.Radar.prototype.Set = function (name, value)
    {
        if (name == 'chart.text.diameter') {
            name = 'chart.text.size';
        }

        this.properties[name] = value;

        /**
        * If the name is chart.color, set chart.colors too
        */
        if (name == 'chart.color') {
            this.properties['chart.colors'] = [value];
        }
    }


    /**
    * A simple hetter
    * 
    * @param string name  The name of the property to get
    */
    RGraph.Radar.prototype.Get = function (name)
    {
        if (name == 'chart.text.diameter') {
            name = 'chart.text.size';
        }

        return this.properties[name];
    }


    /**
    * The draw method which does all the brunt of the work
    */
    RGraph.Radar.prototype.Draw = function ()
    {
        /**
        * Fire the onbeforedraw event
        */
        RGraph.FireCustomEvent(this, 'onbeforedraw');


        // Reset the coords array to stop it growing
        this.coords = [];

        /**
        * Reset the data to the original_data
        */
        this.data = RGraph.array_clone(this.original_data);
        
        // Loop thru the data array if chart.accumulative is enable checking to see if all the
        // datasets have the same number of elements.
        if (this.Get('chart.accumulative')) {
            for (var i=0; i<this.data.length; ++i) {
                if (this.data[i].length != this.data[0].length) {
                    alert('[RADAR] Error! When the radar has chart.accumulative set to true all the datasets must have the same number of elements');
                }
            }
        }
        
        /**
        * This is new in May 2011 and facilitates indiviual gutter settings,
        * eg chart.gutter.left
        */
        this.gutterLeft   = this.Get('chart.gutter.left');
        this.gutterRight  = this.Get('chart.gutter.right');
        this.gutterTop    = this.Get('chart.gutter.top');
        this.gutterBottom = this.Get('chart.gutter.bottom');

        this.centerx  = ((this.canvas.width - this.gutterLeft - this.gutterRight) / 2) + this.gutterLeft;
        this.centery  = ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2) + this.gutterTop;
        this.radius   = Math.min(this.canvas.width - this.gutterLeft - this.gutterRight, this.canvas.height - this.gutterTop - this.gutterBottom) / 2;



        /**
        * Allow these to be set by hand
        */
        if (typeof(this.Get('chart.centerx')) == 'number') this.centerx = 2 * this.Get('chart.centerx');
        if (typeof(this.Get('chart.centery')) == 'number') this.centery = 2 * this.Get('chart.centery');
        if (typeof(this.Get('chart.radius')) == 'number') this.radius   = this.Get('chart.radius');



        // Work out the maximum value and the sum
        if (!this.Get('chart.ymax')) {

            // this.max is calculated in the constructor

            // Work out this.max again if the chart is (now) set to be accumulative
            if (this.Get('chart.accumulative')) {
                
                var accumulation = [];
                var len = this.original_data[0].length

                for (var i=1; i<this.original_data.length; ++i) {
                    if (this.original_data[i].length != len) {
                        alert('[RADAR] Error! Stacked Radar chart datasets must all be the same size!');
                    }
                    
                    for (var j=0; j<this.original_data[i].length; ++j) {
                        this.data[i][j] += this.data[i - 1][j];
                        this.max = Math.max(this.max, this.data[i][j]);
                    }
                }
            }

            this.scale = RGraph.getScale(this.max, this);
            this.max = this.scale[4];
        
        } else {
            var ymax = this.Get('chart.ymax');

            this.scale = [
                          ymax * 0.2,
                          ymax * 0.4,
                          ymax * 0.6,
                          ymax * 0.8,
                          ymax * 1
                         ];
            this.max = this.scale[4];
        }

        this.DrawBackground();
        this.DrawAxes();
        this.DrawCircle();
        this.DrawAxisLabels();

        
        this.DrawChart();
        this.DrawLabels();
        
        // Draw the title
        if (this.Get('chart.title')) {
            RGraph.DrawTitle(this, this.Get('chart.title'), this.gutterTop, null, this.Get('chart.title.diameter') ? this.Get('chart.title.diameter') : null)
        }

        // Draw the key if necessary
        // obj, key, colors
        if (this.Get('chart.key')) {
            RGraph.DrawKey(this, this.Get('chart.key'), this.Get('chart.colors'));
        }

        /**
        * Show the context menu
        */
        if (this.Get('chart.contextmenu')) {
            RGraph.ShowContext(this);
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
    * Draws the background circles
    */
    RGraph.Radar.prototype.DrawBackground = function ()
    {
        var color = '#ddd';

        /**
        * Draws the background circles
        */
        if (this.Get('chart.background.circles')) {

           this.context.strokeStyle = color;
           this.context.beginPath();

           for (var r=5; r<this.radius; r+=15) {

                this.context.moveTo(this.centerx, this.centery);
                this.context.arc(this.centerx, this.centery,r, 0, TWOPI, 0);
            }
            
            this.context.stroke();
        
        
            /**
            * Draw diagonals
            */
            this.context.strokeStyle = color;
            for (var i=0; i<360; i+=15) {
                this.context.beginPath();
                this.context.arc(this.centerx, this.centery, this.radius, (i / 360) * TWOPI, ((i+0.01) / 360) * TWOPI, 0); // The 0.01 avoids a bug in Chrome 6
                this.context.lineTo(this.centerx, this.centery);
                this.context.stroke();
            }
        }
    }


    /**
    * Draws the axes
    */
    RGraph.Radar.prototype.DrawAxes = function ()
    {
        this.context.strokeStyle = 'black';

        var halfsize = this.radius;

        this.context.beginPath();

        /**
        * The Y axis
        */
        this.context.moveTo(AA(this, this.centerx), this.centery + halfsize);
        this.context.lineTo(AA(this, this.centerx), this.centery - halfsize);
        

        // Draw the bits at either end of the Y axis
        this.context.moveTo(this.centerx - 5, AA(this, this.centery + halfsize));
        this.context.lineTo(this.centerx + 5, AA(this, this.centery + halfsize));
        this.context.moveTo(this.centerx - 5, AA(this, this.centery - halfsize));
        this.context.lineTo(this.centerx + 5, AA(this, this.centery - halfsize));
        
        // Draw Y axis tick marks
        for (var y=(this.centery - halfsize); y<(this.centery + halfsize); y+=15) {
            this.context.moveTo(this.centerx - 3, AA(this, y));
            this.context.lineTo(this.centerx + 3, AA(this, y));
        }

        /**
        * The X axis
        */
        this.context.moveTo(this.centerx - halfsize, AA(this, this.centery));
        this.context.lineTo(this.centerx + halfsize, AA(this, this.centery));

        // Draw the bits at the end of the X axis
        this.context.moveTo(AA(this, this.centerx - halfsize), this.centery - 5);
        this.context.lineTo(AA(this, this.centerx - halfsize), this.centery + 5);
        this.context.moveTo(AA(this, this.centerx + halfsize), this.centery - 5);
        this.context.lineTo(AA(this, this.centerx + halfsize), this.centery + 5);

        // Draw X axis tick marks
        for (var x=(this.centerx - halfsize); x<(this.centerx + halfsize); x+=15) {
            this.context.moveTo(AA(this, x), this.centery - 3);
            this.context.lineTo(AA(this, x), this.centery + 3);
        }

        /**
        * Finally draw it to the canvas
        */
        this.context.stroke();
    }


    /**
    * The function which actually draws the radar chart
    */
    RGraph.Radar.prototype.DrawChart = function ()
    {
        var alpha = this.Get('chart.colors.alpha');

        if (typeof(alpha) == 'number') {
            var oldAlpha = this.context.globalAlpha;
            this.context.globalAlpha = alpha;
        }
        
        var numDatasets = this.data.length;

        for (var dataset=0; dataset<this.data.length; ++dataset) {

            this.context.beginPath();

                var coords_dataset = [];
    
                for (var i=0; i<this.data[dataset].length; ++i) {
                    
                    var coords = this.GetCoordinates(dataset, i);

                    if (coords_dataset == null) {
                        coords_dataset = [];
                    }

                    coords_dataset.push(coords);
                    this.coords.push(coords);
                }

                /**
                * Now go through the coords and draw the chart itself
                */
                this.context.strokeStyle = this.Get('chart.strokestyle');
                this.context.fillStyle   = this.Get('chart.colors')[dataset];
                this.context.lineWidth   = this.Get('chart.linewidth');

                for (i=0; i<coords_dataset.length; ++i) {
                    if (i == 0) {
                        this.context.moveTo(coords_dataset[i][0], coords_dataset[i][1]);
                    } else {
                        this.context.lineTo(coords_dataset[i][0], coords_dataset[i][1]);
                    }
                }
                

                // If on the second or greater dataset, backtrack
                if (this.Get('chart.accumulative') && dataset > 0) {

                    // This goes back to the start coords of this particular dataset
                    this.context.lineTo(coords_dataset[0][0], coords_dataset[0][1]);
                    
                    //Now move down to the end point of the previous dataset
                    this.context.lineTo(last_coords[0][0], last_coords[0][1]);

                    for (var i=coords_dataset.length - 1; i>=0; --i) {
                        this.context.lineTo(last_coords[i][0], last_coords[i][1]);
                    }
                }
            
            // This is used by the next iteration of the loop
            var last_coords = coords_dataset;

            this.context.closePath();
    
            this.context.stroke();
            this.context.fill();
        }
        
        // Reset the globalAlpha
        if (typeof(alpha) == 'number') {
            this.context.globalAlpha = oldAlpha;
        }
    }

    /**
    * Gets the coordinates for a particular mark
    * 
    * @param  number i The index of the data (ie which one it is)
    * @return array    A two element array of the coordinates
    */
    RGraph.Radar.prototype.GetCoordinates = function (dataset, index)
    {
        // The number  of data points
        var len = this.data[dataset].length;

        // The magnitude of the data (NOT the x/y coords)
        var mag = (this.data[dataset][index] / this.max) * this.radius;

        /**
        * Get the angle
        */
        var angle = (TWOPI / len) * index; // In radians
        angle -= HALFPI;


        /**
        * Work out the X/Y coordinates
        */
        var x = Math.cos(angle) * mag;
        var y = Math.sin(angle) * mag;

        /**
        * Put the coordinate in the right quadrant
        */
        x = this.centerx + x;
        y = this.centery + y;
        
        return [x,y];
    }
    
    
    /**
    * This function adds the labels to the chart
    */
    RGraph.Radar.prototype.DrawLabels = function ()
    {
        var labels = this.Get('chart.labels');

        if (labels && labels.length > 0) {

            this.context.lineWidth = 1;
            this.context.strokeStyle = 'gray';
            this.context.fillStyle = this.Get('chart.text.color');
            
            var offsetx = this.Get('chart.labels.offsetx');
            var offsety = this.Get('chart.labels.offsety');
            var font    = this.Get('chart.text.font');
            var size    = this.Get('chart.text.size');
            var radius  = this.radius;
                

            for (var i=0; i<labels.length; ++i) {
                
                var angle = (TWOPI / this.Get('chart.labels').length) * i;
                    angle -= HALFPI;

                var x = this.centerx + (Math.cos(angle) * (radius + offsetx));
                var y = this.centery + (Math.sin(angle) * (radius + offsety));

                if (labels[i] && labels[i].length) {
                    RGraph.Text(this.context, font, size, x, y, labels[i], 'center', 'center', true, null, 'white');
                }
            }
        }
    }


    /**
    * Draws the circle. No arguments as it gets the information from the object properties.
    */
    RGraph.Radar.prototype.DrawCircle = function ()
    {
        var circle   = {};
        circle.limit = this.Get('chart.circle');
        circle.fill  = this.Get('chart.circle.fill');
        circle.stroke  = this.Get('chart.circle.stroke');

        if (circle.limit) {

            var r = (circle.limit / this.max) * this.radius;
            
            this.context.fillStyle = circle.fill;
            this.context.strokeStyle = circle.stroke;

            this.context.beginPath();
            this.context.arc(this.centerx, this.centery, r, 0, TWOPI, 0);
            this.context.fill();
            this.context.stroke();
        }
    }


    /**
    * Unsuprisingly, draws the labels
    */
    RGraph.Radar.prototype.DrawAxisLabels = function ()
    {
        /**
        * Draw specific axis labels
        */
        if (this.Get('chart.labels.specific')) {
            this.DrawSpecificAxisLabels();
            return;
        }

        this.context.lineWidth = 1;
        
        // Set the color to black
        this.context.fillStyle = 'black';
        this.context.strokeStyle = 'black';

        var r          = this.radius;
        var font_face  = this.Get('chart.text.font');
        var font_size  = this.Get('chart.text.size');
        var context    = this.context;
        var axes       = this.Get('chart.labels.axes').toLowerCase();
        var color      = 'rgba(255,255,255,0.8)';
        var drawzero   = false;
        var units_pre  = this.Get('chart.units.pre');
        var units_post = this.Get('chart.units.post');
        var decimals   = this.Get('chart.scale.decimals');

        // The "North" axis labels
        if (axes.indexOf('n') > -1) {
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - (r * 0.2), RGraph.number_format(this, this.scale[0].toFixed(decimals), units_pre, units_post),'center','center',true,false,color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - (r * 0.4), RGraph.number_format(this, this.scale[1].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - (r * 0.6), RGraph.number_format(this, this.scale[2].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - (r * 0.8), RGraph.number_format(this, this.scale[3].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - r, RGraph.number_format(this, this.scale[4].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            
            drawzero = true;
        }

        // The "South" axis labels
        if (axes.indexOf('s') > -1) {
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + (r * 0.2), RGraph.number_format(this, this.scale[0].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + (r * 0.4), RGraph.number_format(this, this.scale[1].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + (r * 0.6), RGraph.number_format(this, this.scale[2].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + (r * 0.8), RGraph.number_format(this, this.scale[3].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + r, RGraph.number_format(this, this.scale[4].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            
            drawzero = true;
        }
        
        // The "East" axis labels
        if (axes.indexOf('e') > -1) {
            RGraph.Text(context, font_face, font_size, this.centerx + (r * 0.2), this.centery, RGraph.number_format(this, this.scale[0].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + (r * 0.4), this.centery, RGraph.number_format(this, this.scale[1].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + (r * 0.6), this.centery, RGraph.number_format(this, this.scale[2].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + (r * 0.8), this.centery, RGraph.number_format(this, this.scale[3].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + r, this.centery, RGraph.number_format(this, this.scale[4].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            
            drawzero = true;
        }

        // The "West" axis labels
        if (axes.indexOf('w') > -1) {
            RGraph.Text(context, font_face, font_size, this.centerx - (r * 0.2), this.centery, RGraph.number_format(this, this.scale[0].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - (r * 0.4), this.centery, RGraph.number_format(this, this.scale[1].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - (r * 0.6), this.centery, RGraph.number_format(this, this.scale[2].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - (r * 0.8), this.centery, RGraph.number_format(this, this.scale[3].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - r, this.centery, RGraph.number_format(this, this.scale[4].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - r, this.centery, RGraph.number_format(this, this.scale[4].toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            
            drawzero = true;
        }

        if (drawzero) {
            RGraph.Text(context, font_face, font_size, this.centerx,  this.centery, RGraph.number_format(this, (0).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
        }
    }


    /**
    * Draws specific axis labels
    */
    RGraph.Radar.prototype.DrawSpecificAxisLabels = function ()
    {
        /**
        * Specific Y labels
        */
        var labels  = this.Get('chart.labels.specific');
        var context = this.context;
        var font    = this.Get('chart.text.font');
        var size    = this.Get('chart.text.size');
        var axes    = this.Get('chart.labels.axes').toLowerCase();
        var interval = (this.radius * 2) / (labels.length * 2);

        for (var i=0; i<labels.length; ++i) {

            if (axes.indexOf('n') > -1) RGraph.Text(context,font,size,this.gutterLeft + this.radius,this.gutterTop + (i * interval),labels[i],'center','center', true, false, 'rgba(255,255,255,0.8)');
            if (axes.indexOf('s') > -1) RGraph.Text(context,font,size,this.gutterLeft + this.radius,this.gutterTop + this.radius + (i * interval) + interval,RGraph.array_reverse(labels)[i],'center','center', true, false, 'rgba(255,255,255,0.8)');
            if (axes.indexOf('w') > -1) RGraph.Text(context,font,size,this.gutterLeft + (i * interval),this.gutterTop + this.radius,labels[i],'center','center', true, false, 'rgba(255,255,255,0.8)');
            if (axes.indexOf('e') > -1) RGraph.Text(context,font,size,this.gutterLeft + (i * interval) + interval + this.radius,this.gutterTop + this.radius,RGraph.array_reverse(labels)[i],'center','center', true, false, 'rgba(255,255,255,0.8)');
        }
    }


    /**
    * This method eases getting the focussed point (if any)
    * 
    * @param event e The event object
    */
    RGraph.Radar.prototype.getShape =
    RGraph.Radar.prototype.getPoint = function (e)
    {
        var canvas  = this.canvas;
        var context = this.context;

        for (var i=0; i<this.coords.length; ++i) {

            var x        = this.coords[i][0];
            var y        = this.coords[i][1];
            var tooltips = this.Get('chart.tooltips');
            var index    = Number(i);
            var mouseXY  = RGraph.getMouseXY(e);
            var mouseX   = mouseXY[0];
            var mouseY   = mouseXY[1];

            if (   mouseX < (x + 5)
                && mouseX > (x - 5)
                && mouseY > (y - 5)
                && mouseY < (y + 5)
               ) {
                
                var tooltip = RGraph.parseTooltipText(this.Get('chart.tooltips'), index);

                return {0: this,    'object':  this,
                        1: x,       'x':       x,
                        2: y,       'y':       y,
                        3: null, 'dataset': null,
                        4: index,       'index':   i,
                                    'tooltip': tooltip
                       }
            }
        }
    }



    /**
    * Each object type has its own Highlight() function which highlights the appropriate shape
    * 
    * @param object shape The shape to highlight
    */
    RGraph.Radar.prototype.Highlight = function (shape)
    {
        // Add the new highlight
        RGraph.Highlight.Point(this, shape);
    }



    /**
    * The getObjectByXY() worker method. Don't call this call:
    * 
    * RGraph.ObjectRegistry.getObjectByXY(e)
    * 
    * @param object e The event object
    */
    RGraph.Radar.prototype.getObjectByXY = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e);

        if (
               mouseXY[0] > (this.centerx - this.radius)
            && mouseXY[0] < (this.centerx + this.radius)
            && mouseXY[1] > (this.centery - this.radius)
            && mouseXY[1] < (this.centery + this.radius)
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
    RGraph.Radar.prototype.positionTooltip = function (obj, x, y, tooltip, idx)
    {
        var dataset    = tooltip.__dataset__;
        var index      = tooltip.__index__;
        var coordX     = obj.coords[index][0];
        var coordY     = obj.coords[index][1];
        var canvasXY   = RGraph.getCanvasXY(obj.canvas);
        var gutterLeft = obj.Get('chart.gutter.left');
        var gutterTop  = obj.Get('chart.gutter.top');
        var width      = tooltip.offsetWidth;

        // Set the top position
        tooltip.style.left = 0;
        tooltip.style.top  = parseInt(tooltip.style.top) - 9 + 'px';
        
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
        if ((canvasXY[0] + coordX - (width / 2)) < 10) {
            tooltip.style.left = (canvasXY[0] + coordX - (width * 0.1)) + 'px';
            img.style.left = ((width * 0.1) - 8.5) + 'px';

        // RIGHT edge
        } else if ((canvasXY[0] + coordX + (width / 2)) > document.body.offsetWidth) {
            tooltip.style.left = canvasXY[0] + coordX - (width * 0.9) + 'px';
            img.style.left = ((width * 0.9) - 8.5) + 'px';

        // Default positioning - CENTERED
        } else {
            tooltip.style.left = (canvasXY[0] + coordX - (width * 0.5)) + 'px';
            img.style.left = ((width * 0.5) - 8.5) + 'px';
        }
    }