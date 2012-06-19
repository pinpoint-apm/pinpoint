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
    * The pie chart constructor
    * 
    * @param data array The data to be represented on the Pie chart
    */
    RGraph.Pie = function (id, data)
    {
        this.id                = id;
        this.canvas            = document.getElementById(id);
        this.context           = this.canvas.getContext("2d");
        this.canvas.__object__ = this;
        this.total             = 0;
        this.subTotal          = 0;
        this.angles            = [];
        this.data              = data;
        this.properties        = [];
        this.type              = 'pie';
        this.isRGraph          = true;
        this.coords            = [];
        this.coords.key        = [];
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();


        /**
        * Compatibility with older browsers
        */
        RGraph.OldBrowserCompat(this.context);

        this.properties = {
            'chart.colors':                 ['red', '#ddd', '#0f0', 'blue', 'pink', 'yellow', 'black', 'cyan'],
            'chart.strokestyle':            '#999',
            'chart.linewidth':              1,
            'chart.labels':                 [],
            'chart.labels.sticks':          false,
            'chart.labels.sticks.length':   7,
            'chart.labels.sticks.color':    '#aaa',
            'chart.segments':               [],
            'chart.gutter.left':            25,
            'chart.gutter.right':           25,
            'chart.gutter.top':             25,
            'chart.gutter.bottom':          25,
            'chart.title':                  '',
            'chart.title.background':       null,
            'chart.title.hpos':             null,
            'chart.title.vpos':             0.5,
            'chart.title.bold':             true,
            'chart.title.font':             null,
            'chart.shadow':                 false,
            'chart.shadow.color':           'rgba(0,0,0,0.5)',
            'chart.shadow.offsetx':         3,
            'chart.shadow.offsety':         3,
            'chart.shadow.blur':            3,
            'chart.text.size':              10,
            'chart.text.color':             'black',
            'chart.text.font':              'Arial',
            'chart.contextmenu':            null,
            'chart.tooltips':               null,
            'chart.tooltips.event':         'onclick',
            'chart.tooltips.effect':        'fade',
            'chart.tooltips.css.class':     'RGraph_tooltip',
            'chart.tooltips.highlight':     true,
            'chart.highlight.style':        '2d',
            'chart.highlight.style.2d.fill': 'rgba(255,255,255,0.7)',
            'chart.highlight.style.2d.stroke': 'rgba(255,255,255,0.7)',
            'chart.centerx':                null,
            'chart.centery':                null,
            'chart.radius':                 null,
            'chart.border':                 false,
            'chart.border.color':           'rgba(255,255,255,0.5)',
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
            'chart.annotatable':            false,
            'chart.annotate.color':         'black',
            'chart.align':                  'center',
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
            'chart.variant':                'pie',
            'chart.variant.donut.width':    null,
            'chart.exploded':               [],
            'chart.effect.roundrobin.multiplier': 1,
            'chart.events.click':             null,
            'chart.events.mousemove':         null,
            'chart.centerx':                  null,
            'chart.centery':                  null,
            'chart.radius':                   null,
            'chart.centerpin':                null,
            'chart.centerpin.fill':           'white',
            'chart.centerpin.stroke':         null
        }

        /**
        * Calculate the total
        */
        for (var i=0,len=data.length; i<len; i++) {
            this.total += data[i];
        }
        
        
        /**
        * Now all charts are always registered
        */
        RGraph.Register(this);
    }


    /**
    * A generic setter
    */
    RGraph.Pie.prototype.Set = function (name, value)
    {
        if (name == 'chart.highlight.style.2d.color') {
            name = 'chart.highlight.style.2d.fill';
        }

        this.properties[name] = value;
    }


    /**
    * A generic getter
    */
    RGraph.Pie.prototype.Get = function (name)
    {
        if (name == 'chart.highlight.style.2d.color') {
            name = 'chart.highlight.style.2d.fill';
        }

        return this.properties[name];
    }



    /**
    * This draws the pie chart
    */
    RGraph.Pie.prototype.Draw = function ()
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


        
        this.radius   = this.getRadius();// MUST be first
        this.centerx  = (this.graph.width / 2) + this.gutterLeft
        this.centery  = (this.graph.height / 2) + this.gutterTop
        this.subTotal = 0;
        this.angles   = [];

        /**
        * Allow specification of a custom radius & center X/Y
        */
        if (typeof(this.Get('chart.radius')) == 'number')  this.radius = this.Get('chart.radius');
        if (typeof(this.Get('chart.centerx')) == 'number') this.centerx = this.Get('chart.centerx');
        if (typeof(this.Get('chart.centery')) == 'number') this.centery = this.Get('chart.centery');


        if (this.radius <= 0) {
            return;
        }
        /**
        * Alignment (Pie is center aligned by default) Only if centerx is not defined - donut defines the centerx
        *
        if (this.Get('chart.align') == 'left') {
            this.centerx = this.radius + this.gutterLeft;
        
        } else if (this.Get('chart.align') == 'right') {
            this.centerx = this.canvas.width - this.radius - this.gutterRight;
        
        } else {
            this.centerx = this.canvas.width / 2;
        }
        */

        /**
        * Draw the title
        */
        RGraph.DrawTitle(this,
                         this.Get('chart.title'),
                         (this.canvas.height / 2) - this.radius - 5,
                         this.centerx,
                         this.Get('chart.title.size') ? this.Get('chart.title.size') : this.Get('chart.text.size') + 2);

        /**
        * Draw the shadow if required
        */
        if (this.Get('chart.shadow') && 0) {
        
            var offsetx = document.all ? this.Get('chart.shadow.offsetx') : 0;
            var offsety = document.all ? this.Get('chart.shadow.offsety') : 0;

            this.context.beginPath();
            this.context.fillStyle = this.Get('chart.shadow.color');

            this.context.shadowColor   = this.Get('chart.shadow.color');
            this.context.shadowBlur    = this.Get('chart.shadow.blur');
            this.context.shadowOffsetX = this.Get('chart.shadow.offsetx');
            this.context.shadowOffsetY = this.Get('chart.shadow.offsety');
            
            this.context.arc(this.centerx + offsetx, this.centery + offsety, this.radius, 0, TWOPI, 0);
            
            this.context.fill();
            
            // Now turn off the shadow
            RGraph.NoShadow(this);
        }

        /**
        * The total of the array of values
        */
        this.total = RGraph.array_sum(this.data);

        for (var i=0,len=this.data.length; i<len; i++) {
            
            var angle = ((this.data[i] / this.total) * TWOPI);

            // Draw the segment
            this.DrawSegment(angle,this.Get('chart.colors')[i],i == (this.data.length - 1), i);
        }

        RGraph.NoShadow(this);


        /**
        * Redraw the seperating lines
        */
        this.DrawBorders();

        /**
        * Now draw the segments again with shadow turned off. This is always performed,
        * not just if the shadow is on.
        */

        for (var i=0; i<this.angles.length; i++) {
    
            this.context.beginPath();
                this.context.strokeStyle = typeof(this.Get('chart.strokestyle')) == 'object' ? this.Get('chart.strokestyle')[i] : this.Get('chart.strokestyle');
                this.context.fillStyle = this.Get('chart.colors')[i];
                
                this.context.arc(this.angles[i][2],
                                 this.angles[i][3],
                                 this.radius,
                                 (this.angles[i][0]),
                                 (this.angles[i][1]),
                                 false);
                if (this.Get('chart.variant') == 'donut') {

                    this.context.arc(this.angles[i][2],
                                     this.angles[i][3],
                                     typeof(this.Get('chart.variant.donut.width')) == 'number' ? this.radius - this.Get('chart.variant.donut.width'): this.radius / 2,
                                     (this.angles[i][1]),
                                     (this.angles[i][0]),
                                     true);
                    
                } else {
                    this.context.lineTo(this.angles[i][2], this.angles[i][3]);
                }
            this.context.closePath();
            this.context.stroke();
            this.context.fill();
        }


        /**
        * Draw label sticks
        */
        if (this.Get('chart.labels.sticks')) {
            
            this.DrawSticks();
            
            // Redraw the border going around the Pie chart if the stroke style is NOT white
            var strokeStyle = this.Get('chart.strokestyle');
            var isWhite     = strokeStyle == 'white' || strokeStyle == '#fff' || strokeStyle == '#fffffff' || strokeStyle == 'rgb(255,255,255)' || strokeStyle == 'rgba(255,255,255,0)';

            if (!isWhite || (isWhite && this.Get('chart.shadow'))) {
               // Again (?)
              this.DrawBorders();
           }
        }

        /**
        * Draw the labels
        */
        this.DrawLabels();
        
        
        /**
        * Draw centerpin if requested
        */
        this.DrawCenterpin();
        
        
        /**
        * Setup the context menu if required
        */
        if (this.Get('chart.contextmenu')) {
            RGraph.ShowContext(this);
        }



        /**
        * If a border is pecified, draw it
        */
        if (this.Get('chart.border')) {
            this.context.beginPath();
            this.context.lineWidth = 5;
            this.context.strokeStyle = this.Get('chart.border.color');

            this.context.arc(this.centerx,
                             this.centery,
                             this.radius - 2,
                             0,
                             TWOPI,
                             0);

            this.context.stroke();
        }
        
        /**
        * Draw the kay if desired
        */
        if (this.Get('chart.key') && this.Get('chart.key').length) {
            RGraph.DrawKey(this, this.Get('chart.key'), this.Get('chart.colors'));
        }

        RGraph.NoShadow(this);

        
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
    * Draws a single segment of the pie chart
    * 
    * @param int degrees The number of degrees for this segment
    */
    RGraph.Pie.prototype.DrawSegment = function (radians, color, last, index)
    {
        var context  = this.context;
        var canvas   = this.canvas;
        var subTotal = this.subTotal;
            radians  = radians * this.Get('chart.effect.roundrobin.multiplier');

        context.beginPath();

            context.fillStyle   = color;
            context.strokeStyle = this.Get('chart.strokestyle');
            context.lineWidth   = 0;
            
            if (this.Get('chart.shadow')) {
                RGraph.SetShadow(this, this.Get('chart.shadow.color'),this.Get('chart.shadow.offsetx'), this.Get('chart.shadow.offsety'), this.Get('chart.shadow.blur'));
            }

            /**
            * Exploded segments
            */
            if ( (typeof(this.Get('chart.exploded')) == 'object' && this.Get('chart.exploded')[index] > 0) || typeof(this.Get('chart.exploded')) == 'number') {
                var explosion = typeof(this.Get('chart.exploded')) == 'number' ? this.Get('chart.exploded') : this.Get('chart.exploded')[index];
                var x         = 0;
                var y         = 0;
                var h         = explosion;
                var t         = (subTotal + (radians / 2)) - HALFPI;
                var x         = (Math.cos(t) * explosion);
                var y         = (Math.sin(t) * explosion);
            
                this.context.moveTo(this.centerx + x, this.centery + y);
            } else {
                var x = 0;
                var y = 0;
            }
            
            /**
            * Calculate the angles
            */
            var startAngle = (subTotal) - HALFPI;
            var endAngle   = (((subTotal + radians))) - HALFPI;

            context.arc(this.centerx + x,
                        this.centery + y,
                        this.radius,
                        startAngle,
                        endAngle,
                        0);

            if (this.Get('chart.variant') == 'donut') {
    
                context.arc(this.centerx + x,
                            this.centery + y,
                            typeof(this.Get('chart.variant.donut.width')) == 'number' ? this.radius - this.Get('chart.variant.donut.width'): this.radius / 2,
                            endAngle,
                            startAngle,
                            true);
            } else {
                context.lineTo(this.centerx + x, this.centery + y);
            }

        this.context.closePath();


        // Keep hold of the angles
        this.angles.push([subTotal - HALFPI, subTotal + radians - HALFPI, this.centerx + x, this.centery + y]);


        
        //this.context.stroke();
        this.context.fill();

        /**
        * Calculate the segment angle
        */
        this.Get('chart.segments').push([subTotal, subTotal + radians]);
        this.subTotal += radians;
    }

    /**
    * Draws the graphs labels
    */
    RGraph.Pie.prototype.DrawLabels = function ()
    {
        var hAlignment = 'left';
        var vAlignment = 'center';
        var labels     = this.Get('chart.labels');
        var context    = this.context;

        /**
        * Turn the shadow off
        */
        RGraph.NoShadow(this);
        
        context.fillStyle = 'black';
        context.beginPath();

        /**
        * Draw the key (ie. the labels)
        */
        if (labels && labels.length) {

            var text_size = this.Get('chart.text.size');

            for (i=0; i<this.angles.length; ++i) {
            
                if (typeof(labels[i]) != 'string' && typeof(labels[i]) != 'number') {
                    continue;
                }

                // Move to the centre
                context.moveTo(this.centerx,this.centery);
                
                var a = this.angles[i][0] + ((this.angles[i][1] - this.angles[i][0]) / 2);

                /**
                * Alignment
                */
                if (a < HALFPI) {
                    hAlignment = 'left';
                    vAlignment = 'center';
                } else if (a < TWOPI) {
                    hAlignment = 'right';
                    vAlignment = 'center';
                } else if (a < (PI + HALFPI)) {
                    hAlignment = 'right';
                    vAlignment = 'center';
                } else if (a < TWOPI) {
                    hAlignment = 'left';
                    vAlignment = 'center';
                }

                var angle = ((this.angles[i][1] - this.angles[i][0]) / 2) + this.angles[i][0];

                /**
                * Handle the additional "explosion" offset
                */
                if (typeof(this.Get('chart.exploded')) == 'object' && this.Get('chart.exploded')[i] || typeof(this.Get('chart.exploded')) == 'number') {

                    var t = ((this.angles[i][1] - this.angles[i][0]) / 2);
                    var seperation = typeof(this.Get('chart.exploded')) == 'number' ? this.Get('chart.exploded') : this.Get('chart.exploded')[i];

                    // Adjust the angles
                    var explosion_offsetx = (Math.cos(angle) * seperation);
                    var explosion_offsety = (Math.sin(angle) * seperation);
                } else {
                    var explosion_offsetx = 0;
                    var explosion_offsety = 0;
                }
                
                /**
                * Allow for the label sticks
                */
                if (this.Get('chart.labels.sticks')) {
                    explosion_offsetx += (Math.cos(angle) * this.Get('chart.labels.sticks.length'));
                    explosion_offsety += (Math.sin(angle) * this.Get('chart.labels.sticks.length'));
                }


                context.fillStyle = this.Get('chart.text.color');

                RGraph.Text(context,
                            this.Get('chart.text.font'),
                            text_size,
                            this.centerx + explosion_offsetx + ((this.radius + 10)* Math.cos(a)) + (this.Get('chart.labels.sticks') ? (a < HALFPI || a > (TWOPI + HALFPI) ? 2 : -2) : 0),
                            this.centery + explosion_offsety + (((this.radius + 10) * Math.sin(a))),
                            labels[i],
                            vAlignment,
                            hAlignment);
            }
            
            context.fill();
        }
    }


    /**
    * This function draws the pie chart sticks (for the labels)
    */
    RGraph.Pie.prototype.DrawSticks = function ()
    {
        var context  = this.context;
        var offset   = this.Get('chart.linewidth') / 2;
        var exploded = this.Get('chart.exploded');
        var sticks   = this.Get('chart.labels.sticks');

        for (var i=0; i<this.angles.length; ++i) {

            // This allows the chart.labels.sticks to be an array as well as a boolean
            if (typeof(sticks) == 'object' && !sticks[i]) {
                continue;
            }

            var radians = this.angles[i][1] - this.angles[i][0];

            context.beginPath();
            context.strokeStyle = this.Get('chart.labels.sticks.color');
            context.lineWidth   = 1;

            var midpoint = (this.angles[i][0] + (radians / 2));

            if (typeof(exploded) == 'object' && exploded[i]) {
                var extra = exploded[i];
            } else if (typeof(exploded) == 'number') {
                var extra = exploded;
            } else {
                var extra = 0;
            }

            context.lineJoin = 'round';
            context.lineWidth = 1;

            context.arc(this.centerx,
                        this.centery,
                        this.radius + this.Get('chart.labels.sticks.length') + extra,
                        midpoint,
                        midpoint + 0.001,
                        0);
            context.arc(this.centerx,
                        this.centery,
                        this.radius + extra,
                        midpoint,
                        midpoint + 0.001,
                        0);

            context.stroke();
        }
    }


    /**
    * The (now Pie chart specific) getSegment function
    * 
    * @param object e The event object
    */
    RGraph.Pie.prototype.getShape =
    RGraph.Pie.prototype.getSegment = function (e)
    {
        RGraph.FixEventObject(e);

        // The optional arg provides a way of allowing some accuracy (pixels)
        var accuracy = arguments[1] ? arguments[1] : 0;

        var canvas      = this.canvas;
        var context     = this.context;
        var mouseCoords = RGraph.getMouseXY(e);
        var r           = this.radius;
        var angles      = this.angles;
        var ret         = [];

        for (var i=0; i<angles.length; ++i) {

            var x     = mouseCoords[0] - angles[i][2];
            var y     = mouseCoords[1] - angles[i][3];
            var theta = Math.atan(y / x); // RADIANS
            var hyp   = y == 0 ? x : y / Math.sin(theta);
            var hyp   = (hyp < 0) ? hyp + accuracy : hyp - accuracy;


            /**
            * Account for the correct quadrant
            */
            if (x < 0 && y >= 0) {
                theta += PI;
            } else if (x < 0 && y < 0) {
                theta += PI;
            }

            if (theta > TWOPI) {
                theta -= TWOPI;
            }

            if (theta >= angles[i][0] && theta < angles[i][1]) {

                hyp = Math.abs(hyp);

                if (!hyp || (this.radius && hyp > this.radius) ) {
                    return null;
                }

                if (this.type == 'pie' && this.Get('chart.variant') == 'donut' && (hyp > this.radius || hyp < (typeof(this.Get('chart.variant.donut.width')) == 'number' ? this.radius - this.Get('chart.variant.donut.width') : this.radius / 2) ) ) {
                    return null;
                }



                ret[0] = angles[i][2];
                ret[1] = angles[i][3];
                ret[2] = this.radius;
                ret[3] = angles[i][0] - TWOPI;
                ret[4] = angles[i][1];
                ret[5] = i;


                
                if (ret[3] < 0) ret[3] += TWOPI;
                if (ret[4] > TWOPI) ret[4] -= TWOPI;
                
                ret[3] = ret[3];
                ret[4] = ret[4];
                
                /**
                * Add the tooltip to the returned shape
                */
                var tooltip = RGraph.parseTooltipText ? RGraph.parseTooltipText(this.Get('chart.tooltips'), ret[5]) : null;
                
                /**
                * Now return textual keys as well as numerics
                */
                ret['object']      = this;
                ret['x']           = ret[0];
                ret['y']           = ret[1];
                ret['radius']      = ret[2];
                ret['angle.start'] = ret[3];
                ret['angle.end']   = ret[4];
                ret['index']       = ret[5];
                ret['tooltip']     = tooltip;

                return ret;
            }
        }
        
        return null;
    }


    RGraph.Pie.prototype.DrawBorders = function ()
    {
        if (this.Get('chart.linewidth') > 0) {

            this.context.lineWidth = this.Get('chart.linewidth');
            this.context.strokeStyle = this.Get('chart.strokestyle');

            for (var i=0,len=this.angles.length; i<len; ++i) {

                this.context.beginPath();
                    this.context.arc(this.angles[i][2],
                                     this.angles[i][3],
                                     this.radius,
                                     (this.angles[i][0]),
                                     (this.angles[i][0] + 0.001),
                                     0);
                    this.context.arc(this.angles[i][2],
                                     this.angles[i][3],
                                     this.Get('chart.variant') == 'donut' ? (typeof(this.Get('chart.variant.donut.width')) == 'number' ? this.radius - this.Get('chart.variant.donut.width') : this.radius / 2): this.radius,
                                     this.angles[i][0],
                                     this.angles[i][0] + 0.0001,
                                     0);
                this.context.closePath();
            
                this.context.stroke();

            }
        }
    }


    /**
    * Returns the radius of the pie chart
    * 
    * [06-02-2012] Maintained for compatibility ONLY.
    */
    RGraph.Pie.prototype.getRadius = function ()
    {

        this.graph        = {};
        this.graph.width  = this.canvas.width - this.Get('chart.gutter.left') - this.Get('chart.gutter.right');
        this.graph.height = this.canvas.height - this.Get('chart.gutter.top') - this.Get('chart.gutter.bottom');

        if (typeof(this.Get('chart.radius')) == 'number') {
            this.radius = this.Get('chart.radius');
        } else {
            this.radius = Math.min(this.graph.width, this.graph.height) / 2;
        }

        return this.radius;
    }


    /**
    * A programmatic explode function
    * 
    * @param object obj   The chart object
    * @param number index The zero-indexed number of the segment
    * @param number size  The size (in pixels) of the explosion
    */
    RGraph.Pie.prototype.Explode = function (index, size)
    {
        var obj = this;
        
        this.Set('chart.exploded', []);
        this.Get('chart.exploded')[index] = 0;

        for (var o=0; o<size; ++o) {
            setTimeout(
                function ()
                {
                    obj.Get('chart.exploded')[index] +=1;
                    RGraph.Clear(obj.canvas);
                    RGraph.Redraw()
                }, o * (document.all ? 25 : 16.666));
        }
        
        /**
        * Now set the property accordingly
        */
        //setTimeout(
        //    function ()
        //    {
        //        obj.Set('chart.exploded', []);
        //    }, size * (document.all ? 50 : 20)
        //)
    }


    /**
    * This function highlights a segment
    * 
    * @param array segment The segment information that is returned by the pie.getSegment(e) function
    */
    RGraph.Pie.prototype.highlight_segment = function (segment)
    {
        var context = this.context;

        context.beginPath();
    
        context.strokeStyle = this.Get('chart.highlight.style.2d.stroke');
        context.fillStyle   = this.Get('chart.highlight.style.2d.fill');
    
        context.moveTo(segment[0], segment[1]);
        context.arc(segment[0], segment[1], segment[2], this.angles[segment[5]][0], this.angles[segment[5]][1], 0);
        context.lineTo(segment[0], segment[1]);
        context.closePath();
        
        context.stroke();
        context.fill();
    }


    /**
    * Each object type has its own Highlight() function which highlights the appropriate shape
    * 
    * @param object shape The shape to highlight
    */
    RGraph.Pie.prototype.Highlight = function (shape)
    {
        if (this.Get('chart.tooltips.highlight')) {
            /**
            * 3D style of highlighting
            */
            if (this.Get('chart.highlight.style') == '3d') {
        
                this.context.lineWidth = 1;
                
                // This is the extent of the 2D effect. Bigger values will give the appearance of a larger "protusion"
                var extent = 2;
        
                // Draw a white-out where the segment is
                this.context.beginPath();
                    RGraph.NoShadow(this);
                    this.context.fillStyle   = 'rgba(0,0,0,0)';
                    this.context.arc(shape['x'], shape['y'], shape['radius'], shape['angle.start'], shape['angle.end'], false);
                    if (this.Get('chart.variant') == 'donut') {
                        this.context.arc(shape['x'], shape['y'], shape['radius'] / 5, shape['angle.end'], shape['angle.start'], true);
                    } else {
                        this.context.lineTo(shape['x'], shape['y']);
                    }
                this.context.closePath();
                this.context.fill();
    
                // Draw the new segment
                this.context.beginPath();
    
                    this.context.shadowColor   = '#666';
                    this.context.shadowBlur    = 3;
                    this.context.shadowOffsetX = 3;
                    this.context.shadowOffsetY = 3;
    
                    this.context.fillStyle   = this.Get('chart.colors')[shape['index']];
                    this.context.strokeStyle = this.Get('chart.strokestyle');
                    this.context.arc(shape['x'] - extent, shape['y'] - extent, shape['radius'], shape['angle.start'], shape['angle.end'], false);
                    if (this.Get('chart.variant') == 'donut') {
                        this.context.arc(shape['x'] - extent, shape['y'] - extent, shape['radius'] / 2, shape['angle.end'], shape['angle.start'],  true)
                    } else {
                        this.context.lineTo(shape['x'] - extent, shape['y'] - extent);
                    }
                this.context.closePath();
                
                this.context.stroke();
                this.context.fill();
                
                // Turn off the shadow
                RGraph.NoShadow(this);
    
                /**
                * If a border is defined, redraw that
                */
                if (this.Get('chart.border')) {
                    this.context.beginPath();
                    this.context.strokeStyle = obj.Get('chart.border.color');
                    this.context.lineWidth = 5;
                    this.context.arc(shape['x'] - extent, shape['y'] - extent, shape['radius'] - 2, shape['angle.start'], shape['angle.end'], false);
                    this.context.stroke();
                }
    
    
    
    
            // Default 2D style of  highlighting
            } else {

                this.context.beginPath();

                    this.context.strokeStyle = this.Get('chart.highlight.style.2d.stroke');
                    this.context.fillStyle   = this.Get('chart.highlight.style.2d.fill');
                    
                    if (this.Get('chart.variant') == 'donut') {
                        this.context.arc(shape['x'], shape['y'], shape['radius'], shape['angle.start'], shape['angle.end'], false);
                        this.context.arc(shape['x'], shape['y'], typeof(this.Get('chart.variant.donut.width')) == 'number' ? this.radius - this.Get('chart.variant.donut.width') : shape['radius'] / 2, shape['angle.end'], shape['angle.start'], true);
                    } else {
                        this.context.arc(shape['x'], shape['y'], shape['radius'] + 1, shape['angle.start'], shape['angle.end'], false);
                        this.context.lineTo(shape['x'], shape['y']);
                    }
                this.context.closePath();
    
                //this.context.stroke();
                this.context.fill();
            }
        }
    }



    /**
    * The getObjectByXY() worker method. The Pie chart is able to use the
    * getShape() method - so it does.
    */
    RGraph.Pie.prototype.getObjectByXY = function (e)
    {
        if (this.getShape(e)) {
            return this;
        }
    }
    
    
    
    /**
    * Draws the centerpin if requested
    */
    RGraph.Pie.prototype.DrawCenterpin = function ()
    {
        if (typeof(this.Get('chart.centerpin')) == 'number' &&this.Get('chart.centerpin') > 0) {
            this.context.beginPath();
                this.context.strokeStyle = this.Get('chart.centerpin.stroke') ? this.Get('chart.centerpin.stroke') : this.Get('chart.strokestyle');
                this.context.fillStyle = this.Get('chart.centerpin.fill') ? this.Get('chart.centerpin.fill') : this.Get('chart.strokestyle');
                this.context.moveTo(this.centerx, this.centery);
                this.context.arc(this.centerx, this.centery, this.Get('chart.centerpin'), 0, TWOPI, false);                
            this.context.stroke();
            this.context.fill();
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
    RGraph.Pie.prototype.positionTooltip = function (obj, x, y, tooltip, idx)
    {
        var coordX      = obj.angles[idx][2];
        var coordY      = obj.angles[idx][3];
        var angleStart  = obj.angles[idx][0];
        var angleEnd    = obj.angles[idx][1];
        var angleCenter = ((angleEnd - angleStart) / 2) + angleStart;
        var canvasXY    = RGraph.getCanvasXY(obj.canvas);
        var gutterLeft  = obj.Get('chart.gutter.left');
        var gutterTop   = obj.Get('chart.gutter.top');
        var width       = tooltip.offsetWidth;
        var height      = tooltip.offsetHeight;
        var x           = canvasXY[0] + this.angles[idx][2] + (Math.cos(angleCenter) * (this.Get('chart.variant') == 'donut' && typeof(this.Get('chart.variant.donut.width')) == 'number' ? ((this.radius - this.Get('chart.variant.donut.width')) + (this.Get('chart.variant.donut.width') / 2)) : (this.radius * (this.Get('chart.variant') == 'donut' ? 0.75 : 0.5))));
        var y           = canvasXY[1] + this.angles[idx][3] + (Math.sin(angleCenter) * (this.Get('chart.variant') == 'donut' && typeof(this.Get('chart.variant.donut.width')) == 'number' ? ((this.radius - this.Get('chart.variant.donut.width')) + (this.Get('chart.variant.donut.width') / 2)) : (this.radius * (this.Get('chart.variant') == 'donut' ? 0.75 : 0.5))));

        
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
        if ((window.event.pageX - (width / 2)) < 10) {
            tooltip.style.left = (x - (width * 0.1)) + 'px';
            tooltip.style.top  = (y - height - 4) + 'px';
            img.style.left = ((width * 0.1) - 8.5) + 'px';

        // RIGHT edge
        } else if ((x + (width / 2)) > (document.body.offsetWidth - 10) ) {
            tooltip.style.left = (x - (width * 0.9)) + 'px';
            tooltip.style.top  = (y - height - 4) + 'px';
            img.style.left = ((width * 0.9) - 8.5) + 'px';

        // Default positioning - CENTERED
        } else {
            tooltip.style.left = (x - (width / 2)) + 'px';
            tooltip.style.top  = (y - height - 4) + 'px';
            img.style.left = ((width * 0.5) - 8.5) + 'px';
        }
    }