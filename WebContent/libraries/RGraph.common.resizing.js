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
    * This is an array of CSS properties that should be preserved when adding theplaceholder DIV
    */
    __rgraph_resizing_preserve_css_properties__ = [];

    /**
    * This function can be used to allow resizing
    * 
    * @param object obj Your graph object
    */
    RGraph.AllowResizing = function (obj)
    {
        if (obj.Get('chart.resizable')) {
            var canvas  = obj.canvas;
            var context = obj.context;
            var resizeHandle = 15;
            RGraph.Resizing.canvas = canvas;
            RGraph.Resizing.placeHolders = [];
            
            /**
            * Add the original width and height to the canvas
            */
            if (!canvas.__original_width__ && !canvas.__original_height__) {
                canvas.__original_width__  = canvas.width;
                canvas.__original_height__ = canvas.height;
            }


            var adjustX = (typeof(obj.Get('chart.resize.handle.adjust')) == 'object' && typeof(obj.Get('chart.resize.handle.adjust')[0]) == 'number' ? obj.Get('chart.resize.handle.adjust')[0] : 0);
            var adjustY = (typeof(obj.Get('chart.resize.handle.adjust')) == 'object' && typeof(obj.Get('chart.resize.handle.adjust')[1]) == 'number' ? obj.Get('chart.resize.handle.adjust')[1] : 0);


            /**
            * Draw the resize handle
            */
            var textWidth = context.measureText('Reset').width + 2;


            // Draw the white background for the resize handle - OPTIONAL default is rgba(0,0,0,0);
            var bgcolor = obj.Get('chart.resize.handle.background');
            
            if (!bgcolor) {
                bgcolor = 'rgba(0,0,0,0)';
            }

            context.beginPath();
                context.fillStyle = bgcolor;
                context.moveTo(canvas.width - resizeHandle - resizeHandle + adjustX, canvas.height - resizeHandle);
                context.fillRect(canvas.width - resizeHandle - resizeHandle + adjustX, canvas.height - resizeHandle + adjustY, 2 * resizeHandle, resizeHandle);
            context.fill();


            obj.context.beginPath();
                obj.context.strokeStyle = 'gray';
                obj.context.fillStyle = 'rgba(0,0,0,0)';
                obj.context.lineWidth = 1;
                obj.context.fillRect(obj.canvas.width - resizeHandle + adjustX, obj.canvas.height - resizeHandle - 2 + adjustY, resizeHandle, resizeHandle + 2);
                obj.context.fillRect(obj.canvas.width - resizeHandle - textWidth + adjustX, obj.canvas.height - resizeHandle + adjustY, resizeHandle + textWidth, resizeHandle + 2);


                // Draw the arrows
                
                    // Vertical line
                    obj.context.moveTo(AA(this, obj.canvas.width - (resizeHandle / 2) + adjustX), obj.canvas.height - resizeHandle + adjustY);
                    obj.context.lineTo(AA(this, obj.canvas.width - (resizeHandle / 2) + adjustX), obj.canvas.height + adjustY);


                    // Horizontal line
                    obj.context.moveTo(obj.canvas.width + adjustX, AA(this, obj.canvas.height - (resizeHandle / 2) + adjustY));
                    obj.context.lineTo(obj.canvas.width - resizeHandle + adjustX, AA(this, obj.canvas.height - (resizeHandle / 2) + adjustY));
                
            context.fill();
            context.stroke();


            // Top arrow head
            context.fillStyle = 'gray';
            context.beginPath();
                context.moveTo(canvas.width - (resizeHandle / 2) + adjustX, canvas.height - resizeHandle + adjustY);
                context.lineTo(canvas.width - (resizeHandle / 2) + 3 + adjustX, canvas.height - resizeHandle + 3 + adjustY);
                context.lineTo(canvas.width - (resizeHandle / 2) - 3 + adjustX, canvas.height - resizeHandle + 3 + adjustY);
            context.closePath();
            context.fill();

            // Bottom arrow head
            context.beginPath();
                context.moveTo(canvas.width - (resizeHandle / 2) + adjustX, canvas.height + adjustY);
                context.lineTo(canvas.width - (resizeHandle / 2) + 3 + adjustX, canvas.height - 3 + adjustY);
                context.lineTo(canvas.width - (resizeHandle / 2) - 3 + adjustX, canvas.height - 3 + adjustY);
            context.closePath();
            context.fill();

            // Left arrow head
            context.beginPath();
                context.moveTo(canvas.width - resizeHandle + adjustX, canvas.height - (resizeHandle / 2) + adjustY);
                context.lineTo(canvas.width - resizeHandle + 3 + adjustX, canvas.height - (resizeHandle / 2) + 3 + adjustY);
                context.lineTo(canvas.width - resizeHandle + 3 + adjustX, canvas.height - (resizeHandle / 2) - 3 + adjustY);
            context.closePath();
            context.fill();

            // Right arrow head
            context.beginPath();
                context.moveTo(canvas.width + adjustX, canvas.height - (resizeHandle / 2) + adjustY);
                context.lineTo(canvas.width - 3 + adjustX, canvas.height - (resizeHandle / 2) + 3 + adjustY);
                context.lineTo(canvas.width  - 3 + adjustX, canvas.height - (resizeHandle / 2) - 3 + adjustY);
            context.closePath();
            context.fill();
            
            // Square at the centre of the arrows
            context.beginPath();
                context.fillStyle = 'white';
                context.moveTo(canvas.width + adjustX, canvas.height - (resizeHandle / 2) + adjustY);
                context.strokeRect(canvas.width - (resizeHandle / 2) - 2 + adjustX, canvas.height - (resizeHandle / 2) - 2 + adjustY, 4, 4);
                context.fillRect(canvas.width - (resizeHandle / 2) - 2 + adjustX, canvas.height - (resizeHandle / 2) - 2 + adjustY, 4, 4);
            context.stroke();
            context.fill();


            // Draw the "Reset" button
            context.beginPath();
                context.fillStyle = 'gray';
                context.moveTo(AA(this, canvas.width - resizeHandle - 3 + adjustX), canvas.height - resizeHandle / 2 + adjustY);
                context.lineTo(AA(this, canvas.width - resizeHandle - resizeHandle + adjustX), canvas.height - (resizeHandle / 2) + adjustY);
                context.lineTo(canvas.width - resizeHandle - resizeHandle + 2 + adjustX, canvas.height - (resizeHandle / 2) - 2 + adjustY);
                context.lineTo(canvas.width - resizeHandle - resizeHandle + 2 + adjustX, canvas.height - (resizeHandle / 2) + 2 + adjustY);
                context.lineTo(canvas.width - resizeHandle - resizeHandle + adjustX, canvas.height - (resizeHandle / 2) + adjustY);
            context.stroke();
            context.fill();

            context.beginPath();
                context.moveTo(AA(this, canvas.width - resizeHandle - resizeHandle - 1 + adjustX), canvas.height - (resizeHandle / 2) - 3 + adjustY);
                context.lineTo(AA(this, canvas.width - resizeHandle - resizeHandle - 1 + adjustX), canvas.height - (resizeHandle / 2) + 3 + adjustY);
            context.stroke();
            context.fill();
            

            var window_onmousemove = function (e)
            {
                e = RGraph.FixEventObject(e);
                
                var canvas    = RGraph.Resizing.canvas;
                var newWidth  = RGraph.Resizing.originalw - (RGraph.Resizing.originalx - e.pageX);// - 5
                var newHeight = RGraph.Resizing.originalh - (RGraph.Resizing.originaly - e.pageY);// - 5

                if (RGraph.Resizing.mousedown) {
                    if (newWidth > (canvas.__original_width__ / 2)) RGraph.Resizing.div.style.width = newWidth + 'px';
                    if (newHeight > (canvas.__original_height__ / 2)) RGraph.Resizing.div.style.height = newHeight + 'px';
                    
                    RGraph.FireCustomEvent(canvas.__object__, 'onresize');
                }
            }
            // Install the function as an event listener - but only once
            if (typeof(canvas.rgraph_resize_window_mousemove_listener_installed) != 'boolean') {
                window.addEventListener('mousemove', window_onmousemove, false);
                canvas.rgraph_resize_window_mousemove_listener_installed = true;
            }

            /**
            * The window onmouseup function
            */
            var MouseupFunc = function (e)
            {
                if (!RGraph.Resizing || !RGraph.Resizing.div || !RGraph.Resizing.mousedown) {
                    return;
                }

                if (RGraph.Resizing.div) {

                    var div    = RGraph.Resizing.div;
                    var canvas = div.__canvas__;
                    var coords = RGraph.getCanvasXY(div.__canvas__);

                    var parentNode = canvas.parentNode;

                    if (canvas.style.position != 'absolute') {
                        // Create a DIV to go in the canvases place
                        var placeHolderDIV = document.createElement('DIV');
                            placeHolderDIV.style.width = RGraph.Resizing.originalw + 'px';
                            placeHolderDIV.style.height = RGraph.Resizing.originalh + 'px';
                            //placeHolderDIV.style.backgroundColor = 'red';
                            placeHolderDIV.style.display = 'inline-block'; // Added 5th Nov 2010
                            placeHolderDIV.style.position = canvas.style.position;
                            placeHolderDIV.style.left     = canvas.style.left;
                            placeHolderDIV.style.top      = canvas.style.top;
                            placeHolderDIV.style.cssFloat = canvas.style.cssFloat;

                        parentNode.insertBefore(placeHolderDIV, canvas);
                    }


                    // Now set the canvas to be positioned absolutely
                    canvas.style.backgroundColor = 'white';
                    canvas.style.position        = 'absolute';
                    canvas.style.border = '1px dashed gray';
                    canvas.style.left            = (RGraph.Resizing.originalCanvasX  - 1) + 'px';
                    canvas.style.top             = (RGraph.Resizing.originalCanvasY - 1) + 'px';

                    canvas.width = parseInt(div.style.width);
                    canvas.height = parseInt(div.style.height);
                    
                

                    /**
                    * Fire the onresize event
                    */
                    RGraph.FireCustomEvent(canvas.__object__, 'onresizebeforedraw');

                    RGraph.RedrawCanvas(canvas);
                    //canvas.__object__.Draw();

                    // Get rid of transparent semi-opaque DIV
                    RGraph.Resizing.mousedown = false;
                    div.style.display = 'none';
                    document.body.removeChild(div);
                }

                /**
                * If there is zoom enabled in thumbnail mode, lose the zoom image
                */
                if (RGraph.Registry.Get('chart.zoomed.div') || RGraph.Registry.Get('chart.zoomed.img')) {
                    RGraph.Registry.Set('chart.zoomed.div', null);
                    RGraph.Registry.Set('chart.zoomed.img', null);
                }

                /**
                * Fire the onresize event
                */
                RGraph.FireCustomEvent(canvas.__object__, 'onresizeend');
            }


            var window_onmouseup = MouseupFunc;
            // Install the function as an event listener - but only once
            if (typeof(canvas.rgraph_resize_window_mouseup_listener_installed) != 'boolean') {
                window.addEventListener('mouseup', window_onmouseup, false);
                canvas.rgraph_resize_window_mouseup_listener_installed = true;
            }


            var canvas_onmousemove = function (e)
            {
                e = RGraph.FixEventObject(e);
                
                var coords  = RGraph.getMouseXY(e);
                var obj     = e.target.__object__;
                var canvas  = e.target;
                var context = canvas.getContext('2d');
                var cursor  = canvas.style.cursor;

                // Save the original cursor
                if (!RGraph.Resizing.original_cursor) {
                    RGraph.Resizing.original_cursor = cursor;
                }
                
                if (   (coords[0] > (canvas.width - resizeHandle)
                    && coords[0] < canvas.width
                    && coords[1] > (canvas.height - resizeHandle)
                    && coords[1] < canvas.height)) {
                        
                        canvas.style.cursor = 'move';

                } else if (   coords[0] > (canvas.width - resizeHandle - resizeHandle)
                           && coords[0] < canvas.width - resizeHandle
                           && coords[1] > (canvas.height - resizeHandle)
                           && coords[1] < canvas.height) {
                    
                    canvas.style.cursor = 'pointer';

                } else {
                    if (RGraph.Resizing.original_cursor) {
                        canvas.style.cursor = RGraph.Resizing.original_cursor;
                        RGraph.Resizing.original_cursor = null;
                    } else {
                        canvas.style.cursor = 'default';
                    }
                }
            }
            // Install the function as an event listener - but only once
            if (typeof(canvas.rgraph_resize_mousemove_listener_installed) != 'boolean') {
                canvas.addEventListener('mousemove', canvas_onmousemove, false);
                canvas.rgraph_resize_mousemove_listener_installed = true;
            }



            var canvas_onmouseout = function (e)
            {
                e.target.style.cursor = 'default';
                e.target.title        = '';
            }
            // Install the function as an event listener - but only once
            if (typeof(canvas.rgraph_resize_mouseout_listener_installed) != 'boolean') {
                canvas.addEventListener('mouseout', canvas_onmouseout, false);
                canvas.rgraph_resize_mouseout_listener_installed = true;
            }



            var canvas_onmousedown = function (e)
            {
                e = RGraph.FixEventObject(e);

                var coords = RGraph.getMouseXY(e);
                var canvasCoords = RGraph.getCanvasXY(e.target);
                var canvas = e.target;

                if (   coords[0] > (obj.canvas.width - resizeHandle)
                    && coords[0] < obj.canvas.width
                    && coords[1] > (obj.canvas.height - resizeHandle)
                    && coords[1] < obj.canvas.height) {
                    
                    RGraph.FireCustomEvent(obj, 'onresizebegin');
                    
                    // Save the existing border
                    if (canvas.__original_css_border__ == null) {
                        canvas.__original_css_border__ = canvas.style.border;
                    }

                    RGraph.Resizing.mousedown = true;


                    /**
                    * Create the semi-opaque DIV
                    */

                    var div = document.createElement('DIV');
                    div.style.position = 'absolute';
                    div.style.left     = canvasCoords[0] + 'px';
                    div.style.top      = canvasCoords[1] + 'px';
                    div.style.width    = canvas.width + 'px';
                    div.style.height   = canvas.height + 'px';
                    div.style.border   = '1px dotted black';
                    div.style.backgroundColor = 'gray';
                    div.style.opacity  = 0.5;
                    div.__canvas__ = e.target;

                    document.body.appendChild(div);
                    RGraph.Resizing.div = div;
                    RGraph.Resizing.placeHolders.push(div);
                    
                    // Hide the previous resize indicator layers. This is only necessary it seems for the Meter chart
                    for (var i=0; i<(RGraph.Resizing.placeHolders.length - 1); ++i) {
                        RGraph.Resizing.placeHolders[i].style.display = 'none';
                    }

                    // This is a repetition of the window.onmouseup function (No need to use DOM2 here)
                    div.onmouseup = function (e)
                    {
                        MouseupFunc(e);
                    }

                    
                    // No need to use DOM2 here
                    RGraph.Resizing.div.onmouseover = function (e)
                    {
                        e = RGraph.FixEventObject(e);
                        e.stopPropagation();
                    }
    
                    // The mouse
                    RGraph.Resizing.originalx = e.pageX;
                    RGraph.Resizing.originaly = e.pageY;
                    
                    RGraph.Resizing.originalw = obj.canvas.width;
                    RGraph.Resizing.originalh = obj.canvas.height;
                    
                    RGraph.Resizing.originalCanvasX = RGraph.getCanvasXY(obj.canvas)[0];
                    RGraph.Resizing.originalCanvasY = RGraph.getCanvasXY(obj.canvas)[1];
                }


                /**
                * This facilitates the reset button
                */
                if (   coords[0] > (canvas.width - resizeHandle - resizeHandle)
                    && coords[0] < canvas.width - resizeHandle
                    && coords[1] > (canvas.height - resizeHandle)
                    && coords[1] < canvas.height) {
                    
                    /**
                    * Fire the onresizebegin event
                    */
                    RGraph.FireCustomEvent(canvas.__object__, 'onresizebegin');

                    // Restore the original width and height
                    canvas.width = canvas.__original_width__;
                    canvas.height = canvas.__original_height__;

                    // Lose the border
                    canvas.style.border = canvas.__original_css_border__;
                    //canvas.__original_css_border__ = null;
                    
                    // Add 1 pixel to the top/left because the border is going
                    canvas.style.left = (parseInt(canvas.style.left)) + 'px';
                    canvas.style.top  = (parseInt(canvas.style.top)) + 'px';


                    RGraph.FireCustomEvent(canvas.__object__, 'onresizebeforedraw');

                    // Redraw the canvas
                    RGraph.Redraw();
                    
                    // Set the width and height on the DIV
                    if (RGraph.Resizing.div) {
                        RGraph.Resizing.div.style.width  = canvas.__original_width__ + 'px';
                        RGraph.Resizing.div.style.height = canvas.__original_height__ + 'px';
                    }

                    /**
                    * Fire the resize event
                    */
                    RGraph.FireCustomEvent(canvas.__object__, 'onresize');
                    RGraph.FireCustomEvent(canvas.__object__, 'onresizeend');
                }
            }
            // Install the function as an event listener - but only once
            if (typeof(canvas.rgraph_resize_mousedown_listener_installed) != 'boolean') {
                canvas.addEventListener('mousedown', canvas_onmousedown, false);
                canvas.rgraph_resize_mousedown_listener_installed = true;
            }


            /**
            * This function facilitates the reset button
            * 
            * NOTE: 31st December 2010 - doesn't appear to be being used any more
            */

            /*
            canvas.onclick = function (e)
            {
                var coords = RGraph.getMouseXY(e);
                var canvas = e.target;

                if (   coords[0] > (canvas.width - resizeHandle - resizeHandle)
                    && coords[0] < canvas.width - resizeHandle
                    && coords[1] > (canvas.height - resizeHandle)
                    && coords[1] < canvas.height) {

                    // Restore the original width and height
                    canvas.width = canvas.__original_width__;
                    canvas.height = canvas.__original_height__;

                    // Lose the border
                    canvas.style.border = '';
                    
                    // Add 1 pixel to the top/left because the border is going
                    canvas.style.left = (parseInt(canvas.style.left) + 1) + 'px';
                    canvas.style.top  = (parseInt(canvas.style.top) + 1) + 'px';
                    
                    // Fire the onresizebeforedraw event
                    RGraph.FireCustomEvent(canvas.__object__, 'onresizebeforedraw');

                    // Redraw the canvas
                    canvas.__object__.Draw();
                    
                    // Set the width and height on the DIV
                    RGraph.Resizing.div.style.width  = canvas.__original_width__ + 'px';
                    RGraph.Resizing.div.style.height = canvas.__original_height__ + 'px';
                    
                    // Fire the resize event
                    RGraph.FireCustomEvent(canvas.__object__, 'onresize');
                }
            }
            */
        }
    }