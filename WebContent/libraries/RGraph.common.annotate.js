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

    if (typeof(RGraph) == 'undefined') RGraph = {isRGraph:true,type:'common'};


    /**
    * This installs some event handlers
    * 
    * Checking the RGraph.Annotate flag means the annotate code only runs once
    */
    RGraph.Annotating_canvas_onmousedown = function (e)
    {
        if (e.button == 0) {

            e.target.__object__.Set('chart.mousedown', true);

            // Get the object from the canvas. Annotating must be enabled on the
            // last object defined
            var obj = e.target.__object__;
            
            // This staarts the annotating "path" and set the color
            obj.context.beginPath();

                obj.context.strokeStyle = obj.Get('chart.annotate.color');
                obj.context.lineWidth = 1;
            
                var mouseXY = RGraph.getMouseXY(e);
                var mouseX  = mouseXY[0];
                var mouseY  = mouseXY[1];
            
                // Clear the annotation recording
                RGraph.Registry.Set('annotate.actions', [obj.Get('chart.annotate.color')]);
    
                // This sets the initial X/Y position
                obj.context.moveTo(mouseX, mouseY);
                
                RGraph.Registry.Set('started.annotating', false);
                RGraph.Registry.Set('chart.annotating', obj);

                // Fire the onannotatebegin event.
                RGraph.FireCustomEvent(obj, 'onannotatebegin');
        }
        
        return false;
    }


    /**
    * This cancels annotating for ALL canvases
    */
    RGraph.Annotating_window_onmouseup = function (e)
    {
        var obj  = RGraph.Registry.Get('chart.annotating');

        if (e.button != 0 || !obj) {
            return;
        }
        
        // This cancels annotating on ALL canvas tags on the page
        var tags = document.getElementsByTagName('canvas');

        for (var i=0; i<tags.length; ++i) {
            if (tags[i].__object__) {
                tags[i].__object__.Set('chart.mousedown', false);
            }
        }

        // Store the annotations in browser storage if it's available
        if (RGraph.Registry.Get('annotate.actions') && RGraph.Registry.Get('annotate.actions').length > 0 && window.localStorage) {

            var id = '__rgraph_annotations_' + e.target.id + '__';
            var annotations  = window.localStorage[id] ? window.localStorage[id] + '|' : '';
                annotations += RGraph.Registry.Get('annotate.actions');

            // Store the annotations information in HTML5 browser storage here
            window.localStorage[id] = annotations;
        }
        
        // Clear the recorded annotations
        RGraph.Registry.Set('annotate.actions', []);
        
        // Fire the annotate event
        RGraph.FireCustomEvent(obj, 'onannotateend');
    }


    /**
    * The canvas onmousemove function
    */
    RGraph.Annotating_canvas_onmousemove = function (e)
    {
        var e       = RGraph.FixEventObject(e);
        var obj     = e.target.__object__;
        var mouseXY = RGraph.getMouseXY(e);
        var mouseX  = mouseXY[0];
        var mouseY  = mouseXY[1];
        
        if (obj.Get('chart.mousedown')) {
            obj.context.lineTo(mouseX, mouseY);
            RGraph.Registry.Set('annotate.actions', RGraph.Registry.Get('annotate.actions') + '|' + mouseX + ',' + mouseY);
            RGraph.FireCustomEvent(obj, 'onannotate');
            obj.context.stroke();
        }
    }


    /**
    * Shows the mini palette used for annotations
    * 
    * @param object e The event object
    */
    RGraph.Showpalette = function (e)
    {
        var isSafari = navigator.userAgent.indexOf('Safari') ? true : false;

        e = RGraph.FixEventObject(e);

        var canvas  = e.target.parentNode.__canvas__;
        var context = canvas.getContext('2d');
        var obj     = canvas.__object__;
        var div     = document.createElement('DIV');
        var coords  = RGraph.getMouseXY(e);
        
        div.__object__               = obj; // The graph object
        div.className                = 'RGraph_palette';
        div.style.position           = 'absolute';
        div.style.backgroundColor    = 'white';
        div.style.border             = '1px solid black';
        div.style.left               = 0;
        div.style.top                = 0;
        div.style.padding            = '3px';
        div.style.paddingBottom      = 0;
        div.style.paddingRight       = 0;
        div.style.opacity            = 0;
        div.style.boxShadow          = 'rgba(96,96,96,0.5) 3px 3px 3px';
        div.style.WebkitBoxShadow    = 'rgba(96,96,96,0.5) 3px 3px 3px';
        div.style.MozBoxShadow       = 'rgba(96,96,96,0.5) 3px 3px 3px';
        div.style.filter             = 'progid:DXImageTransform.Microsoft.Shadow(color=#666666,direction=135)';
        
        var common_css       = 'padding: 1px; display: inline; display: inline-block; width: 15px; height: 15px; margin-right: 3px; cursor: pointer;' + (isSafari ? 'margin-bottom: 3px' : '');
        var common_mouseover = ' onmouseover="this.style.border = \'1px black solid\'; this.style.padding = 0"';
        var common_mouseout  = ' onmouseout="this.style.border = 0; this.style.padding = \'1px\'" ';

        var str = '';

        var colors = ['red', 'blue', 'green', 'black', 'yellow', 'magenta', 'pink', 'cyan', 'purple', '#ddf', 'gray', '#36905c'];

        for (i=0; i<colors.length; ++i) {
            str = str + '<span ' + common_mouseover + common_mouseout + ' style="background-color: ' + colors[i] + '; ' + common_css  + '" onclick="this.parentNode.__object__.Set(\'chart.annotate.color\', this.style.backgroundColor); this.parentNode.style.display = \'none\'; RGraph.FireCustomEvent(this.parentNode.__object__, \'onannotatecolor\')">&nbsp;</span>';
            
            // This makes the colours go across two levels
            if (i == 5) {
                str += '<br />';
            }
        }

        div.innerHTML = str;
        document.body.appendChild(div);
        
        /**
        * Now the div has been added to the document, move it up and left and set the width and height
        */
        div.style.width  = (div.offsetWidth) + 'px';
        div.style.height = (div.offsetHeight - (RGraph.isIE9up() ? 5 : 5)) + 'px';
        div.style.left   = Math.max(0, e.pageX - div.offsetWidth - 2) + 'px';
        div.style.top    = (e.pageY - div.offsetHeight - 2) + 'px';

        /**
        * Store the palette div in the registry
        */
        RGraph.Registry.Set('palette', div);
        
        setTimeout("RGraph.Registry.Get('palette').style.opacity = 0.2", 50);
        setTimeout("RGraph.Registry.Get('palette').style.opacity = 0.4", 100);
        setTimeout("RGraph.Registry.Get('palette').style.opacity = 0.6", 150);
        setTimeout("RGraph.Registry.Get('palette').style.opacity = 0.8", 200);
        setTimeout("RGraph.Registry.Get('palette').style.opacity = 1", 250);

        RGraph.HideContext();

        window.onclick = function ()
        {
            RGraph.HidePalette();
        }

        // Should this be here? Yes. This function is being used as an event handler.
        e.stopPropagation();
        return false;
    }
    
    
    /**
    * Clears any annotation data from global storage
    * 
    * @param object canvas The canvas tag object
    */
    RGraph.ClearAnnotations = function (canvas)
    {
        /**
        * For BC the argument can also be the ID of the canvas
        */
        if (typeof(canvas) == 'string') {
            var id = canvas;
            canvas = document.getElementById(id);
        } else {
            var id = canvas.id
        }

        var obj = canvas.__object__;

        if (window.localStorage && window.localStorage['__rgraph_annotations_' + id + '__'] && window.localStorage['__rgraph_annotations_' + id + '__'].length) {
            window.localStorage['__rgraph_annotations_' + id + '__'] = [];
            
            RGraph.FireCustomEvent(obj, 'onannotateclear');
        }
    }


    /**
    * Replays stored annotations
    * 
    * @param object obj The graph object
    */
    RGraph.ReplayAnnotations = function (obj)
    {
        // Check for support
        if (!window.localStorage) {
            return;
        }

        var context     = obj.context;
        var annotations = window.localStorage['__rgraph_annotations_' + obj.id + '__'];
        var i, len, move, coords;

        context.beginPath();
        context.lineWidth = 2;

        if (annotations && annotations.length) {
            annotations = annotations.split('|');
        } else {
            return;
        }

        for (i=0, len=annotations.length; i<len; ++i) {
            if (!annotations[i].match(/^[0-9]+,[0-9]+$/)) {
                context.stroke();
                context.beginPath();
                context.strokeStyle = annotations[i];
                move = true;
                continue;
            }
            
            coords = annotations[i].split(',');

            if (move) {
                context.moveTo(coords[0], coords[1]);
                move = false;
            } else {
                context.lineTo(coords[0], coords[1]);
            }
        }
        
        context.stroke();
    }

    window.addEventListener('load', function (e)
    {
        // This delay is necessary to allow the window.onload event listener to run
        setTimeout(function ()
        {
            var tags = document.getElementsByTagName('canvas');
            for (var i=0; i<tags.length; ++i) {
                if (tags[i].__object__ && tags[i].__object__.isRGraph && tags[i].__object__.Get('chart.annotatable')) {
                    RGraph.ReplayAnnotations(tags[i].__object__);
                }
            }
        }, 100); // This delay is sufficient to wait before replaying the annotations
    }, false);