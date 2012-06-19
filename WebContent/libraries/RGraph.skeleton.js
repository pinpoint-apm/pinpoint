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




    /**
    * This is the RGraph.skeleton.js file which you can use as a base for creating new graph types.
    */



    
    /**
    * Having this here means that the RGraph libraries can be included in any order, instead of you having
    * to include the common core library first.
    */
    if (typeof(RGraph) == 'undefined') RGraph = {};




    /**
    * The chart constructor. This function sets up the object. It takes the ID (the HTML attribute) of the canvas as the
    * first argument and the data as the second. If you need to change this, you can.
    * 
    * @param string id    The canvas tag ID
    * @param array  data  The chart data
    */
    RGraph.Skeleton = function (id, data)
    {
        /**
        * Set these as object properties so they don't have to be constantly retrieved. Note that using a dollar
        * function - $() - can cause conflicts with popular javascript libraries, eg jQuery. It's therefore best
        * to stick to document.getElementById(). Setting the canvas and context as object properties means you
        * can reference them like this: myObj.canvas
        *                               myObj.context
        */
        this.id      = id;
        this.canvas  = document.getElementById(id);
        this.context = this.canvas.getContext ? this.canvas.getContext("2d") : null;

        /**
        * This puts a reference to this object on to the canvas. Useful in event handling.
        */
        this.canvas.__object__ = this;

        /**
        * This defines the type of this graph type and should be a one word description.
        */
        this.type = 'skeleton';


        /**
        * This facilitates easy object identification, and should be true
        */
        this.isRGraph = true;


        /**
        * This adds a uid to the object that you can use for identification purposes
        */
        this.uid = RGraph.CreateUID();


        /**
        * This adds a UID to the canvas for identification purposes
        */
        this.canvas.uid = this.canvas.uid ? this.canvas.uid : RGraph.CreateUID();


        /**
        * This does a few things, for example adding the .fillText() method to the canvas 2D context when
        * it doesn't exist. This facilitates the graphs to be still shown in older browser (though without
        * text obviously). You'll find the function in RGraph.common.core.js
        */
        RGraph.OldBrowserCompat(this.context);


        /**
        * Some example background properties, as used by the method RGraph.background.Draw()
        */
        this.properties = {
            'chart.gutter.left':            25,
            'chart.gutter.right':           25,
            'chart.gutter.top':             25,
            'chart.gutter.bottom':          25,
            'chart.colors':                 ['red','blue'],
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
            'chart.background.grid.autofit':false,
            'chart.background.grid.autofit.align':false,
            'chart.background.grid.autofit.numhlines': 7,
            'chart.background.grid.autofit.numvlines': 20,
            'chart.zoom.factor':            1.5,
            'chart.zoom.fade.in':           true,
            'chart.zoom.fade.out':          true,
            'chart.zoom.hdir':              'right',
            'chart.zoom.vdir':              'down',
            'chart.zoom.frames':            25,
            'chart.zoom.delay':             16.666,
            'chart.zoom.shadow':            true,

            'chart.zoom.background':        true,
            'chart.contextmenu':            null,
            'chart.labels':                 null,
            'chart.labels.ingraph':         null,
            'chart.labels.above':           false,
            'chart.labels.above.decimals':  0,
            'chart.labels.above.size':      null,
            'chart.scale.decimals':         0,
            'chart.scale.point':            '.',
            'chart.scale.thousand':         ',',
            'chart.crosshairs':             false,
            'chart.crosshairs.color':       '#333',
            'chart.crosshairs.hline':       true,
            'chart.crosshairs.vline':       true,
            'chart.annotatable':            false,
            'chart.annotate.color':         'black',
            'chart.units.pre':              '',
            'chart.units.post':             '',
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
            'chart.key.color.shape':        'square',
            'chart.key.rounded':            true,
            'chart.key.linewidth':          1,
            'chart.key.colors':             null,
            'chart.title':                  '',
            'chart.title.background':       null,
            'chart.title.hpos':             null,
            'chart.title.vpos':             null,
            'chart.title.bold':             true,
            'chart.title.font':             null,
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
            'chart.title.yaxis.position':   'left',
            'chart.text.color':             'black',
            'chart.text.size':              10,
            'chart.text.angle':             0,
            'chart.text.font':              'Arial',
            'chart.resizable':              false,
            'chart.resize.handle.background': null,
            'chart.adjustable':             false,
            'chart.hmargin':                5,
            'chart.shadow':                 false,
            'chart.shadow.color':           '#666',
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
            'chart.events.click':           null,
            'chart.events.mousemove':       null
        }

        /**
        * A simple check that the browser has canvas support
        */
        if (!this.canvas) {
            alert('[SKELETON] No canvas support');
            return;
        }

        /**
        * Store the data that was passed to this constructor
        */
        this.data = data;
        
        /**
        * This can be used to store the coordinates of shapes on the graph
        */
        this.coords = [];



        /**
        * Objects are now always registered so that the chart is redrawn if need be.
        */
        RGraph.Register(this);
    }




    /**
    * A setter method for setting graph properties. It can be used like this: obj.Set('chart.background.grid', false);
    * 
    * @param name  string The name of the property to set
    * @param value mixed  The value of the property
    */
    RGraph.Skeleton.prototype.Set = function (name, value)
    {
        this.properties[name.toLowerCase()] = value;
    }




    /**
    * A getter method for retrieving graph properties. It can be used like this: obj.Get('chart.background.grid');
    * This can be used inside your methods that draw the graph.
    * 
    * @param name  string The name of the property to get
    */
    RGraph.Skeleton.prototype.Get = function (name)
    {
        return this.properties[name.toLowerCase()];
    }




    /**
    * The function you call to draw the chart after you have set all of the graph properties
    */
    RGraph.Skeleton.prototype.Draw = function ()
    {
        /**
        * Fire the custom RGraph onbeforedraw event (which should be fired before the chart is drawn)
        */
        RGraph.FireCustomEvent(this, 'onbeforedraw');
        
        
        
        /**
        * Draw the background image
        */
        RGraph.DrawBackgroundImage(this);





        /*************************
        * Draw the chart here... *
        *************************/




        /**
        * These call common functions, that facilitate some of RGraphs features
        */




        /**
        * Setup the context menu if required
        */
        if (this.Get('chart.contextmenu')) {
            RGraph.ShowContext(this);
        }

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
        * This function enables adjusting
        */
        if (this.Get('chart.adjustable')) {
            RGraph.AllowAdjusting(this);
        }


        /**
        * This installs the all of the event listeners (ie window.onmouseup, canvas.onmousedown, canvas.onmouseup
        * and canvas.onmousemove).
        */
        RGraph.InstallEventListeners(this);

        /**
        * Fire the custom RGraph ondraw event (which should be fired when you have drawn the chart)
        */
        RGraph.FireCustomEvent(this, 'ondraw');
    }



    /**
    * The getShape method returns details of any shape that may have been clicked or hovered over.
    * For example with the Bar chart it would return a Rect when a bar is clicked or hovered over.
    * 
    * @param  object The event object
    * @return        See below for details
    */
    RGraph.Skeleton.prototype.getShape = function (e)
    {
        // The getShape function should return an array with numeric and testual keys - 
        // these keys are (In the below example a rectangle shape is assumed)):
        //
        // return {
        //         0: this,           // The chart object
        //         1: x,              // The X position
        //         2: y,              // The Y position
        //         3: width,          // The width
        //         4: height,         // The height
        //         5: index,          // The index of the shape. This is used to retrieve relevant tooltips for example.
        //
        //         'object':, this,   // The chart object
        //              'x': x,       // The X position
        //              'y': y,       // The Y position
        //          'width': width,   // The width
        //         'height': height   // The height,
        //          'index': index    // The index of the shape. This is used to retrieve relevant tooltips for example.
        //          'tooltip': tooltip    // The applicable tooltip for this shape
        //         }
    }



    /**
    * Each object type has its own Highlight() function which highlights the appropriate shape
    * 
    * @param object shape The shape to highlight
    */
    RGraph.Skeleton.prototype.Highlight = function (shape)
    {
        // Rectangle highlight
        // RGraph.Highlight.Rect(this, shape);

        // Point highlight
        // RGraph.Highlight.Point(this, shape);
    }



    /**
    * The getObjectByXY() worker method. Don't call this call:
    * 
    * RGraph.ObjectRegistry.getObjectByXY(e)
    * 
    * This function should return the object ie ("this") if the mouseXY is
    * in the relevant area of the canvas (ie this charts area)
    * 
    * @param object e The event object
    */
    RGraph.Skeleton.prototype.getObjectByXY = function (e)
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
    * This function positions a tooltip when it is displayed. This particular
    * example is taken from the Line chart.
    * 
    * @param obj object    The chart object
    * @param int x         The X coordinate specified for the tooltip
    * @param int y         The Y coordinate specified for the tooltip
    * @param objec tooltip The tooltips DIV element
    */
    RGraph.Skeleton.prototype.positionTooltip = function (obj, x, y, tooltip, idx)
    {
        var coordX     = obj.coords[tooltip.__index__][0];
        var coordY     = obj.coords[tooltip.__index__][1];
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