/**
 * jquery.slider - Slider ui control in jQuery
 *
 * Modified by
 * Denny Lim <hi.iamdenny@gmail.com>
 *
 * Written by
 * Egor Khmelev (hmelyoff@gmail.com)
 *
 * Licensed under the MIT (MIT-LICENSE.txt).
 *
 * @author Egor Khmelev
 * @version 1.1.1-RELEASE ($Id$)
 *
 * Dependencies
 *
 * jQuery (http://jquery.com)
 * jquery.numberformatter (http://code.google.com/p/jquery-numberformatter/)
 * tmpl (http://ejohn.org/blog/javascript-micro-templating/)
 * jquery.dependClass
 * draggable
 *
 **/

(function( $ ) {

    function isArray( value ){
        if( typeof value == "undefined" ) return false;

        if (value instanceof Array || (!(value instanceof Object) &&
            (Object.prototype.toString.call((value)) == '[object Array]') ||
            typeof value.length == 'number' &&
                typeof value.splice != 'undefined' &&
                typeof value.propertyIsEnumerable != 'undefined' &&
                !value.propertyIsEnumerable('splice')
            )) {
            return true;
        }

        return false;
    }

    $.jslider = function( node, settings ){
        var jNode = $(node);
        if( !jNode.data( "jslider" ) )
            jNode.data( "jslider", new jSlider( node, settings ) );

        return jNode.data( "jslider" );
    };

    $.fn.jslider = function( action, opt_value ){
        var returnValue, args = arguments;

        function isDef( val ){
            return val !== undefined;
        };

        function isDefAndNotNull( val ){
            return val != null;
        };

        this.each(function(){
            var self = $.jslider( this, action );

            // do actions
            if( typeof action == "string" ){
                switch( action ){
                    case "value":
                        if( isDef( args[ 1 ] ) && isDef( args[ 2 ] ) ){
                            var pointers = self.getPointers();
                            if( isDefAndNotNull( pointers[0] ) && isDefAndNotNull( args[1] ) ){
                                pointers[0].set( args[ 1 ] );
                                pointers[0].setIndexOver();
                            }

                            if( isDefAndNotNull( pointers[1] ) && isDefAndNotNull( args[2] ) ){
                                pointers[1].set( args[ 2 ] );
                                pointers[1].setIndexOver();
                            }
                        }

                        else if( isDef( args[ 1 ] ) ){
                            var pointers = self.getPointers();
                            if( isDefAndNotNull( pointers[0] ) && isDefAndNotNull( args[1] ) ){
                                pointers[0].set( args[ 1 ] );
                                pointers[0].setIndexOver();
                            }
                        }

                        else
                            returnValue = self.getValue();

                        break;

                    case "prc":
                        if( isDef( args[ 1 ] ) && isDef( args[ 2 ] ) ){
                            var pointers = self.getPointers();
                            if( isDefAndNotNull( pointers[0] ) && isDefAndNotNull( args[1] ) ){
                                pointers[0]._set( args[ 1 ] );
                                pointers[0].setIndexOver();
                            }

                            if( isDefAndNotNull( pointers[1] ) && isDefAndNotNull( args[2] ) ){
                                pointers[1]._set( args[ 2 ] );
                                pointers[1].setIndexOver();
                            }
                        }

                        else if( isDef( args[ 1 ] ) ){
                            var pointers = self.getPointers();
                            if( isDefAndNotNull( pointers[0] ) && isDefAndNotNull( args[1] ) ){
                                pointers[0]._set( args[ 1 ] );
                                pointers[0].setIndexOver();
                            }
                        }

                        else
                            returnValue = self.getPrcValue();

                        break;

                    case "calculatedValue":
                        var value = self.getValue().split(";");
                        returnValue = "";
                        for (var i=0; i < value.length; i++) {
                            returnValue += (i > 0 ? ";" : "") + self.nice( value[i] );
                        };

                        break;

                    case "skin":
                        self.setSkin( args[1] );

                        break;
                };

            }

            // return actual object
            else if( !action && !opt_value ){
                if( !isArray( returnValue ) )
                    returnValue = [];

                returnValue.push( self );
            }
        });

        // flatten array just with one slider
        if( isArray( returnValue ) && returnValue.length == 1 )
            returnValue = returnValue[ 0 ];

        return returnValue || this;
    };

    var OPTIONS = {

        settings: {
            from: 1,
            to: 10,
            step: 1,
            smooth: true,
            limits: true,
            round: 0,
            format: { format: "#,##0.##" },
            value: "5;7",
            dimension: ""
        },

        className: "jslider",
        selector: ".jslider-",

        template: tmpl(
            '<span class="<%=className%>">' +
                '<table><tr><td>' +
                '<div class="<%=className%>-bg">' +
                '<i class="l"></i><i class="f"></i><i class="r"></i>' +
                '<i class="v"></i>' +
                '</div>' +

                '<div class="<%=className%>-pointer <%=className%>-pointer-from"></div>' +
                '<div class="<%=className%>-pointer <%=className%>-pointer-to"></div>' +

                '<div class="<%=className%>-label"><span><%=settings.from%></span></div>' +
                '<div class="<%=className%>-label <%=className%>-label-to"><span><%=settings.to%></span><%=settings.dimension%></div>' +

                '<div class="<%=className%>-value <%=className%>-value-from"><span></span><%=settings.dimension%></div>' +
                '<div class="<%=className%>-value <%=className%>-value-to"><span></span><%=settings.dimension%></div>' +

                '<div class="<%=className%>-scale"><%=scale%></div>'+

                '</td></tr></table>' +
                '</span>'
        )

    };

    function jSlider(){
        return this.init.apply( this, arguments );
    };

    jSlider.prototype.init = function( node, settings ){
        this.settings = $.extend(true, {}, OPTIONS.settings, settings ? settings : {});

        // obj.sliderHandler = this;
        this.inputNode = $( node ).hide();

        this.settings.interval = this.settings.to-this.settings.from;
        this.settings.value = this.inputNode.attr("value");

        if( this.settings.calculate && $.isFunction( this.settings.calculate ) )
            this.nice = this.settings.calculate;

        if( this.settings.onstatechange && $.isFunction( this.settings.onstatechange ) )
            this.onstatechange = this.settings.onstatechange;

        this.is = {
            init: false
        };
        this.o = {};

        this.create();
    };

    jSlider.prototype.onstatechange = function(){

    };

    jSlider.prototype.create = function(){
        var $this = this;

        this.domNode = $( OPTIONS.template({
            className: OPTIONS.className,
            settings: {
                from: this.nice( this.settings.from ),
                to: this.nice( this.settings.to ),
                dimension: this.settings.dimension
            },
            scale: this.generateScale()
        }) );

        this.inputNode.after( this.domNode );
        this.drawScale();

        // set skin class
        if( this.settings.skin && this.settings.skin.length > 0 )
            this.setSkin( this.settings.skin );

        this.sizes = {
            domWidth: this.domNode.width(),
            domOffset: this.domNode.offset()
        };

        // find some objects
        $.extend(this.o, {
            pointers: {},
            labels: {
                0: {
                    o: this.domNode.find(OPTIONS.selector + "value").not(OPTIONS.selector + "value-to")
                },
                1: {
                    o: this.domNode.find(OPTIONS.selector + "value").filter(OPTIONS.selector + "value-to")
                }
            },
            limits: {
                0: this.domNode.find(OPTIONS.selector + "label").not(OPTIONS.selector + "label-to"),
                1: this.domNode.find(OPTIONS.selector + "label").filter(OPTIONS.selector + "label-to")
            }
        });

        $.extend(this.o.labels[0], {
            value: this.o.labels[0].o.find("span")
        });

        $.extend(this.o.labels[1], {
            value: this.o.labels[1].o.find("span")
        });


        if( !$this.settings.value.split(";")[1] ){
            this.settings.single = true;
            this.domNode.addDependClass("single");
        }

        if( !$this.settings.limits )
            this.domNode.addDependClass("limitless");

        this.domNode.find(OPTIONS.selector + "pointer").each(function( i ){
            var value = $this.settings.value.split(";")[i];
            if( value ){
                $this.o.pointers[i] = new jSliderPointer( this, i, $this );

                var prev = $this.settings.value.split(";")[i-1];
                if( prev && new Number(value) < new Number(prev) ) value = prev;

                value = value < $this.settings.from ? $this.settings.from : value;
                value = value > $this.settings.to ? $this.settings.to : value;

                $this.o.pointers[i].set( value, true );
            }
        });

        this.o.value = this.domNode.find(".v");
        this.is.init = true;

        $.each(this.o.pointers, function(i){
            $this.redraw(this);
        });

        (function(self){
            $(window).resize(function(){
                self.onresize();
            });
        })(this);

    };

    jSlider.prototype.setSkin = function( skin ){
        if( this.skin_ )
            this.domNode.removeDependClass( this.skin_, "_" );

        this.domNode.addDependClass( this.skin_ = skin, "_" );
    };

    jSlider.prototype.setPointersIndex = function( i ){
        $.each(this.getPointers(), function(i){
            this.index( i );
        });
    };

    jSlider.prototype.getPointers = function(){
        return this.o.pointers;
    };

    jSlider.prototype.generateScale = function(){
        if( this.settings.scale && this.settings.scale.length > 0 ){
            var str = "";
            var s = this.settings.scale;
            var prc = Math.round((100/(s.length-1))*10)/10;
            for( var i=0; i < s.length; i++ ){
                str += '<span style="left: ' + i*prc + '%">' + ( s[i] != '|' ? '<ins>' + s[i] + '</ins>' : '' ) + '</span>';
            };
            return str;
        } else return "";

        return "";
    };

    jSlider.prototype.drawScale = function(){
        this.domNode.find(OPTIONS.selector + "scale span ins").each(function(){
            $(this).css({ marginLeft: -$(this).outerWidth()/2 });
        });
    };

    jSlider.prototype.onresize = function(){
        var self = this;
        this.sizes = {
            domWidth: this.domNode.width(),
            domOffset: this.domNode.offset()
        };

        $.each(this.o.pointers, function(i){
            self.redraw(this);
        });
    };

    jSlider.prototype.update = function(){
        this.onresize();
        this.drawScale();
    };

    jSlider.prototype.limits = function( x, pointer ){
        // smooth
        if( !this.settings.smooth ){
            var step = this.settings.step*100 / ( this.settings.interval );
            x = Math.round( x/step ) * step;
        }

        var another = this.o.pointers[1-pointer.uid];
        if( another && pointer.uid && x < another.value.prc ) x = another.value.prc;
        if( another && !pointer.uid && x > another.value.prc ) x = another.value.prc;

        // base limit
        if( x < 0 ) x = 0;
        if( x > 100 ) x = 100;

        return Math.round( x*10 ) / 10;
    };

    jSlider.prototype.redraw = function( pointer ){
        if( !this.is.init ) return false;

        this.setValue();

        // redraw range line
        if( this.o.pointers[0] && this.o.pointers[1] )
            this.o.value.css({ left: this.o.pointers[0].value.prc + "%", width: ( this.o.pointers[1].value.prc - this.o.pointers[0].value.prc ) + "%" });

        this.o.labels[pointer.uid].value.html(
            this.nice(
                pointer.value.origin
            )
        );

        // redraw position of labels
        this.redrawLabels( pointer );

    };

    jSlider.prototype.redrawLabels = function( pointer ){

        function setPosition( label, sizes, prc ){
            sizes.margin = -sizes.label/2;

            // left limit
            label_left = sizes.border + sizes.margin;
            if( label_left < 0 )
                sizes.margin -= label_left;

            // right limit
            if( sizes.border+sizes.label / 2 > self.sizes.domWidth ){
                sizes.margin = 0;
                sizes.right = true;
            } else
                sizes.right = false;

            label.o.css({ left: prc + "%", marginLeft: sizes.margin, right: "auto" });
            if( sizes.right ) label.o.css({ left: "auto", right: 0 });
            return sizes;
        }

        var self = this;
        var label = this.o.labels[pointer.uid];
        var prc = pointer.value.prc;

        var sizes = {
            label: label.o.outerWidth(),
            right: false,
            border: ( prc * this.sizes.domWidth ) / 100
        };

        if( !this.settings.single ){
            // glue if near;
            var another = this.o.pointers[1-pointer.uid];
            var another_label = this.o.labels[another.uid];

            switch( pointer.uid ){
                case 0:
                    if( sizes.border+sizes.label / 2 > another_label.o.offset().left-this.sizes.domOffset.left ){
                        another_label.o.css({ visibility: "hidden" });
                        another_label.value.html( this.nice( another.value.origin ) );

                        label.o.css({ visibility: "visible" });

                        prc = ( another.value.prc - prc ) / 2 + prc;
                        if( another.value.prc != pointer.value.prc ){
                            label.value.html( this.nice(pointer.value.origin) + "&nbsp;&ndash;&nbsp;" + this.nice(another.value.origin) );
                            sizes.label = label.o.outerWidth();
                            sizes.border = ( prc * this.sizes.domWidth ) / 100;
                        }
                    } else {
                        another_label.o.css({ visibility: "visible" });
                    }
                    break;

                case 1:
                    if( sizes.border - sizes.label / 2 < another_label.o.offset().left - this.sizes.domOffset.left + another_label.o.outerWidth() ){
                        another_label.o.css({ visibility: "hidden" });
                        another_label.value.html( this.nice(another.value.origin) );

                        label.o.css({ visibility: "visible" });

                        prc = ( prc - another.value.prc ) / 2 + another.value.prc;
                        if( another.value.prc != pointer.value.prc ){
                            label.value.html( this.nice(another.value.origin) + "&nbsp;&ndash;&nbsp;" + this.nice(pointer.value.origin) );
                            sizes.label = label.o.outerWidth();
                            sizes.border = ( prc * this.sizes.domWidth ) / 100;
                        }
                    } else {
                        another_label.o.css({ visibility: "visible" });
                    }
                    break;
            }
        }

        sizes = setPosition( label, sizes, prc );

        /* draw second label */
        if( another_label ){
            var sizes = {
                label: another_label.o.outerWidth(),
                right: false,
                border: ( another.value.prc * this.sizes.domWidth ) / 100
            };
            sizes = setPosition( another_label, sizes, another.value.prc );
        }

        this.redrawLimits();
    };

    jSlider.prototype.redrawLimits = function(){
        if( this.settings.limits ){

            var limits = [ true, true ];

            for( key in this.o.pointers ){

                if( !this.settings.single || key == 0 ){

                    var pointer = this.o.pointers[key];
                    var label = this.o.labels[pointer.uid];
                    var label_left = label.o.offset().left - this.sizes.domOffset.left;

                    var limit = this.o.limits[0];
                    if( label_left < limit.outerWidth() )
                        limits[0] = false;

                    var limit = this.o.limits[1];
                    if( label_left + label.o.outerWidth() > this.sizes.domWidth - limit.outerWidth() )
                        limits[1] = false;
                }

            };

            for( var i=0; i < limits.length; i++ ){
                if( limits[i] )
                    this.o.limits[i].fadeIn("fast");
                else
                    this.o.limits[i].fadeOut("fast");
            };

        }
    };

    jSlider.prototype.setValue = function(){
        var value = this.getValue();
        this.inputNode.attr( "value", value );
        this.onstatechange.call( this, value );
    };

    jSlider.prototype.getValue = function(){
        if(!this.is.init) return false;
        var $this = this;

        var value = "";
        $.each( this.o.pointers, function(i){
            if( this.value.prc != undefined && !isNaN(this.value.prc) ) value += (i > 0 ? ";" : "") + $this.prcToValue( this.value.prc );
        });
        return value;
    };

    jSlider.prototype.getPrcValue = function(){
        if(!this.is.init) return false;
        var $this = this;

        var value = "";
        $.each( this.o.pointers, function(i){
            if( this.value.prc != undefined && !isNaN(this.value.prc) ) value += (i > 0 ? ";" : "") + this.value.prc;
        });
        return value;
    };

    jSlider.prototype.prcToValue = function( prc ){

        if( this.settings.heterogeneity && this.settings.heterogeneity.length > 0 ){
            var h = this.settings.heterogeneity;

            var _start = 0;
            var _from = this.settings.from;

            for( var i=0; i <= h.length; i++ ){
                if( h[i] ) var v = h[i].split("/");
                else       var v = [100, this.settings.to];

                v[0] = new Number(v[0]);
                v[1] = new Number(v[1]);

                if( prc >= _start && prc <= v[0] ) {
                    var value = _from + ( (prc-_start) * (v[1]-_from) ) / (v[0]-_start);
                }

                _start = v[0];
                _from = v[1];
            };

        } else {
            var value = this.settings.from + ( prc * this.settings.interval ) / 100;
        }

        return this.round( value );
    };

    jSlider.prototype.valueToPrc = function( value, pointer ){
        if( this.settings.heterogeneity && this.settings.heterogeneity.length > 0 ){
            var h = this.settings.heterogeneity;

            var _start = 0;
            var _from = this.settings.from;

            for (var i=0; i <= h.length; i++) {
                if(h[i]) var v = h[i].split("/");
                else     var v = [100, this.settings.to];
                v[0] = new Number(v[0]); v[1] = new Number(v[1]);

                if(value >= _from && value <= v[1]){
                    var prc = pointer.limits(_start + (value-_from)*(v[0]-_start)/(v[1]-_from));
                }

                _start = v[0]; _from = v[1];
            };

        } else {
            var prc = pointer.limits((value-this.settings.from)*100/this.settings.interval);
        }

        return prc;
    };

    jSlider.prototype.round = function( value ){
        value = Math.round( value / this.settings.step ) * this.settings.step;
        if( this.settings.round ) value = Math.round( value * Math.pow(10, this.settings.round) ) / Math.pow(10, this.settings.round);
        else value = Math.round( value );
        return value;
    };

    jSlider.prototype.nice = function( value ){
        value = value.toString().replace(/,/gi, ".").replace(/ /gi, "");;

        if( $.formatNumber ){
            return $.formatNumber( new Number(value), this.settings.format || {} ).replace( /-/gi, "&minus;" );
        }

        else {
            return new Number(value);
        }
    };


    function jSliderPointer(){
        Draggable.apply( this, arguments );
    }
    jSliderPointer.prototype = new Draggable();

    jSliderPointer.prototype.oninit = function( ptr, id, _constructor ){
        this.uid = id;
        this.parent = _constructor;
        this.value = {};
        this.settings = this.parent.settings;
    };

    jSliderPointer.prototype.onmousedown = function(evt){
        var doMore = false;
        if( this.parent.settings.beforeMouseDown && $.isFunction(this.parent.settings.beforeMouseDown) ){
            doMore = this.parent.settings.beforeMouseDown.call( this.parent, evt, this.parent.getValue() );
        }
        if(doMore === false) return;

        this._parent = {
            offset: this.parent.domNode.offset(),
            width: this.parent.domNode.width()
        };
        this.ptr.addDependClass("hover");
        this.setIndexOver();
    };

    jSliderPointer.prototype.onmousemove = function( evt, x ){
        var doMore = false;
        if( this.parent.settings.beforeMouseMove && $.isFunction(this.parent.settings.beforeMouseMove) ){
            doMore = this.parent.settings.beforeMouseMove.call( this.parent, evt, this.parent.getValue() );
        }
        if(doMore === false) return;

        var coords = this._getPageCoords( evt );
        this._set( this.calc( coords.x ) );
    };

    jSliderPointer.prototype.onmouseup = function( evt ){
        if( this.parent.settings.beforeMouseUp && $.isFunction(this.parent.settings.beforeMouseUp) )
            this.parent.settings.beforeMouseUp.call( this.parent, evt, this.parent.getValue() );

        this.ptr.removeDependClass("hover");
    };

    jSliderPointer.prototype.setIndexOver = function(){
        this.parent.setPointersIndex( 1 );
        this.index( 2 );
    };

    jSliderPointer.prototype.index = function( i ){
        this.ptr.css({ zIndex: i });
    };

    jSliderPointer.prototype.limits = function( x ){
        return this.parent.limits( x, this );
    };

    jSliderPointer.prototype.calc = function(coords){
        var x = this.limits(((coords-this._parent.offset.left)*100)/this._parent.width);
        return x;
    };

    jSliderPointer.prototype.set = function( value, opt_origin ){
        this.value.origin = this.parent.round(value);
        this._set( this.parent.valueToPrc( value, this ), opt_origin );
    };

    jSliderPointer.prototype._set = function( prc, opt_origin ){
        if( !opt_origin )
            this.value.origin = this.parent.prcToValue(prc);

        this.value.prc = prc;
        this.ptr.css({ left: prc + "%" });
        this.parent.redraw(this);
    };

})(jQuery);
