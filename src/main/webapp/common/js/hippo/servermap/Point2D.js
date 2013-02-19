/*****
*
*   Point2D.js
*
*   copyright 2001-2002, Kevin Lindsey
*
*****/

/*****
*
*   Point2D
*
*****/

/*****
*
*   constructor
*
*****/
function Point2D(x, y) {
    if ( arguments.length > 0 ) {
        this.x = x;
        this.y = y;
    }
}


/*****
*
*   clone
*
*****/
Point2D.prototype.clone = function() {
    return new Point2D(this.x, this.y);
};


/*****
*
*   add
*
*****/
Point2D.prototype.add = function(that) {
    return new Point2D(this.x+that.x, this.y+that.y);
};


/*****
*
*   addEquals
*
*****/
Point2D.prototype.addEquals = function(that) {
    this.x += that.x;
    this.y += that.y;

    return this;
};


/*****
*
*   offset - used in dom_graph
*
*   This method is based on code written by Walter Korman
*      http://www.go2net.com/internet/deep/1997/05/07/body.html 
*   which is in turn based on an algorithm by Sven Moen
*
*****/
Point2D.prototype.offset = function(a, b) {
    var result = 0;

    if ( !( b.x <= this.x || this.x + a.x <= 0 ) ) {
        var t = b.x * a.y - a.x * b.y;
        var s;
        var d;

        if ( t > 0 ) {
            if ( this.x < 0 ) {
                s = this.x * a.y;
                d = s / a.x - this.y;
            } else if ( this.x > 0 ) {
                s = this.x * b.y;
                d = s / b.x - this.y
            } else {
                d = -this.y;
            }
        } else {
            if ( b.x < this.x + a.x ) {
                s = ( b.x - this.x ) * a.y;
                d = b.y - (this.y + s / a.x);
            } else if ( b.x > this.x + a.x ) {
                s = (a.x + this.x) * b.y;
                d = s / b.x - (this.y + a.y);
            } else {
                d = b.y - (this.y + a.y);
            }
        }

        if ( d > 0 ) {
            result = d;
        }
    }

    return result;
};


/*****
*
*   rmoveto
*
*****/
Point2D.prototype.rmoveto = function(dx, dy) {
    this.x += dx;
    this.y += dy;
};


/*****
*
*   scalarAdd
*
*****/
Point2D.prototype.scalarAdd = function(scalar) {
    return new Point2D(this.x+scalar, this.y+scalar);
};


/*****
*
*   scalarAddEquals
*
*****/
Point2D.prototype.scalarAddEquals = function(scalar) {
    this.x += scalar;
    this.y += scalar;

    return this;
};


/*****
*
*   subtract
*
*****/
Point2D.prototype.subtract = function(that) {
    return new Point2D(this.x-that.x, this.y-that.y);
};


/*****
*
*   subtractEquals
*
*****/
Point2D.prototype.subtractEquals = function(that) {
    this.x -= that.x;
    this.y -= that.y;

    return this;
};


/*****
*
*   scalarSubtract
*
*****/
Point2D.prototype.scalarSubtract = function(scalar) {
    return new Point2D(this.x-scalar, this.y-scalar);
};


/*****
*
*   scalarSubtractEquals
*
*****/
Point2D.prototype.scalarSubtractEquals = function(scalar) {
    this.x -= scalar;
    this.y -= scalar;

    return this;
};


/*****
*
*   multiply
*
*****/
Point2D.prototype.multiply = function(scalar) {
    return new Point2D(this.x*scalar, this.y*scalar);
};


/*****
*
*   multiplyEquals
*
*****/
Point2D.prototype.multiplyEquals = function(scalar) {
    this.x *= scalar;
    this.y *= scalar;

    return this;
};


/*****
*
*   divide
*
*****/
Point2D.prototype.divide = function(scalar) {
    return new Point2D(this.x/scalar, this.y/scalar);
};


/*****
*
*   divideEquals
*
*****/
Point2D.prototype.divideEquals = function(scalar) {
    this.x /= scalar;
    this.y /= scalar;

    return this;
};


/*****
*
*   comparison methods
*
*   these were a nice idea, but ...  It would be better to define these names
*   in two parts so that the first part is the x comparison and the second is
*   the y.  For example, to test p1.x < p2.x and p1.y >= p2.y, you would call
*   p1.lt_gte(p2).  Honestly, I only did these types of comparisons in one
*   Intersection routine, so these probably could be removed.
*
*****/

/*****
*
*   compare
*
*****/
Point2D.prototype.compare = function(that) {
    return (this.x - that.x || this.y - that.y);
};


/*****
*
*   eq - equal
*
*****/
Point2D.prototype.eq = function(that) {
    return ( this.x == that.x && this.y == that.y );
};


/*****
*
*   lt - less than
*
*****/
Point2D.prototype.lt = function(that) {
    return ( this.x < that.x && this.y < that.y );
};


/*****
*
*   lte - less than or equal
*
*****/
Point2D.prototype.lte = function(that) {
    return ( this.x <= that.x && this.y <= that.y );
};


/*****
*
*   gt - greater than
*
*****/
Point2D.prototype.gt = function(that) {
    return ( this.x > that.x && this.y > that.y );
};


/*****
*
*   gte - greater than or equal
*
*****/
Point2D.prototype.gte = function(that) {
    return ( this.x >= that.x && this.y >= that.y );
};


/*****
*
*   utility methods
*
*****/

/*****
*
*   lerp
*
*****/
Point2D.prototype.lerp = function(that, t) {
    return new Point2D(
        this.x + (that.x - this.x) * t,
        this.y + (that.y - this.y) * t
    );
};


/*****
*
*   distanceFrom
*
*****/
Point2D.prototype.distanceFrom = function(that) {
    var dx = this.x - that.x;
    var dy = this.y - that.y;

    return Math.sqrt(dx*dx + dy*dy);
};


/*****
*
*   min
*
*****/
Point2D.prototype.min = function(that) {
    return new Point2D(
        Math.min( this.x, that.x ),
        Math.min( this.y, that.y )
    );
};


/*****
*
*   max
*
*****/
Point2D.prototype.max = function(that) {
    return new Point2D(
        Math.max( this.x, that.x ),
        Math.max( this.y, that.y )
    );
};


/*****
*
*   toString
*
*****/
Point2D.prototype.toString = function() {
    return this.x + "," + this.y;
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   setXY
*
*****/
Point2D.prototype.setXY = function(x, y) {
    this.x = x;
    this.y = y;
};


/*****
*
*   setFromPoint
*
*****/
Point2D.prototype.setFromPoint = function(that) {
    this.x = that.x;
    this.y = that.y;
};


/*****
*
*   swap
*
*****/
Point2D.prototype.swap = function(that) {
    var x = this.x;
    var y = this.y;

    this.x = that.x;
    this.y = that.y;

    that.x = x;
    that.y = y;
};