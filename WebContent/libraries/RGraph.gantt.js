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
    * The gantt chart constructor
    * 
    * @param object canvas The cxanvas object
    * @param array  data   The chart data
    */
    RGraph.Gantt = function (id)
    {
        // Get the canvas and context objects
        this.id      = id;
        this.canvas  = document.getElementById(id);
        this.context = this.canvas.getContext("2d");
        this.canvas.__object__ = this;
        this.type              = 'gantt';
        this.isRGraph          = true;
        this.uid               = RGraph.CreateUID();
        this.canvas.uid        = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();


        /**
        * Compatibility with older browsers
        */
        RGraph.OldBrowserCompat(this.context);

        
        // Set some defaults
        this.properties = {
            'chart.background.barcolor1':   'white',
            'chart.background.barcolor2':   'white',
            'chart.background.grid':        true,
            'chart.background.grid.width':  1,
            'chart.background.grid.color':  '#ddd',
            'chart.background.grid.hsize':  20,
            'chart.background.grid.vsize':  20,
            'chart.background.grid.hlines': true,
            'chart.background.grid.vlines': true,
            'chart.background.grid.border': true,
            'chart.background.grid.autofit':true,
            'chart.background.grid.autofit.numhlines': 7,
            'chart.background.grid.autofit.numvlines': 20,
            'chart.background.vbars':       [],
            'chart.text.size':              10,
            'chart.text.font':              'Arial',
            'chart.text.color':             'black',
            'chart.gutter.left':            75,
            'chart.gutter.right':           25,
            'chart.gutter.top':             35,
            'chart.gutter.bottom':          25,
            'chart.labels':                 [],
            'chart.labels.align':           'bottom',
            'chart.labels.inbar':           null,
            'chart.labels.inbar.color':     'black',
            'chart.labels.inbar.bgcolor':   null,
            'chart.labels.inbar.align':     'left',
            'chart.labels.inbar.size':      10,
            'chart.labels.inbar.font':      'Arial',
            'chart.labels.inbar.above':     false,
            'chart.vmargin':                 2,
            'chart.title':                  '',
            'chart.title.background':       null,
            'chart.title.hpos':             null,
            'chart.title.vpos':             null,
            'chart.title.bold':             true,
            'chart.title.font':             null,
            'chart.title.yaxis':            '',
            'chart.title.yaxis.bold':        true,
            'chart.title.yaxis.pos':        null,
            'chart.title.yaxis.position':   'right',
            'chart.events':                 [],
            'chart.borders':                true,
            'chart.defaultcolor':           'white',
            'chart.coords':                 [],
            'chart.tooltips':               null,
            'chart.tooltips.effect':         'fade',
            'chart.tooltips.css.class':      'RGraph_tooltip',
            'chart.tooltips.highlight':     true,
            'chart.tooltips.event':         'onclick',
            'chart.highlight.stroke':       'rgba(0,0,0,0)',
            'chart.highlight.fill':         'rgba(255,255,255,0.7)',
            'chart.xmin':                   0,
            'chart.xmax':                   0,
            'chart.contextmenu':            null,
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
            'chart.adjustable':             false,
            'chart.events.click':           null,
            'chart.events.mousemove':       null
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
    RGraph.Gantt.prototype.Set = function (name, value)
    {
        if (name == 'chart.margin') {
            name = 'chart.vmargin'
        }

        this.properties[name.toLowerCase()] = value;
    }


    /**
    * A peudo getter
    * 
    * @param name  string The name of the property to get
    */
    RGraph.Gantt.prototype.Get = function (name)
    {
        if (name == 'chart.margin') {
            name = 'chart.vmargin'
        }

        return this.properties[name.toLowerCase()];
    }

    
    /**
    * Draws the chart
    */
    RGraph.Gantt.prototype.Draw = function ()
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
        * Work out the graphArea
        */
        this.graphArea     = this.canvas.width - this.gutterLeft - this.gutterRight;
        this.graphHeight   = this.canvas.height - this.gutterTop - this.gutterBottom;
        this.numEvents     = this.Get('chart.events').length
        this.barHeight     = this.graphHeight / this.numEvents;
        this.halfBarHeight = this.barHeight / 2;
        
        /**
        * Populate the tooltips option from the events array
        */
        //var tooltips = []
        //for (var i=0; i<this.Get('chart.events').length; ++i) {
        //    tooltips.push(this.Get('chart.events')[i][3]);
        //}
        //this.Set('chart.tooltips', tooltips);




        /**
        * Draw the background
        */
        RGraph.background.Draw(this);



        /**
        * Draw the labels at the top
        */
        this.DrawLabels();



        /**
        * Draw the events
        */
        this.DrawEvents();



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
    * Draws the labels at the top and the left of the chart
    */
    RGraph.Gantt.prototype.DrawLabels = function ()
    {
        this.context.beginPath();
        this.context.fillStyle = this.Get('chart.text.color');

        /**
        * Draw the X labels at the top of the chart.
        */
        var labels = this.Get('chart.labels');
        var labelSpace = (this.graphArea) / labels.length;
        var x      = this.gutterLeft + (labelSpace / 2);
        var y      = this.gutterTop - (this.Get('chart.text.size') / 2) - 5;
        var font   = this.Get('chart.text.font');
        var size   = this.Get('chart.text.size');

        this.context.strokeStyle = 'black'
        
        /**
        * This facilitates chart.labels.align
        */
        if (this.Get('chart.labels.align') == 'bottom') {
            y = this.canvas.height - this.gutterBottom + size + 2;
        }

        /**
        * Draw the horizontal labels
        */
        for (i=0; i<labels.length; ++i) {
            RGraph.Text(this.context,
                        font,
                        size,
                        x + (i * labelSpace),
                        y,
                        String(labels[i]),
                        'center',
                        'center');
        }

        /**
        * Draw the vertical labels
        */
        for (var i=0; i<this.Get('chart.events').length; ++i) {
            
            var ev = this.Get('chart.events')[i];
            var x  = this.gutterLeft;
            var y  = this.gutterTop + this.halfBarHeight + (i * this.barHeight);

            RGraph.Text(this.context,
                        font,
                        size,
                        x - 5, y,
                        RGraph.is_array(ev[0]) ? (ev[0][3] ? String(ev[0][3]) : '') : String(ev[3]),
                        'center',
                        'right');
        }
    }
    
    /**
    * Draws the events to the canvas
    */
    RGraph.Gantt.prototype.DrawEvents = function ()
    {
        var canvas  = this.canvas;
        var context = this.context;
        var events  = this.Get('chart.events');

        /**
        * Reset the coords array to prevent it growing
        */
        this.coords = [];

        /**
        * First draw the vertical bars that have been added
        */
        if (this.Get('chart.vbars')) {
            for (i=0; i<this.Get('chart.vbars').length; ++i) {
                // Boundary checking
                if (this.Get('chart.vbars')[i][0] + this.Get('chart.vbars')[i][1] > this.Get('chart.xmax')) {
                    this.Get('chart.vbars')[i][1] = 364 - this.Get('chart.vbars')[i][0];
                }
    
                var barX   = this.gutterLeft + (( (this.Get('chart.vbars')[i][0] - this.Get('chart.xmin')) / (this.Get('chart.xmax') - this.Get('chart.xmin')) ) * this.graphArea);

                var barY   = this.gutterTop;
                var width  = (this.graphArea / (this.Get('chart.xmax') - this.Get('chart.xmin')) ) * this.Get('chart.vbars')[i][1];
                var height = RGraph.GetHeight(this) - this.gutterTop - this.gutterBottom;
                
                // Right hand bounds checking
                if ( (barX + width) > (RGraph.GetWidth(this) - this.gutterRight) ) {
                    width = RGraph.GetWidth(this) - this.gutterRight - barX;
                }
    
                context.fillStyle = this.Get('chart.vbars')[i][2];
                context.fillRect(barX, barY, width, height);
            }
        }


        /**
        * Draw the events
        */
        for (i=0; i<events.length; ++i) {            
            if (typeof(events[i][0]) == 'number') {
                this.DrawSingleEvent(events[i], i);
            } else {
                for (var j=0; j<events[i].length; ++j) {
                    this.DrawSingleEvent(events[i][j], i);
                }
            }

        }
    }


    /**
    * Retrieves the bar (if any) that has been click on or is hovered over
    * 
    * @param object e The event object
    */
    RGraph.Gantt.prototype.getShape =
    RGraph.Gantt.prototype.getBar = function (e)
    {
        e = RGraph.FixEventObject(e);

        var canvas      = e.target;
        var context     = canvas.getContext('2d');
        var mouseCoords = RGraph.getMouseXY(e);
        var mouseX      = mouseCoords[0];
        var mouseY      = mouseCoords[1];

        /**
        * Loop through the bars determining if the mouse is over a bar
        */
        for (var i=0; i<this.coords.length; i++) {

            var left   = this.coords[i][0];
            var top    = this.coords[i][1];
            var width  = this.coords[i][2];
            var height = this.coords[i][3];

            if (   mouseX >= left
                && mouseX <= (left + width)
                && mouseY >= top
                && mouseY <= (top + height)
               ) {
               
                var tooltip = RGraph.parseTooltipText(this.Get('chart.tooltips'), i);

                return {0: this,   'object': this,
                        1: left,   'x':      left,
                        2: top,    'y':      top,
                        3: width,  'width':  width,
                        4: height, 'height': height,
                        5: i,      'index':  i,
                                   'tooltip': tooltip};
            }
        }
    }


    /**
    * Draws a single event
    */
    RGraph.Gantt.prototype.DrawSingleEvent = function (ev, index)
    {
        var min     = this.Get('chart.xmin');
        var context = this.context;

        context.beginPath();
        context.strokeStyle = 'black';
        context.fillStyle = ev[4] ? ev[4] : this.Get('chart.defaultcolor');

        var barStartX  = this.gutterLeft + (((ev[0] - min) / (this.Get('chart.xmax') - min)) * this.graphArea);
        var barStartY  = this.gutterTop + (index * this.barHeight);
        var barWidth   = (ev[1] / (this.Get('chart.xmax') - min) ) * this.graphArea;

        /**
        * If the width is greater than the graph atrea, curtail it
        */
        if ( (barStartX + barWidth) > (this.canvas.width - this.gutterRight) ) {
            barWidth = this.canvas.width - this.gutterRight - barStartX;
        }
        
        /**
        *  Draw the actual bar storing store the coordinates
        */
        this.coords.push([barStartX, barStartY + this.Get('chart.vmargin'), barWidth, this.barHeight - (2 * this.Get('chart.vmargin'))]);

        // draw the border around the bar
        if (this.Get('chart.borders') || ev[6]) {
            context.strokeStyle = typeof(ev[6]) == 'string' ? ev[6] : 'black';
            context.lineWidth = (typeof(ev[7]) == 'number' ? ev[7] : 1);
            context.beginPath();
            context.strokeRect(barStartX, barStartY + this.Get('chart.vmargin'), barWidth, this.barHeight - (2 * this.Get('chart.vmargin')) );
        }
        
        this.context.beginPath();
            context.fillRect(barStartX, barStartY + this.Get('chart.vmargin'), barWidth, this.barHeight - (2 * this.Get('chart.vmargin')) );
        this.context.fill();

        // Work out the completeage indicator
        var complete = (ev[2] / 100) * barWidth;

        // Draw the % complete indicator. If it's greater than 0
        if (typeof(ev[2]) == 'number') {
            context.beginPath();
            context.fillStyle = ev[5] ? ev[5] : '#0c0';
            context.fillRect(barStartX,
                                  barStartY + this.Get('chart.vmargin'),
                                  (ev[2] / 100) * barWidth,
                                  this.barHeight - (2 * this.Get('chart.vmargin')) );
            
            context.beginPath();
                context.fillStyle = this.Get('chart.text.color');
                RGraph.Text(context, this.Get('chart.text.font'), this.Get('chart.text.size'), barStartX + barWidth + 5, barStartY + this.halfBarHeight, String(ev[2]) + '%', 'center');
        }
        
        /**
        * Draw the inbar label if it's defined
        */
        if (this.Get('chart.labels.inbar') && this.Get('chart.labels.inbar')[index]) {
            
            var label = String(this.Get('chart.labels.inbar')[index]);
            var halign = this.Get('chart.labels.inbar.align') == 'left' ? 'left' : 'center';
                halign = this.Get('chart.labels.inbar.align') == 'right' ? 'right' : halign;
            
            // Work out the position of the text
            if (halign == 'right') {
                var x = (barStartX + barWidth) - 5;
            } else if (halign == 'center') {
                var x = barStartX + (barWidth / 2);
            } else {
                var x = barStartX + 5;
            }


            // Draw the labels "above" the bar
            if (this.Get('chart.labels.inbar.above')) {
                x = barStartX + barWidth + 5;
                halign = 'left';
            }


            // Set the color
            this.context.fillStyle = this.Get('chart.labels.inbar.color');

            this.context.beginPath();
                RGraph.Text(this.context,
                            this.Get('chart.labels.inbar.font'),
                            this.Get('chart.labels.inbar.size'),
                            x,
                            barStartY + this.halfBarHeight,
                            label,
                            'center',
                            halign,
                            typeof(this.Get('chart.labels.inbar.bgcolor')) == 'string' ? true : false,
                            null,
                            typeof(this.Get('chart.labels.inbar.bgcolor')) == 'string' ? this.Get('chart.labels.inbar.bgcolor') : null
                           );
            this.context.fill();
        }
    }



    /**
    * Each object type has its own Highlight() function which highlights the appropriate shape
    * 
    * @param object shape The shape to highlight
    */
    RGraph.Gantt.prototype.Highlight = function (shape)
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
    RGraph.Gantt.prototype.getObjectByXY = function (e)
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
    * This method handles the adjusting calculation for when the mouse is moved
    * 
    * @param object e The event object
    */
    RGraph.Gantt.prototype.Adjusting_mousemove = function (e)
    {
        /**
        * Handle adjusting for the Bar
        */
        if (RGraph.Registry.Get('chart.adjusting') && RGraph.Registry.Get('chart.adjusting').uid == this.uid) {
            
            var bar        = RGraph.Registry.Get('chart.adjusting.gantt');
            
            if (bar) {
                var mouseXY    = RGraph.getMouseXY(e);
                var obj        = RGraph.Registry.Get('chart.adjusting.gantt')['object'];
                var index      = bar['index'];
                var diff       = ((mouseXY[0] - RGraph.Registry.Get('chart.adjusting.gantt')['mousex']) / (obj.canvas.width - obj.gutterLeft - obj.gutterRight)) * obj.Get('chart.xmax');
                var eventStart = RGraph.Registry.Get('chart.adjusting.gantt')['event_start'];
                var duration   = RGraph.Registry.Get('chart.adjusting.gantt')['event_duration'];
    
                if (bar['mode'] == 'move') {
    
                    diff = Math.round(diff);
    
                    if (   eventStart + diff >= 0
                        && (eventStart + diff + obj.Get('chart.events')[index][1]) < obj.Get('chart.xmax')) {
    
                        obj.Get('chart.events')[index][0] = eventStart + diff;
                    
                    } else if (eventStart + diff < 0) {
                        obj.Get('chart.events')[index][0] = 0;
                    //
                    } else if ((eventStart + diff + obj.Get('chart.events')[index][1]) > obj.Get('chart.xmax')) {
                        obj.Get('chart.events')[index][0] = obj.Get('chart.xmax') - obj.Get('chart.events')[index][1];
                    }
                
                } else if (bar['mode'] == 'resize') {
    
                    /*
                    * Account for the right hand gutter. Appears to be a FF bug
                    */
                    if (mouseXY[0] > (obj.canvas.width - obj.gutterRight)) {
                        mouseXY[0] = obj.canvas.width - obj.gutterRight;
                    }
                    
                    var diff = ((mouseXY[0] - RGraph.Registry.Get('chart.adjusting.gantt')['mousex']) / (obj.canvas.width - obj.gutterLeft - obj.gutterRight)) * obj.Get('chart.xmax');
                        diff = Math.round(diff);
    
                    obj.Get('chart.events')[index][1] = duration + diff;
                    
                    if (obj.Get('chart.events')[index][1] < 0) {
                        obj.Get('chart.events')[index][1] = 1;
                    }
                }
                
                RGraph.FireCustomEvent(obj, 'onadjust')
    
                RGraph.Clear(obj.canvas);
                RGraph.RedrawCanvas(obj.canvas);
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
    RGraph.Gantt.prototype.positionTooltip = function (obj, x, y, tooltip, idx)
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