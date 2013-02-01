/**
 * KineticJS JavaScript Library v4.3.1-beta2
 * http://www.kineticjs.com/
 * Copyright 2013, Eric Rowell
 * Licensed under the MIT or GPL Version 2 licenses.
 * Date: Jan 13 2013
 *
 * Copyright (C) 2011 - 2013 by Eric Rowell
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/**
 * @namespace
 */
var Kinetic = {}; (function() {
    Kinetic.version = '4.3.1-beta2';
    /**
     * @namespace
     */
    Kinetic.Filters = {};
    Kinetic.Plugins = {};
    Kinetic.Global = {
        stages: [],
        idCounter: 0,
        ids: {},
        names: {},
        //shapes hash.  rgb keys and shape values
        shapes: {},
        warn: function(str) {
            /*
             * IE9 on Windows7 64bit will throw a JS error
             * if we don't use window.console in the conditional
             */
            if(window.console && console.warn) {
                console.warn('Kinetic warning: ' + str);
            }
        },
        extend: function(c1, c2) {
            for(var key in c2.prototype) {
                if(!( key in c1.prototype)) {
                    c1.prototype[key] = c2.prototype[key];
                }
            }
        },
        _addId: function(node, id) {
            if(id !== undefined) {
                this.ids[id] = node;
            }
        },
        _removeId: function(id) {
            if(id !== undefined) {
                delete this.ids[id];
            }
        },
        _addName: function(node, name) {
            if(name !== undefined) {
                if(this.names[name] === undefined) {
                    this.names[name] = [];
                }
                this.names[name].push(node);
            }
        },
        _removeName: function(name, _id) {
            if(name !== undefined) {
                var nodes = this.names[name];
                if(nodes !== undefined) {
                    for(var n = 0; n < nodes.length; n++) {
                        var no = nodes[n];
                        if(no._id === _id) {
                            nodes.splice(n, 1);
                        }
                    }
                    if(nodes.length === 0) {
                        delete this.names[name];
                    }
                }
            }
        }
    };
})();

// Uses Node, AMD or browser globals to create a module.

// If you want something that will work in other stricter CommonJS environments,
// or if you need to create a circular dependency, see commonJsStrict.js

// Defines a module "returnExports" that depends another module called "b".
// Note that the name of the module is implied by the file name. It is best
// if the file name and the exported global have matching names.

// If the 'b' module also uses this type of boilerplate, then
// in the browser, it will create a global .b that is used below.

// If you do not want to support the browser global path, then you
// can remove the `root` use and the passing `this` as the first arg to
// the top function.

// if the module has no dependencies, the above pattern can be simplified to
( function(root, factory) {
    if( typeof exports === 'object') {
        // Node. Does not work with strict CommonJS, but
        // only CommonJS-like enviroments that support module.exports,
        // like Node.
        module.exports = factory();
    }
    else if( typeof define === 'function' && define.amd) {
        // AMD. Register as an anonymous module.
        define(factory);
    }
    else {
        // Browser globals (root is window)
        root.returnExports = factory();
    }
}(this, function() {

    // Just return a value to define the module export.
    // This example returns an object, but the module
    // can return a function as the exported value.
    return Kinetic;
}));

(function() {
    /*
     * utilities that handle data type detection, conversion, and manipulation
     */
    Kinetic.Type = {
        /*
         * cherry-picked utilities from underscore.js
         */
        _isElement: function(obj) {
            return !!(obj && obj.nodeType == 1);
        },
        _isFunction: function(obj) {
            return !!(obj && obj.constructor && obj.call && obj.apply);
        },
        _isObject: function(obj) {
            return (!!obj && obj.constructor == Object);
        },
        _isArray: function(obj) {
            return Object.prototype.toString.call(obj) == '[object Array]';
        },
        _isNumber: function(obj) {
            return Object.prototype.toString.call(obj) == '[object Number]';
        },
        _isString: function(obj) {
            return Object.prototype.toString.call(obj) == '[object String]';
        },
        /*
         * other utils
         */
        _hasMethods: function(obj) {
            var names = [];
            for(var key in obj) {
                if(this._isFunction(obj[key]))
                    names.push(key);
            }
            return names.length > 0;
        },
        _isInDocument: function(el) {
            while( el = el.parentNode) {
                if(el == document) {
                    return true;
                }
            }
            return false;
        },
        /*
         * The argument can be:
         * - an integer (will be applied to both x and y)
         * - an array of one integer (will be applied to both x and y)
         * - an array of two integers (contains x and y)
         * - an array of four integers (contains x, y, width, and height)
         * - an object with x and y properties
         * - an array of one element which is an array of integers
         * - an array of one element of an object
         */
        _getXY: function(arg) {
            if(this._isNumber(arg)) {
                return {
                    x: arg,
                    y: arg
                };
            }
            else if(this._isArray(arg)) {
                // if arg is an array of one element
                if(arg.length === 1) {
                    var val = arg[0];
                    // if arg is an array of one element which is a number
                    if(this._isNumber(val)) {
                        return {
                            x: val,
                            y: val
                        };
                    }
                    // if arg is an array of one element which is an array
                    else if(this._isArray(val)) {
                        return {
                            x: val[0],
                            y: val[1]
                        };
                    }
                    // if arg is an array of one element which is an object
                    else if(this._isObject(val)) {
                        return val;
                    }
                }
                // if arg is an array of two or more elements
                else if(arg.length >= 2) {
                    return {
                        x: arg[0],
                        y: arg[1]
                    };
                }
            }
            // if arg is an object return the object
            else if(this._isObject(arg)) {
                return arg;
            }

            // default
            return null;
        },
        /*
         * The argument can be:
         * - an integer (will be applied to both width and height)
         * - an array of one integer (will be applied to both width and height)
         * - an array of two integers (contains width and height)
         * - an array of four integers (contains x, y, width, and height)
         * - an object with width and height properties
         * - an array of one element which is an array of integers
         * - an array of one element of an object
         */
        _getSize: function(arg) {
            if(this._isNumber(arg)) {
                return {
                    width: arg,
                    height: arg
                };
            }
            else if(this._isArray(arg)) {
                // if arg is an array of one element
                if(arg.length === 1) {
                    var val = arg[0];
                    // if arg is an array of one element which is a number
                    if(this._isNumber(val)) {
                        return {
                            width: val,
                            height: val
                        };
                    }
                    // if arg is an array of one element which is an array
                    else if(this._isArray(val)) {
                        /*
                         * if arg is an array of one element which is an
                         * array of four elements
                         */
                        if(val.length >= 4) {
                            return {
                                width: val[2],
                                height: val[3]
                            };
                        }
                        /*
                         * if arg is an array of one element which is an
                         * array of two elements
                         */
                        else if(val.length >= 2) {
                            return {
                                width: val[0],
                                height: val[1]
                            };
                        }
                    }
                    // if arg is an array of one element which is an object
                    else if(this._isObject(val)) {
                        return val;
                    }
                }
                // if arg is an array of four elements
                else if(arg.length >= 4) {
                    return {
                        width: arg[2],
                        height: arg[3]
                    };
                }
                // if arg is an array of two elements
                else if(arg.length >= 2) {
                    return {
                        width: arg[0],
                        height: arg[1]
                    };
                }
            }
            // if arg is an object return the object
            else if(this._isObject(arg)) {
                return arg;
            }

            // default
            return null;
        },
        /*
         * arg will be an array of numbers or
         *  an array of point arrays or
         *  an array of point objects
         */
        _getPoints: function(arg) {
            if(arg === undefined) {
                return [];
            }

            // an array of arrays
            if(this._isArray(arg[0])) {
                /*
                 * convert array of arrays into an array
                 * of objects containing x, y
                 */
                var arr = [];
                for(var n = 0; n < arg.length; n++) {
                    arr.push({
                        x: arg[n][0],
                        y: arg[n][1]
                    });
                }

                return arr;
            }
            // an array of objects
            if(this._isObject(arg[0])) {
                return arg;
            }
            // an array of integers
            else {
                /*
                 * convert array of numbers into an array
                 * of objects containing x, y
                 */
                var arr = [];
                for(var n = 0; n < arg.length; n += 2) {
                    arr.push({
                        x: arg[n],
                        y: arg[n + 1]
                    });
                }

                return arr;
            }
        },
        /*
         * arg can be an image object or image data
         */
        _getImage: function(arg, callback) {
            // if arg is null or undefined
            if(!arg) {
                callback(null);
            }

            // if arg is already an image object
            else if(this._isElement(arg)) {
                callback(arg);
            }

            // if arg is a string, then it's a data url
            else if(this._isString(arg)) {
                var imageObj = new Image();
                /** @ignore */
                imageObj.onload = function() {
                    callback(imageObj);
                }
                imageObj.src = arg;
            }

            //if arg is an object that contains the data property, it's an image object
            else if(arg.data) {
                var canvas = document.createElement('canvas');
                canvas.width = arg.width;
                canvas.height = arg.height;
                var context = canvas.getContext('2d');
                context.putImageData(arg, 0, 0);
                var dataUrl = canvas.toDataURL();
                var imageObj = new Image();
                /** @ignore */
                imageObj.onload = function() {
                    callback(imageObj);
                }
                imageObj.src = dataUrl;
            }
            else {
                callback(null);
            }
        },
        _rgbToHex: function(r, g, b) {
            return ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
        },
        _hexToRgb: function(hex) {
            var bigint = parseInt(hex, 16);
            return {
                r: (bigint >> 16) & 255,
                g: (bigint >> 8) & 255,
                b: bigint & 255
            };
        },
        _getRandomColorKey: function() {
            var r = Math.round(Math.random() * 255);
            var g = Math.round(Math.random() * 255);
            var b = Math.round(Math.random() * 255);
            return this._rgbToHex(r, g, b);
        },
        // o1 takes precedence over o2
        _merge: function(o1, o2) {
            var retObj = this._clone(o2);
            for(var key in o1) {
                if(this._isObject(o1[key])) {
                    retObj[key] = this._merge(o1[key], retObj[key]);
                }
                else {
                    retObj[key] = o1[key];
                }
            }
            return retObj;
        },
        // deep clone
        _clone: function(obj) {
            var retObj = {};
            for(var key in obj) {
                if(this._isObject(obj[key])) {
                    retObj[key] = this._clone(obj[key]);
                }
                else {
                    retObj[key] = obj[key];
                }
            }
            return retObj;
        },
        _degToRad: function(deg) {
            return deg * Math.PI / 180;
        },
        _radToDeg: function(rad) {
            return rad * 180 / Math.PI;
        }
    };
})();

(function() {
    /**
     * Canvas Renderer constructor
     * @constructor
     * @param {Number} width
     * @param {Number} height
     */
    Kinetic.Canvas = function(width, height) {
        this.width = width;
        this.height = height;
        this.element = document.createElement('canvas');
        this.context = this.element.getContext('2d');
        this.setSize(width || 0, height || 0);
    };
    // calculate pixel ratio
    var canvas = document.createElement('canvas'), context = canvas.getContext('2d'), devicePixelRatio = window.devicePixelRatio || 1, backingStoreRatio = context.webkitBackingStorePixelRatio || context.mozBackingStorePixelRatio || context.msBackingStorePixelRatio || context.oBackingStorePixelRatio || context.backingStorePixelRatio || 1;
    Kinetic.Canvas.pixelRatio = devicePixelRatio / backingStoreRatio;

    Kinetic.Canvas.prototype = {
        /**
         * clear canvas
         * @name clear
         * @methodOf Kinetic.Canvas.prototype
         */
        clear: function() {
            var context = this.getContext();
            var el = this.getElement();
            context.clearRect(0, 0, el.width, el.height);
        },
        /**
         * get canvas element
         * @name getElement
         * @methodOf Kinetic.Canvas.prototype
         */
        getElement: function() {
            return this.element;
        },
        /**
         * get canvas context
         * @name getContext
         * @methodOf Kinetic.Canvas.prototype
         */
        getContext: function() {
            return this.context;
        },
        /**
         * set width
         * @name setWidth
         * @methodOf Kinetic.Canvas.prototype
         * @param {Number} width
         */
        setWidth: function(width) {
            this.width = width;
            // take into account pixel ratio
            this.element.width = width * Kinetic.Canvas.pixelRatio;
            this.element.style.width = width + 'px';
        },
        /**
         * set height
         * @name setHeight
         * @methodOf Kinetic.Canvas.prototype
         * @param {Number} height
         */
        setHeight: function(height) {
            this.height = height;
            // take into account pixel ratio
            this.element.height = height * Kinetic.Canvas.pixelRatio;
            this.element.style.height = height + 'px';
        },
        /**
         * get width
         * @name getWidth
         * @methodOf Kinetic.Canvas.prototype
         */
        getWidth: function() {
            return this.width;
        },
        /**
         * get height
         * @name getHeight
         * @methodOf Kinetic.Canvas.prototype
         */
        getHeight: function() {
            return this.height;
        },
        /**
         * set size
         * @name setSize
         * @methodOf Kinetic.Canvas.prototype
         * @param {Number} width
         * @param {Number} height
         */
        setSize: function(width, height) {
            this.setWidth(width);
            this.setHeight(height);
        },
        /**
         * to data url
         * @name toDataURL
         * @methodOf Kinetic.Canvas.prototype
         * @param {String} mimeType
         * @param {Number} quality between 0 and 1 for jpg mime types
         */
        toDataURL: function(mimeType, quality) {
            try {
                // If this call fails (due to browser bug, like in Firefox 3.6),
                // then revert to previous no-parameter image/png behavior
                return this.element.toDataURL(mimeType, quality);
            }
            catch(e) {
                try {
                    return this.element.toDataURL();
                }
                catch(e) {
                    Kinetic.Global.warn('Unable to get data URL. ' + e.message)
                    return '';
                }
            }
        },
        /**
         * fill shape
         * @name fill
         * @methodOf Kinetic.Canvas.prototype
         * @param {Kinetic.Shape} shape
         */
        fill: function(shape) {
            this._fill(shape);
        },
        /**
         * stroke shape
         * @name stroke
         * @methodOf Kinetic.Canvas.prototype
         * @param {Kinetic.Shape} shape
         */
        stroke: function(shape) {
            this._stroke(shape);
        },
        /**
         * fill, stroke, and apply shadows
         *  will only be applied to either the fill or stroke.&nbsp; Fill
         *  is given priority over stroke.
         * @name fillStroke
         * @methodOf Kinetic.Canvas.prototype
         * @param {Kinetic.Shape} shape
         */
        fillStroke: function(shape) {
            this._fill(shape);
            this._stroke(shape, shape.hasShadow() && shape.getFill());
        },
        /**
         * apply shadow
         * @name applyShadow
         * @methodOf Kinetic.Canvas.prototype
         * @param {Kinetic.Shape} shape
         * @param {Function} drawFunc
         */
        applyShadow: function(shape, drawFunc) {
            var context = this.context;
            context.save();
            this._applyShadow(shape);
            drawFunc();
            context.restore();
            drawFunc();
        },
        _applyLineCap: function(shape) {
            var lineCap = shape.getLineCap();
            if(lineCap) {
                this.context.lineCap = lineCap;
            }
        },
        _applyOpacity: function(shape) {
            var absOpacity = shape.getAbsoluteOpacity();
            if(absOpacity !== 1) {
                this.context.globalAlpha = absOpacity;
            }
        },
        _applyLineJoin: function(shape) {
            var lineJoin = shape.getLineJoin();
            if(lineJoin) {
                this.context.lineJoin = lineJoin;
            }
        },
        _handlePixelRatio: function() {
            var pixelRatio = Kinetic.Canvas.pixelRatio;
            if(pixelRatio !== 1) {
                this.getContext().scale(pixelRatio, pixelRatio);
            }
        },
        _counterPixelRatio: function() {
            var pixelRatio = Kinetic.Canvas.pixelRatio;
            if(pixelRatio !== 1) {
                pixelRatio = 1 / pixelRatio;
                this.getContext().scale(pixelRatio, pixelRatio);
            }
        },
        _applyAncestorTransforms: function(node) {
            var context = this.context;
            node._eachAncestorReverse(function(no) {
                var t = no.getTransform(), m = t.getMatrix();
                context.transform(m[0], m[1], m[2], m[3], m[4], m[5]);
            }, true);
        }
    };

    Kinetic.SceneCanvas = function(width, height) {
        Kinetic.Canvas.call(this, width, height);
    };

    Kinetic.SceneCanvas.prototype = {
        _fillColor: function(shape) {
            var context = this.context, fill = shape.getFill();
            context.fillStyle = fill;
            context.fill(context);
        },
        _fillPattern: function(shape) {
            var context = this.context, fillPatternImage = shape.getFillPatternImage(), fillPatternX = shape.getFillPatternX(), fillPatternY = shape.getFillPatternY(), fillPatternScale = shape.getFillPatternScale(), fillPatternRotation = shape.getFillPatternRotation(), fillPatternOffset = shape.getFillPatternOffset(), fillPatternRepeat = shape.getFillPatternRepeat();

            if(fillPatternX || fillPatternY) {
                context.translate(fillPatternX || 0, fillPatternY || 0);
            }
            if(fillPatternRotation) {
                context.rotate(fillPatternRotation);
            }
            if(fillPatternScale) {
                context.scale(fillPatternScale.x, fillPatternScale.y);
            }
            if(fillPatternOffset) {
                context.translate(-1 * fillPatternOffset.x, -1 * fillPatternOffset.y);
            }

            context.fillStyle = context.createPattern(fillPatternImage, fillPatternRepeat || 'repeat');
            context.fill(context);
        },
        _fillLinearGradient: function(shape) {
            var context = this.context, start = shape.getFillLinearGradientStartPoint(), end = shape.getFillLinearGradientEndPoint(), colorStops = shape.getFillLinearGradientColorStops(), grd = context.createLinearGradient(start.x, start.y, end.x, end.y);

            // build color stops
            for(var n = 0; n < colorStops.length; n += 2) {
                grd.addColorStop(colorStops[n], colorStops[n + 1]);
            }
            context.fillStyle = grd;
            context.fill(context);
        },
        _fillRadialGradient: function(shape) {
            var context = this.context, start = shape.getFillRadialGradientStartPoint(), end = shape.getFillRadialGradientEndPoint(), startRadius = shape.getFillRadialGradientStartRadius(), endRadius = shape.getFillRadialGradientEndRadius(), colorStops = shape.getFillRadialGradientColorStops(), grd = context.createRadialGradient(start.x, start.y, startRadius, end.x, end.y, endRadius);

            // build color stops
            for(var n = 0; n < colorStops.length; n += 2) {
                grd.addColorStop(colorStops[n], colorStops[n + 1]);
            }
            context.fillStyle = grd;
            context.fill(context);
        },
        _fill: function(shape, skipShadow) {
            var context = this.context, fill = shape.getFill(), fillPatternImage = shape.getFillPatternImage(), fillLinearGradientStartPoint = shape.getFillLinearGradientStartPoint(), fillRadialGradientStartPoint = shape.getFillRadialGradientStartPoint();

            context.save();

            if(!skipShadow && shape.hasShadow()) {
                this._applyShadow(shape);
            }

            if(fill) {
                this._fillColor(shape);
            }
            else if(fillPatternImage) {
                this._fillPattern(shape);
            }
            else if(fillLinearGradientStartPoint) {
                this._fillLinearGradient(shape);
            }
            else if(fillRadialGradientStartPoint) {
                this._fillRadialGradient(shape);
            }
            context.restore();

            if(!skipShadow && shape.hasShadow()) {
                this._fill(shape, true);
            }
        },
        _stroke: function(shape, skipShadow) {
            var context = this.context, stroke = shape.getStroke(), strokeWidth = shape.getStrokeWidth(), dashArray = shape.getDashArray();
            if(stroke || strokeWidth) {
                context.save();
                this._applyLineCap(shape);
                if(dashArray) {
                    if(context.setLineDash) {
                        context.setLineDash(dashArray);
                    }
                }
                if(!skipShadow && shape.hasShadow()) {
                    this._applyShadow(shape);
                }
                context.lineWidth = strokeWidth || 2;
                context.strokeStyle = stroke || 'black';
                context.stroke(context);
                context.restore();

                if(!skipShadow && shape.hasShadow()) {
                    this._stroke(shape, true);
                }
            }
        },
        _applyShadow: function(shape) {
            var context = this.context;
            if(shape.hasShadow()) {
                var aa = shape.getAbsoluteOpacity();
                // defaults
                var color = shape.getShadowColor() || 'black';
                var blur = shape.getShadowBlur() || 5;
                var offset = shape.getShadowOffset() || {
                    x: 0,
                    y: 0
                };

                if(shape.getShadowOpacity()) {
                    context.globalAlpha = shape.getShadowOpacity() * aa;
                }
                context.shadowColor = color;
                context.shadowBlur = blur;
                context.shadowOffsetX = offset.x;
                context.shadowOffsetY = offset.y;
            }
        }
    };
    Kinetic.Global.extend(Kinetic.SceneCanvas, Kinetic.Canvas);

    Kinetic.HitCanvas = function(width, height) {
        Kinetic.Canvas.call(this, width, height);
    };

    Kinetic.HitCanvas.prototype = {
        _fill: function(shape) {
            var context = this.context;
            context.save();
            context.fillStyle = '#' + shape.colorKey;
            context.fill(context);
            context.restore();
        },
        _stroke: function(shape) {
            var context = this.context, stroke = shape.getStroke(), strokeWidth = shape.getStrokeWidth();
            if(stroke || strokeWidth) {
                this._applyLineCap(shape);
                context.save();
                context.lineWidth = strokeWidth || 2;
                context.strokeStyle = '#' + shape.colorKey;
                context.stroke(context);
                context.restore();
            }
        }
    };
    Kinetic.Global.extend(Kinetic.HitCanvas, Kinetic.Canvas);
})();

(function() {
    /*
     * The Tween class was ported from an Adobe Flash Tween library
     * to JavaScript by Xaric.  In the context of KineticJS, a Tween is
     * an animation of a single Node property.  A Transition is a set of
     * multiple tweens
     */
    Kinetic.Tween = function(obj, propFunc, func, begin, finish, duration) {
        this._listeners = [];
        this.addListener(this);
        this.obj = obj;
        this.propFunc = propFunc;
        this.begin = begin;
        this._pos = begin;
        this.setDuration(duration);
        this.isPlaying = false;
        this._change = 0;
        this.prevTime = 0;
        this.prevPos = 0;
        this.looping = false;
        this._time = 0;
        this._position = 0;
        this._startTime = 0;
        this._finish = 0;
        this.name = '';
        this.func = func;
        this.setFinish(finish);
    };
    /*
     * Tween methods
     */
    Kinetic.Tween.prototype = {
        setTime: function(t) {
            this.prevTime = this._time;
            if(t > this.getDuration()) {
                if(this.looping) {
                    this.rewind(t - this._duration);
                    this.update();
                    this.broadcastMessage('onLooped', {
                        target: this,
                        type: 'onLooped'
                    });
                }
                else {
                    this._time = this._duration;
                    this.update();
                    this.stop();
                    this.broadcastMessage('onFinished', {
                        target: this,
                        type: 'onFinished'
                    });
                }
            }
            else if(t < 0) {
                this.rewind();
                this.update();
            }
            else {
                this._time = t;
                this.update();
            }
        },
        getTime: function() {
            return this._time;
        },
        setDuration: function(d) {
            this._duration = (d === null || d <= 0) ? 100000 : d;
        },
        getDuration: function() {
            return this._duration;
        },
        setPosition: function(p) {
            this.prevPos = this._pos;
            this.propFunc(p);
            this._pos = p;
            this.broadcastMessage('onChanged', {
                target: this,
                type: 'onChanged'
            });
        },
        getPosition: function(t) {
            if(t === undefined) {
                t = this._time;
            }
            return this.func(t, this.begin, this._change, this._duration);
        },
        setFinish: function(f) {
            this._change = f - this.begin;
        },
        getFinish: function() {
            return this.begin + this._change;
        },
        start: function() {
            this.rewind();
            this.startEnterFrame();
            this.broadcastMessage('onStarted', {
                target: this,
                type: 'onStarted'
            });
        },
        rewind: function(t) {
            this.stop();
            this._time = (t === undefined) ? 0 : t;
            this.fixTime();
            this.update();
        },
        fforward: function() {
            this._time = this._duration;
            this.fixTime();
            this.update();
        },
        update: function() {
            this.setPosition(this.getPosition(this._time));
        },
        startEnterFrame: function() {
            this.stopEnterFrame();
            this.isPlaying = true;
            this.onEnterFrame();
        },
        onEnterFrame: function() {
            if(this.isPlaying) {
                this.nextFrame();
            }
        },
        nextFrame: function() {
            this.setTime((this.getTimer() - this._startTime) / 1000);
        },
        stop: function() {
            this.stopEnterFrame();
            this.broadcastMessage('onStopped', {
                target: this,
                type: 'onStopped'
            });
        },
        stopEnterFrame: function() {
            this.isPlaying = false;
        },
        continueTo: function(finish, duration) {
            this.begin = this._pos;
            this.setFinish(finish);
            if(this._duration !== undefined) {
                this.setDuration(duration);
            }
            this.start();
        },
        resume: function() {
            this.fixTime();
            this.startEnterFrame();
            this.broadcastMessage('onResumed', {
                target: this,
                type: 'onResumed'
            });
        },
        yoyo: function() {
            this.continueTo(this.begin, this._time);
        },
        addListener: function(o) {
            this.removeListener(o);
            return this._listeners.push(o);
        },
        removeListener: function(o) {
            var a = this._listeners;
            var i = a.length;
            while(i--) {
                if(a[i] == o) {
                    a.splice(i, 1);
                    return true;
                }
            }
            return false;
        },
        broadcastMessage: function() {
            var arr = [];
            for(var i = 0; i < arguments.length; i++) {
                arr.push(arguments[i]);
            }
            var e = arr.shift();
            var a = this._listeners;
            var l = a.length;
            for(var i = 0; i < l; i++) {
                if(a[i][e]) {
                    a[i][e].apply(a[i], arr);
                }
            }
        },
        fixTime: function() {
            this._startTime = this.getTimer() - this._time * 1000;
        },
        getTimer: function() {
            return new Date().getTime() - this._time;
        }
    };

    Kinetic.Tweens = {
        'back-ease-in': function(t, b, c, d, a, p) {
            var s = 1.70158;
            return c * (t /= d) * t * ((s + 1) * t - s) + b;
        },
        'back-ease-out': function(t, b, c, d, a, p) {
            var s = 1.70158;
            return c * (( t = t / d - 1) * t * ((s + 1) * t + s) + 1) + b;
        },
        'back-ease-in-out': function(t, b, c, d, a, p) {
            var s = 1.70158;
            if((t /= d / 2) < 1) {
                return c / 2 * (t * t * (((s *= (1.525)) + 1) * t - s)) + b;
            }
            return c / 2 * ((t -= 2) * t * (((s *= (1.525)) + 1) * t + s) + 2) + b;
        },
        'elastic-ease-in': function(t, b, c, d, a, p) {
            // added s = 0
            var s = 0;
            if(t === 0) {
                return b;
            }
            if((t /= d) == 1) {
                return b + c;
            }
            if(!p) {
                p = d * 0.3;
            }
            if(!a || a < Math.abs(c)) {
                a = c;
                s = p / 4;
            }
            else {
                s = p / (2 * Math.PI) * Math.asin(c / a);
            }
            return -(a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
        },
        'elastic-ease-out': function(t, b, c, d, a, p) {
            // added s = 0
            var s = 0;
            if(t === 0) {
                return b;
            }
            if((t /= d) == 1) {
                return b + c;
            }
            if(!p) {
                p = d * 0.3;
            }
            if(!a || a < Math.abs(c)) {
                a = c;
                s = p / 4;
            }
            else {
                s = p / (2 * Math.PI) * Math.asin(c / a);
            }
            return (a * Math.pow(2, -10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b);
        },
        'elastic-ease-in-out': function(t, b, c, d, a, p) {
            // added s = 0
            var s = 0;
            if(t === 0) {
                return b;
            }
            if((t /= d / 2) == 2) {
                return b + c;
            }
            if(!p) {
                p = d * (0.3 * 1.5);
            }
            if(!a || a < Math.abs(c)) {
                a = c;
                s = p / 4;
            }
            else {
                s = p / (2 * Math.PI) * Math.asin(c / a);
            }
            if(t < 1) {
                return -0.5 * (a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
            }
            return a * Math.pow(2, -10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p) * 0.5 + c + b;
        },
        'bounce-ease-out': function(t, b, c, d) {
            if((t /= d) < (1 / 2.75)) {
                return c * (7.5625 * t * t) + b;
            }
            else if(t < (2 / 2.75)) {
                return c * (7.5625 * (t -= (1.5 / 2.75)) * t + 0.75) + b;
            }
            else if(t < (2.5 / 2.75)) {
                return c * (7.5625 * (t -= (2.25 / 2.75)) * t + 0.9375) + b;
            }
            else {
                return c * (7.5625 * (t -= (2.625 / 2.75)) * t + 0.984375) + b;
            }
        },
        'bounce-ease-in': function(t, b, c, d) {
            return c - Kinetic.Tweens['bounce-ease-out'](d - t, 0, c, d) + b;
        },
        'bounce-ease-in-out': function(t, b, c, d) {
            if(t < d / 2) {
                return Kinetic.Tweens['bounce-ease-in'](t * 2, 0, c, d) * 0.5 + b;
            }
            else {
                return Kinetic.Tweens['bounce-ease-out'](t * 2 - d, 0, c, d) * 0.5 + c * 0.5 + b;
            }
        },
        // duplicate
        /*
         strongEaseInOut: function(t, b, c, d) {
         return c * (t /= d) * t * t * t * t + b;
         },
         */
        'ease-in': function(t, b, c, d) {
            return c * (t /= d) * t + b;
        },
        'ease-out': function(t, b, c, d) {
            return -c * (t /= d) * (t - 2) + b;
        },
        'ease-in-out': function(t, b, c, d) {
            if((t /= d / 2) < 1) {
                return c / 2 * t * t + b;
            }
            return -c / 2 * ((--t) * (t - 2) - 1) + b;
        },
        'strong-ease-in': function(t, b, c, d) {
            return c * (t /= d) * t * t * t * t + b;
        },
        'strong-ease-out': function(t, b, c, d) {
            return c * (( t = t / d - 1) * t * t * t * t + 1) + b;
        },
        'strong-ease-in-out': function(t, b, c, d) {
            if((t /= d / 2) < 1) {
                return c / 2 * t * t * t * t * t + b;
            }
            return c / 2 * ((t -= 2) * t * t * t * t + 2) + b;
        },
        'linear': function(t, b, c, d) {
            return c * t / d + b;
        }
    };
})();

(function() {
    /*
     * Last updated November 2011
     * By Simon Sarris
     * www.simonsarris.com
     * sarris@acm.org
     *
     * Free to use and distribute at will
     * So long as you are nice to people, etc
     */

    /*
     * The usage of this class was inspired by some of the work done by a forked
     * project, KineticJS-Ext by Wappworks, which is based on Simon's Transform
     * class.
     */

    /**
     * Transform constructor
     * @constructor
     */
    Kinetic.Transform = function() {
        this.m = [1, 0, 0, 1, 0, 0];
    }

    Kinetic.Transform.prototype = {
        /**
         * Apply translation
         * @param {Number} x
         * @param {Number} y
         */
        translate: function(x, y) {
            this.m[4] += this.m[0] * x + this.m[2] * y;
            this.m[5] += this.m[1] * x + this.m[3] * y;
        },
        /**
         * Apply scale
         * @param {Number} sx
         * @param {Number} sy
         */
        scale: function(sx, sy) {
            this.m[0] *= sx;
            this.m[1] *= sx;
            this.m[2] *= sy;
            this.m[3] *= sy;
        },
        /**
         * Apply rotation
         * @param {Number} rad  Angle in radians
         */
        rotate: function(rad) {
            var c = Math.cos(rad);
            var s = Math.sin(rad);
            var m11 = this.m[0] * c + this.m[2] * s;
            var m12 = this.m[1] * c + this.m[3] * s;
            var m21 = this.m[0] * -s + this.m[2] * c;
            var m22 = this.m[1] * -s + this.m[3] * c;
            this.m[0] = m11;
            this.m[1] = m12;
            this.m[2] = m21;
            this.m[3] = m22;
        },
        /**
         * Returns the translation
         * @returns {Object} 2D point(x, y)
         */
        getTranslation: function() {
            return {
                x: this.m[4],
                y: this.m[5]
            };
        },
        /**
         * Transform multiplication
         * @param {Kinetic.Transform} matrix
         */
        multiply: function(matrix) {
            var m11 = this.m[0] * matrix.m[0] + this.m[2] * matrix.m[1];
            var m12 = this.m[1] * matrix.m[0] + this.m[3] * matrix.m[1];

            var m21 = this.m[0] * matrix.m[2] + this.m[2] * matrix.m[3];
            var m22 = this.m[1] * matrix.m[2] + this.m[3] * matrix.m[3];

            var dx = this.m[0] * matrix.m[4] + this.m[2] * matrix.m[5] + this.m[4];
            var dy = this.m[1] * matrix.m[4] + this.m[3] * matrix.m[5] + this.m[5];

            this.m[0] = m11;
            this.m[1] = m12;
            this.m[2] = m21;
            this.m[3] = m22;
            this.m[4] = dx;
            this.m[5] = dy;
        },
        /**
         * Invert the matrix
         */
        invert: function() {
            var d = 1 / (this.m[0] * this.m[3] - this.m[1] * this.m[2]);
            var m0 = this.m[3] * d;
            var m1 = -this.m[1] * d;
            var m2 = -this.m[2] * d;
            var m3 = this.m[0] * d;
            var m4 = d * (this.m[2] * this.m[5] - this.m[3] * this.m[4]);
            var m5 = d * (this.m[1] * this.m[4] - this.m[0] * this.m[5]);
            this.m[0] = m0;
            this.m[1] = m1;
            this.m[2] = m2;
            this.m[3] = m3;
            this.m[4] = m4;
            this.m[5] = m5;
        },
        /**
         * return matrix
         */
        getMatrix: function() {
            return this.m;
        }
    };
})();

(function() {
    /**
     * Collection constructor.  Collection extends
     *  Array.  This class is used in conjunction with get()
     * @constructor
     */
    Kinetic.Collection = function() {
        var args = [].slice.call(arguments), length = args.length, i = 0;

        this.length = length;
        for(; i < length; i++) {
            this[i] = args[i];
        }
        return this;
    }
    Kinetic.Collection.prototype = new Array();
    /**
     * apply a method to all nodes in the array
     * @name apply
     * @methodOf Kinetic.Collection.prototype
     * @param {String} method
     * @param val
     */
    Kinetic.Collection.prototype.apply = function(method) {
        args = [].slice.call(arguments);
        args.shift();
        for(var n = 0; n < this.length; n++) {
            if(Kinetic.Type._isFunction(this[n][method])) {
                this[n][method].apply(this[n], args);
            }
        }
    };
    /**
     * iterate through node array
     * @name each
     * @methodOf Kinetic.Collection.prototype
     * @param {Function} func
     */
    Kinetic.Collection.prototype.each = function(func) {
        for(var n = 0; n < this.length; n++) {
            func.call(this[n], n, this[n]);
        }
    };
})();

(function() {
    /**
     * Grayscale Filter
     * @function
     * @memberOf Kinetic.Filters
     * @param {Object} imageData
     * @param {Object} config
     */
    Kinetic.Filters.Grayscale = function(imageData, config) {
        var data = imageData.data;
        for(var i = 0; i < data.length; i += 4) {
            var brightness = 0.34 * data[i] + 0.5 * data[i + 1] + 0.16 * data[i + 2];
            // red
            data[i] = brightness;
            // green
            data[i + 1] = brightness;
            // blue
            data[i + 2] = brightness;
        }
    };
})();

(function() {
    /**
     * Brighten Filter
     * @function
     * @memberOf Kinetic.Filters
     * @param {Object} imageData
     * @param {Object} config
     * @param {Integer} config.val brightness number from -255 to 255.&nbsp; Positive values increase the brightness and negative values decrease the brightness, making the image darker
     */
    Kinetic.Filters.Brighten = function(imageData, config) {
        var brightness = config.val || 0;
        var data = imageData.data;
        for(var i = 0; i < data.length; i += 4) {
            // red
            data[i] += brightness;
            // green
            data[i + 1] += brightness;
            // blue
            data[i + 2] += brightness;
        }
    };
})();

(function() {
    /**
     * Invert Filter
     * @function
     * @memberOf Kinetic.Filters
     * @param {Object} imageData
     * @param {Object} config
     */
    Kinetic.Filters.Invert = function(imageData, config) {
        var data = imageData.data;
        for(var i = 0; i < data.length; i += 4) {
            // red
            data[i] = 255 - data[i];
            // green
            data[i + 1] = 255 - data[i + 1];
            // blue
            data[i + 2] = 255 - data[i + 2];
        }
    };
})();

(function() {
    /**
     * Stage constructor.  A stage is used to contain multiple layers and handle
     * animations
     * @constructor
     * @param {Function} func function executed on each animation frame
     * @param {Kinetic.Node} [node] node to be redrawn.&nbsp; Can be a layer or the stage.  Not specifying a node will result in no redraw.
     */
    Kinetic.Animation = function(func, node) {
        this.func = func;
        this.node = node;
        this.id = Kinetic.Animation.animIdCounter++;
        this.frame = {
            time: 0,
            timeDiff: 0,
            lastTime: new Date().getTime()
        };
    };
    /*
     * Animation methods
     */
    Kinetic.Animation.prototype = {
        /**
         * determine if animation is running.  returns true or false
         * @name isRunning
         * @methodOf Kinetic.Aniamtion.prototype
         */
        isRunning: function() {
            var a = Kinetic.Animation, animations = a.animations;
            for(var n = 0; n < animations.length; n++) {
                if(animations[n].id === this.id) {
                    return true;
                }
            }
            return false;
        },
        /**
         * start animation
         * @name start
         * @methodOf Kinetic.Animation.prototype
         */
        start: function() {
            this.stop();
            this.frame.timeDiff = 0;
            this.frame.lastTime = new Date().getTime();
            Kinetic.Animation._addAnimation(this);
        },
        /**
         * stop animation
         * @name stop
         * @methodOf Kinetic.Animation.prototype
         */
        stop: function() {
            Kinetic.Animation._removeAnimation(this);
        },
        _updateFrameObject: function(time) {
            this.frame.timeDiff = time - this.frame.lastTime;
            this.frame.lastTime = time;
            this.frame.time += this.frame.timeDiff;
            this.frame.frameRate = 1000 / this.frame.timeDiff;
        }
    };
    Kinetic.Animation.animations = [];
    Kinetic.Animation.animIdCounter = 0;
    Kinetic.Animation.animRunning = false;

    Kinetic.Animation.fixedRequestAnimFrame = function(callback) {
        window.setTimeout(callback, 1000 / 60);
    };

    Kinetic.Animation._addAnimation = function(anim) {
        this.animations.push(anim);
        this._handleAnimation();
    };
    Kinetic.Animation._removeAnimation = function(anim) {
        var id = anim.id, animations = this.animations, len = animations.length;
        for(var n = 0; n < len; n++) {
            if(animations[n].id === id) {
                this.animations.splice(n, 1);
                break;
            }
        }
    };

    Kinetic.Animation._runFrames = function() {
        var nodes = {}, animations = this.animations;
        /*
         * loop through all animations and execute animation
         *  function.  if the animation object has specified node,
         *  we can add the node to the nodes hash to eliminate
         *  drawing the same node multiple times.  The node property
         *  can be the stage itself or a layer
         */
        /*
         * WARNING: don't cache animations.length because it could change while
         * the for loop is running, causing a JS error
         */
        for(var n = 0; n < animations.length; n++) {
            var anim = animations[n], node = anim.node, func = anim.func;
            anim._updateFrameObject(new Date().getTime());
            if(node && node._id !== undefined) {
                nodes[node._id] = node;
            }
            // if animation object has a function, execute it
            if(func) {
                func(anim.frame);
            }
        }

        for(var key in nodes) {
            nodes[key].draw();
        }
    };
    Kinetic.Animation._animationLoop = function() {
        var that = this;
        if(this.animations.length > 0) {
            this._runFrames();
            Kinetic.Animation.requestAnimFrame(function() {
                that._animationLoop();
            });
        }
        else {
            this.animRunning = false;
        }
    };
    Kinetic.Animation._handleAnimation = function() {
        var that = this;
        if(!this.animRunning) {
            this.animRunning = true;
            that._animationLoop();
        }
    };
    RAF = (function() {
        return window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || window.oRequestAnimationFrame || window.msRequestAnimationFrame || Kinetic.Animation.fixedRequestAnimFrame;
    })();

    Kinetic.Animation.requestAnimFrame = function(callback) {
        var raf = Kinetic.DD && Kinetic.DD.moving ? this.fixedRequestAnimFrame : RAF;
        raf(callback);
    };
})();

(function() {
    /**
     * Node constructor. Nodes are entities that can be transformed, layered,
     * and have bound events. The stage, layers, groups, and shapes all extend Node.
     * @constructor
     * @param {Object} config
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Node = function(config) {
        this._nodeInit(config);
    };

    Kinetic.Node.prototype = {
        _nodeInit: function(config) {
            this._id = Kinetic.Global.idCounter++;

            this.defaultNodeAttrs = {
                visible: true,
                listening: true,
                name: undefined,
                opacity: 1,
                x: 0,
                y: 0,
                scale: {
                    x: 1,
                    y: 1
                },
                rotation: 0,
                offset: {
                    x: 0,
                    y: 0
                },
                draggable: false,
                dragOnTop: true
            };

            this.setDefaultAttrs(this.defaultNodeAttrs);
            this.eventListeners = {};
            this.setAttrs(config);
        },
        /**
         * bind events to the node. KineticJS supports mouseover, mousemove,
         *  mouseout, mouseenter, mouseleave, mousedown, mouseup, click, dblclick, touchstart, touchmove,
         *  touchend, tap, dbltap, dragstart, dragmove, and dragend events. Pass in a string
         *  of events delimmited by a space to bind multiple events at once
         *  such as 'mousedown mouseup mousemove'. Include a namespace to bind an
         *  event by name such as 'click.foobar'.
         * @name on
         * @methodOf Kinetic.Node.prototype
         * @param {String} typesStr e.g. 'click', 'mousedown touchstart', 'mousedown.foo touchstart.foo'
         * @param {Function} handler The handler function is passed an event object
         */
        on: function(typesStr, handler) {
            var types = typesStr.split(' ');
            /*
             * loop through types and attach event listeners to
             * each one.  eg. 'click mouseover.namespace mouseout'
             * will create three event bindings
             */
            var len = types.length;
            for(var n = 0; n < len; n++) {
                var type = types[n];
                var event = type;
                var parts = event.split('.');
                var baseEvent = parts[0];
                var name = parts.length > 1 ? parts[1] : '';

                if(!this.eventListeners[baseEvent]) {
                    this.eventListeners[baseEvent] = [];
                }

                this.eventListeners[baseEvent].push({
                    name: name,
                    handler: handler
                });
            }
        },
        /**
         * remove event bindings from the node. Pass in a string of
         *  event types delimmited by a space to remove multiple event
         *  bindings at once such as 'mousedown mouseup mousemove'.
         *  include a namespace to remove an event binding by name
         *  such as 'click.foobar'. If you only give a name like '.foobar',
         *  all events in that namespace will be removed.
         * @name off
         * @methodOf Kinetic.Node.prototype
         * @param {String} typesStr e.g. 'click', 'mousedown touchstart', '.foobar'
         */
        off: function(typesStr) {
            var types = typesStr.split(' ');
            var len = types.length;
            for(var n = 0; n < len; n++) {
                var type = types[n];
                //var event = (type.indexOf('touch') === -1) ? 'on' + type : type;
                var event = type;
                var parts = event.split('.');
                var baseEvent = parts[0];

                if(parts.length > 1) {
                    if(baseEvent) {
                        if(this.eventListeners[baseEvent]) {
                            this._off(baseEvent, parts[1]);
                        }
                    }
                    else {
                        for(var type in this.eventListeners) {
                            this._off(type, parts[1]);
                        }
                    }
                }
                else {
                    delete this.eventListeners[baseEvent];
                }
            }
        },
        /**
         * remove child from container, but don't destroy it
         * @name remove
         * @methodOf Kinetic.Node.prototype
         */
        remove: function() {
            var parent = this.getParent();
            if(parent && parent.children) {
                parent.children.splice(this.index, 1);
                parent._setChildrenIndices();
            }
            delete this.parent;
        },
        /**
         * remove and destroy node
         * @name destroy
         * @methodOf Kinetic.Node.prototype
         */
        destroy: function() {
            var parent = this.getParent(), stage = this.getStage(), dd = Kinetic.DD, go = Kinetic.Global;

            // destroy children
            while(this.children && this.children.length > 0) {
                this.children[0].destroy();
            }

            // remove from ids and names hashes
            go._removeId(this.getId());
            go._removeName(this.getName(), this._id);

            // stop DD
            if(dd && dd.node && dd.node._id === this._id) {
                node._endDrag();
            }

            // stop transition
            if(this.trans) {
                this.trans.stop();
            }

            this.remove();
        },
        /**
         * get attrs
         * @name getAttrs
         * @methodOf Kinetic.Node.prototype
         */
        getAttrs: function() {
            return this.attrs;
        },
        /**
         * set default attrs.  This method should only be used if
         *  you're creating a custom node
         * @name setDefaultAttrs
         * @methodOf Kinetic.Node.prototype
         * @param {Object} confic
         */
        setDefaultAttrs: function(config) {
            // create attrs object if undefined
            if(this.attrs === undefined) {
                this.attrs = {};
            }

            if(config) {
                for(var key in config) {
                    /*
                     * only set the attr if it's undefined in case
                     * a developer writes a custom class that extends
                     * a Kinetic Class such that their default property
                     * isn't overwritten by the Kinetic Class default
                     * property
                     */
                    if(this.attrs[key] === undefined) {
                        this.attrs[key] = config[key];
                    }
                }
            }
        },
        /**
         * set attrs
         * @name setAttrs
         * @methodOf Kinetic.Node.prototype
         * @param {Object} config object containing key value pairs
         */
        setAttrs: function(config) {
            if(config) {
                for(var key in config) {
                    var method = 'set' + key.charAt(0).toUpperCase() + key.slice(1);
                    // use setter if available
                    if(Kinetic.Type._isFunction(this[method])) {
                        this[method](config[key]);
                    }
                    // otherwise set directly
                    else {
                        this.setAttr(key, config[key]);
                    }
                }
            }
        },
        /**
         * determine if node is visible or not.  Node is visible only
         *  if it's visible and all of its ancestors are visible.  If an ancestor
         *  is invisible, this means that the node is also invisible
         * @name getVisible
         * @methodOf Kinetic.Node.prototype
         */
        getVisible: function() {
            var visible = this.attrs.visible, parent = this.getParent();
            if(visible && parent && !parent.getVisible()) {
                return false;
            }
            return visible;
        },
        /**
         * determine if node is listening or not.  Node is listening only
         *  if it's listening and all of its ancestors are listening.  If an ancestor
         *  is not listening, this means that the node is also not listening
         * @name getListening
         * @methodOf Kinetic.Node.prototype
         */
        getListening: function() {
            var listening = this.attrs.listening, parent = this.getParent();
            if(listening && parent && !parent.getListening()) {
                return false;
            }
            return listening;
        },
        /**
         * show node
         * @name show
         * @methodOf Kinetic.Node.prototype
         */
        show: function() {
            this.setVisible(true);
        },
        /**
         * hide node.  Hidden nodes are no longer detectable
         * @name hide
         * @methodOf Kinetic.Node.prototype
         */
        hide: function() {
            this.setVisible(false);
        },
        /**
         * get zIndex relative to the node's siblings who share the same parent
         * @name getZIndex
         * @methodOf Kinetic.Node.prototype
         */
        getZIndex: function() {
            return this.index;
        },
        /**
         * get absolute z-index which takes into account sibling
         *  and ancestor indices
         * @name getAbsoluteZIndex
         * @methodOf Kinetic.Node.prototype
         */
        getAbsoluteZIndex: function() {
            var level = this.getLevel();
            var stage = this.getStage();
            var that = this;
            var index = 0;
            function addChildren(children) {
                var nodes = [];
                var len = children.length;
                for(var n = 0; n < len; n++) {
                    var child = children[n];
                    index++;

                    if(child.nodeType !== 'Shape') {
                        nodes = nodes.concat(child.getChildren());
                    }

                    if(child._id === that._id) {
                        n = len;
                    }
                }

                if(nodes.length > 0 && nodes[0].getLevel() <= level) {
                    addChildren(nodes);
                }
            }
            if(that.nodeType !== 'Stage') {
                addChildren(that.getStage().getChildren());
            }

            return index;
        },
        /**
         * get node level in node tree.  Returns an integer.<br><br>
         *  e.g. Stage level will always be 0.  Layers will always be 1.  Groups and Shapes will always
         *  be >= 2
         * @name getLevel
         * @methodOf Kinetic.Node.prototype
         */
        getLevel: function() {
            var level = 0;
            var parent = this.parent;
            while(parent) {
                level++;
                parent = parent.parent;
            }
            return level;
        },
        /**
         * set node position relative to parent
         * @name setPosition
         * @methodOf Kinetic.Node.prototype
         * @param {Number} x
         * @param {Number} y
         */
        setPosition: function() {
            var pos = Kinetic.Type._getXY([].slice.call(arguments));
            this.setAttr('x', pos.x);
            this.setAttr('y', pos.y);
        },
        /**
         * get node position relative to parent
         * @name getPosition
         * @methodOf Kinetic.Node.prototype
         */
        getPosition: function() {
            var attrs = this.attrs;
            return {
                x: attrs.x,
                y: attrs.y
            };
        },
        /**
         * get absolute position relative to the top left corner of the stage container div
         * @name getAbsolutePosition
         * @methodOf Kinetic.Node.prototype
         */
        getAbsolutePosition: function() {
            var trans = this.getAbsoluteTransform();
            var o = this.getOffset();
            trans.translate(o.x, o.y);
            return trans.getTranslation();
        },
        /**
         * set absolute position
         * @name setAbsolutePosition
         * @methodOf Kinetic.Node.prototype
         * @param {Number} x
         * @param {Number} y
         */
        setAbsolutePosition: function() {
            var pos = Kinetic.Type._getXY([].slice.call(arguments));
            var trans = this._clearTransform();
            // don't clear translation
            this.attrs.x = trans.x;
            this.attrs.y = trans.y;
            delete trans.x;
            delete trans.y;

            // unravel transform
            var it = this.getAbsoluteTransform();

            it.invert();
            it.translate(pos.x, pos.y);
            pos = {
                x: this.attrs.x + it.getTranslation().x,
                y: this.attrs.y + it.getTranslation().y
            };

            this.setPosition(pos.x, pos.y);
            this._setTransform(trans);
        },
        /**
         * move node by an amount relative to its current position
         * @name move
         * @methodOf Kinetic.Node.prototype
         * @param {Number} x
         * @param {Number} y
         */
        move: function() {
            var pos = Kinetic.Type._getXY([].slice.call(arguments));
            var x = this.getX();
            var y = this.getY();

            if(pos.x !== undefined) {
                x += pos.x;
            }

            if(pos.y !== undefined) {
                y += pos.y;
            }

            this.setPosition(x, y);
        },
        _eachAncestorReverse: function(func, includeSelf) {
            var family = [], parent = this.getParent();

            // build family by traversing ancestors
            if(includeSelf) {
                family.unshift(this);
            }
            while(parent) {
                family.unshift(parent);
                parent = parent.parent;
            }

            var len = family.length;
            for(var n = 0; n < len; n++) {
                func(family[n]);
            }
        },
        /**
         * rotate node by an amount in radians relative to its current rotation
         * @name rotate
         * @methodOf Kinetic.Node.prototype
         * @param {Number} theta
         */
        rotate: function(theta) {
            this.setRotation(this.getRotation() + theta);
        },
        /**
         * rotate node by an amount in degrees relative to its current rotation
         * @name rotateDeg
         * @methodOf Kinetic.Node.prototype
         * @param {Number} deg
         */
        rotateDeg: function(deg) {
            this.setRotation(this.getRotation() + Kinetic.Type._degToRad(deg));
        },
        /**
         * move node to the top of its siblings
         * @name moveToTop
         * @methodOf Kinetic.Node.prototype
         */
        moveToTop: function() {
            var index = this.index;
            this.parent.children.splice(index, 1);
            this.parent.children.push(this);
            this.parent._setChildrenIndices();
            return true;
        },
        /**
         * move node up
         * @name moveUp
         * @methodOf Kinetic.Node.prototype
         */
        moveUp: function() {
            var index = this.index;
            var len = this.parent.getChildren().length;
            if(index < len - 1) {
                this.parent.children.splice(index, 1);
                this.parent.children.splice(index + 1, 0, this);
                this.parent._setChildrenIndices();
                return true;
            }
        },
        /**
         * move node down
         * @name moveDown
         * @methodOf Kinetic.Node.prototype
         */
        moveDown: function() {
            var index = this.index;
            if(index > 0) {
                this.parent.children.splice(index, 1);
                this.parent.children.splice(index - 1, 0, this);
                this.parent._setChildrenIndices();
                return true;
            }
        },
        /**
         * move node to the bottom of its siblings
         * @name moveToBottom
         * @methodOf Kinetic.Node.prototype
         */
        moveToBottom: function() {
            var index = this.index;
            if(index > 0) {
                this.parent.children.splice(index, 1);
                this.parent.children.unshift(this);
                this.parent._setChildrenIndices();
                return true;
            }
        },
        /**
         * set zIndex relative to siblings
         * @name setZIndex
         * @methodOf Kinetic.Node.prototype
         * @param {Integer} zIndex
         */
        setZIndex: function(zIndex) {
            var index = this.index;
            this.parent.children.splice(index, 1);
            this.parent.children.splice(zIndex, 0, this);
            this.parent._setChildrenIndices();
        },
        /**
         * get absolute opacity
         * @name getAbsoluteOpacity
         * @methodOf Kinetic.Node.prototype
         */
        getAbsoluteOpacity: function() {
            var absOpacity = this.getOpacity();
            if(this.getParent()) {
                absOpacity *= this.getParent().getAbsoluteOpacity();
            }
            return absOpacity;
        },
        /**
         * move node to another container
         * @name moveTo
         * @methodOf Kinetic.Node.prototype
         * @param {Container} newContainer
         */
        moveTo: function(newContainer) {
            Kinetic.Node.prototype.remove.call(this);
            newContainer.add(this);
        },
        /**
         * convert Node into an object for serialization.  Returns an object.
         * @name toObject
         * @methodOf Kinetic.Node.prototype
         */
        toObject: function() {
            var type = Kinetic.Type, obj = {}, attrs = this.attrs;

            obj.attrs = {};

            // serialize only attributes that are not function, image, DOM, or objects with methods
            for(var key in attrs) {
                var val = attrs[key];
                if(!type._isFunction(val) && !type._isElement(val) && !(type._isObject(val) && type._hasMethods(val))) {
                    obj.attrs[key] = val;
                }
            }

            obj.nodeType = this.nodeType;
            obj.shapeType = this.shapeType;

            return obj;
        },
        /**
         * convert Node into a JSON string.  Returns a JSON string.
         * @name toJSON
         * @methodOf Kinetic.Node.prototype
         */
        toJSON: function() {
            return JSON.stringify(this.toObject());
        },
        /**
         * get parent container
         * @name getParent
         * @methodOf Kinetic.Node.prototype
         */
        getParent: function() {
            return this.parent;
        },
        /**
         * get layer ancestor
         * @name getLayer
         * @methodOf Kinetic.Node.prototype
         */
        getLayer: function() {
            return this.getParent().getLayer();
        },
        /**
         * get stage ancestor
         * @name getStage
         * @methodOf Kinetic.Node.prototype
         */
        getStage: function() {
            if(this.getParent()) {
                return this.getParent().getStage();
            }
            else {
                return undefined;
            }
        },
        /**
         * simulate event with event bubbling
         * @name simulate
         * @methodOf Kinetic.Node.prototype
         * @param {String} eventType
         * @param {EventObject} evt event object
         */
        simulate: function(eventType, evt) {
            this._handleEvent(eventType, evt || {});
        },
        /**
         * synthetically fire an event. The event object will not bubble up the Node tree. You can also pass in custom properties
         * @name fire
         * @methodOf Kinetic.Node.prototype
         * @param {String} eventType
         * @param {Object} obj optional object which can be used to pass parameters
         */
        fire: function(eventType, obj) {
            this._executeHandlers(eventType, obj || {});
        },
        /**
         * get absolute transform of the node which takes into
         *  account its ancestor transforms
         * @name getAbsoluteTransform
         * @methodOf Kinetic.Node.prototype
         */
        getAbsoluteTransform: function() {
            // absolute transform
            var am = new Kinetic.Transform();

            this._eachAncestorReverse(function(node) {
                var m = node.getTransform();
                am.multiply(m);
            }, true);
            return am;
        },
        /**
         * get transform of the node
         * @name getTransform
         * @methodOf Kinetic.Node.prototype
         */
        getTransform: function() {
            var m = new Kinetic.Transform(), attrs = this.attrs, x = attrs.x, y = attrs.y, rotation = attrs.rotation, scale = attrs.scale, scaleX = scale.x, scaleY = scale.y, offset = attrs.offset, offsetX = offset.x, offsetY = offset.y;

            if(x !== 0 || y !== 0) {
                m.translate(x, y);
            }
            if(rotation !== 0) {
                m.rotate(rotation);
            }
            if(scaleX !== 1 || scaleY !== 1) {
                m.scale(scaleX, scaleY);
            }
            if(offsetX !== 0 || offsetY !== 0) {
                m.translate(-1 * offsetX, -1 * offsetY);
            }

            return m;
        },
        /**
         * clone node.  Returns a new Node instance with identical attributes
         * @name clone
         * @methodOf Kinetic.Node.prototype
         * @param {Object} attrs override attrs
         */
        clone: function(obj) {
            // instantiate new node
            var classType = this.shapeType || this.nodeType;
            var node = new Kinetic[classType](this.attrs);

            /*
             * copy over user listeners
             */
            for(var key in this.eventListeners) {
                var allListeners = this.eventListeners[key];
                var len = allListeners.length;
                for(var n = 0; n < len; n++) {
                    var listener = allListeners[n];
                    /*
                     * don't include kinetic namespaced listeners because
                     *  these are generated by the constructors
                     */
                    if(listener.name.indexOf('kinetic') < 0) {
                        // if listeners array doesn't exist, then create it
                        if(!node.eventListeners[key]) {
                            node.eventListeners[key] = [];
                        }
                        node.eventListeners[key].push(listener);
                    }
                }
            }

            // apply attr overrides
            node.setAttrs(obj);
            return node;
        },
        /**
         * Creates a composite data URL. If MIME type is not
         * specified, then "image/png" will result. For "image/jpeg", specify a quality
         * level as quality (range 0.0 - 1.0)
         * @name toDataURL
         * @methodOf Kinetic.Node.prototype
         * @param {Object} config
         * @param {Function} config.callback function executed when the composite has completed
         * @param {String} [config.mimeType] can be "image/png" or "image/jpeg".
         *  "image/png" is the default
         * @param {Number} [config.x] x position of canvas section
         * @param {Number} [config.y] y position of canvas section
         * @param {Number} [config.width] width of canvas section
         * @param {Number} [config.height] height of canvas section
         * @param {Number} [config.quality] jpeg quality.  If using an "image/jpeg" mimeType,
         *  you can specify the quality from 0 to 1, where 0 is very poor quality and 1
         *  is very high quality
         */
        toDataURL: function(config) {
            config = config || {};
            var mimeType = config.mimeType || null, quality = config.quality || null, canvas, context, x = config.x || 0, y = config.y || 0;

            //if width and height are defined, create new canvas to draw on, else reuse stage buffer canvas
            if(config.width && config.height) {
                canvas = new Kinetic.SceneCanvas(config.width, config.height);
            }
            else {
                canvas = this.getStage().bufferCanvas;
                canvas.clear();
            }
            context = canvas.getContext();
            context.save();
            canvas._counterPixelRatio();

            if(x || y) {
                context.translate(-1 * x, -1 * y);
            }
            this.drawScene(canvas);
            context.restore();

            return canvas.toDataURL(mimeType, quality);
        },
        /**
         * converts node into an image.  Since the toImage
         *  method is asynchronous, a callback is required.  toImage is most commonly used
         *  to cache complex drawings as an image so that they don't have to constantly be redrawn
         * @name toImage
         * @methodOf Kinetic.Node.prototype
         * @param {Object} config
         * @param {Function} config.callback function executed when the composite has completed
         * @param {String} [config.mimeType] can be "image/png" or "image/jpeg".
         *  "image/png" is the default
         * @param {Number} [config.x] x position of canvas section
         * @param {Number} [config.y] y position of canvas section
         * @param {Number} [config.width] width of canvas section
         * @param {Number} [config.height] height of canvas section
         * @param {Number} [config.quality] jpeg quality.  If using an "image/jpeg" mimeType,
         *  you can specify the quality from 0 to 1, where 0 is very poor quality and 1
         *  is very high quality
         */
        toImage: function(config) {
            Kinetic.Type._getImage(this.toDataURL(config), function(img) {
                config.callback(img);
            });
        },
        /**
         * set size
         * @name setSize
         * @methodOf Kinetic.Node.prototype
         * @param {Number} width
         * @param {Number} height
         */
        setSize: function() {
            // set stage dimensions
            var size = Kinetic.Type._getSize(Array.prototype.slice.call(arguments));
            this.setWidth(size.width);
            this.setHeight(size.height);
        },
        /**
         * get size
         * @name getSize
         * @methodOf Kinetic.Node.prototype
         */
        getSize: function() {
            return {
                width: this.getWidth(),
                height: this.getHeight()
            };
        },
        /**
         * get width
         * @name getWidth
         * @methodOf Kinetic.Node.prototype
         */
        getWidth: function() {
            return this.attrs.width || 0;
        },
        /**
         * get height
         * @name getHeight
         * @methodOf Kinetic.Node.prototype
         */
        getHeight: function() {
            return this.attrs.height || 0;
        },
        _get: function(selector) {
            return this.nodeType === selector ? [this] : [];
        },
        _off: function(type, name) {
            for(var i = 0; i < this.eventListeners[type].length; i++) {
                if(this.eventListeners[type][i].name === name) {
                    this.eventListeners[type].splice(i, 1);
                    if(this.eventListeners[type].length === 0) {
                        delete this.eventListeners[type];
                        break;
                    }
                    i--;
                }
            }
        },
        _clearTransform: function() {
            var attrs = this.attrs, scale = attrs.scale, offset = attrs.offset;
            var trans = {
                x: attrs.x,
                y: attrs.y,
                rotation: attrs.rotation,
                scale: {
                    x: scale.x,
                    y: scale.y
                },
                offset: {
                    x: offset.x,
                    y: offset.y
                }
            };

            this.attrs.x = 0;
            this.attrs.y = 0;
            this.attrs.rotation = 0;
            this.attrs.scale = {
                x: 1,
                y: 1
            };
            this.attrs.offset = {
                x: 0,
                y: 0
            };

            return trans;
        },
        _setTransform: function(trans) {
            for(var key in trans) {
                this.attrs[key] = trans[key];
            }
        },
        _fireBeforeChangeEvent: function(attr, oldVal, newVal) {
            this._handleEvent('before' + attr.toUpperCase() + 'Change', {
                oldVal: oldVal,
                newVal: newVal
            });
        },
        _fireChangeEvent: function(attr, oldVal, newVal) {
            this._handleEvent(attr + 'Change', {
                oldVal: oldVal,
                newVal: newVal
            });
        },
        /**
         * set id
         * @name setId
         * @methodOf Kinetic.Node.prototype
         * @param {String} id
         */
        setId: function(id) {
            var oldId = this.getId(), stage = this.getStage(), go = Kinetic.Global;
            go._removeId(oldId);
            go._addId(this, id);
            this.setAttr('id', id);
        },
        /**
         * set name
         * @name setName
         * @methodOf Kinetic.Node.prototype
         * @param {String} name
         */
        setName: function(name) {
            var oldName = this.getName(), stage = this.getStage(), go = Kinetic.Global;
            go._removeName(oldName, this._id);
            go._addName(this, name);
            this.setAttr('name', name);
        },
        setAttr: function(key, val) {
            if(val !== undefined) {
                var oldVal = this.attrs[key];
                this._fireBeforeChangeEvent(key, oldVal, val);
                this.attrs[key] = val;
                this._fireChangeEvent(key, oldVal, val);
            }
        },
        _handleEvent: function(eventType, evt, compareShape) {
            if(evt && this.nodeType === 'Shape') {
                evt.shape = this;
            }
            var stage = this.getStage();
            var el = this.eventListeners;
            var okayToRun = true;

            if(eventType === 'mouseenter' && compareShape && this._id === compareShape._id) {
                okayToRun = false;
            }
            else if(eventType === 'mouseleave' && compareShape && this._id === compareShape._id) {
                okayToRun = false;
            }

            if(okayToRun) {
                if(el[eventType]) {
                    this.fire(eventType, evt);
                }

                // simulate event bubbling
                if(evt && !evt.cancelBubble && this.parent) {
                    if(compareShape && compareShape.parent) {
                        this._handleEvent.call(this.parent, eventType, evt, compareShape.parent);
                    }
                    else {
                        this._handleEvent.call(this.parent, eventType, evt);
                    }
                }
            }
        },
        _executeHandlers: function(eventType, evt) {
            var events = this.eventListeners[eventType];
            var len = events.length;
            for(var i = 0; i < len; i++) {
                events[i].handler.apply(this, [evt]);
            }
        }
    };

    // add getter and setter methods
    Kinetic.Node.addSetters = function(constructor, arr) {
        var len = arr.length;
        for(var n = 0; n < len; n++) {
            var attr = arr[n];
            this._addSetter(constructor, attr);
        }
    };
    Kinetic.Node.addPointSetters = function(constructor, arr) {
        var len = arr.length;
        for(var n = 0; n < len; n++) {
            var attr = arr[n];
            this._addPointSetter(constructor, attr);
        }
    };
    Kinetic.Node.addRotationSetters = function(constructor, arr) {
        var len = arr.length;
        for(var n = 0; n < len; n++) {
            var attr = arr[n];
            this._addRotationSetter(constructor, attr);
        }
    };
    Kinetic.Node.addGetters = function(constructor, arr) {
        var len = arr.length;
        for(var n = 0; n < len; n++) {
            var attr = arr[n];
            this._addGetter(constructor, attr);
        }
    };
    Kinetic.Node.addRotationGetters = function(constructor, arr) {
        var len = arr.length;
        for(var n = 0; n < len; n++) {
            var attr = arr[n];
            this._addRotationGetter(constructor, attr);
        }
    };
    Kinetic.Node.addGettersSetters = function(constructor, arr) {
        this.addSetters(constructor, arr);
        this.addGetters(constructor, arr);
    };
    Kinetic.Node.addPointGettersSetters = function(constructor, arr) {
        this.addPointSetters(constructor, arr);
        this.addGetters(constructor, arr);
    };
    Kinetic.Node.addRotationGettersSetters = function(constructor, arr) {
        this.addRotationSetters(constructor, arr);
        this.addRotationGetters(constructor, arr);
    };
    Kinetic.Node._addSetter = function(constructor, attr) {
        var that = this;
        var method = 'set' + attr.charAt(0).toUpperCase() + attr.slice(1);
        constructor.prototype[method] = function(val) {
            this.setAttr(attr, val);
        };
    };
    Kinetic.Node._addPointSetter = function(constructor, attr) {
        var that = this;
        var method = 'set' + attr.charAt(0).toUpperCase() + attr.slice(1);
        constructor.prototype[method] = function() {
            var pos = Kinetic.Type._getXY([].slice.call(arguments));
            if(pos && pos.x === undefined) {
                pos.x = this.attrs[attr].x;
            }
            if(pos && pos.y === undefined) {
                pos.y = this.attrs[attr].y;
            }
            this.setAttr(attr, pos);
        };
    };
    Kinetic.Node._addRotationSetter = function(constructor, attr) {
        var that = this;
        var method = 'set' + attr.charAt(0).toUpperCase() + attr.slice(1);
        // radians
        constructor.prototype[method] = function(val) {
            this.setAttr(attr, val);
        };
        // degrees
        constructor.prototype[method + 'Deg'] = function(deg) {
            this.setAttr(attr, Kinetic.Type._degToRad(deg));
        };
    };
    Kinetic.Node._addGetter = function(constructor, attr) {
        var that = this;
        var method = 'get' + attr.charAt(0).toUpperCase() + attr.slice(1);
        constructor.prototype[method] = function(arg) {
            return this.attrs[attr];
        };
    };
    Kinetic.Node._addRotationGetter = function(constructor, attr) {
        var that = this;
        var method = 'get' + attr.charAt(0).toUpperCase() + attr.slice(1);
        // radians
        constructor.prototype[method] = function() {
            return this.attrs[attr];
        };
        // degrees
        constructor.prototype[method + 'Deg'] = function() {
            return Kinetic.Type._radToDeg(this.attrs[attr])
        };
    };
    /**
     * create node with JSON string.  De-serializtion does not generate custom
     *  shape drawing functions, images, or event handlers (this would make the
     * 	serialized object huge).  If your app uses custom shapes, images, and
     *  event handlers (it probably does), then you need to select the appropriate
     *  shapes after loading the stage and set these properties via on(), setDrawFunc(),
     *  and setImage() methods
     * @name create
     * @methodOf Kinetic.Node
     * @param {String} JSON string
     * @param {DomElement} [container] optional container dom element used only if you're
     *  creating a stage node
     */
    Kinetic.Node.create = function(json, container) {
        return this._createNode(JSON.parse(json), container);
    };
    Kinetic.Node._createNode = function(obj, container) {
        var type;

        // determine type
        if(obj.nodeType === 'Shape') {
            // add custom shape
            if(obj.shapeType === undefined) {
                type = 'Shape';
            }
            // add standard shape
            else {
                type = obj.shapeType;
            }
        }
        else {
            type = obj.nodeType;
        }

        // if container was passed in, add it to attrs
        if(container) {
            obj.attrs.container = container;
        }

        var no = new Kinetic[type](obj.attrs);
        if(obj.children) {
            var len = obj.children.length;
            for(var n = 0; n < len; n++) {
                no.add(this._createNode(obj.children[n]));
            }
        }

        return no;
    };
    // add getters setters
    Kinetic.Node.addGettersSetters(Kinetic.Node, ['x', 'y', 'opacity']);

    /**
     * set x position
     * @name setX
     * @methodOf Kinetic.Node.prototype
     * @param {Number} x
     */

    /**
     * set y position
     * @name setY
     * @methodOf Kinetic.Node.prototype
     * @param {Number} y
     */

    /**
     * set opacity.  Opacity values range from 0 to 1.
     *  A node with an opacity of 0 is fully transparent, and a node
     *  with an opacity of 1 is fully opaque
     * @name setOpacity
     * @methodOf Kinetic.Node.prototype
     * @param {Object} opacity
     */

    /**
     * get x position
     * @name getX
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * get y position
     * @name getY
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * get opacity.
     * @name getOpacity
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addGetters(Kinetic.Node, ['name', 'id']);

    /**
     * get name
     * @name getName
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * get id
     * @name getId
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addRotationGettersSetters(Kinetic.Node, ['rotation']);

    /**
     * set rotation in radians
     * @name setRotation
     * @methodOf Kinetic.Node.prototype
     * @param {Number} theta
     */

    /**
     * set rotation in degrees
     * @name setRotationDeg
     * @methodOf Kinetic.Node.prototype
     * @param {Number} deg
     */

    /**
     * get rotation in degrees
     * @name getRotationDeg
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * get rotation in radians
     * @name getRotation
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addPointGettersSetters(Kinetic.Node, ['scale', 'offset']);

    /**
     * set scale
     * @name setScale
     * @param {Number} x
     * @param {Number} y
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * set offset.  A node's offset defines the position and rotation point
     * @name setOffset
     * @methodOf Kinetic.Node.prototype
     * @param {Number} x
     * @param {Number} y
     */

    /**
     * get scale
     * @name getScale
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * get offset
     * @name getOffset
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addSetters(Kinetic.Node, ['width', 'height', 'listening', 'visible']);

    /**
     * set width
     * @name setWidth
     * @methodOf Kinetic.Node.prototype
     * @param {Number} width
     */

    /**
     * set height
     * @name setHeight
     * @methodOf Kinetic.Node.prototype
     * @param {Number} height
     */

    /**
     * listen or don't listen to events
     * @name setListening
     * @methodOf Kinetic.Node.prototype
     * @param {Boolean} listening
     */

    /**
     * set visible
     * @name setVisible
     * @methodOf Kinetic.Node.prototype
     * @param {Boolean} visible
     */

    // aliases
    /**
     * Alias of getListening()
     * @name isListening
     * @methodOf Kinetic.Node.prototype
     */
    Kinetic.Node.prototype.isListening = Kinetic.Node.prototype.getListening;
    /**
     * Alias of getVisible()
     * @name isVisible
     * @methodOf Kinetic.Node.prototype
     */
    Kinetic.Node.prototype.isVisible = Kinetic.Node.prototype.getVisible;

    // collection mappings
    var collectionMappings = ['on', 'off'];
    for(var n = 0; n < 2; n++) {
        // induce scope
        (function(i) {
            var method = collectionMappings[i];
            Kinetic.Collection.prototype[method] = function() {
                var args = [].slice.call(arguments);
                args.unshift(method);
                this.apply.apply(this, args);
            };
        })(n);
    }
})();

(function() {
    Kinetic.DD = {
        anim: new Kinetic.Animation(),
        moving: false,
        offset: {
            x: 0,
            y: 0
        }
    };
    Kinetic.DD._setupDragLayerAndGetContainer = function(no) {
        var dd = Kinetic.DD, stage = no.getStage(), nodeType = no.nodeType, lastContainer, group;

        // re-construct node tree
        no._eachAncestorReverse(function(node) {
            if(node.nodeType === 'Layer') {
                stage.dragLayer.setAttrs({
                    x: node.getX(),
                    y: node.getY(),
                    scale: node.getScale(),
                    rotation: node.getRotation()
                });
                lastContainer = stage.dragLayer;
                stage.add(stage.dragLayer);
            }
            else if(node.nodeType === 'Group') {
                group = new Kinetic.Group({
                    x: node.getX(),
                    y: node.getY(),
                    scale: node.getScale(),
                    rotation: node.getRotation()
                });
                lastContainer.add(group);
                lastContainer = group;
            }
        });
        return lastContainer;
    };
    Kinetic.DD._initDragLayer = function(stage) {
        stage.dragLayer = new Kinetic.Layer();
        stage.dragLayer.getCanvas().getElement().className = 'kinetic-drag-and-drop-layer';
    };
    Kinetic.DD._drag = function(evt) {
        var dd = Kinetic.DD, node = dd.node;

        if(node) {
            var pos = node.getStage().getUserPosition();
            var dbf = node.attrs.dragBoundFunc;

            var newNodePos = {
                x: pos.x - dd.offset.x,
                y: pos.y - dd.offset.y
            };

            if(dbf !== undefined) {
                newNodePos = dbf.call(node, newNodePos, evt);
            }

            node.setAbsolutePosition(newNodePos);

            if(!dd.moving) {
                dd.moving = true;
                node.setListening(false);

                // execute dragstart events if defined
                node._handleEvent('dragstart', evt);
            }

            // execute ondragmove if defined
            node._handleEvent('dragmove', evt);
        }
    };
    Kinetic.DD._endDrag = function(evt) {
        var dd = Kinetic.DD, node = dd.node, nodeType, stage;

        if(node) { nodeType = node.nodeType, stage = node.getStage();
            node.setListening(true);
            if(nodeType === 'Stage') {
                node.draw();
            }
            else {
                if((nodeType === 'Group' || nodeType === 'Shape') && node.getDragOnTop() && dd.prevParent) {
                    node.moveTo(dd.prevParent);
                    node.getStage().dragLayer.remove();
                    dd.prevParent = null;
                }

                node.getLayer().draw();
            }

            // only fire dragend event if the drag and drop
            // operation actually started.  This can be detected by
            // checking dd.moving
            if(dd.moving) {
                dd.moving = false;
                node._handleEvent('dragend', evt);
            }
        }
        delete dd.node;
        dd.anim.stop();
    };
    Kinetic.Node.prototype._startDrag = function() {
        var that = this, dd = Kinetic.DD, stage = this.getStage(), pos = stage.getUserPosition();

        if(pos) {
            var m = this.getTransform().getTranslation(), ap = this.getAbsolutePosition(), nodeType = this.nodeType, container;

            dd.node = this;
            dd.offset.x = pos.x - ap.x;
            dd.offset.y = pos.y - ap.y;

            // Stage and Layer node types
            if(nodeType === 'Stage' || nodeType === 'Layer') {
                dd.anim.node = this;
                dd.anim.start();
            }

            // Group or Shape node types
            else {
                if(this.getDragOnTop()) {
                    container = dd._setupDragLayerAndGetContainer(this);
                    dd.anim.node = stage.dragLayer;
                    dd.prevParent = this.getParent();
                    // WARNING: it's important to delay the moveTo operation,
                    // layer redraws, and anim.start() until after the method execution
                    // has completed or else there will be a flicker on mobile devices
                    // due to the time it takes to append the dd canvas to the DOM
                    setTimeout(function() {
                        if(dd.node) {
                            that.moveTo(container);
                            dd.prevParent.getLayer().draw();
                            stage.dragLayer.draw();
                            dd.anim.start();
                        }
                    }, 0);
                }
                else {
                    dd.anim.node = this.getLayer();
                    dd.anim.start();
                }
            }
        }
    };
    /**
     * set draggable
     * @name setDraggable
     * @methodOf Kinetic.Node.prototype
     * @param {String} draggable
     */
    Kinetic.Node.prototype.setDraggable = function(draggable) {
        this.setAttr('draggable', draggable);
        this._dragChange();
    };
    /**
     * get draggable
     * @name getDraggable
     * @methodOf Kinetic.Node.prototype
     */
    Kinetic.Node.prototype.getDraggable = function() {
        return this.attrs.draggable;
    };
    /**
     * determine if node is currently in drag and drop mode
     * @name isDragging
     * @methodOf Kinetic.Node.prototype
     */
    Kinetic.Node.prototype.isDragging = function() {
        var dd = Kinetic.DD;
        return dd.node && dd.node._id === this._id && dd.moving;
    };

    Kinetic.Node.prototype._listenDrag = function() {
        this._dragCleanup();
        this.on('mousedown.kinetic touchstart.kinetic', this._startDrag);
    };

    Kinetic.Node.prototype._dragChange = function() {
        if(this.attrs.draggable) {
            this._listenDrag();
        }
        else {
            // remove event listeners
            this._dragCleanup();

            /*
             * force drag and drop to end
             * if this node is currently in
             * drag and drop mode
             */
            var stage = this.getStage();
            var dd = Kinetic.DD;
            if(stage && dd.node && dd.node._id === this._id) {
                dd._endDrag();
            }
        }
    };
    Kinetic.Node.prototype._dragCleanup = function() {
        this.off('mousedown.kinetic');
        this.off('touchstart.kinetic');
    };
    /**
     * get draggable.  Alias of getDraggable()
     * @name isDraggable
     * @methodOf Kinetic.Node.prototype
     */
    Kinetic.Node.prototype.isDraggable = Kinetic.Node.prototype.getDraggable;

    Kinetic.Node.addGettersSetters(Kinetic.Node, ['dragBoundFunc', 'dragOnTop']);

    /**
     * set drag bound function.  This is used to override the default
     *  drag and drop position
     * @name setDragBoundFunc
     * @methodOf Kinetic.Node.prototype
     * @param {Function} dragBoundFunc
     */

    /**
     * set flag which enables or disables automatically moving the draggable node to a
     *  temporary top layer to improve performance.  The default is true
     * @name setDragOnTop
     * @methodOf Kinetic.Node.prototype
     * @param {Function} dragOnTop
     */

    /**
     * get dragBoundFunc
     * @name getDragBoundFunc
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * get flag which enables or disables automatically moving the draggable node to a
     *  temporary top layer to improve performance.
     * @name getDragOnTop
     * @methodOf Kinetic.Node.prototype
     */
})();

(function() {
    /**
     * Transition constructor.  The transitionTo() Node method
     *  returns a reference to the transition object which you can use
     *  to stop, resume, or restart the transition
     * @constructor
     */
    Kinetic.Transition = function(node, config) {
        var that = this, obj = {};

        this.node = node;
        this.config = config;
        this.tweens = [];

        // add tween for each property
        function addTween(c, attrs, obj, rootObj) {
            for(var key in c) {
                if(key !== 'duration' && key !== 'easing' && key !== 'callback') {
                    // if val is an object then traverse
                    if(Kinetic.Type._isObject(c[key])) {
                        obj[key] = {};
                        addTween(c[key], attrs[key], obj[key], rootObj);
                    }
                    else {
                        that._add(that._getTween(attrs, key, c[key], obj, rootObj));
                    }
                }
            }
        }
        addTween(config, node.attrs, obj, obj);

        // map first tween event to transition event
        this.tweens[0].onStarted = function() {

        };
        this.tweens[0].onStopped = function() {
            node.transAnim.stop();
        };
        this.tweens[0].onResumed = function() {
            node.transAnim.start();
        };
        this.tweens[0].onLooped = function() {

        };
        this.tweens[0].onChanged = function() {

        };
        this.tweens[0].onFinished = function() {
            var newAttrs = {};
            // create new attr obj
            for(var key in config) {
                if(key !== 'duration' && key !== 'easing' && key !== 'callback') {
                    newAttrs[key] = config[key];
                }
            }
            node.transAnim.stop();
            node.setAttrs(newAttrs);
            if(config.callback) {
                config.callback();
            }
        };
    };
    /*
     * Transition methods
     */
    Kinetic.Transition.prototype = {
        /**
         * start transition
         * @name start
         * @methodOf Kinetic.Transition.prototype
         */
        start: function() {
            for(var n = 0; n < this.tweens.length; n++) {
                this.tweens[n].start();
            }
        },
        /**
         * stop transition
         * @name stop
         * @methodOf Kinetic.Transition.prototype
         */
        stop: function() {
            for(var n = 0; n < this.tweens.length; n++) {
                this.tweens[n].stop();
            }
        },
        /**
         * resume transition
         * @name resume
         * @methodOf Kinetic.Transition.prototype
         */
        resume: function() {
            for(var n = 0; n < this.tweens.length; n++) {
                this.tweens[n].resume();
            }
        },
        _onEnterFrame: function() {
            for(var n = 0; n < this.tweens.length; n++) {
                this.tweens[n].onEnterFrame();
            }
        },
        _add: function(tween) {
            this.tweens.push(tween);
        },
        _getTween: function(attrs, prop, val, obj, rootObj) {
            var config = this.config;
            var node = this.node;
            var easing = config.easing;
            if(easing === undefined) {
                easing = 'linear';
            }

            var tween = new Kinetic.Tween(node, function(i) {
                obj[prop] = i;
                node.setAttrs(rootObj);
            }, Kinetic.Tweens[easing], attrs[prop], val, config.duration);

            return tween;
        }
    };

    /**
     * transition node to another state.  Any property that can accept a real
     *  number can be transitioned, including x, y, rotation, opacity, strokeWidth,
     *  radius, scale.x, scale.y, offset.x, offset.y, etc.
     * @name transitionTo
     * @methodOf Kinetic.Node.prototype
     * @param {Object} config
     * @config {Number} duration duration that the transition runs in seconds
     * @config {String} [easing] easing function.  can be linear, ease-in, ease-out, ease-in-out,
     *  back-ease-in, back-ease-out, back-ease-in-out, elastic-ease-in, elastic-ease-out,
     *  elastic-ease-in-out, bounce-ease-out, bounce-ease-in, bounce-ease-in-out,
     *  strong-ease-in, strong-ease-out, or strong-ease-in-out
     *  linear is the default
     * @config {Function} [callback] callback function to be executed when
     *  transition completes
     */
    Kinetic.Node.prototype.transitionTo = function(config) {
        var that = this, trans = new Kinetic.Transition(this, config);

        if(!this.transAnim) {
            this.transAnim = new Kinetic.Animation();
        }
        this.transAnim.func = function() {
            trans._onEnterFrame();
        };
        this.transAnim.node = this.nodeType === 'Stage' ? this : this.getLayer();

        // auto start
        trans.start();
        this.transAnim.start();
        this.trans = trans;
        return trans;
    };
})();

(function() {
    /**
     * Container constructor.&nbsp; Containers are used to contain nodes or other containers
     * @constructor
     * @augments Kinetic.Node
     * @param {Object} config
     */
    Kinetic.Container = function(config) {
        this._containerInit(config);
    };

    Kinetic.Container.prototype = {
        _containerInit: function(config) {
            this.children = [];
            Kinetic.Node.call(this, config);
        },
        /**
         * get children
         * @name getChildren
         * @methodOf Kinetic.Container.prototype
         */
        getChildren: function() {
            return this.children;
        },
        /**
         * remove all children
         * @name removeChildren
         * @methodOf Kinetic.Container.prototype
         */
        removeChildren: function() {
            while(this.children.length > 0) {
                this.children[0].remove();
            }
        },
        /**
         * add node to container
         * @name add
         * @methodOf Kinetic.Container.prototype
         * @param {Node} child
         */
        add: function(child) {
            var go = Kinetic.Global, children = this.children;
            child.index = children.length;
            child.parent = this;
            children.push(child);

            // chainable
            return this;
        },
        /**
         * return an array of nodes that match the selector.  Use '#' for id selections
         * and '.' for name selections
         * ex:
         * var node = stage.get('#foo'); // selects node with id foo
         * var nodes = layer.get('.bar'); // selects nodes with name bar inside layer
         * @name get
         * @methodOf Kinetic.Container.prototype
         * @param {String} selector
         */
        get: function(selector) {
            var collection = new Kinetic.Collection();
            // ID selector
            if(selector.charAt(0) === '#') {
                var node = this._getNodeById(selector.slice(1));
                if(node) {
                    collection.push(node);
                }
            }
            // name selector
            else if(selector.charAt(0) === '.') {
                var nodeList = this._getNodesByName(selector.slice(1));
                Kinetic.Collection.apply(collection, nodeList);
            }
            // unrecognized selector, pass to children
            else {
                var retArr = [];
                var children = this.getChildren();
                var len = children.length;
                for(var n = 0; n < len; n++) {
                    retArr = retArr.concat(children[n]._get(selector));
                }
                Kinetic.Collection.apply(collection, retArr);
            }
            return collection;
        },
        _getNodeById: function(key) {
            var stage = this.getStage(), go = Kinetic.Global, node = go.ids[key];
            if(node !== undefined && this.isAncestorOf(node)) {
                return node;
            }
            return null;
        },
        _getNodesByName: function(key) {
            var go = Kinetic.Global, arr = go.names[key] || [];
            return this._getDescendants(arr);
        },
        _get: function(selector) {
            var retArr = Kinetic.Node.prototype._get.call(this, selector);
            var children = this.getChildren();
            var len = children.length;
            for(var n = 0; n < len; n++) {
                retArr = retArr.concat(children[n]._get(selector));
            }
            return retArr;
        },
        // extenders
        toObject: function() {
            var obj = Kinetic.Node.prototype.toObject.call(this);

            obj.children = [];

            var children = this.getChildren();
            var len = children.length;
            for(var n = 0; n < len; n++) {
                var child = children[n];
                obj.children.push(child.toObject());
            }

            return obj;
        },
        _getDescendants: function(arr) {
            var retArr = [];
            var len = arr.length;
            for(var n = 0; n < len; n++) {
                var node = arr[n];
                if(this.isAncestorOf(node)) {
                    retArr.push(node);
                }
            }

            return retArr;
        },
        /**
         * determine if node is an ancestor
         * of descendant
         * @name isAncestorOf
         * @methodOf Kinetic.Container.prototype
         * @param {Kinetic.Node} node
         */
        isAncestorOf: function(node) {
            var parent = node.getParent();
            while(parent) {
                if(parent._id === this._id) {
                    return true;
                }
                parent = parent.getParent();
            }

            return false;
        },
        /**
         * clone node
         * @name clone
         * @methodOf Kinetic.Container.prototype
         * @param {Object} attrs override attrs
         */
        clone: function(obj) {
            // call super method
            var node = Kinetic.Node.prototype.clone.call(this, obj)

            // perform deep clone on containers
            for(var key in this.children) {
                node.add(this.children[key].clone());
            }
            return node;
        },
        /**
         * get shapes that intersect a point
         * @name getIntersections
         * @methodOf Kinetic.Container.prototype
         * @param {Object} point
         */
        getIntersections: function() {
            var pos = Kinetic.Type._getXY(Array.prototype.slice.call(arguments));
            var arr = [];
            var shapes = this.get('Shape');

            var len = shapes.length;
            for(var n = 0; n < len; n++) {
                var shape = shapes[n];
                if(shape.isVisible() && shape.intersects(pos)) {
                    arr.push(shape);
                }
            }

            return arr;
        },
        /**
         * set children indices
         */
        _setChildrenIndices: function() {
            var children = this.children, len = children.length;
            for(var n = 0; n < len; n++) {
                children[n].index = n;
            }
        },
        /*
         * draw both scene and hit graphs
         */
        draw: function() {
            this.drawScene();
            this.drawHit();
        },
        drawScene: function(canvas) {
            if(this.isVisible()) {
                var children = this.children, len = children.length;
                for(var n = 0; n < len; n++) {
                    children[n].drawScene(canvas);
                }
            }
        },
        drawHit: function() {
            if(this.isVisible() && this.isListening()) {
                var children = this.children, len = children.length;
                for(var n = 0; n < len; n++) {
                    children[n].drawHit();
                }
            }
        }
    };
    Kinetic.Global.extend(Kinetic.Container, Kinetic.Node);
})();

(function() {
    /**
     * Stage constructor.  A stage is used to contain multiple layers
     * @constructor
     * @augments Kinetic.Container
     * @param {Object} config
     * @param {String|DomElement} config.container Container id or DOM element
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Stage = function(config) {
        this._initStage(config);
    };

    Kinetic.Stage.prototype = {
        _initStage: function(config) {
            var dd = Kinetic.DD;
            this.setDefaultAttrs({
                width: 400,
                height: 200
            });

            // call super constructor
            Kinetic.Container.call(this, config);

            this._setStageDefaultProperties();
            this._id = Kinetic.Global.idCounter++;
            this._buildDOM();
            this._bindContentEvents();
            Kinetic.Global.stages.push(this);

            if(dd) {
                dd._initDragLayer(this);
            }
        },
        /**
         * set container dom element which contains the stage wrapper div element
         * @name setContainer
         * @methodOf Kinetic.Stage.prototype
         * @param {DomElement} container can pass in a dom element or id string
         */
        setContainer: function(container) {
            /*
             * if container is a string, assume it's an id for
             * a DOM element
             */
            if( typeof container === 'string') {
                container = document.getElementById(container);
            }
            this.setAttr('container', container);
        },
        /**
         * draw layer scene graphs
         * @name draw
         * @methodOf Kinetic.Stage.prototype
         */

        /**
         * draw layer hit graphs
         * @name drawHit
         * @methodOf Kinetic.Stage.prototype
         */

        /**
         * set height
         * @name setHeight
         * @methodOf Kinetic.Stage.prototype
         * @param {Number} height
         */
        setHeight: function(height) {
            Kinetic.Node.prototype.setHeight.call(this, height);
            this._resizeDOM();
        },
        /**
         * set width
         * @name setWidth
         * @methodOf Kinetic.Stage.prototype
         * @param {Number} width
         */
        setWidth: function(width) {
            Kinetic.Node.prototype.setWidth.call(this, width);
            this._resizeDOM();
        },
        /**
         * clear all layers
         * @name clear
         * @methodOf Kinetic.Stage.prototype
         */
        clear: function() {
            var layers = this.children;
            for(var n = 0; n < layers.length; n++) {
                layers[n].clear();
            }
        },
        /**
         * remove stage
         */
        remove: function() {
            var content = this.content;
            Kinetic.Node.prototype.remove.call(this);

            if(content && Kinetic.Type._isInDocument(content)) {
                this.attrs.container.removeChild(content);
            }
        },
        /**
         * reset stage to default state
         * @name reset
         * @methodOf Kinetic.Stage.prototype
         */
        reset: function() {
            // remove children
            this.removeChildren();

            // defaults
            this._setStageDefaultProperties();
            this.setAttrs(this.defaultNodeAttrs);
        },
        /**
         * get mouse position for desktop apps
         * @name getMousePosition
         * @methodOf Kinetic.Stage.prototype
         */
        getMousePosition: function() {
            return this.mousePos;
        },
        /**
         * get touch position for mobile apps
         * @name getTouchPosition
         * @methodOf Kinetic.Stage.prototype
         */
        getTouchPosition: function() {
            return this.touchPos;
        },
        /**
         * get user position which can be a touc position or mouse position
         * @name getUserPosition
         * @methodOf Kinetic.Stage.prototype
         */
        getUserPosition: function() {
            return this.getTouchPosition() || this.getMousePosition();
        },
        getStage: function() {
            return this;
        },
        /**
         * get stage content div element which has the
         *  the class name "kineticjs-content"
         * @name getContent
         * @methodOf Kinetic.Stage.prototype
         */
        getContent: function() {
            return this.content;
        },
        /**
         * Creates a composite data URL and requires a callback because the composite is generated asynchronously.
         * @name toDataURL
         * @methodOf Kinetic.Stage.prototype
         * @param {Object} config
         * @param {Function} config.callback function executed when the composite has completed
         * @param {String} [config.mimeType] can be "image/png" or "image/jpeg".
         *  "image/png" is the default
         * @param {Number} [config.x] x position of canvas section
         * @param {Number} [config.y] y position of canvas section
         * @param {Number} [config.width] width of canvas section
         * @param {Number} [config.height] height of canvas section
         * @param {Number} [config.quality] jpeg quality.  If using an "image/jpeg" mimeType,
         *  you can specify the quality from 0 to 1, where 0 is very poor quality and 1
         *  is very high quality
         */
        toDataURL: function(config) {
            config = config || {};
            var mimeType = config.mimeType || null, quality = config.quality || null, x = config.x || 0, y = config.y || 0, canvas = new Kinetic.SceneCanvas(config.width || this.getWidth(), config.height || this.getHeight()), context = canvas.getContext(), layers = this.children;

            if(x || y) {
                context.translate(-1 * x, -1 * y);
            }

            function drawLayer(n) {
                var layer = layers[n];
                var layerUrl = layer.toDataURL();
                var imageObj = new Image();
                imageObj.onload = function() {
                    context.drawImage(imageObj, 0, 0);

                    if(n < layers.length - 1) {
                        drawLayer(n + 1);
                    }
                    else {
                        config.callback(canvas.toDataURL(mimeType, quality));
                    }
                };
                imageObj.src = layerUrl;
            }
            drawLayer(0);
        },
        /**
         * converts stage into an image.
         * @name toImage
         * @methodOf Kinetic.Stage.prototype
         * @param {Object} config
         * @param {Function} config.callback function executed when the composite has completed
         * @param {String} [config.mimeType] can be "image/png" or "image/jpeg".
         *  "image/png" is the default
         * @param {Number} [config.x] x position of canvas section
         * @param {Number} [config.y] y position of canvas section
         * @param {Number} [config.width] width of canvas section
         * @param {Number} [config.height] height of canvas section
         * @param {Number} [config.quality] jpeg quality.  If using an "image/jpeg" mimeType,
         *  you can specify the quality from 0 to 1, where 0 is very poor quality and 1
         *  is very high quality
         */
        toImage: function(config) {
            var cb = config.callback;

            config.callback = function(dataUrl) {
                Kinetic.Type._getImage(dataUrl, function(img) {
                    cb(img);
                });
            };
            this.toDataURL(config);
        },
        /**
         * get intersection object that contains shape and pixel data
         * @name getIntersection
         * @methodOf Kinetic.Stage.prototype
         * @param {Object} pos point object
         */
        getIntersection: function(pos) {
            var shape;
            var layers = this.getChildren();

            /*
             * traverse through layers from top to bottom and look
             * for hit detection
             */
            for(var n = layers.length - 1; n >= 0; n--) {
                var layer = layers[n];
                if(layer.isVisible() && layer.isListening()) {
                    var p = layer.hitCanvas.context.getImageData(Math.round(pos.x), Math.round(pos.y), 1, 1).data;
                    // this indicates that a hit pixel may have been found
                    if(p[3] === 255) {
                        var colorKey = Kinetic.Type._rgbToHex(p[0], p[1], p[2]);
                        shape = Kinetic.Global.shapes[colorKey];
                        return {
                            shape: shape,
                            pixel: p
                        };
                    }
                    // if no shape mapped to that pixel, return pixel array
                    else if(p[0] > 0 || p[1] > 0 || p[2] > 0 || p[3] > 0) {
                        return {
                            pixel: p
                        };
                    }
                }
            }

            return null;
        },
        _resizeDOM: function() {
            if(this.content) {
                var width = this.attrs.width;
                var height = this.attrs.height;

                // set content dimensions
                this.content.style.width = width + 'px';
                this.content.style.height = height + 'px';

                this.bufferCanvas.setSize(width, height);
                this.hitCanvas.setSize(width, height);
                // set user defined layer dimensions
                var layers = this.children;
                for(var n = 0; n < layers.length; n++) {
                    var layer = layers[n];
                    layer.getCanvas().setSize(width, height);
                    layer.hitCanvas.setSize(width, height);
                    layer.draw();
                }
            }
        },
        /**
         * add layer to stage
         * @param {Kinetic.Layer} layer
         */
        add: function(layer) {
            Kinetic.Container.prototype.add.call(this, layer);
            layer.canvas.setSize(this.attrs.width, this.attrs.height);
            layer.hitCanvas.setSize(this.attrs.width, this.attrs.height);

            // draw layer and append canvas to container
            layer.draw();
            this.content.appendChild(layer.canvas.element);

            // chainable
            return this;
        },
        /**
         * get drag and drop layer
         */
        getDragLayer: function() {
            return this.dragLayer;
        },
        _setUserPosition: function(evt) {
            if(!evt) {
                evt = window.event;
            }
            this._setMousePosition(evt);
            this._setTouchPosition(evt);
        },
        /**
         * begin listening for events by adding event handlers
         * to the container
         */
        _bindContentEvents: function() {
            var go = Kinetic.Global;
            var that = this;
            var events = ['mousedown', 'mousemove', 'mouseup', 'mouseout', 'touchstart', 'touchmove', 'touchend'];

            for(var n = 0; n < events.length; n++) {
                var pubEvent = events[n];
                // induce scope
                ( function() {
                    var event = pubEvent;
                    that.content.addEventListener(event, function(evt) {
                        that['_' + event](evt);
                    }, false);
                }());
            }
        },
        _mouseout: function(evt) {
            this._setUserPosition(evt);
            var dd = Kinetic.DD;
            // if there's a current target shape, run mouseout handlers
            var targetShape = this.targetShape;
            if(targetShape && (!dd || !dd.moving)) {
                targetShape._handleEvent('mouseout', evt);
                targetShape._handleEvent('mouseleave', evt);
                this.targetShape = null;
            }
            this.mousePos = undefined;
        },
        _mousemove: function(evt) {
            this._setUserPosition(evt);
            var dd = Kinetic.DD;
            var obj = this.getIntersection(this.getUserPosition());

            if(obj) {
                var shape = obj.shape;
                if(shape) {
                    if((!dd || !dd.moving) && obj.pixel[3] === 255 && (!this.targetShape || this.targetShape._id !== shape._id)) {
                        if(this.targetShape) {
                            this.targetShape._handleEvent('mouseout', evt, shape);
                            this.targetShape._handleEvent('mouseleave', evt, shape);
                        }
                        shape._handleEvent('mouseover', evt, this.targetShape);
                        shape._handleEvent('mouseenter', evt, this.targetShape);
                        this.targetShape = shape;
                    }
                    else {
                        shape._handleEvent('mousemove', evt);
                    }
                }
            }
            /*
             * if no shape was detected, clear target shape and try
             * to run mouseout from previous target shape
             */
            else if(this.targetShape && (!dd || !dd.moving)) {
                this.targetShape._handleEvent('mouseout', evt);
                this.targetShape._handleEvent('mouseleave', evt);
                this.targetShape = null;
            }

            // start drag and drop
            if(dd) {
                dd._drag(evt);
            }
        },
        _mousedown: function(evt) {
            this._setUserPosition(evt);
            var obj = this.getIntersection(this.getUserPosition());
            if(obj && obj.shape) {
                var shape = obj.shape;
                this.clickStart = true;
                shape._handleEvent('mousedown', evt);
            }

            //init stage drag and drop
            if(Kinetic.DD && this.attrs.draggable) {
                this._startDrag();
            }
        },
        _mouseup: function(evt) {
            this._setUserPosition(evt);
            var that = this, dd = Kinetic.DD, obj = this.getIntersection(this.getUserPosition());

            // end drag and drop
            if(dd) {
                dd._endDrag(evt);
            }

            if(obj && obj.shape) {
                var shape = obj.shape;
                shape._handleEvent('mouseup', evt);

                // detect if click or double click occurred
                if(this.clickStart) {
                    /*
                     * if dragging and dropping, don't fire click or dbl click
                     * event
                     */
                    if(!dd || !dd.moving || !dd.node) {
                        shape._handleEvent('click', evt);

                        if(this.inDoubleClickWindow) {
                            shape._handleEvent('dblclick', evt);
                        }
                        this.inDoubleClickWindow = true;
                        setTimeout(function() {
                            that.inDoubleClickWindow = false;
                        }, this.dblClickWindow);
                    }
                }
            }
            this.clickStart = false;
        },
        _touchstart: function(evt) {
            this._setUserPosition(evt);
            evt.preventDefault();
            var obj = this.getIntersection(this.getUserPosition());

            if(obj && obj.shape) {
                var shape = obj.shape;
                this.tapStart = true;
                shape._handleEvent('touchstart', evt);
            }

            /*
             * init stage drag and drop
             */
            if(Kinetic.DD && this.attrs.draggable) {
                this._startDrag();
            }
        },
        _touchend: function(evt) {
            this._setUserPosition(evt);
            var that = this, dd = Kinetic.DD, obj = this.getIntersection(this.getUserPosition());

            // end drag and drop
            if(dd) {
                dd._endDrag(evt);
            }

            if(obj && obj.shape) {
                var shape = obj.shape;
                shape._handleEvent('touchend', evt);

                // detect if tap or double tap occurred
                if(this.tapStart) {
                    /*
                     * if dragging and dropping, don't fire tap or dbltap
                     * event
                     */
                    if(!dd || !dd.moving || !dd.node) {
                        shape._handleEvent('tap', evt);

                        if(this.inDoubleClickWindow) {
                            shape._handleEvent('dbltap', evt);
                        }
                        this.inDoubleClickWindow = true;
                        setTimeout(function() {
                            that.inDoubleClickWindow = false;
                        }, this.dblClickWindow);
                    }
                }
            }

            this.tapStart = false;
        },
        _touchmove: function(evt) {
            this._setUserPosition(evt);
            var dd = Kinetic.DD;
            evt.preventDefault();
            var obj = this.getIntersection(this.getUserPosition());
            if(obj && obj.shape) {
                var shape = obj.shape;
                shape._handleEvent('touchmove', evt);
            }

            // start drag and drop
            if(dd) {
                dd._drag(evt);
            }
        },
        /**
         * set mouse positon for desktop apps
         * @param {Event} evt
         */
        _setMousePosition: function(evt) {
            var mouseX = evt.clientX - this._getContentPosition().left;
            var mouseY = evt.clientY - this._getContentPosition().top;
            this.mousePos = {
                x: mouseX,
                y: mouseY
            };
        },
        /**
         * set touch position for mobile apps
         * @param {Event} evt
         */
        _setTouchPosition: function(evt) {
            if(evt.touches !== undefined && evt.touches.length === 1) {
                // one finger
                var touch = evt.touches[0];
                // Get the information for finger #1
                var touchX = touch.clientX - this._getContentPosition().left;
                var touchY = touch.clientY - this._getContentPosition().top;

                this.touchPos = {
                    x: touchX,
                    y: touchY
                };
            }
        },
        /**
         * get container position
         */
        _getContentPosition: function() {
            var rect = this.content.getBoundingClientRect();
            return {
                top: rect.top,
                left: rect.left
            };
        },
        /**
         * build dom
         */
        _buildDOM: function() {
            // content
            this.content = document.createElement('div');
            this.content.style.position = 'relative';
            this.content.style.display = 'inline-block';
            this.content.className = 'kineticjs-content';
            this.attrs.container.appendChild(this.content);

            this.bufferCanvas = new Kinetic.SceneCanvas();
            this.hitCanvas = new Kinetic.HitCanvas();

            this._resizeDOM();
        },
        /**
         * bind event listener to container DOM element
         * @param {String} typesStr
         * @param {function} handler
         */
        _onContent: function(typesStr, handler) {
            var types = typesStr.split(' ');
            for(var n = 0; n < types.length; n++) {
                var baseEvent = types[n];
                this.content.addEventListener(baseEvent, handler, false);
            }
        },
        /**
         * set defaults
         */
        _setStageDefaultProperties: function() {
            this.nodeType = 'Stage';
            this.dblClickWindow = 400;
            this.targetShape = null;
            this.mousePos = undefined;
            this.clickStart = false;
            this.touchPos = undefined;
            this.tapStart = false;
        }
    };
    Kinetic.Global.extend(Kinetic.Stage, Kinetic.Container);

    // add getters and setters
    Kinetic.Node.addGetters(Kinetic.Stage, ['container']);

    /**
     * get container DOM element
     * @name getContainer
     * @methodOf Kinetic.Stage.prototype
     */
})();

(function() {
    /**
     * Layer constructor.  Layers are tied to their own canvas element and are used
     * to contain groups or shapes
     * @constructor
     * @augments Kinetic.Container
     * @param {Object} config
     * @param {Boolean} [config.clearBeforeDraw] set this property to false if you don't want
     * to clear the canvas before each layer draw.  The default value is true.
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Layer = function(config) {
        this._initLayer(config);
    };

    Kinetic.Layer.prototype = {
        _initLayer: function(config) {
            this.setDefaultAttrs({
                clearBeforeDraw: true
            });

            this.nodeType = 'Layer';
            this.beforeDrawFunc = undefined;
            this.afterDrawFunc = undefined;
            this.canvas = new Kinetic.SceneCanvas();
            this.canvas.getElement().style.position = 'absolute';
            this.hitCanvas = new Kinetic.HitCanvas();

            // call super constructor
            Kinetic.Container.call(this, config);
        },
        /**
         * draw children nodes.  this includes any groups
         *  or shapes
         * @name draw
         * @methodOf Kinetic.Layer.prototype
         */
        draw: function() {
            // before draw  handler
            if(this.beforeDrawFunc !== undefined) {
                this.beforeDrawFunc.call(this);
            }

            Kinetic.Container.prototype.draw.call(this);

            // after draw  handler
            if(this.afterDrawFunc !== undefined) {
                this.afterDrawFunc.call(this);
            }
        },
        /**
         * draw children nodes on hit.  this includes any groups
         *  or shapes
         * @name drawHit
         * @methodOf Kinetic.Layer.prototype
         */
        drawHit: function() {
            this.hitCanvas.clear();
            Kinetic.Container.prototype.drawHit.call(this);
        },
        /**
         * draw children nodes on scene.  this includes any groups
         *  or shapes
         * @name drawScene
         * @methodOf Kinetic.Layer.prototype
         * @param {Kinetic.Canvas} [canvas]
         */
        drawScene: function(canvas) {
            canvas = canvas || this.getCanvas();
            if(this.attrs.clearBeforeDraw) {
                canvas.clear();
            }
            Kinetic.Container.prototype.drawScene.call(this, canvas);
        },
        toDataURL: function(config) {
            config = config || {};
            var mimeType = config.mimeType || null, quality = config.quality || null, canvas, context, x = config.x || 0, y = config.y || 0;

            // if dimension or position is defined, use Node toDataURL
            if(config.width || config.height || config.x || config.y) {
                return Kinetic.Node.prototype.toDataURL.call(this, config);
            }
            // otherwise get data url of the currently drawn layer
            else {
                return this.getCanvas().toDataURL(mimeType, quality);
            }
        },
        /**
         * set before draw handler
         * @name beforeDraw
         * @methodOf Kinetic.Layer.prototype
         * @param {Function} handler
         */
        beforeDraw: function(func) {
            this.beforeDrawFunc = func;
        },
        /**
         * set after draw handler
         * @name afterDraw
         * @methodOf Kinetic.Layer.prototype
         * @param {Function} handler
         */
        afterDraw: function(func) {
            this.afterDrawFunc = func;
        },
        /**
         * get layer canvas
         * @name getCanvas
         * @methodOf Kinetic.Layer.prototype
         */
        getCanvas: function() {
            return this.canvas;
        },
        /**
         * get layer canvas context
         * @name getContext
         * @methodOf Kinetic.Layer.prototype
         */
        getContext: function() {
            return this.canvas.context;
        },
        /**
         * clear canvas tied to the layer
         * @name clear
         * @methodOf Kinetic.Layer.prototype
         */
        clear: function() {
            this.getCanvas().clear();
        },
        // extenders
        setVisible: function(visible) {
            Kinetic.Node.prototype.setVisible.call(this, visible);
            if(visible) {
                this.canvas.element.style.display = 'block';
                this.hitCanvas.element.style.display = 'block';
            }
            else {
                this.canvas.element.style.display = 'none';
                this.hitCanvas.element.style.display = 'none';
            }
        },
        setZIndex: function(index) {
            Kinetic.Node.prototype.setZIndex.call(this, index);
            var stage = this.getStage();
            if(stage) {
                stage.content.removeChild(this.canvas.element);

                if(index < stage.getChildren().length - 1) {
                    stage.content.insertBefore(this.canvas.element, stage.getChildren()[index + 1].canvas.element);
                }
                else {
                    stage.content.appendChild(this.canvas.element);
                }
            }
        },
        moveToTop: function() {
            Kinetic.Node.prototype.moveToTop.call(this);
            var stage = this.getStage();
            if(stage) {
                stage.content.removeChild(this.canvas.element);
                stage.content.appendChild(this.canvas.element);
            }
        },
        moveUp: function() {
            if(Kinetic.Node.prototype.moveUp.call(this)) {
                var stage = this.getStage();
                if(stage) {
                    stage.content.removeChild(this.canvas.element);

                    if(this.index < stage.getChildren().length - 1) {
                        stage.content.insertBefore(this.canvas.element, stage.getChildren()[this.index + 1].canvas.element);
                    }
                    else {
                        stage.content.appendChild(this.canvas.element);
                    }
                }
            }
        },
        moveDown: function() {
            if(Kinetic.Node.prototype.moveDown.call(this)) {
                var stage = this.getStage();
                if(stage) {
                    var children = stage.getChildren();
                    stage.content.removeChild(this.canvas.element);
                    stage.content.insertBefore(this.canvas.element, children[this.index + 1].canvas.element);
                }
            }
        },
        moveToBottom: function() {
            if(Kinetic.Node.prototype.moveToBottom.call(this)) {
                var stage = this.getStage();
                if(stage) {
                    var children = stage.getChildren();
                    stage.content.removeChild(this.canvas.element);
                    stage.content.insertBefore(this.canvas.element, children[1].canvas.element);
                }
            }
        },
        getLayer: function() {
            return this;
        },
        /**
         * remove layer from stage
         */
        remove: function() {
            var stage = this.getStage(), canvas = this.canvas, element = canvas.element;
            Kinetic.Node.prototype.remove.call(this);

            if(stage && canvas && Kinetic.Type._isInDocument(element)) {
                stage.content.removeChild(element);
            }
        }
    };
    Kinetic.Global.extend(Kinetic.Layer, Kinetic.Container);

    // add getters and setters
    Kinetic.Node.addGettersSetters(Kinetic.Layer, ['clearBeforeDraw']);

    /**
     * set flag which determines if the layer is cleared or not
     *  before drawing
     * @name setClearBeforeDraw
     * @methodOf Kinetic.Layer.prototype
     * @param {Boolean} clearBeforeDraw
     */

    /**
     * get flag which determines if the layer is cleared or not
     *  before drawing
     * @name getClearBeforeDraw
     * @methodOf Kinetic.Layer.prototype
     */
})();

(function() {
    /**
     * Group constructor.  Groups are used to contain shapes or other groups.
     * @constructor
     * @augments Kinetic.Container
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Group = function(config) {
        this._initGroup(config);
    };

    Kinetic.Group.prototype = {
        _initGroup: function(config) {
            this.nodeType = 'Group';

            // call super constructor
            Kinetic.Container.call(this, config);
        }
    };
    Kinetic.Global.extend(Kinetic.Group, Kinetic.Container);
})();

(function() {
    /**
     * Shape constructor.  Shapes are primitive objects such as rectangles,
     *  circles, text, lines, etc.
     * @constructor
     * @augments Kinetic.Node
     * @param {Object} config
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Shape = function(config) {
        this._initShape(config);
    };

    Kinetic.Shape.prototype = {
        _initShape: function(config) {
            this.nodeType = 'Shape';

            // set colorKey
            var shapes = Kinetic.Global.shapes;
            var key;

            while(true) {
                key = Kinetic.Type._getRandomColorKey();
                if(key && !( key in shapes)) {
                    break;
                }
            }

            this.colorKey = key;
            shapes[key] = this;

            // call super constructor
            Kinetic.Node.call(this, config);
        },
        /**
         * get canvas context tied to the layer
         * @name getContext
         * @methodOf Kinetic.Shape.prototype
         */
        getContext: function() {
            return this.getLayer().getContext();
        },
        /**
         * get canvas renderer tied to the layer.  Note that this returns a canvas renderer, not a canvas element
         * @name getCanvas
         * @methodOf Kinetic.Shape.prototype
         */
        getCanvas: function() {
            return this.getLayer().getCanvas();
        },
        /**
         * returns whether or not a shadow will be rendered
         * @name hasShadow
         * @methodOf Kinetic.Shape.prototype
         */
        hasShadow: function() {
            return !!(this.getShadowColor() || this.getShadowBlur() || this.getShadowOffset());
        },
        _get: function(selector) {
            return this.nodeType === selector || this.shapeType === selector ? [this] : [];
        },
        /**
         * determines if point is in the shape
         * @name intersects
         * @methodOf Kinetic.Shape.prototype
         * @param {Object} point point can be an object containing
         *  an x and y property, or it can be an array with two elements
         *  in which the first element is the x component and the second
         *  element is the y component
         */
        intersects: function() {
            var pos = Kinetic.Type._getXY(Array.prototype.slice.call(arguments));
            var stage = this.getStage();
            var hitCanvas = stage.hitCanvas;
            hitCanvas.clear();
            this.drawScene(hitCanvas);
            var p = hitCanvas.context.getImageData(Math.round(pos.x), Math.round(pos.y), 1, 1).data;
            return p[3] > 0;
        },
        remove: function() {
            Kinetic.Node.prototype.remove.call(this);
            delete Kinetic.Global.shapes[this.colorKey];
        },
        drawScene: function(canvas) {
            var attrs = this.attrs, drawFunc = attrs.drawFunc, canvas = canvas || this.getLayer().getCanvas(), context = canvas.getContext();

            if(drawFunc && this.isVisible()) {
                context.save();
                canvas._handlePixelRatio();
                canvas._applyOpacity(this);
                canvas._applyLineJoin(this);
                canvas._applyAncestorTransforms(this);

                drawFunc.call(this, canvas);
                context.restore();
            }
        },
        drawHit: function() {
            var attrs = this.attrs, drawFunc = attrs.drawHitFunc || attrs.drawFunc, canvas = this.getLayer().hitCanvas, context = canvas.getContext();

            if(drawFunc && this.isVisible() && this.isListening()) {
                context.save();
                canvas._applyLineJoin(this);
                canvas._applyAncestorTransforms(this);

                drawFunc.call(this, canvas);
                context.restore();
            }
        },
        _setDrawFuncs: function() {
            if(!this.attrs.drawFunc && this.drawFunc) {
                this.setDrawFunc(this.drawFunc);
            }
            if(!this.attrs.drawHitFunc && this.drawHitFunc) {
                this.setDrawHitFunc(this.drawHitFunc);
            }
        }
    };
    Kinetic.Global.extend(Kinetic.Shape, Kinetic.Node);

    // add getters and setters
    Kinetic.Node.addGettersSetters(Kinetic.Shape, ['stroke', 'lineJoin', 'lineCap', 'strokeWidth', 'drawFunc', 'drawHitFunc', 'dashArray', 'shadowColor', 'shadowBlur', 'shadowOpacity', 'fillPatternImage', 'fill', 'fillPatternX', 'fillPatternY', 'fillLinearGradientColorStops', 'fillRadialGradientStartRadius', 'fillRadialGradientEndRadius', 'fillRadialGradientColorStops', 'fillPatternRepeat']);

    /**
     * set stroke color
     * @name setStroke
     * @methodOf Kinetic.Shape.prototype
     * @param {String} stroke
     */

    /**
     * set line join
     * @name setLineJoin
     * @methodOf Kinetic.Shape.prototype
     * @param {String} lineJoin.  Can be miter, round, or bevel.  The
     *  default is miter
     */

    /**
     * set line cap.  Can be butt, round, or square
     * @name setLineCap
     * @methodOf Kinetic.Shape.prototype
     * @param {String} lineCap
     */

    /**
     * set stroke width
     * @name setStrokeWidth
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} strokeWidth
     */

    /**
     * set draw function
     * @name setDrawFunc
     * @methodOf Kinetic.Shape.prototype
     * @param {Function} drawFunc drawing function
     */

    /**
     * set draw hit function used for hit detection
     * @name setDrawHitFunc
     * @methodOf Kinetic.Shape.prototype
     * @param {Function} drawHitFunc drawing function used for hit detection
     */

    /**
     * set dash array.
     * @name setDashArray
     * @methodOf Kinetic.Shape.prototype
     * @param {Array} dashArray
     *  examples:<br>
     *  [10, 5] dashes are 10px long and 5 pixels apart
     *  [10, 20, 0.001, 20] if using a round lineCap, the line will
     *  be made up of alternating dashed lines that are 10px long
     *  and 20px apart, and dots that have a radius of 5px and are 20px
     *  apart
     */

    /**
     * set shadow color
     * @name setShadowColor
     * @methodOf Kinetic.Shape.prototype
     * @param {String} color
     */

    /**
     * set shadow blur
     * @name setShadowBlur
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} blur
     */

    /**
     * set shadow opacity
     * @name setShadowOpacity
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} opacity must be a value between 0 and 1
     */

    /**
     * set fill pattern image
     * @name setFillPatternImage
     * @methodOf Kinetic.Shape.prototype
     * @param {Image} image object
     */

    /**
     * set fill color
     * @name setFill
     * @methodOf Kinetic.Shape.prototype
     * @param {String} color
     */

    /**
     * set fill pattern x
     * @name setFillPatternX
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} x
     */

    /**
     * set fill pattern y
     * @name setFillPatternY
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} y
     */

    /**
     * set fill linear gradient color stops
     * @name setFillLinearGradientColorStops
     * @methodOf Kinetic.Shape.prototype
     * @param {Array} colorStops
     */

    /**
     * set fill radial gradient start radius
     * @name setFillRadialGradientStartRadius
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} radius
     */

    /**
     * set fill radial gradient end radius
     * @name setFillRadialGradientEndRadius
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} radius
     */

    /**
     * set fill radial gradient color stops
     * @name setFillRadialGradientColorStops
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} colorStops
     */

    /**
     * set fill pattern repeat
     * @name setFillPatternRepeat
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} repeat can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     */

    /**
     * get stroke color
     * @name getStroke
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get line join
     * @name getLineJoin
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get line cap
     * @name getLineCap
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get stroke width
     * @name getStrokeWidth
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get draw function
     * @name getDrawFunc
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get draw hit function
     * @name getDrawHitFunc
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get dash array
     * @name getDashArray
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get shadow color
     * @name getShadowColor
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get shadow blur
     * @name getShadowBlur
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get shadow opacity
     * @name getShadowOpacity
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill pattern image
     * @name getFillPatternImage
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill color
     * @name getFill
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill pattern x
     * @name getFillPatternX
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill pattern y
     * @name getFillPatternY
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill linear gradient color stops
     * @name getFillLinearGradientColorStops
     * @methodOf Kinetic.Shape.prototype
     * @param {Array} colorStops
     */

    /**
     * get fill radial gradient start radius
     * @name getFillRadialGradientStartRadius
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill radial gradient end radius
     * @name getFillRadialGradientEndRadius
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill radial gradient color stops
     * @name getFillRadialGradientColorStops
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill pattern repeat
     * @name getFillPatternRepeat
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addPointGettersSetters(Kinetic.Shape, ['fillPatternOffset', 'fillPatternScale', 'fillLinearGradientStartPoint', 'fillLinearGradientEndPoint', 'fillRadialGradientStartPoint', 'fillRadialGradientEndPoint', 'shadowOffset']);

    /**
     * set fill pattern offset
     * @name setFillPatternOffset
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} offset
     */

    /**
     * set fill pattern scale
     * @name setFillPatternScale
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} scale
     */

    /**
     * set fill linear gradient start point
     * @name setFillLinearGradientStartPoint
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} startPoint
     */

    /**
     * set fill linear gradient end point
     * @name setFillLinearGradientEndPoint
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} endPoint
     */

    /**
     * set fill radial gradient start point
     * @name setFillRadialGradientStartPoint
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} startPoint
     */

    /**
     * set fill radial gradient end point
     * @name setFillRadialGradientEndPoint
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} endPoint
     */

    /**
     * set shadow offset
     * @name setShadowOffset
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} offset
     */

    /**
     * get fill pattern offset
     * @name getFillPatternOffset
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill pattern scale
     * @name getFillPatternScale
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill linear gradient start point
     * @name getFillLinearGradientStartPoint
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill linear gradient end point
     * @name getFillLinearGradientEndPoint
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill radial gradient start point
     * @name getFillRadialGradientStartPoint
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill radial gradient end point
     * @name getFillRadialGradientEndPoint
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get shadow offset
     * @name getShadowOffset
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addRotationGettersSetters(Kinetic.Shape, ['fillPatternRotation']);

    /**
     * set fill pattern rotation in radians
     * @name setFillPatternRotation
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} rotation
     */

    /**
     * set fill pattern rotation in degrees
     * @name setFillPatternRotationDeg
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} rotationDeg
     */

    /**
     * get fill pattern rotation in radians
     * @name getFillPatternRotation
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill pattern rotation in degrees
     * @name getFillPatternRotationDeg
     * @methodOf Kinetic.Shape.prototype
     */

})();

(function() {
    /**
     * Rect constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Number} [config.cornerRadius]
     *

     * @param {String} [config.fill] fill color
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Rect = function(config) {
        this._initRect(config);
    };

    Kinetic.Rect.prototype = {
        _initRect: function(config) {
            this.setDefaultAttrs({
                width: 0,
                height: 0,
                cornerRadius: 0
            });

            Kinetic.Shape.call(this, config);
            this.shapeType = 'Rect';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext();
            context.beginPath();
            var cornerRadius = this.getCornerRadius(), width = this.getWidth(), height = this.getHeight();
            if(cornerRadius === 0) {
                // simple rect - don't bother doing all that complicated maths stuff.
                context.rect(0, 0, width, height);
            }
            else {
                // arcTo would be nicer, but browser support is patchy (Opera)
                context.moveTo(cornerRadius, 0);
                context.lineTo(width - cornerRadius, 0);
                context.arc(width - cornerRadius, cornerRadius, cornerRadius, Math.PI * 3 / 2, 0, false);
                context.lineTo(width, height - cornerRadius);
                context.arc(width - cornerRadius, height - cornerRadius, cornerRadius, 0, Math.PI / 2, false);
                context.lineTo(cornerRadius, height);
                context.arc(cornerRadius, height - cornerRadius, cornerRadius, Math.PI / 2, Math.PI, false);
                context.lineTo(0, cornerRadius);
                context.arc(cornerRadius, cornerRadius, cornerRadius, Math.PI, Math.PI * 3 / 2, false);
            }
            context.closePath();
            canvas.fillStroke(this);
        }
    };

    Kinetic.Global.extend(Kinetic.Rect, Kinetic.Shape);

    Kinetic.Node.addGettersSetters(Kinetic.Rect, ['cornerRadius']);

    /**
     * set corner radius
     * @name setCornerRadius
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} corner radius
     */

    /**
     * get corner radius
     * @name getCornerRadius
     * @methodOf Kinetic.Shape.prototype
     */

})();

(function() {
    /**
     * Circle constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Number} config.radius
     * @param {String} [config.fill] fill color
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Circle = function(config) {
        this._initCircle(config);
    };

    Kinetic.Circle.prototype = {
        _initCircle: function(config) {
            this.setDefaultAttrs({
                radius: 0
            });

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Circle';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext();
            context.beginPath();
            context.arc(0, 0, this.getRadius(), 0, Math.PI * 2, true);
            context.closePath();
            canvas.fillStroke(this);
        },
        getWidth: function() {
            return this.getRadius() * 2;
        },
        getHeight: function() {
            return this.getRadius() * 2;
        },
        setWidth: function(width) {
            Kinetic.Node.prototype.setWidth.call(this, width);
            this.setRadius(width / 2);
        },
        setHeight: function(height) {
            Kinetic.Node.prototype.setHeight.call(this, height);
            this.setRadius(height / 2);
        }
    };
    Kinetic.Global.extend(Kinetic.Circle, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGettersSetters(Kinetic.Circle, ['radius']);

    /**
     * set radius
     * @name setRadius
     * @methodOf Kinetic.Circle.prototype
     * @param {Number} radius
     */

    /**
     * get radius
     * @name getRadius
     * @methodOf Kinetic.Circle.prototype
     */
})();

(function() {
    /**
     * Wedge constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Number} config.angle
     * @param {Number} config.radius
     * @param {Boolean} [config.clockwise]
     *
     * -------------------------------------------------
     * @param {String} [config.fill] fill color
     * -------------------------------------------------
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * -------------------------------------------------
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * -------------------------------------------------
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * -------------------------------------------------
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Wedge = function(config) {
        this._initWedge(config);
    };

    Kinetic.Wedge.prototype = {
        _initWedge: function(config) {
            this.setDefaultAttrs({
                radius: 0,
                angle: 0,
                clickwise: true
            });

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Wedge';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext();
            context.beginPath();
            context.arc(0, 0, this.getRadius(), 0, this.getAngle(), this.getClockwise());
            context.lineTo(0, 0);
            context.closePath();
            canvas.fillStroke(this);
        },
        /**
         * set angle in degrees
         * @name setAngleDeg
         * @methodOf Kinetic.Wedge.prototype
         * @param {Number} deg
         */
        setAngleDeg: function(deg) {
            this.setAngle(Kinetic.Type._degToRad(deg));
        },
        /**
         * set angle in degrees
         * @name getAngleDeg
         * @methodOf Kinetic.Wedge.prototype
         */
        getAngleDeg: function() {
            return Kinetic.Type._radToDeg(this.getAngle());
        }
    };
    Kinetic.Global.extend(Kinetic.Wedge, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGettersSetters(Kinetic.Wedge, ['radius', 'angle', 'clockwise']);

    /**
     * set radius
     * @name setRadius
     * @methodOf Kinetic.Wedge.prototype
     * @param {Number} radius
     */

    /**
     * set angle
     * @name setAngle
     * @methodOf Kinetic.Wedge.prototype
     * @param {Number} angle
     */

    /**
     * set clockwise draw direction.  If set to true, the wedge will be drawn clockwise
     *  If set to false, the wedge will be drawn anti-clockwise.  The default is true.
     * @name setClockwise
     * @methodOf Kinetic.Wedge.prototype
     * @param {Boolean} clockwise
     */

    /**
     * get radius
     * @name getRadius
     * @methodOf Kinetic.Wedge.prototype
     */

    /**
     * get angle
     * @name getAngle
     * @methodOf Kinetic.Wedge.prototype
     */

    /**
     * get clockwise
     * @name getClockwise
     * @methodOf Kinetic.Wedge.prototype
     */
})();

(function() {
    /**
     * Ellipse constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Number|Array|Object} config.radius defines x and y radius
     *
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Ellipse = function(config) {
        this._initEllipse(config);
    };

    Kinetic.Ellipse.prototype = {
        _initEllipse: function(config) {
            this.setDefaultAttrs({
                radius: {
                    x: 0,
                    y: 0
                }
            });

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Ellipse';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext(), r = this.getRadius();
            context.beginPath();
            context.save();
            if(r.x !== r.y) {
                context.scale(1, r.y / r.x);
            }
            context.arc(0, 0, r.x, 0, Math.PI * 2, true);
            context.restore();
            context.closePath();
            canvas.fillStroke(this);
        },
        getWidth: function() {
            return this.getRadius().x * 2;
        },
        getHeight: function() {
            return this.getRadius().y * 2;
        },
        setWidth: function(width) {
            Kinetic.Node.prototype.setWidth.call(this, width);
            this.setRadius({
                x: width / 2
            });
        },
        setHeight: function(height) {
            Kinetic.Node.prototype.setHeight.call(this, height);
            this.setRadius({
                y: height / 2
            });
        }
    };
    Kinetic.Global.extend(Kinetic.Ellipse, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addPointGettersSetters(Kinetic.Ellipse, ['radius']);

    /**
     * set radius
     * @name setRadius
     * @methodOf Kinetic.Ellipse.prototype
     * @param {Object|Array} radius
     *  radius can be a number, in which the ellipse becomes a circle,
     *  it can be an object with an x and y component, or it
     *  can be an array in which the first element is the x component
     *  and the second element is the y component.  The x component
     *  defines the horizontal radius and the y component
     *  defines the vertical radius
     */

    /**
     * get radius
     * @name getRadius
     * @methodOf Kinetic.Ellipse.prototype
     */
})();

(function() {
    /**
     * Image constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {ImageObject} config.image
     * @param {Object} [config.crop]
     *
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Image = function(config) {
        this._initImage(config);
    };

    Kinetic.Image.prototype = {
        _initImage: function(config) {
            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Image';
            this._setDrawFuncs();

            var that = this;
            this.on('imageChange', function(evt) {
                that._syncSize();
            });

            this._syncSize();
        },
        drawFunc: function(canvas) {
            var width = this.getWidth(), height = this.getHeight(), params, that = this, context = canvas.getContext();

            context.beginPath();
            context.rect(0, 0, width, height);
            context.closePath();
            canvas.fillStroke(this);

            if(this.attrs.image) {
                // if cropping
                if(this.attrs.crop && this.attrs.crop.width && this.attrs.crop.height) {
                    var cropX = this.attrs.crop.x || 0;
                    var cropY = this.attrs.crop.y || 0;
                    var cropWidth = this.attrs.crop.width;
                    var cropHeight = this.attrs.crop.height;
                    params = [this.attrs.image, cropX, cropY, cropWidth, cropHeight, 0, 0, width, height];
                }
                // no cropping
                else {
                    params = [this.attrs.image, 0, 0, width, height];
                }

                if(this.hasShadow()) {
                    canvas.applyShadow(this, function() {
                        that._drawImage(context, params);
                    });
                }
                else {
                    this._drawImage(context, params);
                }

            }

        },
        drawHitFunc: function(canvas) {
            var width = this.getWidth(), height = this.getHeight(), imageHitRegion = this.imageHitRegion, appliedShadow = false, context = canvas.getContext();

            if(imageHitRegion) {
                context.drawImage(imageHitRegion, 0, 0, width, height);
                context.beginPath();
                context.rect(0, 0, width, height);
                context.closePath();
                canvas.stroke(this);
            }
            else {
                context.beginPath();
                context.rect(0, 0, width, height);
                context.closePath();
                canvas.fillStroke(this);
            }
        },
        /**
         * apply filter
         * @name applyFilter
         * @methodOf Kinetic.Image.prototype
         * @param {Object} config
         * @param {Function} filter filter function
         * @param {Object} [config] optional config object used to configure filter
         * @param {Function} [callback] callback function to be called once
         *  filter has been applied
         */
        applyFilter: function(filter, config, callback) {
            var canvas = new Kinetic.Canvas(this.attrs.image.width, this.attrs.image.height);
            var context = canvas.getContext();
            context.drawImage(this.attrs.image, 0, 0);
            try {
                var imageData = context.getImageData(0, 0, canvas.getWidth(), canvas.getHeight());
                filter(imageData, config);
                var that = this;
                Kinetic.Type._getImage(imageData, function(imageObj) {
                    that.setImage(imageObj);

                    if(callback) {
                        callback();
                    }
                });
            }
            catch(e) {
                Kinetic.Global.warn('Unable to apply filter. ' + e.message);
            }
        },
        /**
         * set crop
         * @name setCrop
         * @methodOf Kinetic.Image.prototype
         * @param {Object|Array} config
         * @param {Number} config.x
         * @param {Number} config.y
         * @param {Number} config.width
         * @param {Number} config.height
         */
        setCrop: function() {
            var config = [].slice.call(arguments);
            var pos = Kinetic.Type._getXY(config);
            var size = Kinetic.Type._getSize(config);
            var both = Kinetic.Type._merge(pos, size);
            this.setAttr('crop', Kinetic.Type._merge(both, this.getCrop()));
        },
        /**
         * create image hit region which enables more accurate hit detection mapping of the image
         *  by avoiding event detections for transparent pixels
         * @name createImageHitRegion
         * @methodOf Kinetic.Image.prototype
         * @param {Function} [callback] callback function to be called once
         *  the image hit region has been created
         */
        createImageHitRegion: function(callback) {
            var canvas = new Kinetic.Canvas(this.attrs.width, this.attrs.height);
            var context = canvas.getContext();
            context.drawImage(this.attrs.image, 0, 0);
            try {
                var imageData = context.getImageData(0, 0, canvas.getWidth(), canvas.getHeight());
                var data = imageData.data;
                var rgbColorKey = Kinetic.Type._hexToRgb(this.colorKey);
                // replace non transparent pixels with color key
                for(var i = 0, n = data.length; i < n; i += 4) {
                    data[i] = rgbColorKey.r;
                    data[i + 1] = rgbColorKey.g;
                    data[i + 2] = rgbColorKey.b;
                    // i+3 is alpha (the fourth element)
                }

                var that = this;
                Kinetic.Type._getImage(imageData, function(imageObj) {
                    that.imageHitRegion = imageObj;
                    if(callback) {
                        callback();
                    }
                });
            }
            catch(e) {
                Kinetic.Global.warn('Unable to create image hit region. ' + e.message);
            }
        },
        /**
         * clear image hit region
         * @name clearImageHitRegion
         * @methodOf Kinetic.Image.prototype
         */
        clearImageHitRegion: function() {
            delete this.imageHitRegion;
        },
        _syncSize: function() {
            if(this.attrs.image) {
                if(!this.attrs.width) {
                    this.setWidth(this.attrs.image.width);
                }
                if(!this.attrs.height) {
                    this.setHeight(this.attrs.image.height);
                }
            }
        },
        _drawImage: function(context, a) {
            if(a.length === 5) {
                context.drawImage(a[0], a[1], a[2], a[3], a[4]);
            }
            else if(a.length === 9) {
                context.drawImage(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8]);
            }
        }
    };
    Kinetic.Global.extend(Kinetic.Image, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGettersSetters(Kinetic.Image, ['image']);
    Kinetic.Node.addGetters(Kinetic.Image, ['crop']);

    /**
     * set image
     * @name setImage
     * @methodOf Kinetic.Image.prototype
     * @param {ImageObject} image
     */

    /**
     * get crop
     * @name getCrop
     * @methodOf Kinetic.Image.prototype
     */

    /**
     * get image
     * @name getImage
     * @methodOf Kinetic.Image.prototype
     */
})();

(function() {
    /**
     * Polygon constructor.&nbsp; Polygons are defined by an array of points
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Array} config.points can be a flattened array of points, an array of point arrays, or an array of point objects.
     *  e.g. [0,1,2,3], [[0,1],[2,3]] and [{x:0,y:1},{x:2,y:3}] are equivalent
     *
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Polygon = function(config) {
        this._initPolygon(config);
    };

    Kinetic.Polygon.prototype = {
        _initPolygon: function(config) {
            this.setDefaultAttrs({
                points: []
            });

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Polygon';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext(), points = this.getPoints(), length = points.length;
            context.beginPath();
            context.moveTo(points[0].x, points[0].y);
            for(var n = 1; n < length; n++) {
                context.lineTo(points[n].x, points[n].y);
            }
            context.closePath();
            canvas.fillStroke(this);
        },
        /**
         * set points array
         * @name setPoints
         * @methodOf Kinetic.Polygon.prototype
         * @param {Array} can be an array of point objects or an array
         *  of Numbers.  e.g. [{x:1,y:2},{x:3,y:4}] or [1,2,3,4]
         */
        setPoints: function(val) {
            this.setAttr('points', Kinetic.Type._getPoints(val));
        }
    };
    Kinetic.Global.extend(Kinetic.Polygon, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGetters(Kinetic.Polygon, ['points']);

    /**
     * get points array
     * @name getPoints
     * @methodOf Kinetic.Polygon.prototype
     */
})();

(function() {
    /**
     * Text constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {String} [config.fontFamily] default is Calibri
     * @param {Number} [config.fontSize] in pixels.  Default is 12
     * @param {String} [config.fontStyle] can be normal, bold, or italic.  Default is normal
     * @param {String} config.text
     * @param {String} [config.align] can be left, center, or right
     * @param {Number} [config.padding]
     * @param {Number} [config.width] default is auto
     * @param {Number} [config.height] default is auto
     * @param {Number} [config.lineHeight] default is 1
     *
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Text = function(config) {
        this._initText(config);
    };

    Kinetic.Text.prototype = {
        _initText: function(config) {
            this.setDefaultAttrs({
                fontFamily: 'Calibri',
                text: '',
                fontSize: 12,
                align: 'left',
                verticalAlign: 'top',
                fontStyle: 'normal',
                padding: 0,
                width: 'auto',
                height: 'auto',
                lineHeight: 1
            });

            this.dummyCanvas = document.createElement('canvas');

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Text';
            this._setDrawFuncs();

            // update text data for certain attr changes
            var attrs = ['fontFamily', 'fontSize', 'fontStyle', 'padding', 'align', 'lineHeight', 'text', 'width', 'height'];
            var that = this;
            for(var n = 0; n < attrs.length; n++) {
                var attr = attrs[n];
                this.on(attr + 'Change.kinetic', that._setTextData);
            }
            that._setTextData();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext(), p = this.attrs.padding, lineHeightPx = this.attrs.lineHeight * this.getTextHeight(), textArr = this.textArr;

            context.font = this.attrs.fontStyle + ' ' + this.attrs.fontSize + 'px ' + this.attrs.fontFamily;
            context.textBaseline = 'middle';
            context.textAlign = 'left';
            context.save();
            context.translate(p, 0);
            context.translate(0, p + this.getTextHeight() / 2);

            // draw text lines
            for(var n = 0; n < textArr.length; n++) {
                var text = textArr[n];

                // horizontal alignment
                context.save();
                if(this.attrs.align === 'right') {
                    context.translate(this.getWidth() - this._getTextSize(text).width - p * 2, 0);
                }
                else if(this.attrs.align === 'center') {
                    context.translate((this.getWidth() - this._getTextSize(text).width - p * 2) / 2, 0);
                }

                canvas.fillStrokeText(this, text);
                context.restore();
                context.translate(0, lineHeightPx);
            }
            context.restore();
        },
        drawHitFunc: function(canvas) {
            var context = canvas.getContext(), width = this.getWidth(), height = this.getHeight();

            context.beginPath();
            context.rect(0, 0, width, height);
            context.closePath();
            canvas.fillStroke(this);
        },
        /**
         * set text
         * @name setText
         * @methodOf Kinetic.Text.prototype
         * @param {String} text
         */
        setText: function(text) {
            var str = Kinetic.Type._isString(text) ? text : text.toString();
            this.setAttr('text', str);
        },
        /**
         * get width
         * @name getWidth
         * @methodOf Kinetic.Text.prototype
         */
        getWidth: function() {
            return this.attrs.width === 'auto' ? this.getTextWidth() + this.attrs.padding * 2 : this.attrs.width;
        },
        /**
         * get height
         * @name getHeight
         * @methodOf Kinetic.Text.prototype
         */
        getHeight: function() {
            return this.attrs.height === 'auto' ? (this.getTextHeight() * this.textArr.length * this.attrs.lineHeight) + this.attrs.padding * 2 : this.attrs.height;
        },
        /**
         * get text width
         * @name getTextWidth
         * @methodOf Kinetic.Text.prototype
         */
        getTextWidth: function() {
            return this.textWidth;
        },
        /**
         * get text height
         * @name getTextHeight
         * @methodOf Kinetic.Text.prototype
         */
        getTextHeight: function() {
            return this.textHeight;
        },
        _getTextSize: function(text) {
            var dummyCanvas = this.dummyCanvas;
            var context = dummyCanvas.getContext('2d');

            context.save();
            context.font = this.attrs.fontStyle + ' ' + this.attrs.fontSize + 'px ' + this.attrs.fontFamily;
            var metrics = context.measureText(text);
            context.restore();
            return {
                width: metrics.width,
                height: parseInt(this.attrs.fontSize, 10)
            };
        },
        /**
         * set text data.  wrap logic and width and height setting occurs
         * here
         */
        _setTextData: function() {
            var charArr = this.attrs.text.split('');
            var arr = [];
            var row = 0;
            var addLine = true;
            this.textWidth = 0;
            this.textHeight = this._getTextSize(this.attrs.text).height;
            var lineHeightPx = this.attrs.lineHeight * this.textHeight;
            while(charArr.length > 0 && addLine && (this.attrs.height === 'auto' || lineHeightPx * (row + 1) < this.attrs.height - this.attrs.padding * 2)) {
                var index = 0;
                var line = undefined;
                addLine = false;

                while(index < charArr.length) {
                    if(charArr.indexOf('\n') === index) {
                        // remove newline char
                        charArr.splice(index, 1);
                        line = charArr.splice(0, index).join('');
                        break;
                    }

                    // if line exceeds inner box width
                    var lineArr = charArr.slice(0, index);
                    if(this.attrs.width !== 'auto' && this._getTextSize(lineArr.join('')).width > this.attrs.width - this.attrs.padding * 2) {
                        /*
                         * if a single character is too large to fit inside
                         * the text box width, then break out of the loop
                         * and stop processing
                         */
                        if(index == 0) {
                            break;
                        }
                        var lastSpace = lineArr.lastIndexOf(' ');
                        var lastDash = lineArr.lastIndexOf('-');
                        var wrapIndex = Math.max(lastSpace, lastDash);
                        if(wrapIndex >= 0) {
                            line = charArr.splice(0, 1 + wrapIndex).join('');
                            break;
                        }
                        /*
                         * if not able to word wrap based on space or dash,
                         * go ahead and wrap in the middle of a word if needed
                         */
                        line = charArr.splice(0, index).join('');
                        break;
                    }
                    index++;

                    // if the end is reached
                    if(index === charArr.length) {
                        line = charArr.splice(0, index).join('');
                    }
                }
                this.textWidth = Math.max(this.textWidth, this._getTextSize(line).width);
                if(line !== undefined) {
                    arr.push(line);
                    addLine = true;
                }
                row++;
            }
            this.textArr = arr;
        }
    };
    Kinetic.Global.extend(Kinetic.Text, Kinetic.Shape);

    /*
     * extend canvas renderers
     */
    var fillText = function(shape, text, skipShadow) {
        var textFill = shape.getFill(), context = this.context;
        if(textFill) {
            context.save();
            if(!skipShadow && shape.hasShadow()) {
                this._applyShadow(shape);
            }
            context.fillStyle = textFill;
            context.fillText(text, 0, 0);
            context.restore();

            if(!skipShadow && shape.hasShadow()) {
                this.fillText(shape, text, true);
            }
        }
    };
    var strokeText = function(shape, text, skipShadow) {
        var textStroke = shape.getStroke(), textStrokeWidth = shape.getStrokeWidth(), context = this.context;
        if(textStroke || textStrokeWidth) {
            context.save();
            if(!skipShadow && shape.hasShadow()) {
                this._applyShadow(shape);
            }

            context.lineWidth = textStrokeWidth || 2;
            context.strokeStyle = textStroke || 'black';
            context.strokeText(text, 0, 0);
            context.restore();

            if(!skipShadow && shape.hasShadow()) {
                this.strokeText(shape, text, true);
            }
        }
    };
    var fillStrokeText = function(shape, text) {
        this.fillText(shape, text);
        this.strokeText(shape, text, shape.hasShadow() && shape.getFill());
    };

    // scene canvases
    Kinetic.SceneCanvas.prototype.fillText = fillText;
    Kinetic.SceneCanvas.prototype.strokeText = strokeText;
    Kinetic.SceneCanvas.prototype.fillStrokeText = fillStrokeText;

    // hit canvases
    Kinetic.HitCanvas.prototype.fillText = fillText;
    Kinetic.HitCanvas.prototype.strokeText = strokeText;
    Kinetic.HitCanvas.prototype.fillStrokeText = fillStrokeText;

    // add getters setters
    Kinetic.Node.addGettersSetters(Kinetic.Text, ['fontFamily', 'fontSize', 'fontStyle', 'padding', 'align', 'lineHeight']);
    Kinetic.Node.addGetters(Kinetic.Text, ['text']);
    /**
     * set font family
     * @name setFontFamily
     * @methodOf Kinetic.Text.prototype
     * @param {String} fontFamily
     */

    /**
     * set font size in pixels
     * @name setFontSize
     * @methodOf Kinetic.Text.prototype
     * @param {int} fontSize
     */

    /**
     * set font style.  Can be 'normal', 'italic', or 'bold'.  'normal' is the default.
     * @name setFontStyle
     * @methodOf Kinetic.Text.prototype
     * @param {String} fontStyle
     */

    /**
     * set padding
     * @name setPadding
     * @methodOf Kinetic.Text.prototype
     * @param {int} padding
     */

    /**
     * set horizontal align of text
     * @name setAlign
     * @methodOf Kinetic.Text.prototype
     * @param {String} align align can be 'left', 'center', or 'right'
     */

    /**
     * set line height
     * @name setLineHeight
     * @methodOf Kinetic.Text.prototype
     * @param {Number} lineHeight default is 1
     */

    /**
     * get font family
     * @name getFontFamily
     * @methodOf Kinetic.Text.prototype
     */

    /**
     * get font size
     * @name getFontSize
     * @methodOf Kinetic.Text.prototype
     */

    /**
     * get font style
     * @name getFontStyle
     * @methodOf Kinetic.Text.prototype
     */

    /**
     * get padding
     * @name getPadding
     * @methodOf Kinetic.Text.prototype
     */

    /**
     * get horizontal align
     * @name getAlign
     * @methodOf Kinetic.Text.prototype
     */

    /**
     * get line height
     * @name getLineHeight
     * @methodOf Kinetic.Text.prototype
     */

    /**
     * get text
     * @name getText
     * @methodOf Kinetic.Text.prototype
     */
})();

(function() {
    /**
     * Line constructor.&nbsp; Lines are defined by an array of points
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Array} config.points can be a flattened array of points, an array of point arrays, or an array of point objects.
     *  e.g. [0,1,2,3], [[0,1],[2,3]] and [{x:0,y:1},{x:2,y:3}] are equivalent
     *
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Line = function(config) {
        this._initLine(config);
    };

    Kinetic.Line.prototype = {
        _initLine: function(config) {
            this.setDefaultAttrs({
                points: [],
                lineCap: 'butt'
            });

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Line';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var points = this.getPoints(), length = points.length, context = canvas.getContext();
            context.beginPath();
            context.moveTo(points[0].x, points[0].y);

            for(var n = 1; n < length; n++) {
                var point = points[n];
                context.lineTo(point.x, point.y);
            }

            canvas.stroke(this);
        },
        /**
         * set points array
         * @name setPoints
         * @methodOf Kinetic.Line.prototype
         * @param {Array} can be an array of point objects or an array
         *  of Numbers.  e.g. [{x:1,y:2},{x:3,y:4}] or [1,2,3,4]
         */
        setPoints: function(val) {
            this.setAttr('points', Kinetic.Type._getPoints(val));
        }
    };
    Kinetic.Global.extend(Kinetic.Line, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGetters(Kinetic.Line, ['points']);

    /**
     * get points array
     * @name getPoints
     * @methodOf Kinetic.Line.prototype
     */
})();

(function() {
    /**
     * Spline constructor.&nbsp; Splines are defined by an array of points and
     *  a tension
     * @constructor
     * @augments Kinetic.Line
     * @param {Object} config
     * @param {Array} config.points can be a flattened array of points, an array of point arrays, or an array of point objects.
     *  e.g. [0,1,2,3], [[0,1],[2,3]] and [{x:0,y:1},{x:2,y:3}] are equivalent
     * @param {Number} [config.tension] default value is 1.  Higher values will result in a more curvy line.  A value of 0 will result in no interpolation.
     *
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Spline = function(config) {
        this._initSpline(config);
    };
    Kinetic.Spline._getControlPoints = function(p0, p1, p2, t) {
        var x0 = p0.x;
        var y0 = p0.y;
        var x1 = p1.x;
        var y1 = p1.y;
        var x2 = p2.x;
        var y2 = p2.y;
        var d01 = Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
        var d12 = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        var fa = t * d01 / (d01 + d12);
        var fb = t * d12 / (d01 + d12);
        var p1x = x1 - fa * (x2 - x0);
        var p1y = y1 - fa * (y2 - y0);
        var p2x = x1 + fb * (x2 - x0);
        var p2y = y1 + fb * (y2 - y0);
        return [{
            x: p1x,
            y: p1y
        }, {
            x: p2x,
            y: p2y
        }];
    };

    Kinetic.Spline.prototype = {
        _initSpline: function(config) {
            this.setDefaultAttrs({
                tension: 1
            });

            // call super constructor
            Kinetic.Line.call(this, config);
            this.shapeType = 'Spline';
        },
        drawFunc: function(canvas) {
            var points = this.getPoints(), length = points.length, context = canvas.getContext(), tension = this.getTension();
            context.beginPath();
            context.moveTo(points[0].x, points[0].y);

            // tension
            if(tension !== 0 && length > 2) {
                var ap = this.allPoints, len = ap.length;
                context.quadraticCurveTo(ap[0].x, ap[0].y, ap[1].x, ap[1].y);

                var n = 2;
                while(n < len - 1) {
                    context.bezierCurveTo(ap[n].x, ap[n++].y, ap[n].x, ap[n++].y, ap[n].x, ap[n++].y);
                }

                context.quadraticCurveTo(ap[len - 1].x, ap[len - 1].y, points[length - 1].x, points[length - 1].y);

            }
            // no tension
            else {
                for(var n = 1; n < length; n++) {
                    var point = points[n];
                    context.lineTo(point.x, point.y);
                }
            }

            canvas.stroke(this);
        },
        setPoints: function(val) {
            Kinetic.Line.prototype.setPoints.call(this, val);
            this._setAllPoints();
        },
        /**
         * set tension
         * @name setTension
         * @methodOf Kinetic.Spline.prototype
         * @param {Number} tension
         */
        setTension: function(tension) {
            this.setAttr('tension', tension);
            this._setAllPoints();
        },
        _setAllPoints: function() {
            var points = this.getPoints(), length = points.length, tension = this.getTension(), allPoints = [];

            for(var n = 1; n < length - 1; n++) {
                var cp = Kinetic.Spline._getControlPoints(points[n - 1], points[n], points[n + 1], tension);
                allPoints.push(cp[0]);
                allPoints.push(points[n]);
                allPoints.push(cp[1]);
            }

            this.allPoints = allPoints;
        }
    };
    Kinetic.Global.extend(Kinetic.Spline, Kinetic.Line);

    // add getters setters
    Kinetic.Node.addGetters(Kinetic.Spline, ['tension']);

    /**
     * get tension
     * @name getTension
     * @methodOf Kinetic.Spline.prototype
     */
})();

(function() {
    /**
     * Blob constructor.&nbsp; Blobs are defined by an array of points and
     *  a tension
     * @constructor
     * @augments Kinetic.Spline
     * @param {Object} config
     * @param {Array} config.points can be a flattened array of points, an array of point arrays, or an array of point objects.
     *  e.g. [0,1,2,3], [[0,1],[2,3]] and [{x:0,y:1},{x:2,y:3}] are equivalent
     * @param {Number} [config.tension] default value is 1.  Higher values will result in a more curvy line.  A value of 0 will result in no interpolation.
     * @param {String} [config.fill] fill color
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Blob = function(config) {
        this._initBlob(config);
    };

    Kinetic.Blob.prototype = {
        _initBlob: function(config) {
            // call super constructor
            Kinetic.Spline.call(this, config);
            this.shapeType = 'Blob';
        },
        drawFunc: function(canvas) {
            var points = this.getPoints(), length = points.length, context = canvas.getContext(), tension = this.getTension();
            context.beginPath();
            context.moveTo(points[0].x, points[0].y);

            // tension
            if(tension !== 0 && length > 2) {
                var ap = this.allPoints, len = ap.length;
                var n = 0;
                while(n < len-1) {
                    context.bezierCurveTo(ap[n].x, ap[n++].y, ap[n].x, ap[n++].y, ap[n].x, ap[n++].y);
                }
            }
            // no tension
            else {
                for(var n = 1; n < length; n++) {
                    var point = points[n];
                    context.lineTo(point.x, point.y);
                }
            }

            context.closePath();
            canvas.fillStroke(this);
        },
        _setAllPoints: function() {
            var points = this.getPoints(), length = points.length, tension = this.getTension(), firstControlPoints = Kinetic.Spline._getControlPoints(points[length - 1], points[0], points[1], tension), lastControlPoints = Kinetic.Spline._getControlPoints(points[length - 2], points[length - 1], points[0], tension);

            Kinetic.Spline.prototype._setAllPoints.call(this);

            // prepend control point
            this.allPoints.unshift(firstControlPoints[1]);

            // append cp, point, cp, cp, first point
            this.allPoints.push(lastControlPoints[0]);
            this.allPoints.push(points[length - 1]);
            this.allPoints.push(lastControlPoints[1]);
            this.allPoints.push(firstControlPoints[0]);
            this.allPoints.push(points[0]);
        }
    };

    Kinetic.Global.extend(Kinetic.Blob, Kinetic.Spline);
})();

(function() {
    /**
     * Sprite constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {String} config.animation animation key
     * @param {Object} config.animations animation map
     * @param {Integer} [config.index] animation index
     *
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Sprite = function(config) {
        this._initSprite(config);
    };

    Kinetic.Sprite.prototype = {
        _initSprite: function(config) {
            this.setDefaultAttrs({
                index: 0,
                frameRate: 17
            });

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Sprite';
            this._setDrawFuncs();

            this.anim = new Kinetic.Animation();
            var that = this;
            this.on('animationChange', function() {
                // reset index when animation changes
                that.setIndex(0);
            });
        },
        drawFunc: function(canvas) {
            var anim = this.attrs.animation, index = this.attrs.index, f = this.attrs.animations[anim][index], context = canvas.getContext(), image = this.attrs.image;

            if(image) {
                context.drawImage(image, f.x, f.y, f.width, f.height, 0, 0, f.width, f.height);
            }
        },
        drawHitFunc: function(canvas) {
            var anim = this.attrs.animation, index = this.attrs.index, f = this.attrs.animations[anim][index], context = canvas.getContext();

            context.beginPath();
            context.rect(0, 0, f.width, f.height);
            context.closePath();
            canvas.fill(this);
        },
        /**
         * start sprite animation
         * @name start
         * @methodOf Kinetic.Sprite.prototype
         */
        start: function() {
            var that = this;
            var layer = this.getLayer();

            /*
             * animation object has no executable function because
             *  the updates are done with a fixed FPS with the setInterval
             *  below.  The anim object only needs the layer reference for
             *  redraw
             */
            this.anim.node = layer;

            this.interval = setInterval(function() {
                var index = that.attrs.index;
                that._updateIndex();
                if(that.afterFrameFunc && index === that.afterFrameIndex) {
                    that.afterFrameFunc();
                    delete that.afterFrameFunc;
                    delete that.afterFrameIndex;
                }
            }, 1000 / this.attrs.frameRate);

            this.anim.start();
        },
        /**
         * stop sprite animation
         * @name stop
         * @methodOf Kinetic.Sprite.prototype
         */
        stop: function() {
            this.anim.stop();
            clearInterval(this.interval);
        },
        /**
         * set after frame event handler
         * @name afterFrame
         * @methodOf Kinetic.Sprite.prototype
         * @param {Integer} index frame index
         * @param {Function} func function to be executed after frame has been drawn
         */
        afterFrame: function(index, func) {
            this.afterFrameIndex = index;
            this.afterFrameFunc = func;
        },
        _updateIndex: function() {
            var i = this.attrs.index;
            var a = this.attrs.animation;
            if(i < this.attrs.animations[a].length - 1) {
                this.attrs.index++;
            }
            else {
                this.attrs.index = 0;
            }
        }
    };
    Kinetic.Global.extend(Kinetic.Sprite, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGettersSetters(Kinetic.Sprite, ['animation', 'animations', 'index']);

    /**
     * set animation key
     * @name setAnimation
     * @methodOf Kinetic.Sprite.prototype
     * @param {String} anim animation key
     */

    /**
     * set animations object
     * @name setAnimations
     * @methodOf Kinetic.Sprite.prototype
     * @param {Object} animations
     */

    /**
     * set animation frame index
     * @name setIndex
     * @methodOf Kinetic.Sprite.prototype
     * @param {Integer} index frame index
     */

    /**
     * get animation key
     * @name getAnimation
     * @methodOf Kinetic.Sprite.prototype
     */

    /**
     * get animations object
     * @name getAnimations
     * @methodOf Kinetic.Sprite.prototype
     */

    /**
     * get animation frame index
     * @name getIndex
     * @methodOf Kinetic.Sprite.prototype
     */
})();

(function() {
    /**
     * Star constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Integer} config.numPoints
     * @param {Number} config.innerRadius
     * @param {Number} config.outerRadius
     *
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Star = function(config) {
        this._initStar(config);
    };

    Kinetic.Star.prototype = {
        _initStar: function(config) {
            this.setDefaultAttrs({
                numPoints: 0,
                innerRadius: 0,
                outerRadius: 0
            });

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Star';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext(), innerRadius = this.attrs.innerRadius, outerRadius = this.attrs.outerRadius, numPoints = this.attrs.numPoints;

            context.beginPath();
            context.moveTo(0, 0 - this.attrs.outerRadius);

            for(var n = 1; n < numPoints * 2; n++) {
                var radius = n % 2 === 0 ? outerRadius : innerRadius;
                var x = radius * Math.sin(n * Math.PI / numPoints);
                var y = -1 * radius * Math.cos(n * Math.PI / numPoints);
                context.lineTo(x, y);
            }
            context.closePath();

            canvas.fillStroke(this);
        }
    };
    Kinetic.Global.extend(Kinetic.Star, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGettersSetters(Kinetic.Star, ['numPoints', 'innerRadius', 'outerRadius']);

    /**
     * set number of points
     * @name setNumPoints
     * @methodOf Kinetic.Star.prototype
     * @param {Integer} points
     */

    /**
     * set outer radius
     * @name setOuterRadius
     * @methodOf Kinetic.Star.prototype
     * @param {Number} radius
     */

    /**
     * set inner radius
     * @name setInnerRadius
     * @methodOf Kinetic.Star.prototype
     * @param {Number} radius
     */

    /**
     * get number of points
     * @name getNumPoints
     * @methodOf Kinetic.Star.prototype
     */

    /**
     * get outer radius
     * @name getOuterRadius
     * @methodOf Kinetic.Star.prototype
     */

    /**
     * get inner radius
     * @name getInnerRadius
     * @methodOf Kinetic.Star.prototype
     */
})();

(function() {
    /**
     * RegularPolygon constructor.&nbsp; Examples include triangles, squares, pentagons, hexagons, etc.
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Number} config.sides
     * @param {Number} config.radius
     *
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.RegularPolygon = function(config) {
        this._initRegularPolygon(config);
    };

    Kinetic.RegularPolygon.prototype = {
        _initRegularPolygon: function(config) {
            this.setDefaultAttrs({
                radius: 0,
                sides: 0
            });

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'RegularPolygon';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext(), sides = this.attrs.sides, radius = this.attrs.radius;
            context.beginPath();
            context.moveTo(0, 0 - radius);

            for(var n = 1; n < sides; n++) {
                var x = radius * Math.sin(n * 2 * Math.PI / sides);
                var y = -1 * radius * Math.cos(n * 2 * Math.PI / sides);
                context.lineTo(x, y);
            }
            context.closePath();
            canvas.fillStroke(this);
        }
    };
    Kinetic.Global.extend(Kinetic.RegularPolygon, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGettersSetters(Kinetic.RegularPolygon, ['radius', 'sides']);

    /**
     * set radius
     * @name setRadius
     * @methodOf Kinetic.RegularPolygon.prototype
     * @param {Number} radius
     */

    /**
     * set number of sides
     * @name setSides
     * @methodOf Kinetic.RegularPolygon.prototype
     * @param {int} sides
     */

    /**
     * get radius
     * @name getRadius
     * @methodOf Kinetic.RegularPolygon.prototype
     */

    /**
     * get number of sides
     * @name getSides
     * @methodOf Kinetic.RegularPolygon.prototype
     */
})();

(function() {
    /**
     * Path constructor.
     * @author Jason Follas
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {String} config.data SVG data string
     *
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Path = function(config) {
        this._initPath(config);
    };

    Kinetic.Path.prototype = {
        _initPath: function(config) {
            this.dataArray = [];
            var that = this;

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Path';
            this._setDrawFuncs();

            this.dataArray = Kinetic.Path.parsePathData(this.attrs.data);
            this.on('dataChange', function() {
                that.dataArray = Kinetic.Path.parsePathData(that.attrs.data);
            });
        },
        drawFunc: function(canvas) {
            var ca = this.dataArray, context = canvas.getContext();
            // context position
            context.beginPath();
            for(var n = 0; n < ca.length; n++) {
                var c = ca[n].command;
                var p = ca[n].points;
                switch (c) {
                    case 'L':
                        context.lineTo(p[0], p[1]);
                        break;
                    case 'M':
                        context.moveTo(p[0], p[1]);
                        break;
                    case 'C':
                        context.bezierCurveTo(p[0], p[1], p[2], p[3], p[4], p[5]);
                        break;
                    case 'Q':
                        context.quadraticCurveTo(p[0], p[1], p[2], p[3]);
                        break;
                    case 'A':
                        var cx = p[0], cy = p[1], rx = p[2], ry = p[3], theta = p[4], dTheta = p[5], psi = p[6], fs = p[7];

                        var r = (rx > ry) ? rx : ry;
                        var scaleX = (rx > ry) ? 1 : rx / ry;
                        var scaleY = (rx > ry) ? ry / rx : 1;

                        context.translate(cx, cy);
                        context.rotate(psi);
                        context.scale(scaleX, scaleY);
                        context.arc(0, 0, r, theta, theta + dTheta, 1 - fs);
                        context.scale(1 / scaleX, 1 / scaleY);
                        context.rotate(-psi);
                        context.translate(-cx, -cy);

                        break;
                    case 'z':
                        context.closePath();
                        break;
                }
            }
            canvas.fillStroke(this);
        }
    };
    Kinetic.Global.extend(Kinetic.Path, Kinetic.Shape);

    /*
     * Utility methods written by jfollas to
     * handle length and point measurements
     */
    Kinetic.Path.getLineLength = function(x1, y1, x2, y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    };
    Kinetic.Path.getPointOnLine = function(dist, P1x, P1y, P2x, P2y, fromX, fromY) {
        if(fromX === undefined) {
            fromX = P1x;
        }
        if(fromY === undefined) {
            fromY = P1y;
        }

        var m = (P2y - P1y) / ((P2x - P1x) + 0.00000001);
        var run = Math.sqrt(dist * dist / (1 + m * m));
        if(P2x < P1x)
            run *= -1;
        var rise = m * run;
        var pt;

        if((fromY - P1y) / ((fromX - P1x) + 0.00000001) === m) {
            pt = {
                x: fromX + run,
                y: fromY + rise
            };
        }
        else {
            var ix, iy;

            var len = this.getLineLength(P1x, P1y, P2x, P2y);
            if(len < 0.00000001) {
                return undefined;
            }
            var u = (((fromX - P1x) * (P2x - P1x)) + ((fromY - P1y) * (P2y - P1y)));
            u = u / (len * len);
            ix = P1x + u * (P2x - P1x);
            iy = P1y + u * (P2y - P1y);

            var pRise = this.getLineLength(fromX, fromY, ix, iy);
            var pRun = Math.sqrt(dist * dist - pRise * pRise);
            run = Math.sqrt(pRun * pRun / (1 + m * m));
            if(P2x < P1x)
                run *= -1;
            rise = m * run;
            pt = {
                x: ix + run,
                y: iy + rise
            };
        }

        return pt;
    };

    Kinetic.Path.getPointOnCubicBezier = function(pct, P1x, P1y, P2x, P2y, P3x, P3y, P4x, P4y) {
        function CB1(t) {
            return t * t * t;
        }
        function CB2(t) {
            return 3 * t * t * (1 - t);
        }
        function CB3(t) {
            return 3 * t * (1 - t) * (1 - t);
        }
        function CB4(t) {
            return (1 - t) * (1 - t) * (1 - t);
        }
        var x = P4x * CB1(pct) + P3x * CB2(pct) + P2x * CB3(pct) + P1x * CB4(pct);
        var y = P4y * CB1(pct) + P3y * CB2(pct) + P2y * CB3(pct) + P1y * CB4(pct);

        return {
            x: x,
            y: y
        };
    };
    Kinetic.Path.getPointOnQuadraticBezier = function(pct, P1x, P1y, P2x, P2y, P3x, P3y) {
        function QB1(t) {
            return t * t;
        }
        function QB2(t) {
            return 2 * t * (1 - t);
        }
        function QB3(t) {
            return (1 - t) * (1 - t);
        }
        var x = P3x * QB1(pct) + P2x * QB2(pct) + P1x * QB3(pct);
        var y = P3y * QB1(pct) + P2y * QB2(pct) + P1y * QB3(pct);

        return {
            x: x,
            y: y
        };
    };
    Kinetic.Path.getPointOnEllipticalArc = function(cx, cy, rx, ry, theta, psi) {
        var cosPsi = Math.cos(psi), sinPsi = Math.sin(psi);
        var pt = {
            x: rx * Math.cos(theta),
            y: ry * Math.sin(theta)
        };
        return {
            x: cx + (pt.x * cosPsi - pt.y * sinPsi),
            y: cy + (pt.x * sinPsi + pt.y * cosPsi)
        };
    };
    /**
     * get parsed data array from the data
     *  string.  V, v, H, h, and l data are converted to
     *  L data for the purpose of high performance Path
     *  rendering
     */
    Kinetic.Path.parsePathData = function(data) {
        // Path Data Segment must begin with a moveTo
        //m (x y)+  Relative moveTo (subsequent points are treated as lineTo)
        //M (x y)+  Absolute moveTo (subsequent points are treated as lineTo)
        //l (x y)+  Relative lineTo
        //L (x y)+  Absolute LineTo
        //h (x)+    Relative horizontal lineTo
        //H (x)+    Absolute horizontal lineTo
        //v (y)+    Relative vertical lineTo
        //V (y)+    Absolute vertical lineTo
        //z (closepath)
        //Z (closepath)
        //c (x1 y1 x2 y2 x y)+ Relative Bezier curve
        //C (x1 y1 x2 y2 x y)+ Absolute Bezier curve
        //q (x1 y1 x y)+       Relative Quadratic Bezier
        //Q (x1 y1 x y)+       Absolute Quadratic Bezier
        //t (x y)+    Shorthand/Smooth Relative Quadratic Bezier
        //T (x y)+    Shorthand/Smooth Absolute Quadratic Bezier
        //s (x2 y2 x y)+       Shorthand/Smooth Relative Bezier curve
        //S (x2 y2 x y)+       Shorthand/Smooth Absolute Bezier curve
        //a (rx ry x-axis-rotation large-arc-flag sweep-flag x y)+     Relative Elliptical Arc
        //A (rx ry x-axis-rotation large-arc-flag sweep-flag x y)+  Absolute Elliptical Arc

        // return early if data is not defined
        if(!data) {
            return [];
        }

        // command string
        var cs = data;

        // command chars
        var cc = ['m', 'M', 'l', 'L', 'v', 'V', 'h', 'H', 'z', 'Z', 'c', 'C', 'q', 'Q', 't', 'T', 's', 'S', 'a', 'A'];
        // convert white spaces to commas
        cs = cs.replace(new RegExp(' ', 'g'), ',');
        // create pipes so that we can split the data
        for(var n = 0; n < cc.length; n++) {
            cs = cs.replace(new RegExp(cc[n], 'g'), '|' + cc[n]);
        }
        // create array
        var arr = cs.split('|');
        var ca = [];
        // init context point
        var cpx = 0;
        var cpy = 0;
        for(var n = 1; n < arr.length; n++) {
            var str = arr[n];
            var c = str.charAt(0);
            str = str.slice(1);
            // remove ,- for consistency
            str = str.replace(new RegExp(',-', 'g'), '-');
            // add commas so that it's easy to split
            str = str.replace(new RegExp('-', 'g'), ',-');
            str = str.replace(new RegExp('e,-', 'g'), 'e-');
            var p = str.split(',');
            if(p.length > 0 && p[0] === '') {
                p.shift();
            }
            // convert strings to floats
            for(var i = 0; i < p.length; i++) {
                p[i] = parseFloat(p[i]);
            }
            while(p.length > 0) {
                if(isNaN(p[0]))// case for a trailing comma before next command
                    break;

                var cmd = null;
                var points = [];
                var startX = cpx, startY = cpy;

                // convert l, H, h, V, and v to L
                switch (c) {

                    // Note: Keep the lineTo's above the moveTo's in this switch
                    case 'l':
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'L';
                        points.push(cpx, cpy);
                        break;
                    case 'L':
                        cpx = p.shift();
                        cpy = p.shift();
                        points.push(cpx, cpy);
                        break;

                    // Note: lineTo handlers need to be above this point
                    case 'm':
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'M';
                        points.push(cpx, cpy);
                        c = 'l';
                        // subsequent points are treated as relative lineTo
                        break;
                    case 'M':
                        cpx = p.shift();
                        cpy = p.shift();
                        cmd = 'M';
                        points.push(cpx, cpy);
                        c = 'L';
                        // subsequent points are treated as absolute lineTo
                        break;

                    case 'h':
                        cpx += p.shift();
                        cmd = 'L';
                        points.push(cpx, cpy);
                        break;
                    case 'H':
                        cpx = p.shift();
                        cmd = 'L';
                        points.push(cpx, cpy);
                        break;
                    case 'v':
                        cpy += p.shift();
                        cmd = 'L';
                        points.push(cpx, cpy);
                        break;
                    case 'V':
                        cpy = p.shift();
                        cmd = 'L';
                        points.push(cpx, cpy);
                        break;
                    case 'C':
                        points.push(p.shift(), p.shift(), p.shift(), p.shift());
                        cpx = p.shift();
                        cpy = p.shift();
                        points.push(cpx, cpy);
                        break;
                    case 'c':
                        points.push(cpx + p.shift(), cpy + p.shift(), cpx + p.shift(), cpy + p.shift());
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'C';
                        points.push(cpx, cpy);
                        break;
                    case 'S':
                        var ctlPtx = cpx, ctlPty = cpy;
                        var prevCmd = ca[ca.length - 1];
                        if(prevCmd.command === 'C') {
                            ctlPtx = cpx + (cpx - prevCmd.points[2]);
                            ctlPty = cpy + (cpy - prevCmd.points[3]);
                        }
                        points.push(ctlPtx, ctlPty, p.shift(), p.shift());
                        cpx = p.shift();
                        cpy = p.shift();
                        cmd = 'C';
                        points.push(cpx, cpy);
                        break;
                    case 's':
                        var ctlPtx = cpx, ctlPty = cpy;
                        var prevCmd = ca[ca.length - 1];
                        if(prevCmd.command === 'C') {
                            ctlPtx = cpx + (cpx - prevCmd.points[2]);
                            ctlPty = cpy + (cpy - prevCmd.points[3]);
                        }
                        points.push(ctlPtx, ctlPty, cpx + p.shift(), cpy + p.shift());
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'C';
                        points.push(cpx, cpy);
                        break;
                    case 'Q':
                        points.push(p.shift(), p.shift());
                        cpx = p.shift();
                        cpy = p.shift();
                        points.push(cpx, cpy);
                        break;
                    case 'q':
                        points.push(cpx + p.shift(), cpy + p.shift());
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'Q';
                        points.push(cpx, cpy);
                        break;
                    case 'T':
                        var ctlPtx = cpx, ctlPty = cpy;
                        var prevCmd = ca[ca.length - 1];
                        if(prevCmd.command === 'Q') {
                            ctlPtx = cpx + (cpx - prevCmd.points[0]);
                            ctlPty = cpy + (cpy - prevCmd.points[1]);
                        }
                        cpx = p.shift();
                        cpy = p.shift();
                        cmd = 'Q';
                        points.push(ctlPtx, ctlPty, cpx, cpy);
                        break;
                    case 't':
                        var ctlPtx = cpx, ctlPty = cpy;
                        var prevCmd = ca[ca.length - 1];
                        if(prevCmd.command === 'Q') {
                            ctlPtx = cpx + (cpx - prevCmd.points[0]);
                            ctlPty = cpy + (cpy - prevCmd.points[1]);
                        }
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'Q';
                        points.push(ctlPtx, ctlPty, cpx, cpy);
                        break;
                    case 'A':
                        var rx = p.shift(), ry = p.shift(), psi = p.shift(), fa = p.shift(), fs = p.shift();
                        var x1 = cpx, y1 = cpy; cpx = p.shift(), cpy = p.shift();
                        cmd = 'A';
                        points = this.convertEndpointToCenterParameterization(x1, y1, cpx, cpy, fa, fs, rx, ry, psi);
                        break;
                    case 'a':
                        var rx = p.shift(), ry = p.shift(), psi = p.shift(), fa = p.shift(), fs = p.shift();
                        var x1 = cpx, y1 = cpy; cpx += p.shift(), cpy += p.shift();
                        cmd = 'A';
                        points = this.convertEndpointToCenterParameterization(x1, y1, cpx, cpy, fa, fs, rx, ry, psi);
                        break;
                }

                ca.push({
                    command: cmd || c,
                    points: points,
                    start: {
                        x: startX,
                        y: startY
                    },
                    pathLength: this.calcLength(startX, startY, cmd || c, points)
                });
            }

            if(c === 'z' || c === 'Z') {
                ca.push({
                    command: 'z',
                    points: [],
                    start: undefined,
                    pathLength: 0
                });
            }
        }

        return ca;
    };
    Kinetic.Path.calcLength = function(x, y, cmd, points) {
        var len, p1, p2;
        var path = Kinetic.Path;

        switch (cmd) {
            case 'L':
                return path.getLineLength(x, y, points[0], points[1]);
            case 'C':
                // Approximates by breaking curve into 100 line segments
                len = 0.0;
                p1 = path.getPointOnCubicBezier(0, x, y, points[0], points[1], points[2], points[3], points[4], points[5]);
                for( t = 0.01; t <= 1; t += 0.01) {
                    p2 = path.getPointOnCubicBezier(t, x, y, points[0], points[1], points[2], points[3], points[4], points[5]);
                    len += path.getLineLength(p1.x, p1.y, p2.x, p2.y);
                    p1 = p2;
                }
                return len;
            case 'Q':
                // Approximates by breaking curve into 100 line segments
                len = 0.0;
                p1 = path.getPointOnQuadraticBezier(0, x, y, points[0], points[1], points[2], points[3]);
                for( t = 0.01; t <= 1; t += 0.01) {
                    p2 = path.getPointOnQuadraticBezier(t, x, y, points[0], points[1], points[2], points[3]);
                    len += path.getLineLength(p1.x, p1.y, p2.x, p2.y);
                    p1 = p2;
                }
                return len;
            case 'A':
                // Approximates by breaking curve into line segments
                len = 0.0;
                var start = points[4];
                // 4 = theta
                var dTheta = points[5];
                // 5 = dTheta
                var end = points[4] + dTheta;
                var inc = Math.PI / 180.0;
                // 1 degree resolution
                if(Math.abs(start - end) < inc) {
                    inc = Math.abs(start - end);
                }
                // Note: for purpose of calculating arc length, not going to worry about rotating X-axis by angle psi
                p1 = path.getPointOnEllipticalArc(points[0], points[1], points[2], points[3], start, 0);
                if(dTheta < 0) {// clockwise
                    for( t = start - inc; t > end; t -= inc) {
                        p2 = path.getPointOnEllipticalArc(points[0], points[1], points[2], points[3], t, 0);
                        len += path.getLineLength(p1.x, p1.y, p2.x, p2.y);
                        p1 = p2;
                    }
                }
                else {// counter-clockwise
                    for( t = start + inc; t < end; t += inc) {
                        p2 = path.getPointOnEllipticalArc(points[0], points[1], points[2], points[3], t, 0);
                        len += path.getLineLength(p1.x, p1.y, p2.x, p2.y);
                        p1 = p2;
                    }
                }
                p2 = path.getPointOnEllipticalArc(points[0], points[1], points[2], points[3], end, 0);
                len += path.getLineLength(p1.x, p1.y, p2.x, p2.y);

                return len;
        }

        return 0;
    };
    Kinetic.Path.convertEndpointToCenterParameterization = function(x1, y1, x2, y2, fa, fs, rx, ry, psiDeg) {
        // Derived from: http://www.w3.org/TR/SVG/implnote.html#ArcImplementationNotes
        var psi = psiDeg * (Math.PI / 180.0);
        var xp = Math.cos(psi) * (x1 - x2) / 2.0 + Math.sin(psi) * (y1 - y2) / 2.0;
        var yp = -1 * Math.sin(psi) * (x1 - x2) / 2.0 + Math.cos(psi) * (y1 - y2) / 2.0;

        var lambda = (xp * xp) / (rx * rx) + (yp * yp) / (ry * ry);

        if(lambda > 1) {
            rx *= Math.sqrt(lambda);
            ry *= Math.sqrt(lambda);
        }

        var f = Math.sqrt((((rx * rx) * (ry * ry)) - ((rx * rx) * (yp * yp)) - ((ry * ry) * (xp * xp))) / ((rx * rx) * (yp * yp) + (ry * ry) * (xp * xp)));

        if(fa == fs) {
            f *= -1;
        }
        if(isNaN(f)) {
            f = 0;
        }

        var cxp = f * rx * yp / ry;
        var cyp = f * -ry * xp / rx;

        var cx = (x1 + x2) / 2.0 + Math.cos(psi) * cxp - Math.sin(psi) * cyp;
        var cy = (y1 + y2) / 2.0 + Math.sin(psi) * cxp + Math.cos(psi) * cyp;

        var vMag = function(v) {
            return Math.sqrt(v[0] * v[0] + v[1] * v[1]);
        };
        var vRatio = function(u, v) {
            return (u[0] * v[0] + u[1] * v[1]) / (vMag(u) * vMag(v));
        };
        var vAngle = function(u, v) {
            return (u[0] * v[1] < u[1] * v[0] ? -1 : 1) * Math.acos(vRatio(u, v));
        };
        var theta = vAngle([1, 0], [(xp - cxp) / rx, (yp - cyp) / ry]);
        var u = [(xp - cxp) / rx, (yp - cyp) / ry];
        var v = [(-1 * xp - cxp) / rx, (-1 * yp - cyp) / ry];
        var dTheta = vAngle(u, v);

        if(vRatio(u, v) <= -1) {
            dTheta = Math.PI;
        }
        if(vRatio(u, v) >= 1) {
            dTheta = 0;
        }
        if(fs === 0 && dTheta > 0) {
            dTheta = dTheta - 2 * Math.PI;
        }
        if(fs == 1 && dTheta < 0) {
            dTheta = dTheta + 2 * Math.PI;
        }
        return [cx, cy, rx, ry, theta, dTheta, psi, fs];
    };
    // add getters setters
    Kinetic.Node.addGettersSetters(Kinetic.Path, ['data']);

    /**
     * set SVG path data string.  This method
     *  also automatically parses the data string
     *  into a data array.  Currently supported SVG data:
     *  M, m, L, l, H, h, V, v, Q, q, T, t, C, c, S, s, A, a, Z, z
     * @name setData
     * @methodOf Kinetic.Path.prototype
     * @param {String} SVG path command string
     */

    /**
     * get SVG path data string
     * @name getData
     * @methodOf Kinetic.Path.prototype
     */
})();

(function() {
    /**
     * Path constructor.
     * @author Jason Follas
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {String} [config.fontFamily] default is Calibri
     * @param {Number} [config.fontSize] default is 12
     * @param {String} [config.fontStyle] can be normal, bold, or italic.  Default is normal
     * @param {String} config.text
     *
     *
     * @param {String} [config.fill] fill color
     *
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Array|Object} [config.fillPatternOffset] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillPatternScale] array with two elements or object with x and y component
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     *
     * @param {Array|Object} [config.fillLinearGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillLinearGradientEndPoint] array with two elements or object with x and y component
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     *
     * @param {Array|Object} [config.fillRadialGradientStartPoint] array with two elements or object with x and y component
     * @param {Array|Object} [config.fillRadialGradientEndPoint] array with two elements or object with x and y component
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     *
     * @param {String} [config.stroke] stroke color
     * @param {Number} [config.strokeWidth] stroke width
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Number} [config.shadowBlur]
     * @param {Obect} [config.shadowOffset]
     * @param {Number} [config.shadowOffset.x]
     * @param {Number} [config.shadowOffset.y]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Array} [config.dashArray]
     *
     *
     *
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale]
     * @param {Number} [config.scale.x]
     * @param {Number} [config.scale.y]
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offset.x]
     * @param {Number} [config.offset.y]
     * @param {Boolean} [config.draggable]
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.TextPath = function(config) {
        this._initTextPath(config);
    };

    Kinetic.TextPath.prototype = {
        _initTextPath: function(config) {
            this.setDefaultAttrs({
                fontFamily: 'Calibri',
                fontSize: 12,
                fontStyle: 'normal',
                text: ''
            });

            this.dummyCanvas = document.createElement('canvas');
            this.dataArray = [];
            var that = this;

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'TextPath';
            this._setDrawFuncs();

            this.dataArray = Kinetic.Path.parsePathData(this.attrs.data);
            this.on('dataChange', function() {
                that.dataArray = Kinetic.Path.parsePathData(this.attrs.data);
            });
            // update text data for certain attr changes
            var attrs = ['text', 'textStroke', 'textStrokeWidth'];
            for(var n = 0; n < attrs.length; n++) {
                var attr = attrs[n];
                this.on(attr + 'Change', that._setTextData);
            }
            that._setTextData();
        },
        drawFunc: function(canvas) {
            var charArr = this.charArr, context = canvas.getContext();

            context.font = this.attrs.fontStyle + ' ' + this.attrs.fontSize + 'pt ' + this.attrs.fontFamily;
            context.textBaseline = 'middle';
            context.textAlign = 'left';
            context.save();

            var glyphInfo = this.glyphInfo;
            for(var i = 0; i < glyphInfo.length; i++) {
                context.save();

                var p0 = glyphInfo[i].p0;
                var p1 = glyphInfo[i].p1;
                var ht = parseFloat(this.attrs.fontSize);

                context.translate(p0.x, p0.y);
                context.rotate(glyphInfo[i].rotation);

                canvas.fillStrokeText(this, glyphInfo[i].text);

                context.restore();

                //// To assist with debugging visually, uncomment following
                // context.beginPath();
                // if (i % 2)
                // context.strokeStyle = 'cyan';
                // else
                // context.strokeStyle = 'green';

                // context.moveTo(p0.x, p0.y);
                // context.lineTo(p1.x, p1.y);
                // context.stroke();
            }

            context.restore();
        },
        /**
         * get text width in pixels
         * @name getTextWidth
         * @methodOf Kinetic.TextPath.prototype
         */
        getTextWidth: function() {
            return this.textWidth;
        },
        /**
         * get text height in pixels
         * @name getTextHeight
         * @methodOf Kinetic.TextPath.prototype
         */
        getTextHeight: function() {
            return this.textHeight;
        },
        /**
         * set text
         * @name setText
         * @methodOf Kinetic.TextPath.prototype
         * @param {String} text
         */
        setText: function(text) {
            Kinetic.Text.prototype.setText.call(this, text);
        },
        _getTextSize: function(text) {
            var dummyCanvas = this.dummyCanvas;
            var context = dummyCanvas.getContext('2d');

            context.save();

            context.font = this.attrs.fontStyle + ' ' + this.attrs.fontSize + 'pt ' + this.attrs.fontFamily;
            var metrics = context.measureText(text);

            context.restore();

            return {
                width: metrics.width,
                height: parseInt(this.attrs.fontSize, 10)
            };
        },
        /**
         * set text data.
         */
        _setTextData: function() {

            var that = this;
            var size = this._getTextSize(this.attrs.text);
            this.textWidth = size.width;
            this.textHeight = size.height;

            this.glyphInfo = [];

            var charArr = this.attrs.text.split('');

            var p0, p1, pathCmd;

            var pIndex = -1;
            var currentT = 0;

            var getNextPathSegment = function() {
                currentT = 0;
                var pathData = that.dataArray;

                for(var i = pIndex + 1; i < pathData.length; i++) {
                    if(pathData[i].pathLength > 0) {
                        pIndex = i;

                        return pathData[i];
                    }
                    else if(pathData[i].command == 'M') {
                        p0 = {
                            x: pathData[i].points[0],
                            y: pathData[i].points[1]
                        };
                    }
                }

                return {};
            };
            var findSegmentToFitCharacter = function(c, before) {

                var glyphWidth = that._getTextSize(c).width;

                var currLen = 0;
                var attempts = 0;
                var needNextSegment = false;
                p1 = undefined;
                while(Math.abs(glyphWidth - currLen) / glyphWidth > 0.01 && attempts < 25) {
                    attempts++;
                    var cumulativePathLength = currLen;
                    while(pathCmd === undefined) {
                        pathCmd = getNextPathSegment();

                        if(pathCmd && cumulativePathLength + pathCmd.pathLength < glyphWidth) {
                            cumulativePathLength += pathCmd.pathLength;
                            pathCmd = undefined;
                        }
                    }

                    if(pathCmd === {} || p0 === undefined)
                        return undefined;

                    var needNewSegment = false;

                    switch (pathCmd.command) {
                        case 'L':
                            if(Kinetic.Path.getLineLength(p0.x, p0.y, pathCmd.points[0], pathCmd.points[1]) > glyphWidth) {
                                p1 = Kinetic.Path.getPointOnLine(glyphWidth, p0.x, p0.y, pathCmd.points[0], pathCmd.points[1], p0.x, p0.y);
                            }
                            else
                                pathCmd = undefined;
                            break;
                        case 'A':

                            var start = pathCmd.points[4];
                            // 4 = theta
                            var dTheta = pathCmd.points[5];
                            // 5 = dTheta
                            var end = pathCmd.points[4] + dTheta;

                            if(currentT === 0)
                                currentT = start + 0.00000001;
                            // Just in case start is 0
                            else if(glyphWidth > currLen)
                                currentT += (Math.PI / 180.0) * dTheta / Math.abs(dTheta);
                            else
                                currentT -= Math.PI / 360.0 * dTheta / Math.abs(dTheta);

                            if(Math.abs(currentT) > Math.abs(end)) {
                                currentT = end;
                                needNewSegment = true;
                            }
                            p1 = Kinetic.Path.getPointOnEllipticalArc(pathCmd.points[0], pathCmd.points[1], pathCmd.points[2], pathCmd.points[3], currentT, pathCmd.points[6]);
                            break;
                        case 'C':
                            if(currentT === 0) {
                                if(glyphWidth > pathCmd.pathLength)
                                    currentT = 0.00000001;
                                else
                                    currentT = glyphWidth / pathCmd.pathLength;
                            }
                            else if(glyphWidth > currLen)
                                currentT += (glyphWidth - currLen) / pathCmd.pathLength;
                            else
                                currentT -= (currLen - glyphWidth) / pathCmd.pathLength;

                            if(currentT > 1.0) {
                                currentT = 1.0;
                                needNewSegment = true;
                            }
                            p1 = Kinetic.Path.getPointOnCubicBezier(currentT, pathCmd.start.x, pathCmd.start.y, pathCmd.points[0], pathCmd.points[1], pathCmd.points[2], pathCmd.points[3], pathCmd.points[4], pathCmd.points[5]);
                            break;
                        case 'Q':
                            if(currentT === 0)
                                currentT = glyphWidth / pathCmd.pathLength;
                            else if(glyphWidth > currLen)
                                currentT += (glyphWidth - currLen) / pathCmd.pathLength;
                            else
                                currentT -= (currLen - glyphWidth) / pathCmd.pathLength;

                            if(currentT > 1.0) {
                                currentT = 1.0;
                                needNewSegment = true;
                            }
                            p1 = Kinetic.Path.getPointOnQuadraticBezier(currentT, pathCmd.start.x, pathCmd.start.y, pathCmd.points[0], pathCmd.points[1], pathCmd.points[2], pathCmd.points[3]);
                            break;

                    }

                    if(p1 !== undefined) {
                        currLen = Kinetic.Path.getLineLength(p0.x, p0.y, p1.x, p1.y);
                    }

                    if(needNewSegment) {
                        needNewSegment = false;
                        pathCmd = undefined;
                    }
                }
            };
            for(var i = 0; i < charArr.length; i++) {

                // Find p1 such that line segment between p0 and p1 is approx. width of glyph
                findSegmentToFitCharacter(charArr[i]);

                if(p0 === undefined || p1 === undefined)
                    break;

                var width = Kinetic.Path.getLineLength(p0.x, p0.y, p1.x, p1.y);

                // Note: Since glyphs are rendered one at a time, any kerning pair data built into the font will not be used.
                // Can foresee having a rough pair table built in that the developer can override as needed.

                var kern = 0;
                // placeholder for future implementation

                var midpoint = Kinetic.Path.getPointOnLine(kern + width / 2.0, p0.x, p0.y, p1.x, p1.y);

                var rotation = Math.atan2((p1.y - p0.y), (p1.x - p0.x));
                this.glyphInfo.push({
                    transposeX: midpoint.x,
                    transposeY: midpoint.y,
                    text: charArr[i],
                    rotation: rotation,
                    p0: p0,
                    p1: p1
                });
                p0 = p1;
            }
        }
    };
    Kinetic.Global.extend(Kinetic.TextPath, Kinetic.Shape);

    // add setters and getters
    Kinetic.Node.addGettersSetters(Kinetic.TextPath, ['fontFamily', 'fontSize', 'fontStyle']);
    Kinetic.Node.addGetters(Kinetic.TextPath, ['text']);

    // reference Text methods
    Kinetic.TextPath.prototype.fillText = Kinetic.Text.prototype.fillText;
    Kinetic.TextPath.prototype.strokeText = Kinetic.Text.prototype.strokeText;
    Kinetic.TextPath.prototype.fillStrokeText = Kinetic.Text.prototype.strokeText;

    /**
     * set font family
     * @name setFontFamily
     * @methodOf Kinetic.TextPath.prototype
     * @param {String} fontFamily
     */

    /**
     * set font size
     * @name setFontSize
     * @methodOf Kinetic.TextPath.prototype
     * @param {int} fontSize
     */

    /**
     * set font style.  Can be 'normal', 'italic', or 'bold'.  'normal' is the default.
     * @name setFontStyle
     * @methodOf Kinetic.TextPath.prototype
     * @param {String} fontStyle
     */

    /**
     * get font family
     * @name getFontFamily
     * @methodOf Kinetic.TextPath.prototype
     */

    /**
     * get font size
     * @name getFontSize
     * @methodOf Kinetic.TextPath.prototype
     */

    /**
     * get font style
     * @name getFontStyle
     * @methodOf Kinetic.TextPath.prototype
     */

    /**
     * get text
     * @name getText
     * @methodOf Kinetic.TextPath.prototype
     */
})();
