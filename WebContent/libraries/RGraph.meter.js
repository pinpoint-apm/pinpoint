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
    * @param string canvas The canvas ID
    * @param min    integer The minimum value
    * @param max    integer The maximum value
    * @param value  integer The indicated value
    */
    RGraph.Meter = function (id, min, max, value)
    {
        // Get the canvas and context objects
        this.id                = id;
        this.canvas            = document.getElementById(id);
        this.context           = this.canvas.getContext ? this.canvas.getContext("2d") : null;
        this.canvas.__object__ = this;
        this.type              = 'meter';
        this.min               = min;
        this.max               = max;
        this.value             = value;
        this.centerx           = null;
        this.centery           = null;
        this.radius            = null;
        this.isRGraph          = true;
        this.currentValue      = null;
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();


        /**
        * Compatibility with older browsers
        */
        RGraph.OldBrowserCompat(this.context);


        // Various config type stuff
        this.properties = {
            'chart.background.color':       'white',
            'chart.gutter.left':            15,
            'chart.gutter.right':           15,
            'chart.gutter.top':             15,
            'chart.gutter.bottom':          15,
            'chart.linewidth':              1,
            'chart.linewidth.segments':     1,
            'chart.strokestyle':            null,
            'chart.border':                 true,
            'chart.border.color':           'black',
            'chart.text.font':              'Arial',
            'chart.text.size':              10,
            'chart.text.color':             'black',
            'chart.value.label':            false,
            'chart.value.text.decimals':    0,
            'chart.value.text.units.pre':   '',
            'chart.value.text.units.post':  '',
            'chart.title':                  '',
            'chart.title.background':       null,
            'chart.title.hpos':             null,
            'chart.title.vpos':             null,
            'chart.title.color':            'black',
            'chart.title.bold':             true,
            'chart.title.font':             null,
            'chart.green.start':            ((this.max - this.min) * 0.35) + this.min,
            'chart.green.end':              this.max,
            'chart.green.color':            '#207A20',
            'chart.yellow.start':           ((this.max - this.min) * 0.1) + this.min,
            'chart.yellow.end':             ((this.max - this.min) * 0.35) + this.min,
            'chart.yellow.color':           '#D0AC41',
            'chart.red.start':              this.min,
            'chart.red.end':                ((this.max - this.min) * 0.1) + this.min,
            'chart.red.color':              '#9E1E1E',
            'chart.colors.ranges':          null,
            'chart.units.pre':              '',
            'chart.units.post':             '',
            'chart.contextmenu':            null,
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
            'chart.annotatable':            false,
            'chart.annotate.color':         'black',
            'chart.shadow':                 false,
            'chart.shadow.color':           'rgba(0,0,0,0.5)',
            'chart.shadow.blur':            3,
            'chart.shadow.offsetx':         3,
            'chart.shadow.offsety':         3,
            'chart.resizable':              false,
            'chart.resize.handle.adjust':   [0,0],
            'chart.resize.handle.background': null,
            'chart.tickmarks.small.num':      100,
            'chart.tickmarks.big.num':        10,
            'chart.tickmarks.small.color':    '#bbb',
            'chart.tickmarks.big.color':      'black',
            'chart.scale.decimals':           0,
            'chart.scale.point':              '.',
            'chart.scale.thousand':           ',',
            'chart.radius':                   null,
            'chart.centerx':                  null,
            'chart.centery':                  null,
            'chart.labels':                   true,
            'chart.segment.radius.start':     null,
            'chart.needle.radius':            null,
            'chart.needle.tail':              false,
            'chart.adjustable':               false
        }


        // Check for support
        if (!this.canvas) {
            alert('[METER] No canvas support');
            return;
        }
        
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
    RGraph.Meter.prototype.Set = function (name, value)
    {
        if (name == 'chart.value') {
            this.value = value;
            return;
        }

        this.properties[name.toLowerCase()] = value;
    }


    /**
    * A getter
    * 
    * @param name  string The name of the property to get
    */
    RGraph.Meter.prototype.Get = function (name)
    {
        if (name == 'chart.value') {
            return this.value;
        }

        return this.properties[name];
    }


    /**
    * The function you call to draw the bar chart
    */
    RGraph.Meter.prototype.Draw = function ()
    {
        /**
        * Fire the onbeforedraw event
        */
        RGraph.FireCustomEvent(this, 'onbeforedraw');

        /**
        * Constrain the value to be within the min and max
        */
        if (this.value > this.max) this.value = this.max;
        if (this.value < this.min) this.value = this.min;

        /**
        * Set the current value
        */
        this.currentValue = this.value;

        /**
        * This is new in May 2011 and facilitates indiviual gutter settings,
        * eg chart.gutter.left
        */
        this.gutterLeft   = this.Get('chart.gutter.left');
        this.gutterRight  = this.Get('chart.gutter.right');
        this.gutterTop    = this.Get('chart.gutter.top');
        this.gutterBottom = this.Get('chart.gutter.bottom');

        this.centerx = ((this.canvas.width - this.gutterLeft - this.gutterRight) / 2) + this.gutterLeft;
        this.centery = this.canvas.height - this.gutterBottom;
        this.radius  = Math.min(
                                (this.canvas.width - this.gutterLeft - this.gutterRight) / 2,
                                (this.canvas.height - this.gutterTop - this.gutterBottom)
                               );



        /**
        * Custom centerx, centery and radius
        */
        if (typeof(this.Get('chart.centerx')) == 'number') this.centerx = this.Get('chart.centerx');
        if (typeof(this.Get('chart.centery')) == 'number') this.centery = this.Get('chart.centery');
        if (typeof(this.Get('chart.radius')) == 'number')  this.radius  = this.Get('chart.radius');



        this.DrawBackground();
        this.DrawNeedle();
        this.DrawLabels();
        this.DrawReadout();
        
        /**
        * Draw the title
        */
        RGraph.DrawTitle(this, this.Get('chart.title'), this.gutterTop, null, this.Get('chart.title.size') ? this.Get('chart.title.size') : this.Get('chart.text.size') + 2);

        /**
        * Setup the context menu if required
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
        * For MSIE only, to cover the spurious lower ends of the circle
        */
        if (RGraph.isOld()) {
            // Cover the left tail
            this.context.beginPath();
            this.context.moveTo(this.gutterLeft, this.canvas.height - this.gutterBottom);
            this.context.fillStyle = 'white';
            this.context.fillRect(this.centerx - this.radius - 5, RGraph.GetHeight(this) - this.gutterBottom + 1, 10, this.gutterBottom);
            this.context.fill();

            // Cover the right tail
            this.context.beginPath();
            this.context.moveTo(RGraph.GetWidth(this) - this.gutterRight, RGraph.GetHeight(this) - this.gutterBottom);
            this.context.fillStyle = 'white';
            this.context.fillRect(this.centerx + this.radius - 5, RGraph.GetHeight(this) - this.gutterBottom + 1, 10, this.gutterBottom);
            this.context.fill();
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
    * Draws the background of the chart
    */
    RGraph.Meter.prototype.DrawBackground = function ()
    {
        // Draw the shadow
        if (this.Get('chart.shadow')) {
            this.context.beginPath();

                this.context.fillStyle     = this.Get('chart.background.color');
                this.context.shadowColor   = this.Get('chart.shadow.color');
                this.context.shadowBlur    = this.Get('chart.shadow.blur');
                this.context.shadowOffsetX = this.Get('chart.shadow.offsetx');
                this.context.shadowOffsetY = this.Get('chart.shadow.offsety');
                
                this.context.arc(this.centerx, this.centery, this.radius, PI, TWOPI, false);
                //this.context.arc(this.centerx, this.centery, , 0, TWOPI, false);
            this.context.fill();


            this.context.beginPath();
                var r = (this.radius * 0.06) > 40 ? 40 : (this.radius * 0.06);
                this.context.arc(this.centerx, this.centery, r, 0, TWOPI, 0);
            this.context.fill();

            RGraph.NoShadow(this);
        }

        // First, draw the grey tickmarks
        if (this.Get('chart.tickmarks.small.num')) {
            for (var i=0; i<PI; i+=(PI / this.Get('chart.tickmarks.small.num'))) {
                this.context.beginPath();
                    this.context.strokeStyle = this.Get('chart.tickmarks.small.color');
                    this.context.arc(this.centerx, this.centery, this.radius, PI + i, PI + i, 0);
                    this.context.arc(this.centerx, this.centery, this.radius - 5, PI + i, PI + i + 0.00001, 0);
                this.context.stroke();
            }

            // Draw the semi-circle that makes the tickmarks
            this.context.beginPath();
            this.context.fillStyle = this.Get('chart.background.color');
            this.context.arc(this.centerx, this.centery, this.radius - 4, PI, TWOPI, false);
            this.context.closePath();
            this.context.fill();
        }



        // Second, draw the darker tickmarks. First run draws them in white to get rid of the existing tickmark,
        // then the second run draws them in the requested color
        if (this.Get('chart.tickmarks.big.num')) {
            var colors = ['white','white',this.Get('chart.tickmarks.big.color')];
            for (var j=0; j<colors.length; ++j) {
                for (var i=0; i<PI; i+=(PI / this.Get('chart.tickmarks.big.num'))) {
                    this.context.beginPath();
                    this.context.strokeStyle = colors[j];
                    this.context.arc(this.centerx, this.centery, this.radius, PI + i, PI + i, 0);
                    this.context.arc(this.centerx, this.centery, this.radius - 5, PI + i, PI + i + 0.0001, 0);
                    //this.context.lineTo(this.centerx, this.centery);
                    this.context.stroke();
                }
            }
        }

        // Draw the white circle that makes the tickmarks
        this.context.beginPath();
        this.context.fillStyle = this.Get('chart.background.color');
        this.context.arc(this.centerx, this.centery, this.radius - 7, PI, TWOPI, false);
        this.context.closePath();
        this.context.fill();

        /**
        * Color ranges - either green/yellow/red or an arbitrary number of ranges
        */
        var ranges = this.Get('chart.colors.ranges');

        if (RGraph.is_array(this.Get('chart.colors.ranges'))) {

            var ranges = this.Get('chart.colors.ranges');

            for (var i=0; i<ranges.length; ++i) {

                this.context.strokeStyle = this.Get('chart.strokestyle') ? this.Get('chart.strokestyle') : ranges[i][2];
                this.context.fillStyle = ranges[i][2];
                this.context.lineWidth = this.Get('chart.linewidth.segments');

                this.context.beginPath();
                    this.context.moveTo(this.centerx,this.centery);
                    this.context.arc(this.centerx,
                                     this.centery,
                                     this.radius * 0.85,
                                     (((ranges[i][0] - this.min) / (this.max - this.min)) * PI) + PI,
                                     (((ranges[i][1] - this.min) / (this.max - this.min)) * PI) + PI,
                                     false);
                    this.context.lineTo(this.centerx, this.centery);
                this.context.closePath();
                this.context.stroke();
                this.context.fill();
            }

            // Stops the last line from being changed to a big linewidth. So it seems.
            this.context.beginPath();

        } else {
            // Draw the green area
            this.context.strokeStyle = this.Get('chart.strokestyle') ? this.Get('chart.strokestyle') : this.Get('chart.green.color');
            this.context.fillStyle = this.Get('chart.green.color');
            this.context.lineWidth = this.Get('chart.linewidth.segments');
            
            this.context.beginPath();
                this.context.arc(this.centerx,this.centery,this.radius * 0.85,(((this.Get('chart.green.start') - this.min) / (this.max - this.min)) * PI) + PI,(((this.Get('chart.green.end') - this.min) / (this.max - this.min)) * PI) + PI,false);

                if (typeof(this.Get('chart.segment.radius.start')) && this.Get('chart.segment.radius.start')) {
                    this.context.arc(this.centerx,
                                     this.centery,
                                     this.Get('chart.segment.radius.start'),
                                     (((this.Get('chart.green.end') - this.min) / (this.max - this.min)) * PI) + PI,
                                     (((this.Get('chart.green.start') - this.min) / (this.max - this.min)) * PI) + PI,
                                     true);
                } else {
                    this.context.lineTo(this.centerx, this.centery);
                }

            this.context.closePath();
            this.context.stroke();
            this.context.fill();
            
            // Draw the yellow area
            this.context.strokeStyle = this.Get('chart.strokestyle') ? this.Get('chart.strokestyle') : this.Get('chart.yellow.color');
            this.context.fillStyle = this.Get('chart.yellow.color');
            this.context.lineWidth = this.Get('chart.linewidth.segments');
            this.context.beginPath();
            this.context.arc(this.centerx,this.centery,this.radius * 0.85,(((this.Get('chart.yellow.start') - this.min) / (this.max - this.min)) * PI) + PI,(((this.Get('chart.yellow.end') - this.min) / (this.max - this.min)) * PI) + PI,false);
            
            if (typeof(this.Get('chart.segment.radius.start')) && this.Get('chart.segment.radius.start')) {
                this.context.arc(this.centerx,this.centery,this.Get('chart.segment.radius.start'),(((this.Get('chart.yellow.end') - this.min) / (this.max - this.min)) * PI) + PI, (((this.Get('chart.yellow.start') - this.min) / (this.max - this.min)) * PI) + PI,true);
            } else {
                this.context.lineTo(this.centerx, this.centery);
            }

            this.context.closePath();
            this.context.stroke();
            this.context.fill();
            
            // Draw the red area
            this.context.strokeStyle = this.Get('chart.strokestyle') ? this.Get('chart.strokestyle') : this.Get('chart.red.color');
            this.context.fillStyle = this.Get('chart.red.color');
            this.context.lineWidth = this.Get('chart.linewidth.segments');
            this.context.beginPath();
            this.context.arc(this.centerx,this.centery,this.radius * 0.85,(((this.Get('chart.red.start') - this.min) / (this.max - this.min)) * PI) + PI,(((this.Get('chart.red.end') - this.min) / (this.max - this.min)) * PI) + PI,false);

            if (typeof(this.Get('chart.segment.radius.start')) && this.Get('chart.segment.radius.start')) {
                this.context.arc(this.centerx,this.centery, this.Get('chart.segment.radius.start'),(((this.Get('chart.red.end') - this.min) / (this.max - this.min)) * PI) + PI,(((this.Get('chart.red.start') - this.min) / (this.max - this.min)) * PI) + PI,true);
            } else {
                this.context.lineTo(this.centerx, this.centery);
            }

            this.context.closePath();
            this.context.stroke();
            this.context.fill();
            
            // Revert the linewidth
            this.context.lineWidth = 1;
        }

        // Draw the outline
        if (this.Get('chart.border')) {
            this.context.strokeStyle = this.Get('chart.border.color');
            this.context.lineWidth   = this.Get('chart.linewidth');
            
            this.context.beginPath();
            this.context.moveTo(this.centerx, this.centery);
            this.context.arc(this.centerx, this.centery, this.radius, PI, TWOPI, false);
            this.context.closePath();
        }

        this.context.stroke();
        
        // Reset the linewidth back to 1
        this.context.lineWidth = 1;
    }


    /**
    * Draws the pointer
    */
    RGraph.Meter.prototype.DrawNeedle = function ()
    {
        // Allow customising the needle radius
        var needleRadius = typeof(this.Get('chart.needle.radius')) == 'number' ? this.Get('chart.needle.radius') : this.radius * 0.7;

        // First draw the circle at the bottom
        this.context.fillStyle = 'black';
        this.context.lineWidth = this.radius >= 200 ? 7 : 3;
        this.context.lineCap = 'round';

        // Now, draw the arm of the needle
        this.context.beginPath();
        this.context.strokeStyle = 'black';
        if (typeof(this.Get('chart.needle.linewidth')) == 'number') this.context.lineWidth = this.Get('chart.needle.linewidth');
        var a = (((this.value - this.min) / (this.max - this.min)) * PI) + PI;
        this.context.arc(this.centerx, this.centery, needleRadius, a, a + 0.001, false);
        this.context.lineTo(this.centerx, this.centery);
        this.context.stroke();
        
        // Draw the triangular needle head
        this.context.beginPath();
            this.context.lineWidth = 1;
            //this.context.moveTo(this.centerx, this.centery);
            this.context.arc(this.centerx, this.centery, needleRadius + 15, a, a + 0.001, 0);
            this.context.arc(this.centerx, this.centery, needleRadius - 15, a + 0.087, a + 0.087999, 0);
            this.context.arc(this.centerx, this.centery, needleRadius - 15, a - 0.087, a - 0.087999, 1);
        this.context.fill();
        
        // Draw the tail if requested
        if (this.Get('chart.needle.tail')) {
            this.context.beginPath();
                this.context.strokeStyle = 'black';
                if (typeof(this.Get('chart.needle.linewidth')) == 'number') this.context.lineWidth = this.Get('chart.needle.linewidth');
                var a = (((this.value - this.min) / (this.max - this.min)) * PI) + TWOPI;
                this.context.arc(this.centerx, this.centery, 25, a, a + 0.001, false);
                this.context.lineTo(this.centerx, this.centery);
            this.context.stroke();
        }

        // Draw the center circle
        var r = (this.radius * 0.06) > 40 ? 40 : (this.radius * 0.06);

        this.context.beginPath();
        this.context.arc(this.centerx, this.centery, r, 0, TWOPI, 0);
        this.context.fill();
        
        // Draw the centre bit of the circle
        this.context.fillStyle = 'white';
        this.context.beginPath();
        this.context.arc(this.centerx, this.centery, r - 2, 0, TWOPI, 0);
        this.context.fill();
    }


    /**
    * Draws the labels
    */
    RGraph.Meter.prototype.DrawLabels = function ()
    {
        if (!this.Get('chart.labels')) {
            return;
        }

        var context    = this.context;
        var radius     = this.radius;
        var text_size  = this.Get('chart.text.size');
        var text_font  = this.Get('chart.text.font');
        var units_post = this.Get('chart.units.post');
        var units_pre  = this.Get('chart.units.pre');
        var centerx    = this.centerx;
        var centery    = this.centery;
        var min        = this.min;
        var max        = this.max;
        var decimals   = this.Get('chart.scale.decimals');

        context.fillStyle = this.Get('chart.text.color');
        context.lineWidth = 1;

        context.beginPath();


        RGraph.Text(context,text_font,text_size,centerx - radius + (0.075 * radius),centery - (this.Get('chart.border') ? 10 : -5),RGraph.number_format(this, (min).toFixed(decimals),units_pre,units_post),'center', 'left', false, 270);
        RGraph.Text(context,text_font,text_size,centerx - (Math.cos(0.62819 / 2) * (radius - (0.075 * radius)) ),centery - (Math.sin(0.682819 / 2) * (radius - (0.1587 * radius)) ),RGraph.number_format(this, (((max - min) * (1/10)) + min).toFixed(decimals), units_post, units_post),'center','center', false, 288);
        RGraph.Text(context,text_font,text_size,centerx - (Math.cos(0.62819) * (radius - (0.07 * radius)) ),centery - (Math.sin(0.682819) * (radius - (0.15 * radius)) ),RGraph.number_format(this,(((max - min) * (2/10)) + min).toFixed(decimals), units_pre, units_post),'center','center', false, 306);
        RGraph.Text(context, text_font, text_size,centerx - (Math.cos(0.95) * (radius - (0.085 * radius)) ),centery - (Math.sin(0.95) * (radius - (0.0785 * radius)) ),RGraph.number_format(this,(((max - min) * (3/10)) + min).toFixed(decimals), units_pre, units_post),'center', 'center', false, 320);
        RGraph.Text(context, text_font, text_size,centerx - (Math.cos(1.2566) * (radius - (0.085 * radius)) ),centery - (Math.sin(1.2566) * (radius - (0.0785 * radius)) ),RGraph.number_format(this, (((max - min) * (4/10)) + min).toFixed(decimals), units_pre, units_post),'center', 'center', false, 342);
        RGraph.Text(context,text_font,text_size,centerx - (Math.cos(HALFPI) * (radius - (0.075 * radius)) ),centery - (Math.sin(HALFPI) * (radius - (0.075 * radius)) ),RGraph.number_format(this, (((max - min) * (5/10)) + min).toFixed(decimals), units_pre, units_post),'center','center', false, 0);
        RGraph.Text(context,text_font,text_size,centerx - (Math.cos(1.88495562) * (radius - (0.075 * radius)) ),centery - (Math.sin(1.88495562) * (radius - (0.075 * radius)) ),RGraph.number_format(this, (((max - min)* (6/10)) + min).toFixed(decimals), units_pre, units_post),'center','center', false, 18);
        RGraph.Text(context,text_font,text_size,centerx - (Math.cos(2.1989) * (radius - (0.075 * radius)) ),centery - (Math.sin(2.1989) * (radius - (0.075 * radius)) ),RGraph.number_format(this, (((max - min)* (7/10)) + min).toFixed(decimals), units_pre, units_post),'center','center', false, 36);
        RGraph.Text(context,text_font,text_size,centerx - (Math.cos(2.51327416) * (radius - (0.075 * radius)) ),centery - (Math.sin(2.51327416) * (radius - (0.075 * radius)) ), RGraph.number_format(this,(((max - min) * (8/10)) + min).toFixed(decimals), units_pre,units_post),'center','center', false, 54);
        RGraph.Text(context,text_font,text_size,centerx - (Math.cos(2.82764832) * (radius - (0.075 * radius)) ),centery - (Math.sin(2.82764832) * (radius - (0.075 * radius)) ), RGraph.number_format(this,(((max - min) * (9/10)) + min).toFixed(decimals), units_pre, units_post),'center','center', false, 72);
        RGraph.Text(context,text_font,text_size,centerx + radius - (0.075 * radius),centery - (!this.Get('chart.border') ? -5 : 10),RGraph.number_format(this,(max).toFixed(decimals),units_pre, units_post),'center', 'right', false, 90);

        context.fill();
        context.stroke();
    }

    
    /**
    * This function draws the text readout if specified
    */
    RGraph.Meter.prototype.DrawReadout  = function ()
    {
        if (this.Get('chart.value.text')) {
            this.context.beginPath();
            RGraph.Text(this.context,
                        this.Get('chart.text.font'),
                        this.Get('chart.text.size'),
                        this.centerx,
                        this.centery - this.Get('chart.text.size') - 15,
                        this.Get('chart.value.text.units.pre') + (this.value).toFixed(this.Get('chart.value.text.decimals')) + this.Get('chart.value.text.units.post'),
                         'center',
                         'center',
                         true,
                         null,
                         'white');

            this.context.stroke();
            this.context.fill();
        }
    }



    /**
    * A placeholder function
    * 
    * @param object The event object
    */
    RGraph.Meter.prototype.getShape = function (e)
    {
    }



    /**
    * This function returns the pertinent value for a particular click (or other mouse event)
    * 
    * @param obj e The event object
    */
    RGraph.Meter.prototype.getValue = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e);
        var angle   = RGraph.getAngleByXY(this.centerx, this.centery, mouseXY[0], mouseXY[1]);
        
        // Work out the radius
        var radius = RGraph.getHypLength(this.centerx, this.centery, mouseXY[0], mouseXY[1]);
        if (radius > this.radius) {
            return null;
        }


        if (angle < HALFPI) {
            angle = TWOPI;
        }

        var value = ((angle - PI) / PI) * (this.max - this.min);
        
        value = Math.max(value, this.min);
        value = Math.min(value, this.max);

        return value;
    }



    /**
    * The getObjectByXY() worker method. Don't call this call:
    * 
    * RGraph.ObjectRegistry.getObjectByXY(e)
    * 
    * @param object e The event object
    */
    RGraph.Meter.prototype.getObjectByXY = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e);

        // Work out the radius
        var radius = RGraph.getHypLength(this.centerx, this.centery, mouseXY[0], mouseXY[1]);

        if (
               mouseXY[0] > (this.centerx - this.radius)
            && mouseXY[0] < (this.centerx + this.radius)
            && mouseXY[1] > (this.centery - this.radius)
            && mouseXY[1] < (this.centery + 25)
            && radius <= this.radius
            ) {

            return this;
        }
    }



    /**
    * This method handles the adjusting calculation for when the mouse is moved
    * 
    * @param object e The event object
    */
    RGraph.Meter.prototype.Adjusting_mousemove = function (e)
    {
        /**
        * Handle adjusting for the Bar
        */
        if (RGraph.Registry.Get('chart.adjusting') && RGraph.Registry.Get('chart.adjusting').uid == this.uid) {
            this.value = this.getValue(e);
            RGraph.Clear(this.canvas);
            RGraph.RedrawCanvas(this.canvas);
            RGraph.FireCustomEvent(this, 'onadjust');
        }
    }