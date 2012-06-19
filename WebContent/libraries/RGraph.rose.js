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
    * The rose chart constuctor
    * 
    * @param object canvas
    * @param array data
    */
    RGraph.Rose = function (id, data)
    {
        this.id                = id;
        this.canvas            = document.getElementById(id);
        this.context           = this.canvas.getContext('2d');
        this.data              = data;
        this.canvas.__object__ = this;
        this.type              = 'rose';
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
        this.angles  = [];
        
        this.properties = {
            'chart.centerx':                null,
            'chart.centery':                null,
            'chart.radius':                 null,
            'chart.colors':                 ['rgba(255,0,0,0.5)', 'rgba(255,255,0,0.5)', 'rgba(0,255,255,0.5)', 'rgb(0,255,0)', 'gray', 'blue', 'rgb(255,128,255)','green', 'pink', 'gray', 'aqua'],
            'chart.colors.sequential':      false,
            'chart.colors.alpha':           null,
            'chart.margin':                 0,
            'chart.strokestyle':            '#aaa',
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
            'chart.labels.offset':          0,
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
            'chart.tooltips.event':         'onclick',
            'chart.tooltips.effect':        'fade',
            'chart.tooltips.css.class':     'RGraph_tooltip',
            'chart.tooltips.highlight':     true,
            'chart.highlight.stroke':       'rgba(0,0,0,0)',
            'chart.highlight.fill':         'rgba(255,255,255,0.7)',
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
            'chart.ymax':                   null,
            'chart.ymin':                   0,
            'chart.scale.decimals':         null,
            'chart.scale.point':            '.',
            'chart.scale.thousand':         ',',
            'chart.variant':                'stacked',
            'chart.animation.roundrobin.factor':  1,
            'chart.animation.roundrobin.radius': true,
            'chart.exploded':               0,
            'chart.events.mousemove':       null,
            'chart.events.click':           null
        }



        /**
        * Register this object
        */
        RGraph.Register(this);
    }


    /**
    * A simple setter
    * 
    * @param string name  The name of the property to set
    * @param string value The value of the property
    */
    RGraph.Rose.prototype.Set = function (name, value)
    {
        this.properties[name.toLowerCase()] = value;
    }
    
    
    /**
    * A simple getter
    * 
    * @param string name The name of the property to get
    */
    RGraph.Rose.prototype.Get = function (name)
    {
        return this.properties[name.toLowerCase()];
    }

    
    /**
    * This method draws the rose chart
    */
    RGraph.Rose.prototype.Draw = function ()
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
        this.radius       = (Math.min(this.canvas.width - this.gutterLeft - this.gutterRight, this.canvas.height - this.gutterTop - this.gutterBottom) / 2);
        this.centerx      = ((this.canvas.width - this.gutterLeft - this.gutterRight) / 2) + this.gutterLeft;
        this.centery      = ((this.canvas.height - this.gutterTop - this.gutterBottom) / 2) + this.gutterTop;
        this.angles       = [];
        this.total        = 0;
        this.startRadians = 0;
        
        /**
        * Change the centerx marginally if the key is defined
        */
        if (this.Get('chart.key') && this.Get('chart.key').length > 0 && this.Get('chart.key').length >= 3) {
            this.centerx = this.centerx - this.Get('chart.gutter.right') + 5;
        }



        // User specified radius, centerx and centery
        if (typeof(this.Get('chart.centerx')) == 'number') this.centerx = this.Get('chart.centerx');
        if (typeof(this.Get('chart.centery')) == 'number') this.centery = this.Get('chart.centery');
        if (typeof(this.Get('chart.radius')) == 'number')  this.radius  = this.Get('chart.radius');

        this.DrawBackground();
        this.DrawRose();
        this.DrawLabels();

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
        * This function enables adjusting
        */
        if (this.Get('chart.adjustable')) {
            RGraph.AllowAdjusting(this);
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
    RGraph.Rose.prototype.DrawBackground = function ()
    {
        this.context.lineWidth = 1;
    
        // Draw the background grey circles
        this.context.beginPath();
        this.context.strokeStyle = '#ccc';
        for (var i=15; i<this.radius - (RGraph.isOld() ? 5 : 0); i+=15) {// Radius must be greater than 0 for Opera to work
            
            //this.context.moveTo(this.centerx + i, this.centery);

            // Radius must be greater than 0 for Opera to work
            this.context.arc(this.centerx, this.centery, i, 0, TWOPI, 0);
        }
        this.context.stroke();

        // Draw the background lines that go from the center outwards
        this.context.beginPath();
        for (var i=15; i<360; i+=15) {
        
            // Radius must be greater than 0 for Opera to work
            this.context.arc(this.centerx, this.centery, this.radius, i / 57.3, (i + 0.1) / 57.3, 0); // The 0.1 avoids a bug in Chrome 6
        
            this.context.lineTo(this.centerx, this.centery);
        }
        this.context.stroke();
        
        this.context.beginPath();
        this.context.strokeStyle = 'black';
    
        // Draw the X axis
        this.context.moveTo(this.centerx - this.radius, AA(this, this.centery) );
        this.context.lineTo(this.centerx + this.radius, AA(this, this.centery) );
    
        // Draw the X ends
        this.context.moveTo(AA(this, this.centerx - this.radius), this.centery - 5);
        this.context.lineTo(AA(this, this.centerx - this.radius), this.centery + 5);
        this.context.moveTo(AA(this, this.centerx + this.radius), this.centery - 5);
        this.context.lineTo(AA(this, this.centerx + this.radius), this.centery + 5);
        
        // Draw the X check marks
        for (var i=(this.centerx - this.radius); i<(this.centerx + this.radius); i+=20) {
            this.context.moveTo(AA(this, i),  this.centery - 3);
            this.context.lineTo(AA(this, i),  this.centery + 3.5);
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
    * This method draws the data on the graph
    */
    RGraph.Rose.prototype.DrawRose = function ()
    {
        var max  = 0;
        var data = this.data;
        var margin = RGraph.degrees2Radians(this.Get('chart.margin'));

        // Must be at least two data points
        //if (data.length < 2) {
        //    alert('[ROSE] Must be at least two data points! [' + data + ']');
        //    return;
        //}
    
        // Work out the maximum value and the sum
        if (!this.Get('chart.ymax')) {
            // Work out the max
            for (var i=0; i<data.length; ++i) {
                if (typeof(data[i]) == 'number') {
                    max = Math.max(max, data[i]);
                } else if (typeof(data[i]) == 'object' && this.Get('chart.variant') == 'non-equi-angular') {
                    max = Math.max(max, data[i][0]);
                
                // Fallback is stacked
                } else {
                    max = Math.max(max, RGraph.array_sum(data[i]));
                }
            }

            this.scale = RGraph.getScale(max, this);
            this.max = this.scale[4];
        } else {
            var ymax = this.Get('chart.ymax');
            var ymin = this.Get('chart.ymin');

            this.scale = [
                          ((ymax - ymin) * 0.2) + ymin,
                          ((ymax - ymin) * 0.4) + ymin,
                          ((ymax - ymin) * 0.6) + ymin,
                          ((ymax - ymin) * 0.8) + ymin,
                          ((ymax - ymin) * 1.0) + ymin
                         ];
            this.max = this.scale[4];
        }
        
        this.sum = RGraph.array_sum(data);
        
        // Move to the centre
        this.context.moveTo(this.centerx, this.centery);
    
        this.context.stroke(); // Stroke the background so it stays grey
    
        // Transparency
        if (this.Get('chart.colors.alpha')) {
            this.context.globalAlpha = this.Get('chart.colors.alpha');
        }

        /*******************************************************
        * A non-equi-angular Rose chart
        *******************************************************/
        if (typeof(this.Get('chart.variant')) == 'string' && this.Get('chart.variant') == 'non-equi-angular') {
            /*******************************************************
            * NON-EQUI-ANGULAR GOES HERE
            *******************************************************/
            var total=0;
            for (var i=0; i<data.length; ++i) {
                total += data[i][1];
            }
            
            
            for (var i=0; i<this.data.length; ++i) {
            
                var segmentRadians = (this.data[i][1] / total) * TWOPI;
                var radius         = ((this.data[i][0] - this.Get('chart.ymin')) / (this.max - this.Get('chart.ymin'))) * (this.radius - 10);
                
                this.context.strokeStyle = this.Get('chart.strokestyle');
                this.context.fillStyle = this.Get('chart.colors')[0];

                if (this.Get('chart.colors.sequential')) {
                    this.context.fillStyle = this.Get('chart.colors')[i];
                }

                this.context.beginPath(); // Begin the segment

                    var startAngle = (this.startRadians * this.Get('chart.animation.roundrobin.factor')) - HALFPI + margin;
                    var endAngle   = ((this.startRadians + segmentRadians) * this.Get('chart.animation.roundrobin.factor')) - HALFPI - margin;

                    var exploded  = this.getexploded(i, startAngle, endAngle, this.Get('chart.exploded'));
                    var explodedX = exploded[0];
                    var explodedY = exploded[1];


                    this.context.arc(this.centerx + explodedX,
                                     this.centery + explodedY,
                                     this.Get('chart.animation.roundrobin.radius') ? radius * this.Get('chart.animation.roundrobin.factor') : radius,
                                     startAngle,
                                     endAngle,
                                     0);
                    this.context.lineTo(this.centerx + explodedX, this.centery + explodedY);
                this.context.closePath(); // End the segment
                
                this.context.stroke();
                this.context.fill();
                
                // Store the start and end angles

                this.angles.push(gg = [
                                  startAngle,
                                  endAngle,
                                  0,
                                  radius,
                                  this.centerx + explodedX,
                                  this.centery + explodedY
                                 ]);

                this.startRadians += segmentRadians;
            }
        } else {
            /*******************************************************
            * Draw regular segments here
            *******************************************************/
            for (var i=0; i<this.data.length; ++i) {

                this.context.strokeStyle = this.Get('chart.strokestyle');
                this.context.fillStyle = this.Get('chart.colors')[0];

                /*******************************************************
                * This allows sequential colors
                *******************************************************/
                if (this.Get('chart.colors.sequential')) {
                    this.context.fillStyle = this.Get('chart.colors')[i];
                }

                var segmentRadians = (1 / this.data.length) * TWOPI;
    
                if (typeof(this.data[i]) == 'number') {
                    this.context.beginPath(); // Begin the segment

                        var radius = ((this.data[i] - this.Get('chart.ymin')) / (this.max - this.Get('chart.ymin'))) * (this.radius - 10);

                        var startAngle = (this.startRadians * this.Get('chart.animation.roundrobin.factor')) - HALFPI + margin;
                        var endAngle   = (this.startRadians * this.Get('chart.animation.roundrobin.factor')) + (segmentRadians) - HALFPI - margin;

                        var exploded  = this.getexploded(i, startAngle, endAngle, this.Get('chart.exploded'));
                        var explodedX = exploded[0];
                        var explodedY = exploded[1];

                        this.context.arc(this.centerx + explodedX,
                                         this.centery + explodedY,
                                         this.Get('chart.animation.roundrobin.radius') ? radius * this.Get('chart.animation.roundrobin.factor') : radius,
                                         startAngle,
                                         endAngle,
                                         0);
                        this.context.lineTo(this.centerx + explodedX, this.centery + explodedY);
                    this.context.closePath(); // End the segment
                    this.context.stroke();
                    this.context.fill();

                    if (endAngle == 0) {
                        //endAngle = TWOPI;
                    }

                    // Store the start and end angles
                    this.angles.push([
                                      startAngle,
                                      endAngle,
                                      0,
                                      radius * this.Get('chart.animation.roundrobin.factor'),
                                      this.centerx + explodedX,
                                      this.centery + explodedY
                                     ]);

                /*******************************************************
                * Draw a stacked segment
                *******************************************************/
                } else if (typeof(this.data[i]) == 'object') {
                    
                    var margin = this.Get('chart.margin') / (180 / PI);
                    

                    for (var j=0; j<this.data[i].length; ++j) {
                    
                        var startAngle = (this.startRadians * this.Get('chart.animation.roundrobin.factor')) - HALFPI + margin;
                        var endAngle  = (this.startRadians * this.Get('chart.animation.roundrobin.factor'))+ segmentRadians - HALFPI - margin;
                    
                        var exploded  = this.getexploded(i, startAngle, endAngle, this.Get('chart.exploded'));
                        var explodedX = exploded[0];
                        var explodedY = exploded[1];
    
                        this.context.fillStyle = this.Get('chart.colors')[j];
                        if (j == 0) {
                            this.context.beginPath(); // Begin the segment
                                var startRadius = 0;
                                var endRadius = ((this.data[i][j] - this.Get('chart.ymin')) / (this.max - this.Get('chart.ymin'))) * (this.radius - 10);
                    
                                this.context.arc(this.centerx + explodedX,
                                                 this.centery + explodedY,
                                                 this.Get('chart.animation.roundrobin.radius') ? endRadius * this.Get('chart.animation.roundrobin.factor') : endRadius,
                                                 startAngle,
                                                 endAngle,
                                                 0);
                                this.context.lineTo(this.centerx + explodedX, this.centery + explodedY);
                            this.context.closePath(); // End the segment
                            this.context.stroke();
                            this.context.fill();
    
                            this.angles.push([
                                              startAngle,
                                              endAngle,
                                              0,
                                              endRadius * this.Get('chart.animation.roundrobin.factor'),
                                              this.centerx + explodedX,
                                              this.centery + explodedY
                                             ]);
                        
                        } else {

                            this.context.beginPath(); // Begin the segment
                                
                                var startRadius = endRadius; // This comes from the prior iteration of this loop
                                var endRadius = (((this.data[i][j] - this.Get('chart.ymin')) / (this.max - this.Get('chart.ymin'))) * (this.radius - 10)) + startRadius;
                
                                this.context.arc(this.centerx + explodedX,
                                                 this.centery + explodedY,
                                                 startRadius  * this.Get('chart.animation.roundrobin.factor'),
                                                 startAngle,
                                                 endAngle,
                                                 0);
                
                                this.context.arc(this.centerx + explodedX,
                                                 this.centery + explodedY,
                                                 endRadius  * this.Get('chart.animation.roundrobin.factor'),
                                                 endAngle,
                                                 startAngle,
                                                 true);
                
                            this.context.closePath(); // End the segment
                            this.context.stroke();
                            this.context.fill();
    
                            this.angles.push([
                                              startAngle,
                                              endAngle,
                                              startRadius * this.Get('chart.animation.roundrobin.factor'),
                                              endRadius * this.Get('chart.animation.roundrobin.factor'),
                                              this.centerx + explodedX,
                                              this.centery + explodedY
                                             ]);
                        }
                    }
                }
    
                this.startRadians += segmentRadians;
            }
        }

        // Turn off the transparency
        if (this.Get('chart.colors.alpha')) {
            this.context.globalAlpha = 1;
        }

        // Draw the title if any has been set
        if (this.Get('chart.title')) {
            RGraph.DrawTitle(this,
                             this.Get('chart.title'),
                             (this.canvas.height / 2) - this.radius,
                             this.centerx,
                             this.Get('chart.title.size') ? this.Get('chart.title.size') : this.Get('chart.text.size') + 2);
        }
    }


    /**
    * Unsuprisingly, draws the labels
    */
    RGraph.Rose.prototype.DrawLabels = function ()
    {
        this.context.lineWidth = 1;
        var key = this.Get('chart.key');

        if (key && key.length) {
            RGraph.DrawKey(this, key, this.Get('chart.colors'));
        }
        
        // Set the color to black
        this.context.fillStyle = 'black';
        this.context.strokeStyle = 'black';
        
        var r          = this.radius - 10;
        var font_face  = this.Get('chart.text.font');
        var font_size  = this.Get('chart.text.size');
        var context    = this.context;
        var axes       = this.Get('chart.labels.axes').toLowerCase();
        var decimals   = this.Get('chart.scale.decimals');
        var units_pre  = this.Get('chart.units.pre');
        var units_post = this.Get('chart.units.post');

        // Draw any circular labels
        if (typeof(this.Get('chart.labels')) == 'object' && this.Get('chart.labels')) {
            this.DrawCircularLabels(context, this.Get('chart.labels'), font_face, font_size, r + 10);
        }


        var color = 'rgba(255,255,255,0.8)';

        // The "North" axis labels
        if (axes.indexOf('n') > -1) {
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - (r * 0.2), RGraph.number_format(this, Number(this.scale[0]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - (r * 0.4), RGraph.number_format(this, Number(this.scale[1]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - (r * 0.6), RGraph.number_format(this, Number(this.scale[2]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - (r * 0.8), RGraph.number_format(this, Number(this.scale[3]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery - r, RGraph.number_format(this, Number(this.scale[4]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
        }

        // The "South" axis labels
        if (axes.indexOf('s') > -1) {
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + (r * 0.2), RGraph.number_format(this, Number(this.scale[0]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + (r * 0.4), RGraph.number_format(this, Number(this.scale[1]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + (r * 0.6), RGraph.number_format(this, Number(this.scale[2]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + (r * 0.8), RGraph.number_format(this, Number(this.scale[3]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx, this.centery + r, RGraph.number_format(this, Number(this.scale[4]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
        }
        
        // The "East" axis labels
        if (axes.indexOf('e') > -1) {
            RGraph.Text(context, font_face, font_size, this.centerx + (r * 0.2), this.centery, RGraph.number_format(this, Number(this.scale[0]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + (r * 0.4), this.centery, RGraph.number_format(this, Number(this.scale[1]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + (r * 0.6), this.centery, RGraph.number_format(this, Number(this.scale[2]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + (r * 0.8), this.centery, RGraph.number_format(this, Number(this.scale[3]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx + r, this.centery, RGraph.number_format(this, Number(this.scale[4]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
        }

        // The "West" axis labels
        if (axes.indexOf('w') > -1) {
            RGraph.Text(context, font_face, font_size, this.centerx - (r * 0.2), this.centery, RGraph.number_format(this, Number(this.scale[0]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - (r * 0.4), this.centery, RGraph.number_format(this, Number(this.scale[1]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - (r * 0.6), this.centery, RGraph.number_format(this, Number(this.scale[2]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - (r * 0.8), this.centery, RGraph.number_format(this, Number(this.scale[3]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
            RGraph.Text(context, font_face, font_size, this.centerx - r, this.centery, RGraph.number_format(this, Number(this.scale[4]).toFixed(decimals), units_pre, units_post), 'center', 'center', true, false, color);
        }

        if (axes.length > 0) {
            RGraph.Text(context, font_face, font_size, this.centerx,  this.centery, typeof(this.Get('chart.ymin')) == 'number' ? RGraph.number_format(this, Number(this.Get('chart.ymin')).toFixed(this.Get('chart.scale.decimals')), units_pre, units_post) : '0', 'center', 'center', true, false, color);
        }
    }


    /**
    * Draws the circular labels that go around the charts
    * 
    * @param labels array The labels that go around the chart
    */
    RGraph.Rose.prototype.DrawCircularLabels = function (context, labels, font_face, font_size, r)
    {
        var variant = this.Get('chart.variant');
        var position = this.Get('chart.labels.position');
        var r        = r + 10 + this.Get('chart.labels.offset');

        for (var i=0; i<this.angles.length; ++i) {
            
            if (typeof(variant) == 'string' && variant == 'non-equi-angular') {
                var a = Number(this.angles[i][0]) + ((this.angles[i][1] - this.angles[i][0]) / 2);
                var halign = 'center'; // Default halign

                var x = Math.cos(a) * (r + 10);
                var y = Math.sin(a) * (r + 10);
                
                RGraph.Text(context, font_face, font_size, this.centerx + x, this.centery + y, String(labels[i]), 'center', halign);
                
            } else {

                var a = (TWOPI / labels.length) * (i + 1) - (TWOPI / (labels.length * 2));
                var a = a - HALFPI + (this.Get('chart.labels.position') == 'edge' ? ((TWOPI / labels.length) / 2) : 0);
                var halign = 'center'; // Default halign
    
                // Horizontal alignment
                //if (a == 0) {
                //    var halign = 'left';
                //} else if (a == 180) {
                //    var halign = 'right';
                //}
    
                var x = Math.cos(a) * (r + 10);
                var y = Math.sin(a) * (r + 10);
    
                RGraph.Text(context, font_face, font_size, this.centerx + x, this.centery + y, String(labels[i]), 'center', halign);
            }
        }
    }


























    /**
    * This function is for use with circular graph types, eg the Pie or Rose. Pass it your event object
    * and it will pass you back the corresponding segment details as an array:
    * 
    * [x, y, r, startAngle, endAngle]
    * 
    * Angles are measured in degrees, and are measured from the "east" axis (just like the canvas).
    * 
    * @param object e   Your event object
    */
    RGraph.Rose.prototype.getShape =
    RGraph.Rose.prototype.getSegment = function (e)
    {
        RGraph.FixEventObject(e);

        var canvas      = this.canvas;
        var context     = this.context;
        var angles      = this.angles;
        var ret         = [];

        /**
        * Go through all of the angles checking each one
        */
        for (var i=0; i<angles.length ; ++i) {

            var angleStart  = angles[i][0];
            var angleEnd    = angles[i][1];
            var radiusStart = angles[i][2];
            var radiusEnd   = angles[i][3];
            var centerX     = angles[i][4];
            var centerY     = angles[i][5];
            var mouseXY     = RGraph.getMouseXY(e);
            var mouseX      = mouseXY[0] - centerX;
            var mouseY      = mouseXY[1] - centerY;

            var angle = (RGraph.getAngleByXY(0, 0, mouseX, mouseY));
            if (angle > (PI + HALFPI)) {
                angle -= TWOPI;
            }

            if (   (angle >= angleStart && angle <= angleEnd) ) {

                /**
                * Work out the radius
                */
                var radius = (mouseY / Math.sin(angle)) || Math.abs(mouseX);

                if (radius >= radiusStart && radius <= radiusEnd) {
                    angles[i][6] = i;
                    
                    var tooltip = RGraph.parseTooltipText(this.Get('chart.tooltips'), angles[i][6]);

                    // Add the textual keys
                    angles[i]['object']       = this;
                    angles[i]['x']            = angles[i][4];
                    angles[i]['y']            = angles[i][5];
                    angles[i]['angle.start']  = angles[i][0];
                    angles[i]['angle.end']    = angles[i][1];
                    angles[i]['radius.start'] = angles[i][2];
                    angles[i]['radius.end']   = angles[i][3];
                    angles[i]['index']        = angles[i][6];
                    angles[i]['tooltip']      = tooltip;

                    return angles[i];
                }
            
            }
        }

        return null;
    }
























    /**
    * Returns any exploded for a particular segment
    */
    RGraph.Rose.prototype.getexploded = function (index, startAngle, endAngle, exploded)
    {
        var explodedx, explodedy;

        /**
        * Retrieve any exploded - the exploded can be an array of numbers or a single number
        * (which is applied to all segments)
        */
        if (typeof(exploded) == 'object' && typeof(exploded[index]) == 'number') {
            explodedx = Math.cos(((endAngle - startAngle) / 2) + startAngle) * exploded[index];
            explodedy = Math.sin(((endAngle - startAngle) / 2) + startAngle) * exploded[index];
        
        } else if (typeof(exploded) == 'number') {
            explodedx = Math.cos(((endAngle - startAngle) / 2) + startAngle) * exploded;
            explodedy = Math.sin(((endAngle - startAngle) / 2) + startAngle) * exploded;

        } else {
            explodedx = 0;
            explodedy = 0;
        }
        
        return [explodedx, explodedy];
    }



    /**
    * This function facilitates the installation of tooltip event listeners if
    * tooltips are defined.
    */
    RGraph.Rose.prototype.AllowTooltips = function ()
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
    RGraph.Rose.prototype.Highlight = function (shape)
    {
        if (this.Get('chart.tooltips.highlight')) {
            // Add the new segment highlight
            this.context.beginPath();
            
                this.context.strokeStyle = this.Get('chart.highlight.stroke');
                this.context.fillStyle = this.Get('chart.highlight.fill');
            
                this.context.arc(shape['x'], shape['y'], shape['radius.end'], shape['angle.start'], shape['angle.end'], false);
                
                if (shape['radius.start'] > 0) {
                    this.context.arc(shape['x'], shape['y'], shape['radius.start'], shape['angle.end'], shape['angle.start'], true);
                } else {
                    this.context.lineTo(shape['x'], shape['y']);
                }
            this.context.closePath();
    
            this.context.stroke();
            this.context.fill();
        }
    }



    /**
    * The getObjectByXY() worker method. Don't call this call:
    * 
    * RGraph.ObjectRegistry.getObjectByXY(e)
    * 
    * @param object e The event object
    */
    RGraph.Rose.prototype.getObjectByXY = function (e)
    {
        var mouseXY = RGraph.getMouseXY(e);

        // Work out the radius
        var radius = RGraph.getHypLength(this.centerx, this.centery, mouseXY[0], mouseXY[1]);

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
    * This function positions a tooltip when it is displayed
    * 
    * @param obj object    The chart object
    * @param int x         The X coordinate specified for the tooltip
    * @param int y         The Y coordinate specified for the tooltip
    * @param objec tooltip The tooltips DIV element
    */
    RGraph.Rose.prototype.positionTooltip = function (obj, x, y, tooltip, idx)
    {

        var coordX      = obj.angles[idx][4];
        var coordY      = obj.angles[idx][5];
        var angleStart  = obj.angles[idx][0];
        var angleEnd    = obj.angles[idx][1];
        var radius      = ((obj.angles[idx][3] - obj.angles[idx][2]) / 2) + obj.angles[idx][2];

        var angleCenter = ((angleEnd - angleStart) / 2) + angleStart;
        var canvasXY    = RGraph.getCanvasXY(obj.canvas);
        var gutterLeft  = obj.Get('chart.gutter.left');
        var gutterTop   = obj.Get('chart.gutter.top');
        var width       = tooltip.offsetWidth;
        var height      = tooltip.offsetHeight;

        
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
        if ((canvasXY[0] + coordX + (Math.cos(angleCenter) * radius) - (width / 2)) < 10) {
            tooltip.style.left = (canvasXY[0] + coordX + (Math.cos(angleCenter) * radius)- (width * 0.1)) + 'px';
            tooltip.style.top = (canvasXY[1] + coordY + (Math.sin(angleCenter) * radius)- height - 5) + 'px';
            img.style.left = ((width * 0.1) - 8.5) + 'px';

        // RIGHT edge
        } else if ((canvasXY[0] + coordX + (Math.cos(angleCenter) * radius) + (width / 2)) > (document.body.offsetWidth - 10) ) {
            tooltip.style.left = (canvasXY[0] + coordX + (Math.cos(angleCenter) * radius) - (width * 0.9)) + 'px';
            tooltip.style.top = (canvasXY[1] + coordY + (Math.sin(angleCenter) * radius)- height - 5) + 'px';
            img.style.left = ((width * 0.9) - 8.5) + 'px';

        // Default positioning - CENTERED
        } else {
            tooltip.style.left = (canvasXY[0] + coordX + (Math.cos(angleCenter) * radius)- (width / 2)) + 'px';
            tooltip.style.top = (canvasXY[1] + coordY + (Math.sin(angleCenter) * radius)- height - 5) + 'px';
            img.style.left = ((width * 0.5) - 8.5) + 'px';
        }
    }