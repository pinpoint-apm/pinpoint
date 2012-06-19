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
    * The line chart constructor
    * 
    * @param object canvas The cxanvas object
    * @param array  data   The chart data
    * @param array  ...    Other lines to plot
    */
    RGraph.Gauge = function (id, min, max, value)
    {
        // Get the canvas and context objects
        this.id                = id;
        this.canvas            = document.getElementById(id);
        this.context           = this.canvas.getContext ? this.canvas.getContext("2d") : null;
        this.canvas.__object__ = this;
        this.type              = 'gauge';
        this.min               = min;
        this.max               = max;
        this.value             = value;
        this.isRGraph          = true;
        this.currentValue      = null;
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();

        /**
        * Range checking
        */
        if (this.value > this.max) {
            this.value = max;
        }

        if (this.value < this.min) {
            this.value = min;
        }



        /**
        * Compatibility with older browsers
        */
        RGraph.OldBrowserCompat(this.context);


        // Various config type stuff
        this.properties = {
            'chart.centerx':       null,
            'chart.centery':       null,
            'chart.radius':        null,
            'chart.gutter.left':   15,
            'chart.gutter.right':  15,
            'chart.gutter.top':    15,
            'chart.gutter.bottom': 15,
            'chart.border.width':  10,
            'chart.title.top':     '',
            'chart.title.top.font':'Arial',
            'chart.title.top.size':14,
            'chart.title.top.color':'#333',
            'chart.title.top.bold':false,
            'chart.title.top.pos': null,
            'chart.title.bottom':  '',
            'chart.title.bottom.font':'Arial',
            'chart.title.bottom.size':14,
            'chart.title.bottom.color':'#333',
            'chart.title.bottom.bold':false,
            'chart.title.bottom.pos':null,
            'chart.text.align':    'top',
            'chart.text.x':         null,
            'chart.text.y':         null,
            'chart.text.color':     '#666',
            'chart.text.size':      10,
            'chart.scale.decimals': 0,
            'chart.scale.point':    '.',
            'chart.scale.thousand': ',',
            'chart.units.pre':      '',
            'chart.units.post':     '',
            'chart.value.text':     false,
            'chart.value.text.y.pos': 0.5,
            'chart.value.text.units.pre': null,
            'chart.value.text.units.post': null,
            'chart.red.start':      0.9 * this.max,
            'chart.red.color':      '#DC3912',
            'chart.yellow.color':   '#FF9900',
            'chart.green.end':      0.7 * this.max,
            'chart.green.color':    'rgba(0,0,0,0)',
            'chart.colors.ranges':  null,
            'chart.needle.tail':    false,
            'chart.needle.color':    '#D5604D',
            'chart.border.outer':   '#ccc',
            'chart.border.inner':   '#f1f1f1',
            'chart.centerpin.color':        'blue',
            'chart.centerpin.radius':       null,

            'chart.zoom.background':        true,
            'chart.zoom.action':            'zoom',
            'chart.tickmarks.small':        25,
            'chart.tickmarks.medium':       0,
            'chart.tickmarks.big':          5,
            'chart.labels.count':           5,
            'chart.border.gradient':        false,
            'chart.adjustable':             false,
            'chart.shadow':                 true,
            'chart.shadow.color':           'gray',
            'chart.shadow.offsetx':         0,
            'chart.shadow.offsety':         0,
            'chart.shadow.blur':            15
        }


        /**
        * Register the object
        */
        RGraph.Register(this);
    }



    /**
    * An all encompassing accessor
    * 
    * @param string name The name of the property
    * @param mixed value The value of the property
    */
    RGraph.Gauge.prototype.Set = function (name, value)
    {
        /**
        * Title compatibility
        */
        if (name == 'chart.title')       name = 'chart.title.top';
        if (name == 'chart.title.font')  name = 'chart.title.top.font';
        if (name == 'chart.title.size')  name = 'chart.title.top.size';
        if (name == 'chart.title.color') name = 'chart.title.top.color';
        if (name == 'chart.title.bold')  name = 'chart.title.top.bold';

        this.properties[name] = value;
    }


    /**
    * An all encompassing accessor
    * 
    * @param string name The name of the property
    */
    RGraph.Gauge.prototype.Get = function (name)
    {
        return this.properties[name];
    }


    /**
    * The function you call to draw the line chart
    * 
    * @param bool An optional bool used internally to ditinguish whether the
    *             line chart is being called by the bar chart
    */
    RGraph.Gauge.prototype.Draw = function ()
    {
        /**
        * Fire the onbeforedraw event
        */
        RGraph.FireCustomEvent(this, 'onbeforedraw');


        /**
        * Store the value (for animation primarily
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
        this.centery = ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2) + this.gutterTop;
        this.radius  = Math.min(
                                ((this.canvas.width - this.gutterLeft - this.gutterRight) / 2),
                                ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2)
                               );
        this.startAngle = (HALFPI / 3) + HALFPI;
        this.endAngle   = TWOPI + HALFPI - (HALFPI / 3);



        /**
        * You can now override the positioning and radius if you so wish.
        */
        if (typeof(this.Get('chart.centerx')) == 'number') this.centerx = this.Get('chart.centerx');
        if (typeof(this.Get('chart.centery')) == 'number') this.centery = this.Get('chart.centery');
        if (typeof(this.Get('chart.radius')) == 'number')  this.radius = this.Get('chart.radius');



        // This has to be in the constructor
        this.centerpinRadius = 0.16 * this.radius;
        
        if (typeof(this.Get('chart.centerpin.radius')) == 'number') {
            this.centerpinRadius = this.Get('chart.centerpin.radius');
        }


        /**
        * Setup the context menu if required
        */
        if (this.Get('chart.contextmenu')) {
            RGraph.ShowContext(this);
        }



        // DRAW THE CHART HERE
        this.DrawBackGround();
        this.DrawGradient();
        this.DrawGradient();
        this.DrawColorBands();
        this.DrawSmallTickmarks();
        this.DrawMediumTickmarks();
        this.DrawBigTickmarks();
        this.DrawLabels();

        this.DrawTopTitle();
        this.DrawBottomTitle();

        this.DrawNeedle();
        this.DrawCenterpin();
        
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
    * Draw the background
    */
    RGraph.Gauge.prototype.DrawBackGround = function ()
    {
        // Shadow //////////////////////////////////////////////
        if (this.Get('chart.shadow')) {
            RGraph.SetShadow(this, this.Get('chart.shadow.color'), this.Get('chart.shadow.offsetx'), this.Get('chart.shadow.offsety'), this.Get('chart.shadow.blur'));
        }
        
        this.context.beginPath();
            this.context.fillStyle = 'white';
            //this.context.moveTo(this.centerx, this.centery)
            this.context.arc(this.centerx, this.centery, this.radius, 0, TWOPI, 0);
        this.context.fill();
        
        // Turn off the shadow
        RGraph.NoShadow(this);
        // Shadow //////////////////////////////////////////////


        var grad = this.context.createRadialGradient(this.centerx + 50, this.centery - 50, 0, this.centerx + 50, this.centery - 50, 150);
        grad.addColorStop(0, '#eee');
        grad.addColorStop(1, 'white');

        var borderWidth = this.Get('chart.border.width');

        this.context.beginPath();
            this.context.fillStyle = 'white';
            this.context.arc(this.centerx, this.centery, this.radius, 0, TWOPI, 0);
        this.context.fill();

        /**
        * Draw the gray circle
        */
        this.context.beginPath();
            this.context.fillStyle = this.Get('chart.border.outer');
            this.context.arc(this.centerx, this.centery, this.radius, 0, TWOPI, 0);
        this.context.fill();

        /**
        * Draw the light gray inner border
        */
        this.context.beginPath();
            this.context.fillStyle = this.Get('chart.border.inner');
            this.context.arc(this.centerx, this.centery, this.radius - borderWidth, 0, TWOPI, 0);
        this.context.fill();



        // Draw the white circle inner border
        this.context.beginPath();
            this.context.fillStyle = grad;
            this.context.arc(this.centerx, this.centery, this.radius - borderWidth - 4, 0, TWOPI, 0);
        this.context.fill();

        this.context.beginPath();
            this.context.strokeStyle = 'black';
            //this.context.moveTo(this.centerx, this.centery)
            this.context.arc(this.centerx, this.centery, this.radius, 0, TWOPI, 0);
        this.context.stroke();
    }


    /**
    * This function draws the smaller tickmarks
    */
    RGraph.Gauge.prototype.DrawSmallTickmarks = function ()
    {
        var numTicks = this.Get('chart.tickmarks.small');

        for (var i=0; i<=numTicks; ++i) {
            this.context.beginPath();
                this.context.strokeStyle = 'black';
                var a = (((this.endAngle - this.startAngle) / numTicks) * i) + this.startAngle;
                this.context.arc(this.centerx, this.centery, this.radius - this.Get('chart.border.width') - 10, a, a + 0.00001, 0);
                this.context.arc(this.centerx, this.centery, this.radius - this.Get('chart.border.width') - 10 - 5, a, a + 0.00001, 0);
            this.context.stroke();
        }
    }


    /**
    * This function draws the medium sized tickmarks
    */
    RGraph.Gauge.prototype.DrawMediumTickmarks = function ()
    {
        if (this.Get('chart.tickmarks.medium')) {

            var numTicks = this.Get('chart.tickmarks.medium');
            this.context.lineWidth = 3;
            this.context.lineCap   = 'round';
            this.context.strokeStyle = 'black';
    
            for (var i=0; i<=numTicks; ++i) {
                this.context.beginPath();
                    var a = (((this.endAngle - this.startAngle) / numTicks) * i) + this.startAngle + (((this.endAngle - this.startAngle) / (2 * numTicks)));
                    
                    if (a > this.startAngle && a< this.endAngle) {
                        this.context.arc(this.centerx, this.centery, this.radius - this.Get('chart.border.width') - 10, a, a + 0.00001, 0);
                        this.context.arc(this.centerx, this.centery, this.radius - this.Get('chart.border.width') - 10 - 6, a, a + 0.00001, 0);
                    }
                this.context.stroke();
            }
        }
    }


    /**
    * This function draws the large, bold tickmarks
    */
    RGraph.Gauge.prototype.DrawBigTickmarks = function ()
    {
        var numTicks = this.Get('chart.tickmarks.big');
        this.context.lineWidth = 3;
        this.context.lineCap   = 'round';

        for (var i=0; i<=numTicks; ++i) {
            this.context.beginPath();
                this.context.strokeStyle = 'black';
                var a = (((this.endAngle - this.startAngle) / numTicks) * i) + this.startAngle;
                this.context.arc(this.centerx, this.centery, this.radius - this.Get('chart.border.width') - 10, a, a + 0.00001, 0);
                this.context.arc(this.centerx, this.centery, this.radius - this.Get('chart.border.width') - 10 - 10, a, a + 0.00001, 0);
            this.context.stroke();
        }
    }


    /**
    * This function draws the centerpin
    */
    RGraph.Gauge.prototype.DrawCenterpin = function ()
    {
        var offset = 6;

        var grad = this.context.createRadialGradient(this.centerx + offset, this.centery - offset, 0, this.centerx + offset, this.centery - offset, 25);
        grad.addColorStop(0, '#ddf');
        grad.addColorStop(1, this.Get('chart.centerpin.color'));

        this.context.beginPath();
            this.context.fillStyle = grad;
            this.context.arc(this.centerx, this.centery, this.centerpinRadius, 0, TWOPI, 0);
        this.context.fill();
    }


    /**
    * This function draws the labels
    */
    RGraph.Gauge.prototype.DrawLabels = function ()
    {
        this.context.fillStyle = this.Get('chart.text.color');
        var font = this.Get('chart.text.font');
        var size = this.Get('chart.text.size');
        var num  = this.Get('chart.labels.count');

        this.context.beginPath();
            for (var i=0; i<=num; ++i) {
                var hyp = (this.radius - 35 - this.Get('chart.border.width'));
                var a   = (this.endAngle - this.startAngle) / num
                    a   = this.startAngle + (i * a);
                    a  -= HALFPI;

                var x = this.centerx - (Math.sin(a) * hyp);
                var y = this.centery + (Math.cos(a) * hyp);

                var hAlign = 'center';
                var vAlign = 'center'

                RGraph.Text(this.context,
                            font,
                            size,
                            x,
                            y,
                            RGraph.number_format(this, (((this.max - this.min) * (i / num)) + this.min).toFixed(this.Get('chart.scale.decimals')), this.Get('chart.units.pre'), this.Get('chart.units.post')),
                            vAlign,
                            hAlign);
            }
            //RGraph.Text(this.context, font, size, this.centerx - Math.sin(0.52) * (this.radius - 25 - this.Get('chart.border.width')),this.centery + Math.cos(0.52) * (this.radius - 25 - this.Get('chart.border.width')), RGraph.number_format(this, this.min.toFixed(this.Get('chart.scale.decimals')), this.Get('chart.units.pre'), this.Get('chart.units.post')), 'bottom', 'left');
            //RGraph.Text(this.context, font, size, this.centerx - this.radius + 25 + this.Get('chart.border.width'), this.centery,RGraph.number_format(this, (((this.max - this.min) * 0.2) + this.min).toFixed(this.Get('chart.scale.decimals')), this.Get('chart.units.pre'), this.Get('chart.units.post')),'center', 'left');
            //RGraph.Text(this.context, font, size, this.centerx - Math.sin(0.52) * (this.radius - 25 - this.Get('chart.border.width')),this.centery - Math.cos(0.52) * (this.radius - 25 - this.Get('chart.border.width')),RGraph.number_format(this, (((this.max - this.min) * 0.4) + this.min).toFixed(this.Get('chart.scale.decimals')), this.Get('chart.units.pre'), this.Get('chart.units.post')),'top', 'center');
            //RGraph.Text(this.context, font, size, this.centerx + Math.sin(0.52) * (this.radius - 25 - this.Get('chart.border.width')),this.centery - Math.cos(0.52) * (this.radius - 25 - this.Get('chart.border.width')),RGraph.number_format(this, (((this.max - this.min) * 0.6) + this.min).toFixed(this.Get('chart.scale.decimals')), this.Get('chart.units.pre'), this.Get('chart.units.post')),'top', 'center');
            //RGraph.Text(this.context, font, size, this.centerx + this.radius - 25 - this.Get('chart.border.width'), this.centery,RGraph.number_format(this, (((this.max - this.min) * 0.8) + this.min).toFixed(this.Get('chart.scale.decimals')), this.Get('chart.units.pre'), this.Get('chart.units.post')),'center', 'right');
            //RGraph.Text(this.context,font, size, this.centerx + Math.sin(0.52) * (this.radius - 25 - this.Get('chart.border.width')),this.centery + Math.cos(0.52) * (this.radius - 25 - this.Get('chart.border.width')),RGraph.number_format(this, this.max.toFixed(this.Get('chart.scale.decimals')), this.Get('chart.units.pre'), this.Get('chart.units.post')),'bottom', 'right');
        this.context.fill();


        /**
        * Draw the textual value if requested
        */
        if (this.Get('chart.value.text')) {

            var x = this.centerx;
            var y = this.centery + (this.Get('chart.value.text.y.pos') * this.radius);
            
            var units_pre  = typeof(this.Get('chart.value.text.units.pre')) == 'string' ? this.Get('chart.value.text.units.pre') : this.Get('chart.units.pre');
            var units_post = typeof(this.Get('chart.value.text.units.post')) == 'string' ? this.Get('chart.value.text.units.post') : this.Get('chart.units.post');
        
            this.context.beginPath();
                RGraph.Text(this.context,
                            font,
                            size + 2,
                            x,
                            y,
                            RGraph.number_format(this, this.value.toFixed(this.Get('chart.scale.decimals')), units_pre, units_post),
                            'center',
                            'center',
                            true,
                            null,
                            'white');
            this.context.fill();
        }
    }


    /**
    * This function draws the top title
    */
    RGraph.Gauge.prototype.DrawTopTitle = function ()
    {
        var x = this.centerx;
        var y = this.centery - 25;
        
        // Totally override the calculated positioning
        if (typeof(this.Get('chart.title.top.pos')) == 'number') {
            y = this.centery - (this.radius * this.Get('chart.title.top.pos'));
        }

        if (this.Get('chart.title.top')) {
            this.context.fillStyle = this.Get('chart.title.top.color');

            this.context.beginPath();
                RGraph.Text(this.context,
                            this.Get('chart.title.top.font'),
                            this.Get('chart.title.top.size'),
                            x,
                            y,
                            String(this.Get('chart.title.top')),
                            'bottom',
                            'center',
                            null,
                            null,
                            null,
                            this.Get('chart.title.top.bold'));
            this.context.fill();
        }
    }


    /**
    * This function draws the bottom title
    */
    RGraph.Gauge.prototype.DrawBottomTitle = function ()
    {
        var x = this.centerx;
        var y = this.centery + this.centerpinRadius + 10;

        // Totally override the calculated positioning
        if (typeof(this.Get('chart.title.bottom.pos')) == 'number') {
            y = this.centery + (this.radius * this.Get('chart.title.bottom.pos'));
        }

        if (this.Get('chart.title.bottom')) {
            this.context.fillStyle = this.Get('chart.title.bottom.color');

            this.context.beginPath();
                RGraph.Text(this.context,
                            this.Get('chart.title.bottom.font'),
                            this.Get('chart.title.bottom.size'),
                            x,
                            y,
                            String(this.Get('chart.title.bottom')),
                            'top',
                            'center',
                            null,
                            null,
                            null,
                            this.Get('chart.title.bottom.bold'));
            this.context.fill();
        }
    }


    /**
    * This function draws the Needle
    */
    RGraph.Gauge.prototype.DrawNeedle = function ()
    {
        this.context.lineWidth   = 0.5;
        this.context.strokeStyle = '#983724';
        this.context.fillStyle   = this.Get('chart.needle.color');

        var angle = (this.endAngle - this.startAngle) * ((this.value - this.min) / (this.max - this.min));
            angle += this.startAngle;


        this.context.beginPath();
            this.context.arc(this.centerx, this.centery, this.radius - 25 - this.Get('chart.border.width'), angle, angle + 0.00001, false);
            this.context.arc(this.centerx, this.centery, this.centerpinRadius * 0.5, angle + HALFPI, angle + 0.00001 + HALFPI, false);
            
            if (this.Get('chart.needle.tail')) {
                this.context.arc(this.centerx, this.centery, this.radius * 0.2  , angle + PI, angle + 0.00001 + PI, false);
            }

            this.context.arc(this.centerx, this.centery, this.centerpinRadius * 0.5, angle - HALFPI, angle - 0.00001 - HALFPI, false);
        this.context.stroke();
        this.context.fill();
        
        /**
        * Store the angle in an object variable
        */
        this.angle = angle;
    }


    /**
    * This draws the green background to the tickmarks
    */
    RGraph.Gauge.prototype.DrawColorBands = function ()
    {
        if (RGraph.is_array(this.Get('chart.colors.ranges'))) {

            var ranges = this.Get('chart.colors.ranges');

            for (var i=0; i<ranges.length; ++i) {

                //this.context.strokeStyle = this.Get('chart.strokestyle') ? this.Get('chart.strokestyle') : ranges[i][2];
                this.context.fillStyle = ranges[i][2];
                this.context.lineWidth = 0;//this.Get('chart.linewidth.segments');

                this.context.beginPath();
                    this.context.arc(this.centerx,
                                     this.centery,
                                     this.radius - 10 - this.Get('chart.border.width'),
                                     (((ranges[i][0] - this.min) / (this.max - this.min)) * (this.endAngle - this.startAngle)) + this.startAngle,
                                     (((ranges[i][1] - this.min) / (this.max - this.min)) * (this.endAngle - this.startAngle)) + this.startAngle,
                                     false);

                    this.context.arc(this.centerx,
                                     this.centery,
                                     this.radius - 20 - this.Get('chart.border.width'),
                                     (((ranges[i][1] - this.min) / (this.max - this.min)) * (this.endAngle - this.startAngle)) + this.startAngle,
                                     (((ranges[i][0] - this.min) / (this.max - this.min)) * (this.endAngle - this.startAngle)) + this.startAngle,
                                     true);
                this.context.closePath();
                this.context.fill();
            }

            return;
        }




        /**
        * Draw the GREEN region
        */
        this.context.strokeStyle = this.Get('chart.green.color');
        this.context.fillStyle = this.Get('chart.green.color');
        
        var greenStart = this.startAngle;
        var greenEnd   = this.startAngle + (this.endAngle - this.startAngle) * ((this.Get('chart.green.end') - this.min) / (this.max - this.min))

        this.context.beginPath();
            this.context.arc(this.centerx, this.centery, this.radius - 10 - this.Get('chart.border.width'), greenStart, greenEnd, false);
            this.context.arc(this.centerx, this.centery, this.radius - 20 - this.Get('chart.border.width'), greenEnd, greenStart, true);
        this.context.fill();


        /**
        * Draw the YELLOW region
        */
        this.context.strokeStyle = this.Get('chart.yellow.color');
        this.context.fillStyle = this.Get('chart.yellow.color');
        
        var yellowStart = greenEnd;
        var yellowEnd   = this.startAngle + (this.endAngle - this.startAngle) * ((this.Get('chart.red.start') - this.min) / (this.max - this.min))

        this.context.beginPath();
            this.context.arc(this.centerx, this.centery, this.radius - 10 - this.Get('chart.border.width'), yellowStart, yellowEnd, false);
            this.context.arc(this.centerx, this.centery, this.radius - 20 - this.Get('chart.border.width'), yellowEnd, yellowStart, true);
        this.context.fill();


        /**
        * Draw the RED region
        */
        this.context.strokeStyle = this.Get('chart.red.color');
        this.context.fillStyle = this.Get('chart.red.color');
        
        var redStart = yellowEnd;
        var redEnd   = this.startAngle + (this.endAngle - this.startAngle) * ((this.max - this.min) / (this.max - this.min))

        this.context.beginPath();
            this.context.arc(this.centerx, this.centery, this.radius - 10 - this.Get('chart.border.width'), redStart, redEnd, false);
            this.context.arc(this.centerx, this.centery, this.radius - 20 - this.Get('chart.border.width'), redEnd, redStart, true);
        this.context.fill();
    }



    /**
    * A placeholder function
    * 
    * @param object The event object
    */
    RGraph.Gauge.prototype.getShape = function (e)
    {
    }



    /**
    * A getValue method
    * 
    * @param object e An event object
    */
    RGraph.Gauge.prototype.getValue = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e);
        var mouseX  = mouseXY[0];
        var mouseY  = mouseXY[1];

        var angle = RGraph.getAngleByXY(this.centerx, this.centery, mouseX, mouseY);

        if (angle >= 0 && angle <= HALFPI) {
            angle += TWOPI;
        }

        var value = ((angle - this.startAngle) / (this.endAngle - this.startAngle)) * (this.max - this.min);
            value = value + this.min;

        if (value < this.min) {
            value = this.min
        }

        if (value > this.max) {
            value = this.max
        }

        return value;
    }



    /**
    * The getObjectByXY() worker method. Don't call this call:
    * 
    * RGraph.ObjectRegistry.getObjectByXY(e)
    * 
    * @param object e The event object
    */
    RGraph.Gauge.prototype.getObjectByXY = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e);

        if (
               mouseXY[0] > (this.centerx - this.radius)
            && mouseXY[0] < (this.centerx + this.radius)
            && mouseXY[1] > (this.centery - this.radius)
            && mouseXY[1] < (this.centery + this.radius)
            && RGraph.getHypLength(this.centerx, this.centery, mouseXY[0], mouseXY[1]) <= this.radius
            ) {

            return this;
        }
    }



    /**
    * This draws the gradient that goes around the Gauge chart
    */
    RGraph.Gauge.prototype.DrawGradient = function ()
    {
        if (this.Get('chart.border.gradient')) {
            var context = this.context;
            
            context.beginPath();
    
                var grad = context.createRadialGradient(this.centerx, this.centery, this.radius, this.centerx, this.centery, this.radius - 15);
                grad.addColorStop(0, 'gray');
                grad.addColorStop(1, 'white');
    
                context.fillStyle = grad;
                context.arc(this.centerx, this.centery, this.radius, 0, TWOPI, false)
                context.arc(this.centerx, this.centery, this.radius - 15, TWOPI,0, true)
            context.fill();
        }
    }



    /**
    * This method handles the adjusting calculation for when the mouse is moved
    * 
    * @param object e The event object
    */
    RGraph.Gauge.prototype.Adjusting_mousemove = function (e)
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