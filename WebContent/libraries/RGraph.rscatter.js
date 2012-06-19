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
    * The chart constuctor
    * 
    * @param object canvas
    * @param array data
    */
    RGraph.Rscatter = function (id, data)
    {
        this.id                = id;
        this.canvas            = document.getElementById(id);
        this.context           = this.canvas.getContext('2d');
        this.data              = data;
        this.canvas.__object__ = this;
        this.type              = 'rscatter';
        this.hasTooltips       = false;
        this.isRGraph          = true;
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();


        /**
        * Compatibility with older browsers
        */
        RGraph.OldBrowserCompat(this.context);


        this.centerx = 0;
        this.centery = 0;
        this.radius  = 0;
        this.max     = 0;
        
        this.properties = {
            'chart.radius':                 null,
            'chart.colors':                 [], // This is used internally for the key
            'chart.colors.default':         'black',
            'chart.gutter.left':            25,
            'chart.gutter.right':           25,
            'chart.gutter.top':             25,
            'chart.gutter.bottom':          25,
            'chart.title':                  '',
            'chart.title.background':       null,
            'chart.title.hpos':             null,
            'chart.title.vpos':             null,
            'chart.title.bold':             true,
            'chart.title.font':             null,
            'chart.labels':                 null,
            'chart.labels.position':       'center',
            'chart.labels.axes':            'nsew',
            'chart.text.color':             'black',
            'chart.text.font':              'Arial',
            'chart.text.size':              10,
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
            'chart.contextmenu':            null,
            'chart.tooltips':               null,
            'chart.tooltips.event':        'onmousemove',
            'chart.tooltips.effect':        'fade',
            'chart.tooltips.css.class':     'RGraph_tooltip',
            'chart.tooltips.highlight':     true,
            'chart.tooltips.hotspot':       3,
            'chart.tooltips.coords.page':   false,
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
            'chart.resize.handle.background': null,
            'chart.adjustable':             false,
            'chart.ymax':                   null,
            'chart.ymin':                   0,
            'chart.tickmarks':              'cross',
            'chart.ticksize':               3,
            'chart.scale.decimals':         null,
            'chart.scale.point':            '.',
            'chart.scale.thousand':         ',',
            'chart.scale.round':            false,
            'chart.units.pre':              '',
            'chart.units.post':             '',
            'chart.events.mousemove':       null,
            'chart.events.click':           null,
            'chart.highlight.stroke':       'rgba(0,0,0,0)',
            'chart.highlight.fill':         'rgba(255,255,255,0.7)',
            'chart.highlight.point.radius': 3
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
    RGraph.Rscatter.prototype.Set = function (name, value)
    {
        this.properties[name.toLowerCase()] = value;
    }
    
    
    /**
    * A simple getter
    * 
    * @param string name The name of the property to get
    */
    RGraph.Rscatter.prototype.Get = function (name)
    {
        return this.properties[name.toLowerCase()];
    }

    
    /**
    * This method draws the rose chart
    */
    RGraph.Rscatter.prototype.Draw = function ()
    {
        /**
        * Fire the onbeforedraw event
        */
        RGraph.FireCustomEvent(this, 'onbeforedraw');


        /**
        * This doesn't affect the chart, but is used for compatibility
        */
        this.gutterLeft   = this.Get('chart.gutter.left');
        this.gutterRight  = this.Get('chart.gutter.right');
        this.gutterTop    = this.Get('chart.gutter.top');
        this.gutterBottom = this.Get('chart.gutter.bottom');

        // Calculate the radius
        this.radius  = (Math.min(RGraph.GetWidth(this) - this.gutterLeft - this.gutterRight, RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom) / 2);
        this.centerx = ((this.canvas.width - this.gutterLeft - this.gutterRight) / 2) + this.gutterLeft;
        this.centery = ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2) + this.gutterTop;
        this.coords  = [];

        /**
        * If there's a user specified radius/centerx/centery, use them
        */
        if (typeof(this.Get('chart.centerx')) == 'number') this.centerx = this.Get('chart.centerx');
        if (typeof(this.Get('chart.centery')) == 'number') this.centery = this.Get('chart.centery');
        if (typeof(this.Get('chart.radius'))  == 'number') this.radius  = this.Get('chart.radius');
        
        /**
        * Work out the scale
        */
        var max = this.Get('chart.ymax');
        var min = this.Get('chart.ymin');
        
        if (typeof(max) == 'number') {
            this.max   = max;
            this.scale = [((max - min) * 0.2) + min,((max - min) * 0.4) + min,((max - min) * 0.6) + min,((max - min) * 0.8) + min,((max - min) * 1.0) + min];
            
        } else {

            for (var i=0; i<this.data.length; ++i) {
                this.max = Math.max(this.max, this.data[i][1]);
            }
            this.scale = RGraph.getScale(this.max, this);
            this.max   = this.scale[4];

            // Hmmmmmmmm
            if (String(this.scale[0]).indexOf('e') == -1) {

                var decimals = this.Get('chart.scale.decimals');

                this.scale[0] = Number(this.scale[0]).toFixed(decimals);
                this.scale[1] = Number(this.scale[1]).toFixed(decimals);
                this.scale[2] = Number(this.scale[2]).toFixed(decimals);
                this.scale[3] = Number(this.scale[3]).toFixed(decimals);
                this.scale[4] = Number(this.scale[4]).toFixed(decimals);
            }
        }

        /**
        * Change the centerx marginally if the key is defined
        */
        if (this.Get('chart.key') && this.Get('chart.key').length > 0 && this.Get('chart.key').length >= 3) {
            this.centerx = this.centerx - this.Get('chart.gutter.right') + 5;
        }
        
        /**
        * Populate the colors array for the purposes of generating the key
        */
        if (typeof(this.Get('chart.key')) == 'object' && RGraph.is_array(this.Get('chart.key')) && this.Get('chart.key')[0]) {
            for (var i=0; i<this.data.length; ++i) {
                if (this.data[i][2] && typeof(this.data[i][2]) == 'string') {
                    this.Get('chart.colors').push(this.data[i][2]);
                }
            }
        }



        /**
        * Populate the chart.tooltips array
        */

        for (var i=0; i<this.data.length; ++i) {
            if (!RGraph.is_null(this.data[i][3])) {
                if (RGraph.is_null(this.Get('chart.tooltips'))) {
                    this.Set('chart.tooltips', []);
                }
                
                this.Get('chart.tooltips').push(this.data[i][3]);
            }
        }



        // This resets the chart drawing state
        this.context.beginPath();

        this.DrawBackground();
        this.DrawRscatter();
        this.DrawLabels();

        /**
        * Setup the context menu if required
        */
        if (this.Get('chart.contextmenu')) {
            RGraph.ShowContext(this);
        }



        // Draw the title if any has been set
        if (this.Get('chart.title')) {
            RGraph.DrawTitle(this,
                             this.Get('chart.title'),
                             this.centery - this.radius - 10,
                             this.centerx,
                             this.Get('chart.title.size') ? this.Get('chart.title.size') : this.Get('chart.text.size') + 2);
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
    * This method draws the rose charts background
    */
    RGraph.Rscatter.prototype.DrawBackground = function ()
    {
        this.context.lineWidth = 1;
    
        // Draw the background grey circles
        this.context.strokeStyle = '#ccc';
        for (var i=15; i<this.radius - (document.all ? 5 : 0); i+=15) {// Radius must be greater than 0 for Opera to work
            //this.context.moveTo(this.centerx + i, this.centery);
    
            // Radius must be greater than 0 for Opera to work
            this.context.arc(this.centerx, this.centery, i, 0, TWOPI, 0);
        }
        this.context.stroke();

        // Draw the background lines that go from the center outwards
        this.context.beginPath();
        for (var i=15; i<360; i+=15) {
        
            // Radius must be greater than 0 for Opera to work
            this.context.arc(this.centerx, this.centery, this.radius, i / 57.3, (i + 0.01) / 57.3, 0);
        
            this.context.lineTo(this.centerx, this.centery);
        }
        this.context.stroke();
        
        this.context.beginPath();
        this.context.strokeStyle = 'black';
    
        // Draw the X axis
        this.context.moveTo(this.centerx - this.radius, AA(this, this.centery));
        this.context.lineTo(this.centerx + this.radius, AA(this, this.centery));
    
        // Draw the X ends
        this.context.moveTo(AA(this, this.centerx - this.radius), this.centery - 5);
        this.context.lineTo(AA(this, this.centerx - this.radius), this.centery + 5);
        this.context.moveTo(AA(this, this.centerx + this.radius), this.centery - 5);
        this.context.lineTo(AA(this, this.centerx + this.radius), this.centery + 5);
        
        // Draw the X check marks
        for (var i=(this.centerx - this.radius); i<(this.centerx + this.radius); i+=20) {
            this.context.moveTo(AA(this, i),  this.centery - 3);
            this.context.lineTo(AA(this, i),  this.centery + 3);
        }
        
        // Draw the Y check marks
        for (var i=(this.centery - this.radius); i<(this.centery + this.radius); i+=20) {
            this.context.moveTo(this.centerx - 3, AA(this, i));
            this.context.lineTo(this.centerx + 3, AA(this, i));
        }
    
        // Draw the Y axis
        this.context.moveTo(AA(this, this.centerx), this.centery - this.radius);
        this.context.lineTo(AA(this, this.centerx), this.centery + this.radius);
    
        // Draw the Y ends
        this.context.moveTo(this.centerx - 5, AA(this, this.centery - this.radius));
        this.context.lineTo(this.centerx + 5, AA(this, this.centery - this.radius));
    
        this.context.moveTo(this.centerx - 5, AA(this, this.centery + this.radius));
        this.context.lineTo(this.centerx + 5, AA(this, this.centery + this.radius));
        
        // Stroke it
        this.context.closePath();
        this.context.stroke();
    }


    /**
    * This method draws a set of data on the graph
    */
    RGraph.Rscatter.prototype.DrawRscatter = function ()
    {
        var data = this.data;

        for (var i=0; i<data.length; ++i) {

            var d1 = data[i][0];
            var d2 = data[i][1];
            var a   = d1 / (180 / PI); // RADIANS
            var r   = ( (d2 - this.Get('chart.ymin')) / (this.max - this.Get('chart.ymin')) ) * this.radius;
            var x   = Math.sin(a) * r;
            var y   = Math.cos(a) * r;
            var color = data[i][2] ? data[i][2] : this.Get('chart.colors.default');
            var tooltip = data[i][3] ? data[i][3] : null;

            if (tooltip && String(tooltip).length) {
                this.hasTooltips = true;
            }

            /**
            * Account for the correct quadrant
            */
            x = x + this.centerx;
            y = this.centery - y;


            this.DrawTick(x, y, color);
            
            // Populate the coords array with the coordinates and the tooltip
            this.coords.push([x, y, color, tooltip]);
        }
    }


    /**
    * Unsuprisingly, draws the labels
    */
    RGraph.Rscatter.prototype.DrawLabels = function ()
    {
        this.context.lineWidth = 1;
        var key = this.Get('chart.key');
        
        // Set the color to black
        this.context.fillStyle = 'black';
        this.context.strokeStyle = 'black';
        
        var r          = this.radius;
        var color      = this.Get('chart.text.color');
        var font_face  = this.Get('chart.text.font');
        var font_size  = this.Get('chart.text.size');
        var context    = this.context;
        var axes       = this.Get('chart.labels.axes').toLowerCase();
        var units_pre  = this.Get('chart.units.pre');
        var units_post = this.Get('chart.units.post');
        var decimals   = this.Get('chart.scale.decimals');
        
        this.context.fillStyle = this.Get('chart.text.color');

        // Draw any labels
        if (typeof(this.Get('chart.labels')) == 'object' && this.Get('chart.labels')) {
            this.DrawCircularLabels(context, this.Get('chart.labels'), font_face, font_size, r);
        }


        var color = 'rgba(255,255,255,0.8)';

        // The "North" axis labels
        if (axes.indexOf('n') > -1) {
            RGraph.Text(context,font_face,font_size,this.centerx,this.centery - ((r) * 0.2),RGraph.number_format(this, Number(this.scale[0]).toFixed(decimals), units_pre, units_post),'center','center',true,false,color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - ((r) * 0.4), RGraph.number_format(this, Number(this.scale[1]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - ((r) * 0.6), RGraph.number_format(this, Number(this.scale[2]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - ((r) * 0.8), RGraph.number_format(this, Number(this.scale[3]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - r, RGraph.number_format(this, Number(this.scale[4]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
        }

        // The "South" axis labels
        if (axes.indexOf('s') > -1) {
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + ((r) * 0.2), RGraph.number_format(this, Number(this.scale[0]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + ((r) * 0.4), RGraph.number_format(this, Number(this.scale[1]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + ((r) * 0.6), RGraph.number_format(this, Number(this.scale[2]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + ((r) * 0.8), RGraph.number_format(this, Number(this.scale[3]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + r, RGraph.number_format(this, Number(this.scale[4]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
        }
        
        // The "East" axis labels
        if (axes.indexOf('e') > -1) {
            RGraph.Text(context, font_face, font_size, this.centerx + ((r) * 0.2), this.centery, RGraph.number_format(this, Number(this.scale[0]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + ((r) * 0.4), this.centery, RGraph.number_format(this, Number(this.scale[1]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + ((r) * 0.6), this.centery, RGraph.number_format(this, Number(this.scale[2]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + ((r) * 0.8), this.centery, RGraph.number_format(this, Number(this.scale[3]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + r, this.centery, RGraph.number_format(this, Number(this.scale[4]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
        }

        // The "West" axis labels
        if (axes.indexOf('w') > -1) {
            RGraph.Text(context, font_face, font_size, this.centerx - ((r) * 0.2), this.centery, RGraph.number_format(this, Number(this.scale[0]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - ((r) * 0.4), this.centery, RGraph.number_format(this, Number(this.scale[1]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - ((r) * 0.6), this.centery, RGraph.number_format(this, Number(this.scale[2]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - ((r) * 0.8), this.centery, RGraph.number_format(this, Number(this.scale[3]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - r, this.centery, RGraph.number_format(this, Number(this.scale[4]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
        }
        
        // Draw the center minimum value (but only if there's at least one axes labels stipulated)
        if (this.Get('chart.labels.axes').length > 0) {
            RGraph.Text(context,
                        font_face,
                        font_size,
                        this.centerx,
                        this.centery,
                        RGraph.number_format(this, Number(this.Get('chart.ymin')).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post),
                        'center',
                        'center',
                        true,
                        false,
                        color);
        }

        /**
        * Draw the key
        */
        if (key && key.length) {
            RGraph.DrawKey(this, key, this.Get('chart.colors'));
        }
    }


    /**
    * Draws the circular labels that go around the charts
    * 
    * @param labels array The labels that go around the chart
    */
    RGraph.Rscatter.prototype.DrawCircularLabels = function (context, labels, font_face, font_size, r)
    {
        var position = this.Get('chart.labels.position');
        var r        = r + 10;

        for (var i=0; i<labels.length; ++i) {


            var a = (360 / labels.length) * (i + 1) - (360 / (labels.length * 2));
            var a = a - 90 + (this.Get('chart.labels.position') == 'edge' ? ((360 / labels.length) / 2) : 0);

            var x = Math.cos(a / 57.29577866666) * (r + 10);
            var y = Math.sin(a / 57.29577866666) * (r + 10);

            RGraph.Text(context, font_face, font_size, this.centerx + x, this.centery + y, String(labels[i]), 'center', 'center');
        }
    }


    /**
    * Draws a single tickmark
    */
    RGraph.Rscatter.prototype.DrawTick = function (x, y, color)
    {
        var tickmarks    = this.Get('chart.tickmarks');
        var ticksize     = this.Get('chart.ticksize');

        this.context.strokeStyle = color;
        this.context.fillStyle   = color;

        // Cross
        if (tickmarks == 'cross') {

            this.context.beginPath();
            this.context.moveTo(x + ticksize, y + ticksize);
            this.context.lineTo(x - ticksize, y - ticksize);
            this.context.stroke();
    
            this.context.beginPath();
            this.context.moveTo(x - ticksize, y + ticksize);
            this.context.lineTo(x + ticksize, y - ticksize);
            this.context.stroke();
        
        // Circle
        } else if (tickmarks == 'circle') {

            this.context.beginPath();
            this.context.arc(x, y, ticksize, 0, 6.2830, false);
            this.context.fill();

        // Square
        } else if (tickmarks == 'square') {

            this.context.beginPath();
            this.context.fillRect(x - ticksize, y - ticksize, 2 * ticksize, 2 * ticksize);
            this.context.fill();
        
        // Diamond shape tickmarks
         } else if (tickmarks == 'diamond') {

            this.context.beginPath();
                this.context.moveTo(x, y - ticksize);
                this.context.lineTo(x + ticksize, y);
                this.context.lineTo(x, y + ticksize);
                this.context.lineTo(x - ticksize, y);
            this.context.closePath();
            this.context.fill();

        // Plus style tickmarks
        } else if (tickmarks == 'plus') {
        
            this.context.lineWidth = 1;

            this.context.beginPath();
                this.context.moveTo(x, y - ticksize);
                this.context.lineTo(x, y + ticksize);
                this.context.moveTo(x - ticksize, y);
                this.context.lineTo(x + ticksize, y);
            this.context.stroke();
        }
    }


    /**
    * This function makes it much easier to get the (if any) point that is currently being hovered over.
    * 
    * @param object e The event object
    */
    RGraph.Rscatter.prototype.getShape =
    RGraph.Rscatter.prototype.getPoint = function (e)
    {
        var canvas      = e.target;
        var context     = this.context;
        var mouseXY     = RGraph.getMouseXY(e);
        var mouseX      = mouseXY[0];
        var mouseY      = mouseXY[1];
        var overHotspot = false;
        var offset      = this.Get('chart.tooltips.hotspot'); // This is how far the hotspot extends

        for (var i=0; i<this.coords.length; ++i) {
        
            var x       = this.coords[i][0];
            var y       = this.coords[i][1];
            var tooltip = this.coords[i][3];

            if (
                mouseX < (x + offset) &&
                mouseX > (x - offset) &&
                mouseY < (y + offset) &&
                mouseY > (y - offset)
               ) {
               
                var tooltip = RGraph.parseTooltipText(this.Get('chart.tooltips'), i);

                return {0:this,1:x,2:y,3:i,'object':this, 'x':x, 'y':y, 'index':i, 'tooltip': tooltip};
            }
        }
    }



    /**
    * This function facilitates the installation of tooltip event listeners if
    * tooltips are defined.
    */
    RGraph.Rscatter.prototype.AllowTooltips = function ()
    {
        // Preload any tooltip images that are used in the tooltips
        RGraph.PreLoadTooltipImages(this);


        /**
        * This installs the window mousedown event listener that lears any
        * highlight that may be visible.
        */
        RGraph.InstallWindowMousedownTooltipListener(this);


        /**
        * This installs the canvas mousemove event listener. This function
        * controls the pointer shape.
        */
        RGraph.InstallCanvasMousemoveTooltipListener(this);


        /**
        * This installs the canvas mouseup event listener. This is the
        * function that actually shows the appropriate tooltip (if any).
        */
        RGraph.InstallCanvasMouseupTooltipListener(this);
    }



    /**
    * Each object type has its own Highlight() function which highlights the appropriate shape
    * 
    * @param object shape The shape to highlight
    */
    RGraph.Rscatter.prototype.Highlight = function (shape)
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
    RGraph.Rscatter.prototype.getObjectByXY = function (e)
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
    RGraph.Rscatter.prototype.positionTooltip = function (obj, x, y, tooltip, idx)
    {
        var coordX     = obj.coords[tooltip.__index__][0];
        var coordY     = obj.coords[tooltip.__index__][1];
        var canvasXY   = RGraph.getCanvasXY(obj.canvas);
        var gutterLeft = obj.Get('chart.gutter.left');
        var gutterTop  = obj.Get('chart.gutter.top');
        var width      = tooltip.offsetWidth;

        // Set the top position
        tooltip.style.left = 0;
        tooltip.style.top  = parseInt(tooltip.style.top) - 7 + 'px';
        
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