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
    * The bi-polar/age frequency constructor.
    * 
    * @param string id The id of the canvas
    * @param array  left  The left set of data points
    * @param array  right The right set of data points
    * 
    * REMEMBER If ymin is implemented you need to update the .getValue() method
    */
    RGraph.Bipolar = function (id, left, right)
    {
        // Get the canvas and context objects
        this.id                = id;
        this.canvas            = document.getElementById(id);
        this.context           = this.canvas.getContext('2d');
        this.canvas.__object__ = this;
        this.type              = 'bipolar';
        this.coords            = [];
        this.max               = 0;
        this.isRGraph          = true;
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();


        /**
        * Compatibility with older browsers
        */
        RGraph.OldBrowserCompat(this.context);

        
        // The left and right data respectively
        this.left       = left;
        this.right      = right;
        this.data       = [left, right];

        this.properties = {
            'chart.margin':                 2,
            'chart.xtickinterval':          null,
            'chart.labels':                 [],
            'chart.text.size':              10,
            'chart.text.color':             'black',
            'chart.text.font':              'Arial',
            'chart.title.left':             '',
            'chart.title.right':            '',
            'chart.gutter.center':          60,
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
            'chart.colors':                 ['#0f0'],
            'chart.contextmenu':            null,
            'chart.tooltips':               null,
            'chart.tooltips.effect':         'fade',
            'chart.tooltips.css.class':      'RGraph_tooltip',
            'chart.tooltips.highlight':     true,
            'chart.tooltips.event':         'onclick',
            'chart.highlight.stroke':       'rgba(0,0,0,0)',
            'chart.highlight.fill':         'rgba(255,255,255,0.7)',
            'chart.units.pre':              '',
            'chart.units.post':             '',
            'chart.shadow':                 false,
            'chart.shadow.color':           '#666',
            'chart.shadow.offsetx':         3,
            'chart.shadow.offsety':         3,
            'chart.shadow.blur':            3,
            'chart.annotatable':            false,
            'chart.annotate.color':         'black',
            'chart.xmax':                   null,
            'chart.scale.decimals':         null,
            'chart.scale.point':            '.',
            'chart.scale.thousand':         ',',
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
            'chart.resizable':              false,
            'chart.resize.handle.adjust':   [0,0],
            'chart.resize.handle.background': null,
            'chart.strokestyle':            'transparent',
            'chart.events.mousemove':       null,
            'chart.events.click':           null
        }

        // Pad the arrays so they're the same size
        while (this.left.length < this.right.length) this.left.push(0);
        while (this.left.length > this.right.length) this.right.push(0);
        
        /**
        * Objects are now always registered so that when RGraph.Redraw()
        * is called this chart will be redrawn.
        */
        RGraph.Register(this);
    }


    /**
    * The setter
    * 
    * @param name  string The name of the parameter to set
    * @param value mixed  The value of the paraneter 
    */
    RGraph.Bipolar.prototype.Set = function (name, value)
    {
        this.properties[name.toLowerCase()] = value;
    }


    /**
    * The getter
    * 
    * @param name string The name of the parameter to get
    */
    RGraph.Bipolar.prototype.Get = function (name)
    {
        return this.properties[name.toLowerCase()];
    }

    
    /**
    * Draws the graph
    */
    RGraph.Bipolar.prototype.Draw = function ()
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
        


        // Reset the data to what was initially supplied
        this.left  = this.data[0];
        this.right = this.data[1];


        /**
        * Reset the coords array
        */
        this.coords = [];

        this.GetMax();
        this.DrawAxes();
        this.DrawTicks();
        this.DrawLeftBars();
        this.DrawRightBars();

        if (this.Get('chart.axis.color') != 'black') {
            this.DrawAxes(); // Draw the axes again (if the axes color is not black)
        }

        this.DrawLabels();
        this.DrawTitles();


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
    * Draws the axes
    */
    RGraph.Bipolar.prototype.DrawAxes = function ()
    {
        // Draw the left set of axes
        this.context.beginPath();
        this.context.strokeStyle = this.Get('chart.axis.color');

        this.axisWidth  = (this.canvas.width - this.Get('chart.gutter.center') - this.gutterLeft - this.gutterRight) / 2;
        this.axisHeight = this.canvas.height - this.gutterTop - this.gutterBottom;

        this.context.moveTo(this.gutterLeft, AA(this, this.canvas.height - this.gutterBottom));
        this.context.lineTo(this.gutterLeft + this.axisWidth, AA(this, this.canvas.height - this.gutterBottom));
        
        this.context.moveTo(AA(this, this.gutterLeft + this.axisWidth), this.canvas.height - this.gutterBottom);
        this.context.lineTo(AA(this, this.gutterLeft + this.axisWidth), this.gutterTop);
        
        this.context.stroke();


        // Draw the right set of axes
        this.context.beginPath();

        var x = this.gutterLeft + this.axisWidth + this.Get('chart.gutter.center');
        
        this.context.moveTo(AA(this, x), this.gutterTop);
        this.context.lineTo(AA(this, x), this.canvas.height - this.gutterBottom);
        
        this.context.moveTo(AA(this, x), AA(this, this.canvas.height - this.gutterBottom));
        this.context.lineTo(this.canvas.width - this.gutterRight, AA(this, this.canvas.height - this.gutterBottom));

        this.context.stroke();
    }


    /**
    * Draws the tick marks on the axes
    */
    RGraph.Bipolar.prototype.DrawTicks = function ()
    {
        var numDataPoints = this.left.length;
        var barHeight     = ( (this.canvas.height - this.gutterTop - this.gutterBottom)- (this.left.length * (this.Get('chart.margin') * 2) )) / numDataPoints;
        
        // Draw the left Y tick marks
        for (var i = this.canvas.height - this.gutterBottom; i >= this.gutterTop; i -= (barHeight + ( this.Get('chart.margin') * 2)) ) {
            if (i < (this.canvas.height - this.gutterBottom) ) {
                this.context.beginPath();
                this.context.moveTo(this.gutterLeft + this.axisWidth, AA(this, i));
                this.context.lineTo(this.gutterLeft + this.axisWidth + 3, AA(this, i));
                this.context.stroke();
            }
        }

        //Draw the right axis Y tick marks
        for (var i = this.canvas.height - this.gutterBottom; i >= this.gutterTop; i -= (barHeight + ( this.Get('chart.margin') * 2)) ) {
            if (i < (this.canvas.height - this.gutterBottom) ) {
                this.context.beginPath();
                this.context.moveTo(this.gutterLeft + this.axisWidth + this.Get('chart.gutter.center'), AA(this, i));
                this.context.lineTo(this.gutterLeft + this.axisWidth + this.Get('chart.gutter.center') - 3, AA(this, i));
                this.context.stroke();
            }
        }
        
        var xInterval = this.axisWidth / 10;

        // Is chart.xtickinterval specified ? If so, use that.
        if (typeof(this.Get('chart.xtickinterval')) == 'number') {
            xInterval = this.Get('chart.xtickinterval');
        }

        
        // Draw the left sides X tick marks
        for (i=this.gutterLeft; i<(this.gutterLeft + this.axisWidth); i+=xInterval) {
            this.context.beginPath();
            this.context.moveTo(AA(this, i), this.canvas.height - this.gutterBottom);
            this.context.lineTo(AA(this, i), (this.canvas.height - this.gutterBottom) + 4);
            this.context.closePath();
            
            this.context.stroke();
        }

        // Draw the right sides X tick marks
        var stoppingPoint = this.canvas.width - this.gutterRight;

        for (i=(this.gutterLeft + this.axisWidth + this.Get('chart.gutter.center') + xInterval); i<=stoppingPoint; i+=xInterval) {
            this.context.beginPath();
                this.context.moveTo(AA(this, i), this.canvas.height - this.gutterBottom);
                this.context.lineTo(AA(this, i), (this.canvas.height - this.gutterBottom) + 4);
            this.context.closePath();
            
            this.context.stroke();
        }
        
        // Store this for later
        this.barHeight = barHeight;
    }


    /**
    * Figures out the maximum value, or if defined, uses xmax
    */
    RGraph.Bipolar.prototype.GetMax = function()
    {
        var max = 0;
        var dec = this.Get('chart.scale.decimals');
        
        // chart.xmax defined
        if (this.Get('chart.xmax')) {

            max = this.Get('chart.xmax');
            
            this.scale    = [];
            this.scale[0] = Number((max / 5) * 1).toFixed(dec);
            this.scale[1] = Number((max / 5) * 2).toFixed(dec);
            this.scale[2] = Number((max / 5) * 3).toFixed(dec);
            this.scale[3] = Number((max / 5) * 4).toFixed(dec);
            this.scale[4] = Number(max).toFixed(dec);

            this.max = max;
            

        // Generate the scale ourselves
        } else {
            this.leftmax  = RGraph.array_max(this.left);
            this.rightmax = RGraph.array_max(this.right);
            max = Math.max(this.leftmax, this.rightmax);

            this.scale    = RGraph.getScale(max, this);
            this.scale[0] = Number(this.scale[0]).toFixed(dec);
            this.scale[1] = Number(this.scale[1]).toFixed(dec);
            this.scale[2] = Number(this.scale[2]).toFixed(dec);
            this.scale[3] = Number(this.scale[3]).toFixed(dec);
            this.scale[4] = Number(this.scale[4]).toFixed(dec);

            this.max = this.scale[4];
        }

        // Don't need to return it as it is stored in this.max
    }


    /**
    * Function to draw the left hand bars
    */
    RGraph.Bipolar.prototype.DrawLeftBars = function ()
    {
        // Set the stroke colour
        this.context.strokeStyle = this.Get('chart.strokestyle');

        for (i=0; i<this.left.length; ++i) {
            
            /**
            * Turn on a shadow if requested
            */
            if (this.Get('chart.shadow')) {
                this.context.shadowColor   = this.Get('chart.shadow.color');
                this.context.shadowBlur    = this.Get('chart.shadow.blur');
                this.context.shadowOffsetX = this.Get('chart.shadow.offsetx');
                this.context.shadowOffsetY = this.Get('chart.shadow.offsety');
            }

            this.context.beginPath();

                // Set the colour
                if (this.Get('chart.colors')[i]) {
                    this.context.fillStyle = this.Get('chart.colors')[i];
                }
                
                // Set the stroke colour
                this.context.strokeStyle = this.Get('chart.strokestyle');
                
                /**
                * Work out the coordinates
                */
                var width = ( (this.left[i] / this.max) *  this.axisWidth);
                var coords = [AA(this, this.gutterLeft + this.axisWidth - width),
                              AA(this, this.gutterTop + (i * ( this.axisHeight / this.left.length)) + this.Get('chart.margin')),
                              width,
                              this.barHeight];

                // Draw the IE shadow if necessary
                if (RGraph.isOld() && this.Get('chart.shadow')) {
                    this.DrawIEShadow(coords);
                }
    
                
                this.context.strokeRect(coords[0], coords[1], coords[2], coords[3]);
                this.context.fillRect(coords[0], coords[1], coords[2], coords[3]);

            this.context.stroke();
            this.context.fill();

            /**
            * Add the coordinates to the coords array
            */
            this.coords.push([
                              coords[0],
                              coords[1],
                              coords[2],
                              coords[3]
                             ]);
        }

        /**
        * Turn off any shadow
        */
        RGraph.NoShadow(this);
    }


    /**
    * Function to draw the right hand bars
    */
    RGraph.Bipolar.prototype.DrawRightBars = function ()
    {
        // Set the stroke colour
        this.context.strokeStyle = this.Get('chart.strokestyle');
            
        /**
        * Turn on a shadow if requested
        */
        if (this.Get('chart.shadow')) {
            this.context.shadowColor   = this.Get('chart.shadow.color');
            this.context.shadowBlur    = this.Get('chart.shadow.blur');
            this.context.shadowOffsetX = this.Get('chart.shadow.offsetx');
            this.context.shadowOffsetY = this.Get('chart.shadow.offsety');
        }

        for (var i=0; i<this.right.length; ++i) {
            this.context.beginPath();

                // Set the colour
                if (this.Get('chart.colors')[i]) {
                    this.context.fillStyle = this.Get('chart.colors')[i];
                }

                // Set the stroke colour
                this.context.strokeStyle = this.Get('chart.strokestyle');
    
    
                var width = ( (this.right[i] / this.max) * this.axisWidth);
                var coords = [
                              AA(this, this.gutterLeft + this.axisWidth + this.Get('chart.gutter.center')),
                              AA(this, this.Get('chart.margin') + (i * (this.axisHeight / this.right.length)) + this.gutterTop),
                              width,
                              this.barHeight
                            ];
    
                    // Draw the IE shadow if necessary
                    if (RGraph.isOld() && this.Get('chart.shadow')) {
                        this.DrawIEShadow(coords);
                    }
                this.context.strokeRect(AA(this, coords[0]), AA(this, coords[1]), coords[2], coords[3]);
                this.context.fillRect(AA(this, coords[0]), AA(this, coords[1]), coords[2], coords[3]);

            this.context.closePath();
            
            /**
            * Add the coordinates to the coords array
            */
            this.coords.push([
                              coords[0],
                              coords[1],
                              coords[2],
                              coords[3]
                             ]);
        }
        
        this.context.stroke();

        /**
        * Turn off any shadow
        */
        RGraph.NoShadow(this);
    }


    /**
    * Draws the titles
    */
    RGraph.Bipolar.prototype.DrawLabels = function ()
    {
        this.context.fillStyle = this.Get('chart.text.color');

        var labelPoints = new Array();
        var font = this.Get('chart.text.font');
        var size = this.Get('chart.text.size');
        
        var max = Math.max(this.left.length, this.right.length);
        
        for (i=0; i<max; ++i) {
            var barAreaHeight = this.canvas.height - this.gutterTop - this.gutterBottom;
            var barHeight     = barAreaHeight / this.left.length;
            var yPos          = (i * barAreaHeight) + this.gutterTop;

            labelPoints.push(this.gutterTop + (i * barHeight) + (barHeight / 2) + 5);
        }

        for (i=0; i<labelPoints.length; ++i) {
            RGraph.Text(this.context,
                        this.Get('chart.text.font'),
                        this.Get('chart.text.size'),
                        this.gutterLeft + this.axisWidth + (this.Get('chart.gutter.center') / 2),
                        labelPoints[i],
                        String(this.Get('chart.labels')[i] ? this.Get('chart.labels')[i] : ''),
                        null,
                        'center');
        }

        // Now draw the X labels for the left hand side
        RGraph.Text(this.context,font,size,this.gutterLeft,this.canvas.height - this.gutterBottom + 14,RGraph.number_format(this, this.scale[4], this.Get('chart.units.pre'), this.Get('chart.units.post')),null,'center');
        RGraph.Text(this.context, font, size, this.gutterLeft + ((this.canvas.width - this.Get('chart.gutter.center') - this.gutterLeft - this.gutterRight) / 2) * (1/5), this.canvas.height - this.gutterBottom + 14, RGraph.number_format(this, this.scale[3], this.Get('chart.units.pre'), this.Get('chart.units.post')), null, 'center');
        RGraph.Text(this.context, font, size, this.gutterLeft + ((this.canvas.width - this.Get('chart.gutter.center') - this.gutterLeft - this.gutterRight) / 2) * (2/5), this.canvas.height - this.gutterBottom + 14, RGraph.number_format(this, this.scale[2], this.Get('chart.units.pre'), this.Get('chart.units.post')), null, 'center');
        RGraph.Text(this.context, font, size, this.gutterLeft + ((this.canvas.width - this.Get('chart.gutter.center') - this.gutterLeft - this.gutterRight) / 2) * (3/5), this.canvas.height - this.gutterBottom + 14, RGraph.number_format(this, this.scale[1], this.Get('chart.units.pre'), this.Get('chart.units.post')), null, 'center');
        RGraph.Text(this.context, font, size, this.gutterLeft + ((this.canvas.width - this.Get('chart.gutter.center') - this.gutterLeft - this.gutterRight) / 2) * (4/5), this.canvas.height - this.gutterBottom + 14, RGraph.number_format(this, this.scale[0], this.Get('chart.units.pre'), this.Get('chart.units.post')), null, 'center');

        // Now draw the X labels for the right hand side
        RGraph.Text(this.context, font, size, this.canvas.width - this.gutterRight, this.canvas.height - this.gutterBottom + 14, RGraph.number_format(this, this.scale[4], this.Get('chart.units.pre'), this.Get('chart.units.post')), null, 'center');
        RGraph.Text(this.context, font, size, this.canvas.width - this.gutterRight - (this.axisWidth * 0.2), this.canvas.height - this.gutterBottom + 14,RGraph.number_format(this, this.scale[3], this.Get('chart.units.pre'), this.Get('chart.units.post')), null, 'center');
        RGraph.Text(this.context, font, size, this.canvas.width - this.gutterRight - (this.axisWidth * 0.4), this.canvas.height - this.gutterBottom + 14,RGraph.number_format(this, this.scale[2], this.Get('chart.units.pre'), this.Get('chart.units.post')), null, 'center');
        RGraph.Text(this.context, font, size, this.canvas.width - this.gutterRight - (this.axisWidth * 0.6), this.canvas.height - this.gutterBottom + 14,RGraph.number_format(this, this.scale[1], this.Get('chart.units.pre'), this.Get('chart.units.post')), null, 'center');
        RGraph.Text(this.context, font, size, this.canvas.width - this.gutterRight - (this.axisWidth * 0.8), this.canvas.height - this.gutterBottom + 14,RGraph.number_format(this, this.scale[0], this.Get('chart.units.pre'), this.Get('chart.units.post')), null, 'center');
    }
    
    /**
    * Draws the titles
    */
    RGraph.Bipolar.prototype.DrawTitles = function ()
    {
        RGraph.Text(this.context, this.Get('chart.text.font'), this.Get('chart.text.size'), this.gutterLeft + 5, (this.gutterTop / 2) + 5, String(this.Get('chart.title.left')), 'center');
        RGraph.Text(this.context,this.Get('chart.text.font'), this.Get('chart.text.size'), this.canvas.width - this.gutterRight - 5, (this.gutterTop / 2) + 5, String(this.Get('chart.title.right')), 'center', 'right');
        
        // Draw the main title for the whole chart
        RGraph.DrawTitle(this, this.Get('chart.title'), this.gutterTop, null, this.Get('chart.title.size') ? this.Get('chart.title.size') : null);
    }


    /**
    * This function is used by MSIE only to manually draw the shadow
    * 
    * @param array coords The coords for the bar
    */
    RGraph.Bipolar.prototype.DrawIEShadow = function (coords)
    {
        var prevFillStyle = this.context.fillStyle;
        var offsetx = this.Get('chart.shadow.offsetx');
        var offsety = this.Get('chart.shadow.offsety');
        
        this.context.lineWidth = this.Get('chart.linewidth');
        this.context.fillStyle = this.Get('chart.shadow.color');
        this.context.beginPath();
        
        // Draw shadow here
        this.context.fillRect(coords[0] + offsetx, coords[1] + offsety, coords[2],coords[3]);

        this.context.fill();
        
        // Change the fillstyle back to what it was
        this.context.fillStyle = prevFillStyle;
    }



    /**
    * Returns the appropriate focussed bar coordinates
    * 
    * @param e object The event object
    */
    RGraph.Bipolar.prototype.getShape = 
    RGraph.Bipolar.prototype.getBar = function (e)
    {
        var canvas      = this.canvas;
        var context     = this.context;
        var mouseCoords = RGraph.getMouseXY(e);

        /**
        * Loop through the bars determining if the mouse is over a bar
        */
        for (var i=0; i<this.coords.length; i++) {

            var mouseX = mouseCoords[0];
            var mouseY = mouseCoords[1];
            var left   = this.coords[i][0];
            var top    = this.coords[i][1];
            var width  = this.coords[i][2];
            var height = this.coords[i][3];

            if (mouseX >= left && mouseX <= (left + width) && mouseY >= top && mouseY <= (top + height) ) {
            
                var tooltip = RGraph.parseTooltipText(this.Get('chart.tooltips'), i);

                return {
                        0: this,1: left,2: top,3: width,4: height,5: i,
                        'object': this, 'x': left, 'y': top, 'width': width, 'height': height, 'index': i, 'tooltip': tooltip
                       };
            }
        }

        return null;
    }



    /**
    * Each object type has its own Highlight() function which highlights the appropriate shape
    * 
    * @param object shape The shape to highlight
    */
    RGraph.Bipolar.prototype.Highlight = function (shape)
    {
        // Add the new highlight
        RGraph.Highlight.Rect(this, shape);
    }



    /**
    * When you click on the canvas, this will return the relevant value (if any)
    * 
    * REMEMBER This function will need updating if the Bipolar ever gets chart.ymin
    * 
    * @param object e The event object
    */
    RGraph.Bipolar.prototype.getValue = function (e)
    {
        var obj     = e.target.__object__;
        var mouseXY = RGraph.getMouseXY(e);
        var mouseX  = mouseXY[0];
        
        /**
        * Left hand side
        */
        if (mouseX > this.gutterLeft && mouseX < ( (this.canvas.width / 2) - (this.Get('chart.gutter.center') / 2) )) {
            var value = (mouseX - this.Get('chart.gutter.left')) / this.axisWidth;
                value = this.max - (value * this.max);
        }
        
        /**
        * Right hand side
        */
        if (mouseX < (this.canvas.width -  this.gutterRight) && mouseX > ( (this.canvas.width / 2) + (this.Get('chart.gutter.center') / 2) )) {
            var value = (mouseX - this.Get('chart.gutter.left') - this.axisWidth - this.Get('chart.gutter.center')) / this.axisWidth;
                value = (value * this.max);
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
    RGraph.Bipolar.prototype.getObjectByXY = function (e)
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
    RGraph.Bipolar.prototype.positionTooltip = function (obj, x, y, tooltip, idx)
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
        if ((canvasXY[0] + coordX + (coordW / 2)- (width / 2)) < 0) {
            tooltip.style.left = (canvasXY[0] + coordX - (width * 0.1)) + (coordW / 2) + 'px';
            img.style.left = ((width * 0.1) - 8.5) + 'px';

        // RIGHT edge
        } else if ((canvasXY[0] + coordX + width) > document.body.offsetWidth) {
            tooltip.style.left = canvasXY[0] + coordX - (width * 0.9) + (coordW / 2) + 'px';
            img.style.left = ((width * 0.9) - 8.5) + 'px';

        // Default positioning - CENTERED
        } else {
            tooltip.style.left = (canvasXY[0] + coordX + (coordW / 2) - (width * 0.5)) + 'px';
            img.style.left = ((width * 0.5) - 8.5) + 'px';
        }
    }