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
    * The chart constructor. This function sets up the object. It takes the ID (the HTML attribute) of the canvas as the
    * first argument and the data as the second. If you need to change this, you can.
    * 
    * NB: If tooltips are ever implemented they must go below the use event listeners!!
    * 
    * @param string id    The canvas tag ID
    * @param number min   The minimum value
    * @param number max   The maximum value
    * @param number value The value reported by the thermometer
    */
    RGraph.Thermometer = function (id, min, max, value)
    {
        this.id                = id;
        this.canvas            = document.getElementById(id);
        this.context           = this.canvas.getContext ? this.canvas.getContext("2d") : null;
        this.canvas.__object__ = this;
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();

        this.type         = 'thermometer';
        this.isRGraph     = true;
        this.min          = min;
        this.max          = max;
        this.value        = value;
        this.coords       = [];
        this.graphArea    = [];
        this.currentValue = null;

        RGraph.OldBrowserCompat(this.context);

        this.properties = {
            'chart.colors':                 ['red'],
            'chart.gutter.left':            15,
            'chart.gutter.right':           15,
            'chart.gutter.top':             15,
            'chart.gutter.bottom':          15,
            'chart.ticksize':               5,
            'chart.text.color':             'black',
            'chart.text.font':              'Arial',
            'chart.text.size':              10,
            'chart.units.pre':              '',
            'chart.units.post':             '',
            'chart.zoom.factor':            1.5,
            'chart.zoom.fade.in':           true,
            'chart.zoom.fade.out':          true,
            'chart.zoom.hdir':              'right',
            'chart.zoom.vdir':              'down',
            'chart.zoom.frames':            25,
            'chart.zoom.delay':             16.666,
            'chart.zoom.shadow':            true,
            'chart.zoom.background':        true,
            'chart.title':                  '',
            'chart.title.hpos':             0.5,
            'chart.title.side':             '',
            'chart.title.side.bold':        true,
            'chart.title.side.font':        null,
            'chart.shadow':                 true,
            'chart.shadow.offsetx':         0,
            'chart.shadow.offsety':         0,
            'chart.shadow.blur':            15,
            'chart.shadow.color':           'gray',
            'chart.resizable':              false,
            'chart.contextmenu':            null,
            'chart.adjustable':             false,
            'chart.value.label':            true,
            'chart.scale.visible':          false,
            'chart.scale.decimals':         0,
            'chart.ylabels.count':          5,
            'chart.annotatable':            false,
            'chart.annotate.color':         'black',
            'chart.scale.decimals':         0,
            'chart.scale.point':            '.',
            'chart.scale.thousand':         ',',
            'chart.tooltips':               null,
            'chart.tooltips.highlight':     true,
            'chart.tooltips.effect':        'fade',
            'chart.tooltips.event':         'onclick',
            'chart.highlight.stroke':       'rgba(0,0,0,0)',
            'chart.highlight.fill':         'rgba(255,255,255,0.7)'
        }

        /**
        * A simple check that the browser has canvas support
        */
        if (!this.canvas) {
            alert('[THERMOMETER] No canvas support');
            return;
        }
        
        /**
        * Now, because canvases can support multiple charts, canvases must always be registered
        */
        RGraph.Register(this);
    }




    /**
    * A setter.
    * 
    * @param name  string The name of the property to set
    * @param value mixed  The value of the property
    */
    RGraph.Thermometer.prototype.Set = function (name, value)
    {
        this.properties[name.toLowerCase()] = value;
    }




    /**
    * A getter.
    * 
    * @param name  string The name of the property to get
    */
    RGraph.Thermometer.prototype.Get = function (name)
    {
        return this.properties[name];
    }




    /**
    * Draws the thermometer
    */
    RGraph.Thermometer.prototype.Draw = function ()
    {
        /**
        * Fire the custom RGraph onbeforedraw event (which should be fired before the chart is drawn)
        */
        RGraph.FireCustomEvent(this, 'onbeforedraw');

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


        /**
        * Draw the background
        */
        this.DrawBackground();
        
        /**
        * Draw the bar that represents the value
        */
        this.DrawBar();

        /**
        * Draw the tickmarks/hatchmarks
        */
        this.DrawTickMarks();

        /**
        * Draw the label
        */
        this.DrawLabels();

        /**
        * Draw the title
        */
        if (this.Get('chart.title')) {
            this.DrawTitle();
        }
        
        /**
        * Draw the side title
        */
        if (this.Get('chart.title.side')) {
            this.DrawSideTitle();
        }
        
        /**
        * This function enables resizing
        */
        if (this.Get('chart.resizable')) {
            RGraph.AllowResizing(this);
        }
        
        
        /**
        * Setup the context menu if required
        */
        if (this.Get('chart.contextmenu')) {
            RGraph.ShowContext(this);
        }


        /**
        * This installs the event listeners
        */
        RGraph.InstallEventListeners(this);

        
        /**
        * Fire the custom RGraph ondraw event (which should be fired when you have drawn the chart)
        */
        RGraph.FireCustomEvent(this, 'ondraw');
    }





    /**
    * Draws the thermometer itself
    */
    RGraph.Thermometer.prototype.DrawBackground = function ()
    {
        var canvas     = this.canvas;
        var context    = this.context;
        var bulbRadius = (this.canvas.width - this.gutterLeft - this.gutterRight) / 2;
        
        // Cache the bulbRadius as an object variable
        this.bulbRadius = bulbRadius;

        // Draw the black background that becomes the border
        context.beginPath();
            context.fillStyle = 'black';

            if (this.Get('chart.shadow')) {
                RGraph.SetShadow(this, this.Get('chart.shadow.color'), this.Get('chart.shadow.offsetx'), this.Get('chart.shadow.offsety'), this.Get('chart.shadow.blur'));
            }

            context.fillRect(this.gutterLeft + 12,this.gutterTop + bulbRadius,this.canvas.width - this.gutterLeft - this.gutterRight - 24, this.canvas.height - this.gutterTop - this.gutterBottom - bulbRadius - bulbRadius);
            context.arc(this.gutterLeft + bulbRadius, this.canvas.height - this.gutterBottom - bulbRadius, bulbRadius, 0, TWOPI, 0);
            context.arc(this.gutterLeft + bulbRadius,this.gutterTop + bulbRadius,(this.canvas.width - this.gutterLeft - this.gutterRight - 24)/ 2,0,TWOPI,0);
        context.fill();
        
        RGraph.NoShadow(this);

        // Draw the white inner content background that creates the border
        context.beginPath();
            context.fillStyle = 'white';
            context.fillRect(this.gutterLeft + 12 + 1,this.gutterTop + bulbRadius,this.canvas.width - this.gutterLeft - this.gutterRight - 24 - 2,this.canvas.height - this.gutterTop - this.gutterBottom - bulbRadius - bulbRadius);
            context.arc(this.gutterLeft + bulbRadius, this.canvas.height - this.gutterBottom - bulbRadius, bulbRadius - 1, 0, TWOPI, 0);
            context.arc(this.gutterLeft + bulbRadius,this.gutterTop + bulbRadius,((this.canvas.width - this.gutterLeft - this.gutterRight - 24)/ 2) - 1,0,TWOPI,0);
        context.fill();

        // Draw the bottom content of the thermometer
        context.beginPath();
            context.fillStyle = this.Get('chart.colors')[0];
            context.arc(this.gutterLeft + bulbRadius, this.canvas.height - this.gutterBottom - bulbRadius, bulbRadius - 1, 0, TWOPI, 0);
            context.fillRect(this.gutterLeft + 12 + 1, this.canvas.height - this.gutterBottom - bulbRadius - bulbRadius,this.canvas.width - this.gutterLeft - this.gutterRight - 24 - 2, bulbRadius);
        context.fill();
        
        // Save the X/Y/width/height
        this.graphArea[0] = this.gutterLeft + 12 + 1;
        this.graphArea[1] = this.gutterTop + bulbRadius;
        this.graphArea[2] = this.canvas.width - this.gutterLeft - this.gutterRight - 24 - 2;
        this.graphArea[3] = (this.canvas.height - this.gutterBottom - bulbRadius - bulbRadius) - (this.graphArea[1]);
    }


    /**
    * This draws the bar that indicates the value of the thermometer
    */
    RGraph.Thermometer.prototype.DrawBar = function ()
    {
        var barHeight = ((this.value - this.min) / (this.max - this.min)) * this.graphArea[3];
        var context   = this.context;

        // Draw the actual bar that indicates the value
        context.beginPath();
            context.fillStyle = this.Get('chart.colors')[0];
            context.fillRect(this.graphArea[0],
                             this.graphArea[1] + this.graphArea[3] - barHeight,
                             this.graphArea[2],
                             barHeight);
        context.fill();
        
        this.coords[0] = [this.graphArea[0],this.graphArea[1] + this.graphArea[3] - barHeight,this.graphArea[2],barHeight];
    }

    
    /**
    * Draws the tickmarks of the thermometer
    */
    RGraph.Thermometer.prototype.DrawTickMarks = function ()
    {
        this.context.strokeStyle = 'black'
        var ticksize = this.Get('chart.ticksize');

        // Left hand side tickmarks
            this.context.beginPath();
                for (var i=this.graphArea[1]; i<=(this.graphArea[1] + this.graphArea[3]); i += (this.graphArea[3] / 10)) {
                    this.context.moveTo(this.gutterLeft + 12, AA(this, i));
                    this.context.lineTo(this.gutterLeft + 12 + ticksize, AA(this, i));
                }
            this.context.stroke();

        // Right hand side tickmarks
            this.context.beginPath();
                for (var i=this.graphArea[1]; i<=(this.graphArea[1] + this.graphArea[3]); i += (this.graphArea[3] / 10)) {
                    this.context.moveTo(this.canvas.width - (this.gutterRight + 12), AA(this, i));
                    this.context.lineTo(this.canvas.width - (this.gutterRight + 12 + ticksize), AA(this, i));
                }
            this.context.stroke();
    }

    
    /**
    * Draws the labels of the thermometer. Now (4th August 2011) draws
    * the scale too
    */
    RGraph.Thermometer.prototype.DrawLabels = function ()
    {
        if (this.Get('chart.value.label')) {
            this.context.beginPath();
                this.context.fillStyle = this.Get('chart.text.color');

                var text = this.Get('chart.scale.visible') ? 
                
                RGraph.number_format(this, this.value.toFixed(this.Get('chart.scale.decimals'))) : RGraph.number_format(this, this.value.toFixed(this.Get('chart.scale.decimals')), this.Get('chart.units.pre'), this.Get('chart.units.post'));

                RGraph.Text(this.context,
                            this.Get('chart.text.font'),
                            this.Get('chart.text.size'),
                            this.gutterLeft + this.bulbRadius,
                            this.coords[0][1] - this.Get('chart.text.size') - 2,
                            text,
                            'center',
                            'center',
                            true,
                            null,
                            'white');
            this.context.fill();
        }


        /**
        * Draw the scale if requested
        */
        if (this.Get('chart.scale.visible')) {
            this.DrawScale();
        }
    }

    
    /**
    * Draws the title
    */
    RGraph.Thermometer.prototype.DrawTitle = function ()
    {
        this.context.beginPath();
            this.context.fillStyle = this.Get('chart.text.color');
            RGraph.Text(this.context,this.Get('chart.text.font'),this.Get('chart.text.size') + 2,this.gutterLeft + ((this.canvas.width - this.gutterLeft - this.gutterRight) / 2),this.gutterTop,String(this.Get('chart.title')),'center','center',null,null,null,true);
        this.context.fill();
    }

    
    /**
    * Draws the title
    */
    RGraph.Thermometer.prototype.DrawSideTitle = function ()
    {
        var font = this.Get('chart.title.side.font') ? this.Get('chart.title.side.font') : this.Get('chart.text.font');
        var size = this.Get('chart.title.side.size') ? this.Get('chart.title.side.size') : this.Get('chart.text.size') + 2;

        this.context.beginPath();
            this.context.fillStyle = this.Get('chart.text.color');
            RGraph.Text(this.context,
                        font,
                        size,
                        this.gutterLeft * this.Get('chart.title.hpos'),
                        this.canvas.height / 2,
                        String(this.Get('chart.title.side')),
                        'center',
                        'center',
                        null,
                        270,
                        null,
                        true);
        this.context.fill();
    }


    /**
    * Draw the scale if requested
    */
    RGraph.Thermometer.prototype.DrawScale = function ()
    {
        var numLabels = this.Get('chart.ylabels.count'); // The -1 is so that  the number of labels tallies with what is displayed
        var step      = (this.max - this.min) / numLabels;
        
        this.context.fillStyle = this.Get('chart.text.color');
        
        var font      = this.Get('chart.text.font');
        var size       = this.Get('chart.text.size');
        var units_pre  = this.Get('chart.units.pre');
        var units_post = this.Get('chart.units.post');
        var decimals   = this.Get('chart.scale.decimals');

        this.context.beginPath();
            for (var i=1; i<=numLabels; ++i) {

                var x          = this.canvas.width - this.gutterRight;
                var y          = this.canvas.height - this.gutterBottom - (2 * this.bulbRadius) - ((this.graphArea[3] / numLabels) * i);
                var text       = RGraph.number_format(this, String((this.min + (i * step)).toFixed(decimals)), units_pre, units_post);

                RGraph.Text(this.context,
                            font,
                            size,
                            x - 6,
                            y,
                            text,
                            'center');
            }
            
            // Draw zero            
            RGraph.Text(this.context,
                        font,
                        size,
                        x - 6,
                        this.canvas.height - this.gutterBottom - (2 * this.bulbRadius),
                        RGraph.number_format(this, (this.min).toFixed(decimals),
                        units_pre,
                        units_post),
                        'center');
        this.context.fill();
    }


    /**
    * Returns the focused/clicked bar
    * 
    * @param event  e The event object
    * @param object   The chart object to use (OPTIONAL)
    */
    RGraph.Thermometer.prototype.getShape =
    RGraph.Thermometer.prototype.getBar = function (e)
    {
        for (var i=0; i<this.coords.length; i++) {

            var mouseCoords = RGraph.getMouseXY(e);
            var mouseX = mouseCoords[0];
            var mouseY = mouseCoords[1];

            var left   = this.coords[i][0];
            var top    = this.coords[i][1];
            var width  = this.coords[i][2];
            var height = this.coords[i][3];

            if (mouseX >= left && mouseX <= (left + width) && mouseY >= top && mouseY <= (top + height) ) {
            
                var tooltip = RGraph.parseTooltipText(this.Get('chart.tooltips'), i);

                return {       0: this,   'object': this,
                               1: left,   'x': left,
                               2: top,    'y': top,
                               3: width,  'width': width,
                               4: height, 'height': height,
                               5: i,      'index': i,
                                          'tooltip': tooltip
                       };
            }
        }
        
        return null;
    }


    /**
    * This function returns the value that the mouse is positioned t, regardless of
    * the actual indicated value.
    * 
    * @param object e The event object
    */
    RGraph.Thermometer.prototype.getValue = function (arg)
    {
        if (arg.length == 2) {
            var mouseX = arg[0];
            var mouseY = arg[1];
        } else {
            var mouseCoords = RGraph.getMouseXY(arg);
            var mouseX      = mouseCoords[0];
            var mouseY      = mouseCoords[1];
        }

        var canvas  = this.canvas;
        var context = this.context;

        
        var value = this.graphArea[3] - (mouseY - this.graphArea[1]);
            value = (value / this.graphArea[3]) * (this.max - this.min);
            value = value + this.min;
        
        value = Math.max(value, this.min);
        value = Math.min(value, this.max);

        return value;
    }



    /**
    * Each object type has its own Highlight() function which highlights the appropriate shape
    * 
    * @param object shape The shape to highlight
    */
    RGraph.Thermometer.prototype.Highlight = function (shape)
    {
        if (this.Get('chart.tooltips.highlight')) {
            // Add the new highlight
            RGraph.Highlight.Rect(this, shape);
        }
    }



    /**
    * The getObjectByXY() worker method. Don't call this - call:
    * 
    * RGraph.ObjectRegistry.getObjectByXY(e)
    * 
    * @param object e The event object
    */
    RGraph.Thermometer.prototype.getObjectByXY = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e);

        if (
               mouseXY[0] > this.Get('chart.gutter.left')
            && mouseXY[0] < (this.canvas.width - this.Get('chart.gutter.right'))
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
    RGraph.Thermometer.prototype.Adjusting_mousemove = function (e)
    {
        /**
        * Handle adjusting for the Thermometer
        */
        if (RGraph.Registry.Get('chart.adjusting') && RGraph.Registry.Get('chart.adjusting').uid == this.uid) {

            var mouseXY = RGraph.getMouseXY(e);
            var value   = this.getValue(e);
            
            if (typeof(value) == 'number') {

                // Fire the onadjust event
                RGraph.FireCustomEvent(this, 'onadjust');

                this.value = Number(value.toFixed(this.Get('chart.scale.decimals')));

                RGraph.Redraw();
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
    RGraph.Thermometer.prototype.positionTooltip = function (obj, x, y, tooltip, idx)
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
        tooltip.style.top  = canvasXY[1] + coordY - height - 7 + (coordH / 2)+ 'px';
        
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