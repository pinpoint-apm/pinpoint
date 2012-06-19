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
    * This is used in two functions, hence it's here
    */
    RGraph.Highlight          = {};
    RGraph.tooltips           = {};
    RGraph.tooltips.padding   = '3px';
    RGraph.tooltips.font_face = 'Tahoma';
    RGraph.tooltips.font_size = '10pt';


    /**
    * Shows a tooltip next to the mouse pointer
    * 
    * @param canvas object The canvas element object
    * @param text   string The tooltip text
    * @param int     x      The X position that the tooltip should appear at. Combined with the canvases offsetLeft
    *                       gives the absolute X position
    * @param int     y      The Y position the tooltip should appear at. Combined with the canvases offsetTop
    *                       gives the absolute Y position
    * @param int     idx    The index of the tooltip in the graph objects tooltip array
    * @param object  e      The event object
    */
    RGraph.Tooltip = function (obj, text, x, y, idx, e)
    {
        /**
        * chart.tooltip.override allows you to totally take control of rendering the tooltip yourself
        */
        if (typeof(obj.Get('chart.tooltips.override')) == 'function') {
            return obj.Get('chart.tooltips.override')(obj, text, x, y, idx);
        }

        /**
        * Save the X/Y coords
        */
        var originalX = x;
        var originalY = y;

        /**
        * This facilitates the "id:xxx" format
        */
        text = RGraph.getTooltipTextFromDIV(text);

        /**
        * First clear any exising timers
        */
        var timers = RGraph.Registry.Get('chart.tooltip.timers');

        if (timers && timers.length) {
            for (i=0; i<timers.length; ++i) {
                clearTimeout(timers[i]);
            }
        }
        RGraph.Registry.Set('chart.tooltip.timers', []);

        /**
        * Hide the context menu if it's currently shown
        */
        if (obj.Get('chart.contextmenu')) {
            RGraph.HideContext();
        }

        var effect = obj.Get('chart.tooltips.effect').toLowerCase();





        /**
        * Show a tool tip
        */
        var tooltipObj  = document.createElement('DIV');
        tooltipObj.className             = obj.Get('chart.tooltips.css.class');
        tooltipObj.style.display         = 'none';
        tooltipObj.style.position        = 'absolute';
        tooltipObj.style.left            = 0;
        tooltipObj.style.top             = 0;
        tooltipObj.style.backgroundColor = 'rgb(255,255,239)';
        tooltipObj.style.color           = 'black';
        if (!document.all) tooltipObj.style.border = '';
        tooltipObj.style.visibility      = 'visible';
        tooltipObj.style.paddingLeft     = RGraph.tooltips.padding;
        tooltipObj.style.paddingRight    = RGraph.tooltips.padding;
        tooltipObj.style.fontFamily      = RGraph.tooltips.font_face;
        tooltipObj.style.fontSize        = RGraph.tooltips.font_size;
        tooltipObj.style.zIndex          = 3;

        // Only apply a border if there's content
        if (RGraph.trim(text).length > 0) {
            tooltipObj.style.border             = '1px #bbb solid';
        }

        tooltipObj.style.borderRadius       = '5px';
        tooltipObj.style.MozBorderRadius    = '5px';
        tooltipObj.style.WebkitBorderRadius = '5px';
        tooltipObj.style.WebkitBoxShadow    = 'rgba(96,96,96,0.5) 0 0 15px';
        tooltipObj.style.MozBoxShadow       = 'rgba(96,96,96,0.5) 0 0 15px';
        tooltipObj.style.boxShadow          = 'rgba(96,96,96,0.5) 0 0 15px';
        tooltipObj.style.filter             = 'progid:DXImageTransform.Microsoft.Shadow(color=#666666,direction=135)';
        tooltipObj.style.opacity            = 0;
        //tooltipObj.style.overflow           = 'hidden';
        tooltipObj.innerHTML                = text;
        tooltipObj.__text__                 = text; // This is set because the innerHTML can change when it's set
        tooltipObj.__canvas__               = obj.canvas;
        tooltipObj.style.display            = 'inline';
        tooltipObj.id                       = '__rgraph_tooltip_' + obj.canvas.id + '_' + obj.uid + '_'+ idx;
        tooltipObj.__event__                = obj.Get('chart.tooltips.event') || 'click';
        tooltipObj.__object__               = obj;
        
        if (typeof(idx) == 'number') {
            tooltipObj.__index__ = idx;
            origIdx = idx;
        }
        
        if (obj.type == 'line' || obj.type == 'radar') {
            for (var ds=0; ds<obj.data.length; ++ds) {
                if (idx >= obj.data[ds].length) {
                    idx -= obj.data[ds].length;
                } else {
                    break;
                }
            }
            
            tooltipObj.__dataset__ = ds;
            tooltipObj.__index2__  = idx;
        }

        document.body.appendChild(tooltipObj);

        var width  = tooltipObj.offsetWidth;
        var height = tooltipObj.offsetHeight;

        //if ((y - height - 2) > 0) {
        //    y = y - height - 2;
        //} else {
        //    y = y + 2;
        //}
        /**
        * Set the width on the tooltip so it doesn't resize if the window is resized
        */
        tooltipObj.style.width = width + 'px';
        //tooltipObj.style.height = 0; // Initially set the tooltip height to nothing

        /**
        * If the mouse is towards the right of the browser window and the tooltip would go outside of the window,
        * move it left
        *
        if ( (x + width) > document.body.offsetWidth ) {
            x = x - width - 7;
            var placementLeft = true;
            
            if (obj.Get('chart.tooltips.effect') == 'none') {
                x = x - 3;
            }

            tooltipObj.style.left = x + 'px';
            tooltipObj.style.top  = y + 'px';

        } else {
            x += 5;

            tooltipObj.style.left = x + 'px';
            tooltipObj.style.top = y + 'px';
        }*/

        tooltipObj.style.top  = (y - height - 2) + 'px';


        /**
        * If the function exists call the object specific tooltip positioning function
        */
        if (typeof(obj.positionTooltip) == 'function') {
            if (tooltipObj.innerHTML.length > 0) {

                obj.positionTooltip(obj, x, y, tooltipObj, origIdx ? origIdx : idx);

                if (obj.Get('chart.tooltips.coords.page')) {

                    tooltipObj.style.left = e.pageX - (width / 2) - 4.25 + 'px';
                    tooltipObj.style.top = e.pageY - height - 7 + 'px';
                    
                    document.getElementById('__rgraph_tooltip_pointer__').style.left = (parseInt(tooltipObj.offsetWidth) / 2)  - 8.5 + 'px';
                }
            }
        }


        if (effect == 'fade' || effect == 'expand' || effect == 'contract' || effect == 'snap') {
            setTimeout(function () {tooltipObj.style.opacity = 0.1;}, 25);
            setTimeout(function () {tooltipObj.style.opacity = 0.2;}, 50);
            setTimeout(function () {tooltipObj.style.opacity = 0.3;}, 75);
            setTimeout(function () {tooltipObj.style.opacity = 0.4;}, 100);
            setTimeout(function () {tooltipObj.style.opacity = 0.5;}, 125);
            setTimeout(function () {tooltipObj.style.opacity = 0.6;}, 150);
            setTimeout(function () {tooltipObj.style.opacity = 0.7;}, 175);
            setTimeout(function () {tooltipObj.style.opacity = 0.8;}, 200);
            setTimeout(function () {tooltipObj.style.opacity = 0.9;}, 225);
            
            if (effect == 'expand' || effect == 'contract' || effect == 'snap') {
                console.log('[RGRAPH] The snap, expand and contract tooltip effects are deprecated. Available effects now are fade and none');
            }
        }

        setTimeout("if (RGraph.Registry.Get('chart.tooltip')) { RGraph.Registry.Get('chart.tooltip').style.opacity = 1;}", effect == 'none' ? 50 : 250);

        /**
        * If the tooltip it self is clicked, cancel it
        */
        tooltipObj.onmousedown = function (e){e.stopPropagation();}
        tooltipObj.onmouseup   = function (e){e.stopPropagation();}
        tooltipObj.onclick     = function (e){if (e.button == 0) {e.stopPropagation();}}







        /**
        * Keep a reference to the tooltip in the registry
        */
        RGraph.Registry.Set('chart.tooltip', tooltipObj);

        /**
        * Fire the tooltip event
        */
        RGraph.FireCustomEvent(obj, 'ontooltip');
    }
    
    
    /**
    * 
    */
    RGraph.getTooltipTextFromDIV = function (text)
    {
        // This regex is duplicated firher down on roughly line 888
        var result = /^id:(.*)/.exec(text);

        if (result && result[1] && document.getElementById(result[1])) {
            text = document.getElementById(result[1]).innerHTML;
        } else if (result && result[1]) {
            text = '';
        }
        
        return text;
    }


    /**
    * 
    */
    RGraph.getTooltipWidth = function (text, obj)
    {
        var div = document.createElement('DIV');
            div.className             = obj.Get('chart.tooltips.css.class');
            div.style.paddingLeft     = RGraph.tooltips.padding;
            div.style.paddingRight    = RGraph.tooltips.padding;
            div.style.fontFamily      = RGraph.tooltips.font_face;
            div.style.fontSize        = RGraph.tooltips.font_size;
            div.style.visibility      = 'hidden';
            div.style.position        = 'absolute';
            div.style.top            = '300px';
            div.style.left             = 0;
            div.style.display         = 'inline';
            div.innerHTML             = RGraph.getTooltipTextFromDIV(text);
        document.body.appendChild(div);

        return div.offsetWidth;
    }


    /**
    * Hides the currently shown tooltip
    */
    RGraph.HideTooltip = function ()
    {
        var tooltip = RGraph.Registry.Get('chart.tooltip');

        if (tooltip) {
            tooltip.parentNode.removeChild(tooltip);
            tooltip.style.display = 'none';                
            tooltip.style.visibility = 'hidden';
            RGraph.Registry.Set('chart.tooltip', null);
        }
    }

    
    
    /**
    * This installs the window mousedown event listener. It clears any highlight that may
    * be present.
    * 
    * @param object obj The chart object
    *
    RGraph.InstallWindowMousedownTooltipListener = function (obj)
    {
        if (RGraph.Registry.Get('__rgraph_event_listeners__')['window_mousedown']) {
            return;
        }
        
        // When the canvas is cleared, reset this flag so that the event listener is installed again
        RGraph.AddCustomEventListener(obj, 'onclear', function (obj) {RGraph.Registry.Get('__rgraph_event_listeners__')['window_mousedown'] = false;})

        // NOTE: Global on purpose
        rgraph_window_mousedown = function (e)
        {
            if (RGraph.Registry.Get('chart.tooltip')) {

                var obj    = RGraph.Registry.Get('chart.tooltip').__object__;
                var canvas = obj.canvas;

                /**
                * Get rid of the tooltip and redraw all canvases on the page
                *
                RGraph.HideTooltip();
                
                /**
                * No need to clear if highlighting is disabled
                * 
                * TODO Really, need to check ALL of the pertinent objects that
                * are drawing on the canvas using the ObjectRegistry -
                * ie RGraph.ObjectRegistry.getObjectsByCanvasID()
                *
                if (obj.Get('chart.tooltips.highlight')) {
                    RGraph.RedrawCanvas(canvas);
                }
            }
        }
        window.addEventListener('mousedown', rgraph_window_mousedown, false);
        RGraph.AddEventListener('window_' + obj.id, 'mousedown', rgraph_window_mousedown);
    }
    */


    /**
    * This installs the canvas mouseup event listener. This is the function that
    * actually shows the appropriate (if any) tooltip.
    * 
    * @param object obj The chart object
    *
    RGraph.InstallCanvasMouseupTooltipListener = function (obj)
    {
        if (RGraph.Registry.Get('__rgraph_event_listeners__')[obj.canvas.id + '_mouseup']) {
            return;
        }
        RGraph.Registry.Get('__rgraph_event_listeners__')[obj.canvas.id + '_mouseup'] = true;

        // When the canvas is cleared, reset this flag so that the event listener is installed again
        RGraph.AddCustomEventListener(obj, 'onclear', function (obj) {RGraph.Registry.Get('__rgraph_event_listeners__')[obj.canvas.id + '_mouseup'] = false});

        // Install the onclick event handler for the tooltips
        //
        // // NOTE: Global on purpose
        rgraph_canvas_mouseup_func = function (e)
        {
            var x = arguments[1] ? arguments[1] : e.pageX;
            var y = arguments[2] ? arguments[2] : e.pageY;

            var objects = RGraph.ObjectRegistry.getObjectsByCanvasID(e.target.id);

            // It's important to go backwards through the array so that the front charts
            // are checked first, then the charts at the back
            for (var i=(objects.length - 1); i>=0; --i) {
                
                var shape = objects[i].getShape(e);

                if (shape && shape['object'] && !RGraph.Registry.Get('chart.tooltip')) {

                    /**
                    * This allows the Scatter chart funky tooltips style
                    *
                    if (objects[i].type == 'scatter' && shape['dataset'] > 0) {
                        for (var j=0; j<(objects[i].data.length - 1); ++j) {
                            shape['index'] += objects[i].data[j].length;
                        }
                    }

                    var text = RGraph.parseTooltipText(objects[i].Get('chart.tooltips'), shape['index']);
    
                    if (text) {
                    
                        if (shape['object'].Get('chart.tooltips.hotspot.xonly')) {
                            var canvasXY = RGraph.getCanvasXY(objects[i].canvas);
                            x = canvasXY[0] + shape[1];
                            y = canvasXY[1] + shape[2];
                        }

                        RGraph.Tooltip(objects[i], text, x, y, shape['index']);
                        objects[i].Highlight(shape);
    
                        e.stopPropagation();
                        e.cancelBubble = true;
                        return false;
                    }
                }
            }
        }
        obj.canvas.addEventListener('mouseup', rgraph_canvas_mouseup_func, false);
        RGraph.AddEventListener(obj.id, 'mouseup', rgraph_canvas_mouseup_func);
    }
    */



    /**
    * This installs the canvas mousemove event listener. This is the function that
    * changes the mouse pointer if need be.
    * 
    * @param object obj The chart object
    *
    RGraph.InstallCanvasMousemoveTooltipListener = function (obj)
    {
        if (RGraph.Registry.Get('__rgraph_event_listeners__')[obj.canvas.id + '_mousemove']) {
            return;
        }
        RGraph.Registry.Get('__rgraph_event_listeners__')[obj.canvas.id + '_mousemove'] = true;
        
        // When the canvas is cleared, reset this flag so that the event listener is installed again
        RGraph.AddCustomEventListener(obj, 'onclear', function (obj) {RGraph.Registry.Get('__rgraph_event_listeners__')[obj.canvas.id + '_mousemove'] = false})

        // Install the mousemove event handler for the tooltips
        //
        // NOTE: Global on purpose
        rgraph_canvas_mousemove_func = function (e)
        {
            var objects = RGraph.ObjectRegistry.getObjectsByCanvasID(e.target.id);

            for (var i=0; i<objects.length; ++i) {

                var shape = objects[i].getShape(e);

                if (shape && shape['object']) {

                    /**
                    * This allows the Scatter chart funky tooltips style
                    *
                    if (objects[i].type == 'scatter' && shape['dataset'] > 0) {
                        for (var j=0; j<(objects[i].data.length - 1); ++j) {
                            shape['index'] += objects[i].data[j].length;
                        }
                    }

                    var text = RGraph.parseTooltipText(objects[i].Get('chart.tooltips'), shape['index']);


                    if (text) {

                        e.target.style.cursor = 'pointer';

                        /**
                        * This facilitates the event triggering the tooltips being mousemove
                        *

                        if (   typeof(objects[i].Get('chart.tooltips.event')) == 'string'
                            && objects[i].Get('chart.tooltips.event') == 'onmousemove'
                            && (!RGraph.Registry.Get('chart.tooltip') || shape['index'] != RGraph.Registry.Get('chart.tooltip').__index__ || shape['object'].uid != RGraph.Registry.Get('chart.tooltip').__object__.uid)
                           ) {
                           
                           // Hide any current tooltip
                           rgraph_window_mousedown(e);
                           
                           rgraph_canvas_mouseup_func(e);
                        }
                    }
                }
            }
        }
        obj.canvas.addEventListener('mousemove', rgraph_canvas_mousemove_func, false);
        RGraph.AddEventListener(obj.id, 'mousemove', rgraph_canvas_mousemove_func);
    }
    */



    /**
    * This function highlights a rectangle
    * 
    * @param object obj    The chart object
    * @param number shape  The coordinates of the rect to highlight
    */
    RGraph.Highlight.Rect = function (obj, shape)
    {
        if (obj.Get('chart.tooltips.highlight')) {
            var canvas  = obj.canvas;
            var context = obj.context;
    
            /**
            * Draw a rectangle on the canvas to highlight the appropriate area
            */
            context.beginPath();

                context.strokeStyle = obj.Get('chart.highlight.stroke');
                context.fillStyle   = obj.Get('chart.highlight.fill');
    
                context.strokeRect(shape['x'],shape['y'],shape['width'],shape['height']);
                context.fillRect(shape['x'],shape['y'],shape['width'],shape['height']);
            context.stroke;
            context.fill();
        }
    }



    /**
    * This function highlights a point
    * 
    * @param object obj    The chart object
    * @param number shape  The coordinates of the rect to highlight
    */
    RGraph.Highlight.Point = function (obj, shape)
    {
        if (obj.Get('chart.tooltips.highlight')) {
            var canvas  = obj.canvas;
            var context = obj.context;
    
            /**
            * Draw a rectangle on the canvas to highlight the appropriate area
            */
            context.beginPath();
                context.strokeStyle = obj.Get('chart.highlight.stroke');
                context.fillStyle   = obj.Get('chart.highlight.fill');
                var radius   = obj.Get('chart.highlight.point.radius') || 2;
                context.arc(shape['x'],shape['y'],radius, 0, TWOPI, 0);
            context.stroke();
            context.fill();
        }
    }



    /**
    * This (as the name suggests preloads any images it can find in the tooltip text
    * 
    * @param object obj The chart object
    */
    RGraph.PreLoadTooltipImages = function (obj)
    {
        var tooltips = obj.Get('chart.tooltips');
        
        if (RGraph.hasTooltips(obj)) {
        
            if (obj.type == 'rscatter') {
                tooltips = [];
                for (var i=0; i<obj.data.length; ++i) {
                    tooltips.push(obj.data[3]);
                }
            }
            
            for (var i=0; i<tooltips.length; ++i) {
                // Add the text to an offscreen DIV tag
                var div = document.createElement('DIV');
                    div.style.position = 'absolute';
                    div.style.opacity = 0;
                    div.style.top = '-100px';
                    div.style.left = '-100px';
                    div.innerHTML  = tooltips[i];
                document.body.appendChild(div);
                
                // Now get the IMG tags and create them
                var img_tags = div.getElementsByTagName('IMG');
    
                // Create the image in an off-screen image tag
                for (var j=0; j<img_tags.length; ++j) {
                        if (img_tags && img_tags[i]) {
                        var img = document.createElement('IMG');
                            img.style.position = 'absolute';
                            img.style.opacity = 0;
                            img.style.top = '-100px';
                            img.style.left = '-100px';
                            img.src = img_tags[i].src
                        document.body.appendChild(img);
                        
                        setTimeout(function () {document.body.removeChild(img);}, 250);
                    }
                }
    
                // Now remove the div
                document.body.removeChild(div);
            }
        }
    }



    /**
    * This is the tooltips canvas onmousemove listener
    */
    RGraph.Tooltips_mousemove  = function (obj, e)
    {
        var shape = obj.getShape(e);
        var changeCursor_tooltips = false

        if (   shape
            && typeof(shape['index']) == 'number'
            && obj.Get('chart.tooltips')[shape['index']]
           ) {

            var text = RGraph.parseTooltipText(obj.Get('chart.tooltips'), shape['index']);

            if (text) {

                /**
                * Change the cursor
                */
                changeCursor_tooltips = true;

                if (obj.Get('chart.tooltips.event') == 'onmousemove') {

                    // Show the tooltip if it's not the same as the one already visible
                    if (
                           !RGraph.Registry.Get('chart.tooltip')
                        || RGraph.Registry.Get('chart.tooltip').__object__.uid != obj.uid
                        || RGraph.Registry.Get('chart.tooltip').__index__ != shape['index']
                       ) {

                        RGraph.HideTooltip();
                        RGraph.Clear(obj.canvas);
                        RGraph.Redraw();
                        RGraph.Tooltip(obj, text, e.pageX, e.pageY, shape['index']);
                        obj.Highlight(shape);
                    }
                }
            }
        
        /**
        * More highlighting
        */
        } else if (shape && typeof(shape['index']) == 'number') {

            var text = RGraph.parseTooltipText(obj.Get('chart.tooltips'), shape['index']);

            if (text) {
                changeCursor_tooltips = true
            }
        }

        return changeCursor_tooltips;
    }