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
    * The odometer constructor. Pass it the ID of the canvas tag, the start value of the odo,
    * the end value, and the value that the pointer should point to.
    * 
    * @param string id    The ID of the canvas tag
    * @param int    start The start value of the Odo
    * @param int    end   The end value of the odo
    * @param int    value The indicated value (what the needle points to)
    */
    RGraph.Odometer = function (id, start, end, value)
    {
        this.id                = id
        this.canvas            = document.getElementById(id);
        this.context           = this.canvas.getContext('2d');
        this.canvas.__object__ = this;
        this.type              = 'odo';
        this.isRGraph          = true;
        this.start             = start;
        this.min               = start;
        this.end               = end;
        this.max               = end;
        this.value             = value;
        this.currentValue      = null;
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();


        /**
        * Compatibility with older browsers
        */
        RGraph.OldBrowserCompat(this.context);


        this.properties = {
            'chart.centerx':                null,
            'chart.centery':                null,
            'chart.radius':                 null,
            'chart.value.text':             false,
            'chart.value.text.decimals':    0,
            'chart.needle.color':           'black',
            'chart.needle.width':           2,
            'chart.needle.head':            true,
            'chart.needle.tail':            true,
            'chart.needle.type':            'pointer',
            'chart.needle.extra':            [],
            'chart.needle.triangle.border': '#aaa',
            'chart.text.size':              10,
            'chart.text.color':             'black',
            'chart.text.font':              'Arial',
            'chart.green.max':              end * 0.75,
            'chart.red.min':                end * 0.9,
            'chart.green.color':            'green',
            'chart.yellow.color':           'yellow',
            'chart.red.color':              'red',
            'chart.green.solid':            false,
            'chart.yellow.solid':           false,
            'chart.red.solid':              false,
            'chart.label.area':             35,
            'chart.gutter.left':            25,
            'chart.gutter.right':           25,
            'chart.gutter.top':             25,
            'chart.gutter.bottom':          25,
            'chart.title':                  '',
            'chart.title.background':       null,
            'chart.title.hpos':             null,
            'chart.title.vpos':             null,
            'chart.title.font':             null,
            'chart.title.bold':             true,
            'chart.contextmenu':            null,
            'chart.linewidth':              1,
            'chart.shadow.inner':           false,
            'chart.shadow.inner.color':     'black',
            'chart.shadow.inner.offsetx':   3,
            'chart.shadow.inner.offsety':   3,
            'chart.shadow.inner.blur':      6,
            'chart.shadow.outer':           false,
            'chart.shadow.outer.color':     '#666',
            'chart.shadow.outer.offsetx':   0,
            'chart.shadow.outer.offsety':   0,
            'chart.shadow.outer.blur':      15,
            'chart.annotatable':            false,
            'chart.annotate.color':         'black',
            'chart.scale.decimals':         0,
            'chart.scale.point':            '.',
            'chart.scale.thousand':         ',',
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
            'chart.units.pre':              '',
            'chart.units.post':             '',
            'chart.border':                 false,
            'chart.border.color1':          '#BEBCB0',
            'chart.border.color2':          '#F0EFEA',
            'chart.border.color3':          '#BEBCB0',
            'chart.tickmarks':              true,
            'chart.tickmarks.highlighted':  false,
            'chart.zerostart':              false,
            'chart.labels':                 null,
            'chart.units.pre':              '',
            'chart.units.post':             '',
            'chart.value.units.pre':        '',
            'chart.value.units.post':       '',
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
            'chart.key.colors':             null,
            'chart.adjustable':             false
        }



        /**
        * Register the object
        */
        RGraph.Register(this);
    }


    /**
    * A peudo setter
    * 
    * @param name  string The name of the property to set
    * @param value mixed  The value of the property
    */
    RGraph.Odometer.prototype.Set = function (name, value)
    {
        if (name == 'chart.needle.style') {
            alert('[RGRAPH] The RGraph property chart.needle.style has changed to chart.needle.color');
        }

        if (name == 'chart.needle.thickness') {
            name = 'chart.needle.width';
        }

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
    RGraph.Odometer.prototype.Get = function (name)
    {
        if (name == 'chart.value') {
            return this.value;
        }

        return this.properties[name.toLowerCase()];
    }


    /**
    * Draws the odometer
    */
    RGraph.Odometer.prototype.Draw = function ()
    {
        /**
        * Fire the onbeforedraw event
        */
        RGraph.FireCustomEvent(this, 'onbeforedraw');

        
        /**
        * Set the current value
        */
        this.currentValue = this.value;

        /**
        * No longer allow values outside the range of the Odo
        */
        if (this.value > this.end) {
            this.value = this.end;
        }

        if (this.value < this.start) {
            this.value = this.start;
        }

        /**
        * This is new in May 2011 and facilitates indiviual gutter settings,
        * eg chart.gutter.left
        */
        this.gutterLeft   = this.Get('chart.gutter.left');
        this.gutterRight  = this.Get('chart.gutter.right');
        this.gutterTop    = this.Get('chart.gutter.top');
        this.gutterBottom = this.Get('chart.gutter.bottom');

        // Work out a few things
        this.radius   = Math.min(
                                 (this.canvas.width - this.gutterLeft - this.gutterRight) / 2,
                                 (this.canvas.height - this.gutterTop - this.gutterBottom) / 2
                                )
                                - (this.Get('chart.border') ? 25 : 0);
        this.diameter = 2 * this.radius;
        this.centerx  = ((this.canvas.width - this.gutterLeft- this.gutterRight) / 2) + this.gutterLeft;
        this.centery  = ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2) + this.gutterTop;
        this.range    = this.end - this.start;
        
        /**
        * Move the centerx if the key is defined
        */
        if (this.Get('chart.key') && this.Get('chart.key').length > 0 && this.canvas.width > this.canvas.height) {
            this.centerx = 5 + this.radius;
        }



        /**
        * Allow custom setting of the chart.centerx value
        */
        if (typeof(this.Get('chart.centerx')) == 'number') {
            this.centerx = this.Get('chart.centerx');
        }

        
        /**
        * Allow custom setting of the chart.centery value
        */
        if (typeof(this.Get('chart.centery')) == 'number') {
            this.centery = this.Get('chart.centery');
        }

        
        /**
        * Allow custom setting of the radius
        */
        if (typeof(this.Get('chart.radius')) == 'number') {
            this.radius = this.Get('chart.radius');
            
            if (this.Get('chart.border')) {
                this.radius -= 25;
            }
        }



        this.context.lineWidth = this.Get('chart.linewidth');

        // Draw the background
        this.DrawBackground();

        // And lastly, draw the labels
        this.DrawLabels();

        // Draw the needle
        this.DrawNeedle(this.value, this.Get('chart.needle.color'));
        
        /**
        * Draw any extra needles
        */
        if (this.Get('chart.needle.extra').length > 0) {
            for (var i=0; i<this.Get('chart.needle.extra').length; ++i) {
                var needle = this.Get('chart.needle.extra')[i];
                this.DrawNeedle(needle[0], needle[1], needle[2]);
            }
        }
        
        /**
        * Draw the key if requested
        */
        if (this.Get('chart.key') && this.Get('chart.key').length > 0) {
            // Build a colors array out of the needle colors
            var colors = [this.Get('chart.needle.color')];
            
            if (this.Get('chart.needle.extra').length > 0) {
                for (var i=0; i<this.Get('chart.needle.extra').length; ++i) {
                    var needle = this.Get('chart.needle.extra')[i];
                    colors.push(needle[1]);
                }
            }

            RGraph.DrawKey(this, this.Get('chart.key'), colors);
        }
        
        
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
        * This installs the event listeners
        */
        RGraph.InstallEventListeners(this);
        
        /**
        * Fire the RGraph ondraw event
        */
        RGraph.FireCustomEvent(this, 'ondraw');
    }

    /**
    * Draws the background
    */
    RGraph.Odometer.prototype.DrawBackground = function ()
    {
        this.context.beginPath();

        /**
        * Turn on the shadow if need be
        */
        if (this.Get('chart.shadow.outer')) {
            RGraph.SetShadow(this, this.Get('chart.shadow.outer.color'), this.Get('chart.shadow.outer.offsetx'), this.Get('chart.shadow.outer.offsety'), this.Get('chart.shadow.outer.blur'));
        }

        var backgroundColor = '#eee';

        // Draw the grey border
        this.context.fillStyle = backgroundColor;
        this.context.arc(this.centerx, this.centery, this.radius, 0.0001, TWOPI, false);
        this.context.fill();

        /**
        * Turn off the shadow
        */
        RGraph.NoShadow(this);


        // Draw a circle
        this.context.strokeStyle = '#666';
        this.context.arc(this.centerx, this.centery, this.radius, 0, TWOPI, false);

        // Now draw a big white circle to make the lines appear as tick marks
        // This is solely for Chrome
        this.context.fillStyle = backgroundColor;
        this.context.arc(this.centerx, this.centery, this.radius, 0, TWOPI, false);
        this.context.fill();

        /**
        * Draw more tickmarks
        */
        if (this.Get('chart.tickmarks')) {
            this.context.beginPath();
            this.context.strokeStyle = '#bbb';
        
            for (var i=0; i<=360; i+=3) {
                this.context.arc(this.centerx, this.centery, this.radius, 0, i / 57.3, false);
                this.context.lineTo(this.centerx, this.centery);
            }
            this.context.stroke();
        }

        this.context.beginPath();
        this.context.lineWidth = 1;
        this.context.strokeStyle = 'black';

        // Now draw a big white circle to make the lines appear as tick marks
        this.context.fillStyle = backgroundColor;
        this.context.strokeStyle = backgroundColor;
        this.context.arc(this.centerx, this.centery, this.radius - 5, 0, TWOPI, false);
        this.context.fill();
        this.context.stroke();

        // Gray lines at 18 degree intervals
        this.context.beginPath();
        this.context.strokeStyle = '#ddd';
        for (var i=0; i<360; i+=18) {
            this.context.arc(this.centerx, this.centery, this.radius, 0, RGraph.degrees2Radians(i), false);
            this.context.lineTo(this.centerx, this.centery);
        }
        this.context.stroke();
        
        // Redraw the outer circle
        this.context.beginPath();
        this.context.strokeStyle = 'black';
        this.context.arc(this.centerx, this.centery, this.radius, 0, TWOPI, false);
        this.context.stroke();

        /**
        * Now draw the center bits shadow if need be
        */
        if (this.Get('chart.shadow.inner')) {
            this.context.beginPath();
            RGraph.SetShadow(this, this.Get('chart.shadow.inner.color'), this.Get('chart.shadow.inner.offsetx'), this.Get('chart.shadow.inner.offsety'), this.Get('chart.shadow.inner.blur'));
            this.context.arc(this.centerx, this.centery, this.radius - this.Get('chart.label.area'), 0, TWOPI, 0);
            this.context.fill();
            this.context.stroke();
    
            /**
            * Turn off the shadow
            */
            RGraph.NoShadow(this);
        }

        /*******************************************************
        * Draw the green area
        *******************************************************/
        if (this.Get('chart.green.solid')) {
            var greengrad = this.Get('chart.green.color');

        } else {
            var greengrad = this.context.createRadialGradient(this.centerx,this.centery,0,this.centerx,this.centery,this.radius);
            greengrad.addColorStop(0, 'white');
            greengrad.addColorStop(1, this.Get('chart.green.color'));
        }

        // Draw the "tick highlight"
        if (this.Get('chart.tickmarks.highlighted')) {
            this.context.beginPath();
            this.context.lineWidth = 5;
            this.context.strokeStyle = greengrad;
            this.context.arc(this.centerx, this.centery, this.radius - 2.5,
            
                -1 * HALFPI,
                (((this.Get('chart.green.max') - this.start)/ (this.end - this.start)) * TWOPI) - HALFPI,
                0);

            this.context.stroke();
            
            this.context.lineWidth = 1;
        }

        this.context.beginPath();
            this.context.fillStyle = greengrad;
            this.context.arc(
                             this.centerx,
                             this.centery,
                             this.radius - this.Get('chart.label.area'),
                             0 - HALFPI,
                             (((this.Get('chart.green.max') - this.start)/ (this.end - this.start)) * TWOPI) - HALFPI,
                             false
                            );
            this.context.lineTo(this.centerx, this.centery);
        this.context.closePath();
        this.context.fill();


        /*******************************************************
        * Draw the yellow area
        *******************************************************/
        if (this.Get('chart.yellow.solid')) {
            var yellowgrad = this.Get('chart.yellow.color');

        } else {
            var yellowgrad = this.context.createRadialGradient(this.centerx,this.centery,0,this.centerx,this.centery,this.radius);
            yellowgrad.addColorStop(0, 'white');
            yellowgrad.addColorStop(1, this.Get('chart.yellow.color'));
        }

        // Draw the "tick highlight"
        if (this.Get('chart.tickmarks.highlighted')) {
            this.context.beginPath();
            this.context.lineWidth = 5;
            this.context.strokeStyle = yellowgrad;
            this.context.arc(this.centerx, this.centery, this.radius - 2.5, (
            
                ((this.Get('chart.green.max') - this.start) / (this.end - this.start)) * TWOPI) - HALFPI,
                (((this.Get('chart.red.min') - this.start) / (this.end - this.start)) * TWOPI) - HALFPI,
                0);

            this.context.stroke();
            
            this.context.lineWidth = 1;
        }

        this.context.beginPath();
            this.context.fillStyle = yellowgrad;
            this.context.arc(
                             this.centerx,
                             this.centery,
                             this.radius - this.Get('chart.label.area'),
                             ( ((this.Get('chart.green.max') -this.start) / (this.end - this.start)) * TWOPI) - HALFPI,
                             ( ((this.Get('chart.red.min') - this.start) / (this.end - this.start)) * TWOPI) - HALFPI,
                             false
                            );
            this.context.lineTo(this.centerx, this.centery);
        this.context.closePath();
        this.context.fill();

        /*******************************************************
        * Draw the red area
        *******************************************************/
        if (this.Get('chart.red.solid')) {
            var redgrad = this.Get('chart.red.color');

        } else {
            var redgrad = this.context.createRadialGradient(this.centerx,
                                                            this.centery,
                                                            0,
                                                            this.centerx,
                                                            this.centery,
                                                            this.radius);
            redgrad.addColorStop(0, 'white');
            redgrad.addColorStop(1, this.Get('chart.red.color'));
        }


        // Draw the "tick highlight"
        if (this.Get('chart.tickmarks.highlighted')) {
            this.context.beginPath();
            this.context.lineWidth = 5;
            this.context.strokeStyle = redgrad;
            this.context.arc(this.centerx, this.centery, this.radius - 2.5,(((this.Get('chart.red.min') - this.start) / (this.end - this.start)) * TWOPI) - HALFPI,TWOPI - HALFPI,0);
            this.context.stroke();
            
            this.context.lineWidth = 1;
        }

        this.context.beginPath();
            this.context.fillStyle = redgrad;
            this.context.strokeStyle = redgrad;
            this.context.arc(
                             this.centerx,
                             this.centery,
                             this.radius - this.Get('chart.label.area'),
                             (((this.Get('chart.red.min') - this.start) / (this.end - this.start)) * TWOPI) - HALFPI,
                             TWOPI - HALFPI,
                             false
                            );
            this.context.lineTo(this.centerx, this.centery);
        this.context.closePath();
        this.context.fill();


        /**
        * Draw the thick border
        */
        if (this.Get('chart.border')) {            

            var grad = this.context.createRadialGradient(this.centerx, this.centery, this.radius, this.centerx, this.centery, this.radius + 20);
            grad.addColorStop(0, this.Get('chart.border.color1'));
            grad.addColorStop(0.5, this.Get('chart.border.color2'));
            grad.addColorStop(1, this.Get('chart.border.color3'));

            
            this.context.beginPath();
                this.context.fillStyle = grad;
                this.context.strokeStyle = 'rgba(0,0,0,0)'
                this.context.lineWidth = 0.001;
                this.context.arc(this.centerx, this.centery, this.radius + 20, 0, TWOPI, 0);
                this.context.arc(this.centerx, this.centery, this.radius - 2, TWOPI, 0, 1);
            this.context.fill();
        }
        
        // Put the linewidth back to what it was
        this.context.lineWidth = this.Get('chart.linewidth');


        /**
        * Draw the title if specified
        */
        if (this.Get('chart.title')) {
            RGraph.DrawTitle(this,
                             this.Get('chart.title'),
                             this.centery - this.radius,
                             null,
                             this.Get('chart.title.size') ? this.Get('chart.title.size') : this.Get('chart.text.size') + 2);
        }


        // Draw the big tick marks
        if (!this.Get('chart.tickmarks.highlighted')) {
            for (var i=18; i<=360; i+=36) {
                this.context.beginPath();
                    this.context.strokeStyle = '#999';
                    this.context.lineWidth = 2;
                    this.context.arc(this.centerx, this.centery, this.radius - 1, RGraph.degrees2Radians(i), RGraph.degrees2Radians(i+0.01), false);
                    this.context.arc(this.centerx, this.centery, this.radius - 7, RGraph.degrees2Radians(i), RGraph.degrees2Radians(i+0.01), false);
                this.context.stroke();
            }
        }
    }


    /**
    * Draws the needle of the odometer
    * 
    * @param number value The value to represent
    * @param string color The color of the needle
    * @param number       The OPTIONAL length of the needle
    */
    RGraph.Odometer.prototype.DrawNeedle = function (value, color)
    {
        // The optional length of the needle
        var length = arguments[2] ? arguments[2] : this.radius - this.Get('chart.label.area');

        // ===== First draw a grey background circle =====
        
        this.context.fillStyle = '#999';

        this.context.beginPath();
            this.context.moveTo(this.centerx, this.centery);
            this.context.arc(this.centerx, this.centery, 10, 0, TWOPI, false);
            this.context.fill();
        this.context.closePath();

        this.context.fill();

        // ===============================================
        
        this.context.fillStyle = color
        this.context.strokeStyle = '#666';

        // Draw the centre bit
        this.context.beginPath();
            this.context.moveTo(this.centerx, this.centery);
            this.context.arc(this.centerx, this.centery, 8, 0, TWOPI, false);
            this.context.fill();
        this.context.closePath();
        
        this.context.stroke();
        this.context.fill();

        if (this.Get('chart.needle.type') == 'pointer') {

            this.context.strokeStyle = color;
            this.context.lineWidth   = this.Get('chart.needle.width');
            this.context.lineCap     = 'round';
            this.context.lineJoin    = 'round';
            
            // Draw the needle
            this.context.beginPath();
                // The trailing bit on the opposite side of the dial
                this.context.beginPath();
                    this.context.moveTo(this.centerx, this.centery);
                    
                    if (this.Get('chart.needle.tail')) {

                        this.context.arc(this.centerx,
                                         this.centery,
                                         20,
                                          (((value / this.range) * 360) + 90) / (180 / PI),
                                         (((value / this.range) * 360) + 90 + 0.01) / (180 / PI), // The 0.01 avoids a bug in ExCanvas and Chrome 6
                                         false
                                        );
                    }

                // Draw the long bit on the opposite side
                this.context.arc(this.centerx,
                                 this.centery,
                                 length - 10,
                                 (((value / this.range) * 360) - 90) / (180 / PI),
                                 (((value / this.range) * 360) - 90 + 0.1 ) / (180 / PI), // The 0.1 avoids a bug in ExCanvas and Chrome 6
                                 false
                                );
            this.context.closePath();
            
            //this.context.stroke();
            //this.context.fill();
        

        } else if (this.Get('chart.needle.type') == 'triangle') {

            this.context.lineWidth = 0.01;
            this.context.lineEnd  = 'square';
            this.context.lineJoin = 'miter';

            /**
            * This draws the version of the pointer that becomes the border
            */
            this.context.beginPath();
                this.context.fillStyle = this.Get('chart.needle.triangle.border');
                this.context.arc(this.centerx, this.centery, 11, (((value / this.range) * 360)) / 57.3, ((((value / this.range) * 360)) + 0.01) / 57.3, 0);
                this.context.arc(this.centerx, this.centery, 11, (((value / this.range) * 360) + 180) / 57.3, ((((value / this.range) * 360) + 180) + 0.01)/ 57.3, 0);
                this.context.arc(this.centerx, this.centery, length - 5, (((value / this.range) * 360) - 90) / 57.3, ((((value / this.range) * 360) - 90) / 57.3) + 0.01, 0);
            this.context.closePath();
            this.context.fill();

            this.context.beginPath();
                this.context.arc(this.centerx, this.centery, 15, 0, TWOPI, 0);
            this.context.closePath();
            this.context.fill();

            // This draws the pointer
            this.context.beginPath();
                this.context.strokeStyle = 'black';
                this.context.fillStyle = color;
                this.context.arc(this.centerx, this.centery, 7, (((value / this.range) * 360)) / 57.3, ((((value / this.range) * 360)) + 0.01) / 57.3, 0);
                this.context.arc(this.centerx, this.centery, 7, (((value / this.range) * 360) + 180) / 57.3, ((((value / this.range) * 360) + 180) + 0.01)/ 57.3, 0);
                this.context.arc(this.centerx, this.centery, length - 13, (((value / this.range) * 360) - 90) / 57.3, ((((value / this.range) * 360) - 90) / 57.3) + 0.01, 0);
            this.context.closePath();
            this.context.stroke();
            this.context.fill();

            /**
            * This is here to accomodate the MSIE/ExCanvas combo
            */
            this.context.beginPath();
                this.context.arc(this.centerx, this.centery, 7, 0, TWOPI, 0);
            this.context.closePath();
            this.context.fill();
        }


        this.context.stroke();
        this.context.fill();

        // Draw the mini center circle
        this.context.beginPath();
        this.context.fillStyle = color;
            this.context.arc(this.centerx, this.centery, this.Get('chart.needle.type') == 'pointer' ? 7 : 12, 0.01, TWOPI, false);
        this.context.fill();

        // This draws the arrow at the end of the line
        if (this.Get('chart.needle.head') && this.Get('chart.needle.type') == 'pointer') {
            this.context.lineWidth = 1;
            this.context.fillStyle = color;

            // round, bevel, miter
            this.context.lineJoin = 'miter';
            this.context.lineCap  = 'butt';

            this.context.beginPath();
                this.context.arc(this.centerx, this.centery, length - 5, (((value / this.range) * 360) - 90) / 57.3, (((value / this.range) * 360) - 90 + 0.1) / 57.3, false);

                this.context.arc(this.centerx,
                                 this.centery,
                                 length - 20,
                                 RGraph.degrees2Radians( ((value / this.range) * 360) - (length < 60 ? 80 : 85) ),
                                 RGraph.degrees2Radians( ((value / this.range) * 360) - (length < 60 ? 100 : 95) ),
                                 1);
            this.context.closePath();
    
            this.context.fill();
            //this.context.stroke();
        }
        
        /**
        * Draw a white circle at the centre
        */
        this.context.beginPath();
        this.context.fillStyle = 'gray';
            this.context.moveTo(this.centerx, this.centery);
            this.context.arc(this.centerx,this.centery,2,0,6.2795,false);
        this.context.closePath();

        this.context.fill();
    }
    
    /**
    * Draws the labels for the Odo
    */
    RGraph.Odometer.prototype.DrawLabels = function ()
    {
        var context   = this.context;
        var size      = this.Get('chart.text.size');
        var font      = this.Get('chart.text.font');
        var centerx   = this.centerx;
        var centery   = this.centery;
        var r         = this.radius - (this.Get('chart.label.area') / 2);
        var start     = this.start;
        var end       = this.end;
        var decimals  = this.Get('chart.scale.decimals');
        var labels    = this.Get('chart.labels');
        var units_pre = this.Get('chart.units.pre');
        var units_post = this.Get('chart.units.post');

        context.beginPath();
        context.fillStyle = this.Get('chart.text.color');

        /**
        * If label are specified, use those
        */
        if (labels) {
            for (var i=0; i<labels.length; ++i) {

                RGraph.Text(context,
                            font,
                            size,
                            centerx + (Math.cos(((i / labels.length) * TWOPI) - HALFPI) * (this.radius - (this.Get('chart.label.area') / 2) ) ), // Sin A = Opp / Hyp
                            centery + (Math.sin(((i / labels.length) * TWOPI) - HALFPI) * (this.radius - (this.Get('chart.label.area') / 2) ) ), // Cos A = Adj / Hyp
                            String(labels[i]),
                            'center',
                            'center');
            }

        /**
        * If not, use the maximum value
        */
        } else {
            RGraph.Text(context, font, size, centerx + (0.588 * r ), centery - (0.809 * r ), RGraph.number_format(this, (((end - start) * (1/10)) + start).toFixed(decimals), units_pre, units_post), 'center', 'center', false, 36);
            RGraph.Text(context, font, size, centerx + (0.951 * r ), centery - (0.309 * r), RGraph.number_format(this, (((end - start) * (2/10)) + start).toFixed(decimals), units_pre, units_post), 'center', 'center', false, 72);
            RGraph.Text(context, font, size, centerx + (0.949 * r), centery + (0.287 * r), RGraph.number_format(this, (((end - start) * (3/10)) + start).toFixed(decimals), units_pre, units_post), 'center', 'center', false, 108);
            RGraph.Text(context, font, size, centerx + (0.588 * r ), centery + (0.809 * r ), RGraph.number_format(this, (((end - start) * (4/10)) + start).toFixed(decimals), units_pre, units_post), 'center', 'center', false, 144);
            RGraph.Text(context, font, size, centerx, centery + r, RGraph.number_format(this, (((end - start) * (5/10)) + start).toFixed(decimals),units_pre, units_post), 'center', 'center', false, 180);
            RGraph.Text(context, font, size, centerx - (0.588 * r ), centery + (0.809 * r ), RGraph.number_format(this, (((end - start) * (6/10)) + start).toFixed(decimals), units_pre, units_post), 'center', 'center', false, 216);
            RGraph.Text(context, font, size, centerx - (0.949 * r), centery + (0.300 * r), RGraph.number_format(this, (((end - start) * (7/10)) + start).toFixed(decimals), units_pre, units_post), 'center', 'center', false, 252);
            RGraph.Text(context, font, size, centerx - (0.951 * r), centery - (0.309 * r), RGraph.number_format(this, (((end - start) * (8/10)) + start).toFixed(decimals), units_pre, units_post), 'center', 'center', false, 288);
            RGraph.Text(context, font, size, centerx - (0.588 * r ), centery - (0.809 * r ), RGraph.number_format(this, (((end - start) * (9/10)) + start).toFixed(decimals), units_pre, units_post), 'center', 'center', false, 324);
            RGraph.Text(context, font, size, centerx, centery - r, this.Get('chart.zerostart') ? RGraph.number_format(this, this.start.toFixed(decimals), units_pre, units_post) : RGraph.number_format(this, (((end - start) * (10/10)) + start).toFixed(decimals), units_pre, units_post), 'center', 'center', false, 360);
        }
        
        this.context.fill();
        
        /**
        * Draw the text label below the center point
        */
        if (this.Get('chart.value.text')) {
            context.strokeStyle = 'black';
            RGraph.Text(context, font, size + 2, centerx, centery + size + 2 + 10, String(this.Get('chart.value.units.pre') + this.value.toFixed(this.Get('chart.value.text.decimals')) + this.Get('chart.value.units.post')), 'center', 'center', true,  null, 'white');
        }
    }



    /**
    * A placeholder function
    * 
    * @param object The event object
    */
    RGraph.Odometer.prototype.getShape = function (e)
    {
    }



    /**
    * This function returns the pertinent value at the point of click
    * 
    * @param object The event object
    */
    RGraph.Odometer.prototype.getValue = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e)
        var angle   = RGraph.getAngleByXY(this.centerx, this.centery, mouseXY[0], mouseXY[1]);
            angle  += HALFPI;
        
        if (mouseXY[0] >= this.centerx && mouseXY[1] <= this.centery) {
            angle -= TWOPI;
        }

        var value = ((angle / TWOPI) * (this.max - this.min)) + this.min;

        return value;
    }



    /**
    * The getObjectByXY() worker method. Don't call this call:
    * 
    * RGraph.ObjectRegistry.getObjectByXY(e)
    * 
    * @param object e The event object
    */
    RGraph.Odometer.prototype.getObjectByXY = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e);
        var radius  = RGraph.getHypLength(this.centerx, this.centery, mouseXY[0], mouseXY[1]);

        if (
               mouseXY[0] > (this.centerx - this.radius)
            && mouseXY[0] < (this.centerx + this.radius)
            && mouseXY[1] > (this.centery - this.radius)
            && mouseXY[1] < (this.centery + this.radius)
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
    RGraph.Odometer.prototype.Adjusting_mousemove = function (e)
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