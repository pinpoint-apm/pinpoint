/*****
*
*   Intersection.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   constructor
*
*****/
function Intersection(status) {
    if ( arguments.length > 0 ) {
        this.init(status);
    }
}


/*****
*
*   init
*
*****/
Intersection.prototype.init = function(status) {
    this.status = status;
    this.points = new Array();
};


/*****
*
*   appendPoint
*
*****/
Intersection.prototype.appendPoint = function(point) {
    this.points.push(point);
};


/*****
*
*   appendPoints
*
*****/
Intersection.prototype.appendPoints = function(points) {
    this.points = this.points.concat(points);
};


/*****
*
*   class methods
*
*****/

/*****
*
*   intersectShapes
*
*****/
Intersection.intersectShapes = function(shape1, shape2) {
    var ip1 = shape1.getIntersectionParams();
    var ip2 = shape2.getIntersectionParams();
    var result;

    if ( ip1 != null && ip2 != null ) {
        if ( ip1.name == "Path" ) {
            result = Intersection.intersectPathShape(shape1, shape2);
        } else if ( ip2.name == "Path" ) {
            result = Intersection.intersectPathShape(shape2, shape1);
        } else {
            var method;
            var params;

            if ( ip1.name < ip2.name ) {
                method = "intersect" + ip1.name + ip2.name;
                params = ip1.params.concat( ip2.params );
            } else {
                method = "intersect" + ip2.name + ip1.name;
                params = ip2.params.concat( ip1.params );
            }

            if ( !(method in Intersection) )
                throw new Error("Intersection not available: " + method);

            result = Intersection[method].apply(null, params);
        }
    } else {
        result = new Intersection("No Intersection");
    }

    return result;
};


/*****
*
*   intersectPathShape
*
*****/
Intersection.intersectPathShape = function(path, shape) {
    return path.intersectShape(shape);
};


/*****
*
*   intersectBezier2Bezier2
*
*****/
Intersection.intersectBezier2Bezier2 = function(a1, a2, a3, b1, b2, b3) {
    var a, b;
    var c12, c11, c10;
    var c22, c21, c20;
    var result = new Intersection("No Intersection");
    var poly;

    a = a2.multiply(-2);
    c12 = a1.add(a.add(a3));

    a = a1.multiply(-2);
    b = a2.multiply(2);
    c11 = a.add(b);

    c10 = new Point2D(a1.x, a1.y);

    a = b2.multiply(-2);
    c22 = b1.add(a.add(b3));

    a = b1.multiply(-2);
    b = b2.multiply(2);
    c21 = a.add(b);

    c20 = new Point2D(b1.x, b1.y);
    
    if ( c12.y == 0 ) {
        var v0 = c12.x*(c10.y - c20.y);
        var v1 = v0 - c11.x*c11.y;
        var v2 = v0 + v1;
        var v3 = c11.y*c11.y;

        poly = new Polynomial(
            c12.x*c22.y*c22.y,
            2*c12.x*c21.y*c22.y,
            c12.x*c21.y*c21.y - c22.x*v3 - c22.y*v0 - c22.y*v1,
            -c21.x*v3 - c21.y*v0 - c21.y*v1,
            (c10.x - c20.x)*v3 + (c10.y - c20.y)*v1
        );
    } else {
        var v0 = c12.x*c22.y - c12.y*c22.x;
        var v1 = c12.x*c21.y - c21.x*c12.y;
        var v2 = c11.x*c12.y - c11.y*c12.x;
        var v3 = c10.y - c20.y;
        var v4 = c12.y*(c10.x - c20.x) - c12.x*v3;
        var v5 = -c11.y*v2 + c12.y*v4;
        var v6 = v2*v2;

        poly = new Polynomial(
            v0*v0,
            2*v0*v1,
            (-c22.y*v6 + c12.y*v1*v1 + c12.y*v0*v4 + v0*v5) / c12.y,
            (-c21.y*v6 + c12.y*v1*v4 + v1*v5) / c12.y,
            (v3*v6 + v4*v5) / c12.y
        );
    }

    var roots = poly.getRoots();
    for ( var i = 0; i < roots.length; i++ ) {
        var s = roots[i];

        if ( 0 <= s && s <= 1 ) {
            var xRoots = new Polynomial(
                c12.x,
                c11.x,
                c10.x - c20.x - s*c21.x - s*s*c22.x
            ).getRoots();
            var yRoots = new Polynomial(
                c12.y,
                c11.y,
                c10.y - c20.y - s*c21.y - s*s*c22.y
            ).getRoots();

            if ( xRoots.length > 0 && yRoots.length > 0 ) {
                var TOLERANCE = 1e-4;

                checkRoots:
                for ( var j = 0; j < xRoots.length; j++ ) {
                    var xRoot = xRoots[j];

                    if ( 0 <= xRoot && xRoot <= 1 ) {
                        for ( var k = 0; k < yRoots.length; k++ ) {
                            if ( Math.abs( xRoot - yRoots[k] ) < TOLERANCE ) {
                                result.points.push( c22.multiply(s*s).add(c21.multiply(s).add(c20)) );
                                break checkRoots;
                            }
                        }
                    }
                }
            }
        }
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier2Bezier3
*
*****/
Intersection.intersectBezier2Bezier3 = function(a1, a2, a3, b1, b2, b3, b4) {
    var a, b,c, d;
    var c12, c11, c10;
    var c23, c22, c21, c20;
    var result = new Intersection("No Intersection");

    a = a2.multiply(-2);
    c12 = a1.add(a.add(a3));

    a = a1.multiply(-2);
    b = a2.multiply(2);
    c11 = a.add(b);

    c10 = new Point2D(a1.x, a1.y);

    a = b1.multiply(-1);
    b = b2.multiply(3);
    c = b3.multiply(-3);
    d = a.add(b.add(c.add(b4)));
    c23 = new Vector2D(d.x, d.y);

    a = b1.multiply(3);
    b = b2.multiply(-6);
    c = b3.multiply(3);
    d = a.add(b.add(c));
    c22 = new Vector2D(d.x, d.y);

    a = b1.multiply(-3);
    b = b2.multiply(3);
    c = a.add(b);
    c21 = new Vector2D(c.x, c.y);

    c20 = new Vector2D(b1.x, b1.y);

    var c10x2 = c10.x*c10.x;
    var c10y2 = c10.y*c10.y;
    var c11x2 = c11.x*c11.x;
    var c11y2 = c11.y*c11.y;
    var c12x2 = c12.x*c12.x;
    var c12y2 = c12.y*c12.y;
    var c20x2 = c20.x*c20.x;
    var c20y2 = c20.y*c20.y;
    var c21x2 = c21.x*c21.x;
    var c21y2 = c21.y*c21.y;
    var c22x2 = c22.x*c22.x;
    var c22y2 = c22.y*c22.y;
    var c23x2 = c23.x*c23.x;
    var c23y2 = c23.y*c23.y;

    var poly = new Polynomial(
        -2*c12.x*c12.y*c23.x*c23.y + c12x2*c23y2 + c12y2*c23x2,
        -2*c12.x*c12.y*c22.x*c23.y - 2*c12.x*c12.y*c22.y*c23.x + 2*c12y2*c22.x*c23.x +
            2*c12x2*c22.y*c23.y,
        -2*c12.x*c21.x*c12.y*c23.y - 2*c12.x*c12.y*c21.y*c23.x - 2*c12.x*c12.y*c22.x*c22.y +
            2*c21.x*c12y2*c23.x + c12y2*c22x2 + c12x2*(2*c21.y*c23.y + c22y2),
        2*c10.x*c12.x*c12.y*c23.y + 2*c10.y*c12.x*c12.y*c23.x + c11.x*c11.y*c12.x*c23.y +
            c11.x*c11.y*c12.y*c23.x - 2*c20.x*c12.x*c12.y*c23.y - 2*c12.x*c20.y*c12.y*c23.x -
            2*c12.x*c21.x*c12.y*c22.y - 2*c12.x*c12.y*c21.y*c22.x - 2*c10.x*c12y2*c23.x -
            2*c10.y*c12x2*c23.y + 2*c20.x*c12y2*c23.x + 2*c21.x*c12y2*c22.x -
            c11y2*c12.x*c23.x - c11x2*c12.y*c23.y + c12x2*(2*c20.y*c23.y + 2*c21.y*c22.y),
        2*c10.x*c12.x*c12.y*c22.y + 2*c10.y*c12.x*c12.y*c22.x + c11.x*c11.y*c12.x*c22.y +
            c11.x*c11.y*c12.y*c22.x - 2*c20.x*c12.x*c12.y*c22.y - 2*c12.x*c20.y*c12.y*c22.x -
            2*c12.x*c21.x*c12.y*c21.y - 2*c10.x*c12y2*c22.x - 2*c10.y*c12x2*c22.y +
            2*c20.x*c12y2*c22.x - c11y2*c12.x*c22.x - c11x2*c12.y*c22.y + c21x2*c12y2 +
            c12x2*(2*c20.y*c22.y + c21y2),
        2*c10.x*c12.x*c12.y*c21.y + 2*c10.y*c12.x*c21.x*c12.y + c11.x*c11.y*c12.x*c21.y +
            c11.x*c11.y*c21.x*c12.y - 2*c20.x*c12.x*c12.y*c21.y - 2*c12.x*c20.y*c21.x*c12.y -
            2*c10.x*c21.x*c12y2 - 2*c10.y*c12x2*c21.y + 2*c20.x*c21.x*c12y2 -
            c11y2*c12.x*c21.x - c11x2*c12.y*c21.y + 2*c12x2*c20.y*c21.y,
        -2*c10.x*c10.y*c12.x*c12.y - c10.x*c11.x*c11.y*c12.y - c10.y*c11.x*c11.y*c12.x +
            2*c10.x*c12.x*c20.y*c12.y + 2*c10.y*c20.x*c12.x*c12.y + c11.x*c20.x*c11.y*c12.y +
            c11.x*c11.y*c12.x*c20.y - 2*c20.x*c12.x*c20.y*c12.y - 2*c10.x*c20.x*c12y2 +
            c10.x*c11y2*c12.x + c10.y*c11x2*c12.y - 2*c10.y*c12x2*c20.y -
            c20.x*c11y2*c12.x - c11x2*c20.y*c12.y + c10x2*c12y2 + c10y2*c12x2 +
            c20x2*c12y2 + c12x2*c20y2
    );
    var roots = poly.getRootsInInterval(0,1);

    for ( var i = 0; i < roots.length; i++ ) {
        var s = roots[i];
        var xRoots = new Polynomial(
            c12.x,
            c11.x,
            c10.x - c20.x - s*c21.x - s*s*c22.x - s*s*s*c23.x
        ).getRoots();
        var yRoots = new Polynomial(
            c12.y,
            c11.y,
            c10.y - c20.y - s*c21.y - s*s*c22.y - s*s*s*c23.y
        ).getRoots();

        if ( xRoots.length > 0 && yRoots.length > 0 ) {
            var TOLERANCE = 1e-4;

            checkRoots:
            for ( var j = 0; j < xRoots.length; j++ ) {
                var xRoot = xRoots[j];
                
                if ( 0 <= xRoot && xRoot <= 1 ) {
                    for ( var k = 0; k < yRoots.length; k++ ) {
                        if ( Math.abs( xRoot - yRoots[k] ) < TOLERANCE ) {
                            result.points.push(
                                c23.multiply(s*s*s).add(c22.multiply(s*s).add(c21.multiply(s).add(c20)))
                            );
                            break checkRoots;
                        }
                    }
                }
            }
        }
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;

};


/*****
*
*   intersectBezier2Circle
*
*****/
Intersection.intersectBezier2Circle = function(p1, p2, p3, c, r) {
    return Intersection.intersectBezier2Ellipse(p1, p2, p3, c, r, r);
};


/*****
*
*   intersectBezier2Ellipse
*
*****/
Intersection.intersectBezier2Ellipse = function(p1, p2, p3, ec, rx, ry) {
    var a, b;       // temporary variables
    var c2, c1, c0; // coefficients of quadratic
    var result = new Intersection("No Intersection");

    a = p2.multiply(-2);
    c2 = p1.add(a.add(p3));

    a = p1.multiply(-2);
    b = p2.multiply(2);
    c1 = a.add(b);

    c0 = new Point2D(p1.x, p1.y);

    var rxrx  = rx*rx;
    var ryry  = ry*ry;
    var roots = new Polynomial(
        ryry*c2.x*c2.x + rxrx*c2.y*c2.y,
        2*(ryry*c2.x*c1.x + rxrx*c2.y*c1.y),
        ryry*(2*c2.x*c0.x + c1.x*c1.x) + rxrx*(2*c2.y*c0.y+c1.y*c1.y) -
            2*(ryry*ec.x*c2.x + rxrx*ec.y*c2.y),
        2*(ryry*c1.x*(c0.x-ec.x) + rxrx*c1.y*(c0.y-ec.y)),
        ryry*(c0.x*c0.x+ec.x*ec.x) + rxrx*(c0.y*c0.y + ec.y*ec.y) -
            2*(ryry*ec.x*c0.x + rxrx*ec.y*c0.y) - rxrx*ryry
    ).getRoots();

    for ( var i = 0; i < roots.length; i++ ) {
        var t = roots[i];

        if ( 0 <= t && t <= 1 )
            result.points.push( c2.multiply(t*t).add(c1.multiply(t).add(c0)) );
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier2Line
*
*****/
Intersection.intersectBezier2Line = function(p1, p2, p3, a1, a2) {
    var a, b;             // temporary variables
    var c2, c1, c0;       // coefficients of quadratic
    var cl;               // c coefficient for normal form of line
    var n;                // normal for normal form of line
    var min = a1.min(a2); // used to determine if point is on line segment
    var max = a1.max(a2); // used to determine if point is on line segment
    var result = new Intersection("No Intersection");
    
    a = p2.multiply(-2);
    c2 = p1.add(a.add(p3));

    a = p1.multiply(-2);
    b = p2.multiply(2);
    c1 = a.add(b);

    c0 = new Point2D(p1.x, p1.y);

    // Convert line to normal form: ax + by + c = 0
    // Find normal to line: negative inverse of original line's slope
    n = new Vector2D(a1.y - a2.y, a2.x - a1.x);
    
    // Determine new c coefficient
    cl = a1.x*a2.y - a2.x*a1.y;

    // Transform cubic coefficients to line's coordinate system and find roots
    // of cubic
    roots = new Polynomial(
        n.dot(c2),
        n.dot(c1),
        n.dot(c0) + cl
    ).getRoots();

    // Any roots in closed interval [0,1] are intersections on Bezier, but
    // might not be on the line segment.
    // Find intersections and calculate point coordinates
    for ( var i = 0; i < roots.length; i++ ) {
        var t = roots[i];

        if ( 0 <= t && t <= 1 ) {
            // We're within the Bezier curve
            // Find point on Bezier
            var p4 = p1.lerp(p2, t);
            var p5 = p2.lerp(p3, t);

            var p6 = p4.lerp(p5, t);

            // See if point is on line segment
            // Had to make special cases for vertical and horizontal lines due
            // to slight errors in calculation of p6
            if ( a1.x == a2.x ) {
                if ( min.y <= p6.y && p6.y <= max.y ) {
                    result.status = "Intersection";
                    result.appendPoint( p6 );
                }
            } else if ( a1.y == a2.y ) {
                if ( min.x <= p6.x && p6.x <= max.x ) {
                    result.status = "Intersection";
                    result.appendPoint( p6 );
                }
            } else if ( p6.gte(min) && p6.lte(max) ) {
                result.status = "Intersection";
                result.appendPoint( p6 );
            }
        }
    }

    return result;
};


/*****
*
*   intersectBezier2Polygon
*
*****/
Intersection.intersectBezier2Polygon = function(p1, p2, p3, points) {
    var result = new Intersection("No Intersection");
    var length = points.length;

    for ( var i = 0; i < length; i++ ) {
        var a1 = points[i];
        var a2 = points[(i+1) % length];
        var inter = Intersection.intersectBezier2Line(p1, p2, p3, a1, a2);

        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier2Rectangle
*
*****/
Intersection.intersectBezier2Rectangle = function(p1, p2, p3, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectBezier2Line(p1, p2, p3, min, topRight);
    var inter2 = Intersection.intersectBezier2Line(p1, p2, p3, topRight, max);
    var inter3 = Intersection.intersectBezier2Line(p1, p2, p3, max, bottomLeft);
    var inter4 = Intersection.intersectBezier2Line(p1, p2, p3, bottomLeft, min);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier3Bezier3
*
*****/
Intersection.intersectBezier3Bezier3 = function(a1, a2, a3, a4, b1, b2, b3, b4) {
    var a, b, c, d;         // temporary variables
    var c13, c12, c11, c10; // coefficients of cubic
    var c23, c22, c21, c20; // coefficients of cubic
    var result = new Intersection("No Intersection");

    // Calculate the coefficients of cubic polynomial
    a = a1.multiply(-1);
    b = a2.multiply(3);
    c = a3.multiply(-3);
    d = a.add(b.add(c.add(a4)));
    c13 = new Vector2D(d.x, d.y);

    a = a1.multiply(3);
    b = a2.multiply(-6);
    c = a3.multiply(3);
    d = a.add(b.add(c));
    c12 = new Vector2D(d.x, d.y);

    a = a1.multiply(-3);
    b = a2.multiply(3);
    c = a.add(b);
    c11 = new Vector2D(c.x, c.y);

    c10 = new Vector2D(a1.x, a1.y);

    a = b1.multiply(-1);
    b = b2.multiply(3);
    c = b3.multiply(-3);
    d = a.add(b.add(c.add(b4)));
    c23 = new Vector2D(d.x, d.y);

    a = b1.multiply(3);
    b = b2.multiply(-6);
    c = b3.multiply(3);
    d = a.add(b.add(c));
    c22 = new Vector2D(d.x, d.y);

    a = b1.multiply(-3);
    b = b2.multiply(3);
    c = a.add(b);
    c21 = new Vector2D(c.x, c.y);

    c20 = new Vector2D(b1.x, b1.y);

    var c10x2 = c10.x*c10.x;
    var c10x3 = c10.x*c10.x*c10.x;
    var c10y2 = c10.y*c10.y;
    var c10y3 = c10.y*c10.y*c10.y;
    var c11x2 = c11.x*c11.x;
    var c11x3 = c11.x*c11.x*c11.x;
    var c11y2 = c11.y*c11.y;
    var c11y3 = c11.y*c11.y*c11.y;
    var c12x2 = c12.x*c12.x;
    var c12x3 = c12.x*c12.x*c12.x;
    var c12y2 = c12.y*c12.y;
    var c12y3 = c12.y*c12.y*c12.y;
    var c13x2 = c13.x*c13.x;
    var c13x3 = c13.x*c13.x*c13.x;
    var c13y2 = c13.y*c13.y;
    var c13y3 = c13.y*c13.y*c13.y;
    var c20x2 = c20.x*c20.x;
    var c20x3 = c20.x*c20.x*c20.x;
    var c20y2 = c20.y*c20.y;
    var c20y3 = c20.y*c20.y*c20.y;
    var c21x2 = c21.x*c21.x;
    var c21x3 = c21.x*c21.x*c21.x;
    var c21y2 = c21.y*c21.y;
    var c22x2 = c22.x*c22.x;
    var c22x3 = c22.x*c22.x*c22.x;
    var c22y2 = c22.y*c22.y;
    var c23x2 = c23.x*c23.x;
    var c23x3 = c23.x*c23.x*c23.x;
    var c23y2 = c23.y*c23.y;
    var c23y3 = c23.y*c23.y*c23.y;
    var poly = new Polynomial(
        -c13x3*c23y3 + c13y3*c23x3 - 3*c13.x*c13y2*c23x2*c23.y +
            3*c13x2*c13.y*c23.x*c23y2,
        -6*c13.x*c22.x*c13y2*c23.x*c23.y + 6*c13x2*c13.y*c22.y*c23.x*c23.y + 3*c22.x*c13y3*c23x2 -
            3*c13x3*c22.y*c23y2 - 3*c13.x*c13y2*c22.y*c23x2 + 3*c13x2*c22.x*c13.y*c23y2,
        -6*c21.x*c13.x*c13y2*c23.x*c23.y - 6*c13.x*c22.x*c13y2*c22.y*c23.x + 6*c13x2*c22.x*c13.y*c22.y*c23.y +
            3*c21.x*c13y3*c23x2 + 3*c22x2*c13y3*c23.x + 3*c21.x*c13x2*c13.y*c23y2 - 3*c13.x*c21.y*c13y2*c23x2 -
            3*c13.x*c22x2*c13y2*c23.y + c13x2*c13.y*c23.x*(6*c21.y*c23.y + 3*c22y2) + c13x3*(-c21.y*c23y2 -
            2*c22y2*c23.y - c23.y*(2*c21.y*c23.y + c22y2)),
        c11.x*c12.y*c13.x*c13.y*c23.x*c23.y - c11.y*c12.x*c13.x*c13.y*c23.x*c23.y + 6*c21.x*c22.x*c13y3*c23.x +
            3*c11.x*c12.x*c13.x*c13.y*c23y2 + 6*c10.x*c13.x*c13y2*c23.x*c23.y - 3*c11.x*c12.x*c13y2*c23.x*c23.y -
            3*c11.y*c12.y*c13.x*c13.y*c23x2 - 6*c10.y*c13x2*c13.y*c23.x*c23.y - 6*c20.x*c13.x*c13y2*c23.x*c23.y +
            3*c11.y*c12.y*c13x2*c23.x*c23.y - 2*c12.x*c12y2*c13.x*c23.x*c23.y - 6*c21.x*c13.x*c22.x*c13y2*c23.y -
            6*c21.x*c13.x*c13y2*c22.y*c23.x - 6*c13.x*c21.y*c22.x*c13y2*c23.x + 6*c21.x*c13x2*c13.y*c22.y*c23.y +
            2*c12x2*c12.y*c13.y*c23.x*c23.y + c22x3*c13y3 - 3*c10.x*c13y3*c23x2 + 3*c10.y*c13x3*c23y2 +
            3*c20.x*c13y3*c23x2 + c12y3*c13.x*c23x2 - c12x3*c13.y*c23y2 - 3*c10.x*c13x2*c13.y*c23y2 +
            3*c10.y*c13.x*c13y2*c23x2 - 2*c11.x*c12.y*c13x2*c23y2 + c11.x*c12.y*c13y2*c23x2 - c11.y*c12.x*c13x2*c23y2 +
            2*c11.y*c12.x*c13y2*c23x2 + 3*c20.x*c13x2*c13.y*c23y2 - c12.x*c12y2*c13.y*c23x2 -
            3*c20.y*c13.x*c13y2*c23x2 + c12x2*c12.y*c13.x*c23y2 - 3*c13.x*c22x2*c13y2*c22.y +
            c13x2*c13.y*c23.x*(6*c20.y*c23.y + 6*c21.y*c22.y) + c13x2*c22.x*c13.y*(6*c21.y*c23.y + 3*c22y2) +
            c13x3*(-2*c21.y*c22.y*c23.y - c20.y*c23y2 - c22.y*(2*c21.y*c23.y + c22y2) - c23.y*(2*c20.y*c23.y + 2*c21.y*c22.y)),
        6*c11.x*c12.x*c13.x*c13.y*c22.y*c23.y + c11.x*c12.y*c13.x*c22.x*c13.y*c23.y + c11.x*c12.y*c13.x*c13.y*c22.y*c23.x -
            c11.y*c12.x*c13.x*c22.x*c13.y*c23.y - c11.y*c12.x*c13.x*c13.y*c22.y*c23.x - 6*c11.y*c12.y*c13.x*c22.x*c13.y*c23.x -
            6*c10.x*c22.x*c13y3*c23.x + 6*c20.x*c22.x*c13y3*c23.x + 6*c10.y*c13x3*c22.y*c23.y + 2*c12y3*c13.x*c22.x*c23.x -
            2*c12x3*c13.y*c22.y*c23.y + 6*c10.x*c13.x*c22.x*c13y2*c23.y + 6*c10.x*c13.x*c13y2*c22.y*c23.x +
            6*c10.y*c13.x*c22.x*c13y2*c23.x - 3*c11.x*c12.x*c22.x*c13y2*c23.y - 3*c11.x*c12.x*c13y2*c22.y*c23.x +
            2*c11.x*c12.y*c22.x*c13y2*c23.x + 4*c11.y*c12.x*c22.x*c13y2*c23.x - 6*c10.x*c13x2*c13.y*c22.y*c23.y -
            6*c10.y*c13x2*c22.x*c13.y*c23.y - 6*c10.y*c13x2*c13.y*c22.y*c23.x - 4*c11.x*c12.y*c13x2*c22.y*c23.y -
            6*c20.x*c13.x*c22.x*c13y2*c23.y - 6*c20.x*c13.x*c13y2*c22.y*c23.x - 2*c11.y*c12.x*c13x2*c22.y*c23.y +
            3*c11.y*c12.y*c13x2*c22.x*c23.y + 3*c11.y*c12.y*c13x2*c22.y*c23.x - 2*c12.x*c12y2*c13.x*c22.x*c23.y -
            2*c12.x*c12y2*c13.x*c22.y*c23.x - 2*c12.x*c12y2*c22.x*c13.y*c23.x - 6*c20.y*c13.x*c22.x*c13y2*c23.x -
            6*c21.x*c13.x*c21.y*c13y2*c23.x - 6*c21.x*c13.x*c22.x*c13y2*c22.y + 6*c20.x*c13x2*c13.y*c22.y*c23.y +
            2*c12x2*c12.y*c13.x*c22.y*c23.y + 2*c12x2*c12.y*c22.x*c13.y*c23.y + 2*c12x2*c12.y*c13.y*c22.y*c23.x +
            3*c21.x*c22x2*c13y3 + 3*c21x2*c13y3*c23.x - 3*c13.x*c21.y*c22x2*c13y2 - 3*c21x2*c13.x*c13y2*c23.y +
            c13x2*c22.x*c13.y*(6*c20.y*c23.y + 6*c21.y*c22.y) + c13x2*c13.y*c23.x*(6*c20.y*c22.y + 3*c21y2) +
            c21.x*c13x2*c13.y*(6*c21.y*c23.y + 3*c22y2) + c13x3*(-2*c20.y*c22.y*c23.y - c23.y*(2*c20.y*c22.y + c21y2) -
            c21.y*(2*c21.y*c23.y + c22y2) - c22.y*(2*c20.y*c23.y + 2*c21.y*c22.y)),
        c11.x*c21.x*c12.y*c13.x*c13.y*c23.y + c11.x*c12.y*c13.x*c21.y*c13.y*c23.x + c11.x*c12.y*c13.x*c22.x*c13.y*c22.y -
            c11.y*c12.x*c21.x*c13.x*c13.y*c23.y - c11.y*c12.x*c13.x*c21.y*c13.y*c23.x - c11.y*c12.x*c13.x*c22.x*c13.y*c22.y -
            6*c11.y*c21.x*c12.y*c13.x*c13.y*c23.x - 6*c10.x*c21.x*c13y3*c23.x + 6*c20.x*c21.x*c13y3*c23.x +
            2*c21.x*c12y3*c13.x*c23.x + 6*c10.x*c21.x*c13.x*c13y2*c23.y + 6*c10.x*c13.x*c21.y*c13y2*c23.x +
            6*c10.x*c13.x*c22.x*c13y2*c22.y + 6*c10.y*c21.x*c13.x*c13y2*c23.x - 3*c11.x*c12.x*c21.x*c13y2*c23.y -
            3*c11.x*c12.x*c21.y*c13y2*c23.x - 3*c11.x*c12.x*c22.x*c13y2*c22.y + 2*c11.x*c21.x*c12.y*c13y2*c23.x +
            4*c11.y*c12.x*c21.x*c13y2*c23.x - 6*c10.y*c21.x*c13x2*c13.y*c23.y - 6*c10.y*c13x2*c21.y*c13.y*c23.x -
            6*c10.y*c13x2*c22.x*c13.y*c22.y - 6*c20.x*c21.x*c13.x*c13y2*c23.y - 6*c20.x*c13.x*c21.y*c13y2*c23.x -
            6*c20.x*c13.x*c22.x*c13y2*c22.y + 3*c11.y*c21.x*c12.y*c13x2*c23.y - 3*c11.y*c12.y*c13.x*c22x2*c13.y +
            3*c11.y*c12.y*c13x2*c21.y*c23.x + 3*c11.y*c12.y*c13x2*c22.x*c22.y - 2*c12.x*c21.x*c12y2*c13.x*c23.y -
            2*c12.x*c21.x*c12y2*c13.y*c23.x - 2*c12.x*c12y2*c13.x*c21.y*c23.x - 2*c12.x*c12y2*c13.x*c22.x*c22.y -
            6*c20.y*c21.x*c13.x*c13y2*c23.x - 6*c21.x*c13.x*c21.y*c22.x*c13y2 + 6*c20.y*c13x2*c21.y*c13.y*c23.x +
            2*c12x2*c21.x*c12.y*c13.y*c23.y + 2*c12x2*c12.y*c21.y*c13.y*c23.x + 2*c12x2*c12.y*c22.x*c13.y*c22.y -
            3*c10.x*c22x2*c13y3 + 3*c20.x*c22x2*c13y3 + 3*c21x2*c22.x*c13y3 + c12y3*c13.x*c22x2 +
            3*c10.y*c13.x*c22x2*c13y2 + c11.x*c12.y*c22x2*c13y2 + 2*c11.y*c12.x*c22x2*c13y2 -
            c12.x*c12y2*c22x2*c13.y - 3*c20.y*c13.x*c22x2*c13y2 - 3*c21x2*c13.x*c13y2*c22.y +
            c12x2*c12.y*c13.x*(2*c21.y*c23.y + c22y2) + c11.x*c12.x*c13.x*c13.y*(6*c21.y*c23.y + 3*c22y2) +
            c21.x*c13x2*c13.y*(6*c20.y*c23.y + 6*c21.y*c22.y) + c12x3*c13.y*(-2*c21.y*c23.y - c22y2) +
            c10.y*c13x3*(6*c21.y*c23.y + 3*c22y2) + c11.y*c12.x*c13x2*(-2*c21.y*c23.y - c22y2) +
            c11.x*c12.y*c13x2*(-4*c21.y*c23.y - 2*c22y2) + c10.x*c13x2*c13.y*(-6*c21.y*c23.y - 3*c22y2) +
            c13x2*c22.x*c13.y*(6*c20.y*c22.y + 3*c21y2) + c20.x*c13x2*c13.y*(6*c21.y*c23.y + 3*c22y2) +
            c13x3*(-2*c20.y*c21.y*c23.y - c22.y*(2*c20.y*c22.y + c21y2) - c20.y*(2*c21.y*c23.y + c22y2) -
            c21.y*(2*c20.y*c23.y + 2*c21.y*c22.y)),
        -c10.x*c11.x*c12.y*c13.x*c13.y*c23.y + c10.x*c11.y*c12.x*c13.x*c13.y*c23.y + 6*c10.x*c11.y*c12.y*c13.x*c13.y*c23.x -
            6*c10.y*c11.x*c12.x*c13.x*c13.y*c23.y - c10.y*c11.x*c12.y*c13.x*c13.y*c23.x + c10.y*c11.y*c12.x*c13.x*c13.y*c23.x +
            c11.x*c11.y*c12.x*c12.y*c13.x*c23.y - c11.x*c11.y*c12.x*c12.y*c13.y*c23.x + c11.x*c20.x*c12.y*c13.x*c13.y*c23.y +
            c11.x*c20.y*c12.y*c13.x*c13.y*c23.x + c11.x*c21.x*c12.y*c13.x*c13.y*c22.y + c11.x*c12.y*c13.x*c21.y*c22.x*c13.y -
            c20.x*c11.y*c12.x*c13.x*c13.y*c23.y - 6*c20.x*c11.y*c12.y*c13.x*c13.y*c23.x - c11.y*c12.x*c20.y*c13.x*c13.y*c23.x -
            c11.y*c12.x*c21.x*c13.x*c13.y*c22.y - c11.y*c12.x*c13.x*c21.y*c22.x*c13.y - 6*c11.y*c21.x*c12.y*c13.x*c22.x*c13.y -
            6*c10.x*c20.x*c13y3*c23.x - 6*c10.x*c21.x*c22.x*c13y3 - 2*c10.x*c12y3*c13.x*c23.x + 6*c20.x*c21.x*c22.x*c13y3 +
            2*c20.x*c12y3*c13.x*c23.x + 2*c21.x*c12y3*c13.x*c22.x + 2*c10.y*c12x3*c13.y*c23.y - 6*c10.x*c10.y*c13.x*c13y2*c23.x +
            3*c10.x*c11.x*c12.x*c13y2*c23.y - 2*c10.x*c11.x*c12.y*c13y2*c23.x - 4*c10.x*c11.y*c12.x*c13y2*c23.x +
            3*c10.y*c11.x*c12.x*c13y2*c23.x + 6*c10.x*c10.y*c13x2*c13.y*c23.y + 6*c10.x*c20.x*c13.x*c13y2*c23.y -
            3*c10.x*c11.y*c12.y*c13x2*c23.y + 2*c10.x*c12.x*c12y2*c13.x*c23.y + 2*c10.x*c12.x*c12y2*c13.y*c23.x +
            6*c10.x*c20.y*c13.x*c13y2*c23.x + 6*c10.x*c21.x*c13.x*c13y2*c22.y + 6*c10.x*c13.x*c21.y*c22.x*c13y2 +
            4*c10.y*c11.x*c12.y*c13x2*c23.y + 6*c10.y*c20.x*c13.x*c13y2*c23.x + 2*c10.y*c11.y*c12.x*c13x2*c23.y -
            3*c10.y*c11.y*c12.y*c13x2*c23.x + 2*c10.y*c12.x*c12y2*c13.x*c23.x + 6*c10.y*c21.x*c13.x*c22.x*c13y2 -
            3*c11.x*c20.x*c12.x*c13y2*c23.y + 2*c11.x*c20.x*c12.y*c13y2*c23.x + c11.x*c11.y*c12y2*c13.x*c23.x -
            3*c11.x*c12.x*c20.y*c13y2*c23.x - 3*c11.x*c12.x*c21.x*c13y2*c22.y - 3*c11.x*c12.x*c21.y*c22.x*c13y2 +
            2*c11.x*c21.x*c12.y*c22.x*c13y2 + 4*c20.x*c11.y*c12.x*c13y2*c23.x + 4*c11.y*c12.x*c21.x*c22.x*c13y2 -
            2*c10.x*c12x2*c12.y*c13.y*c23.y - 6*c10.y*c20.x*c13x2*c13.y*c23.y - 6*c10.y*c20.y*c13x2*c13.y*c23.x -
            6*c10.y*c21.x*c13x2*c13.y*c22.y - 2*c10.y*c12x2*c12.y*c13.x*c23.y - 2*c10.y*c12x2*c12.y*c13.y*c23.x -
            6*c10.y*c13x2*c21.y*c22.x*c13.y - c11.x*c11.y*c12x2*c13.y*c23.y - 2*c11.x*c11y2*c13.x*c13.y*c23.x +
            3*c20.x*c11.y*c12.y*c13x2*c23.y - 2*c20.x*c12.x*c12y2*c13.x*c23.y - 2*c20.x*c12.x*c12y2*c13.y*c23.x -
            6*c20.x*c20.y*c13.x*c13y2*c23.x - 6*c20.x*c21.x*c13.x*c13y2*c22.y - 6*c20.x*c13.x*c21.y*c22.x*c13y2 +
            3*c11.y*c20.y*c12.y*c13x2*c23.x + 3*c11.y*c21.x*c12.y*c13x2*c22.y + 3*c11.y*c12.y*c13x2*c21.y*c22.x -
            2*c12.x*c20.y*c12y2*c13.x*c23.x - 2*c12.x*c21.x*c12y2*c13.x*c22.y - 2*c12.x*c21.x*c12y2*c22.x*c13.y -
            2*c12.x*c12y2*c13.x*c21.y*c22.x - 6*c20.y*c21.x*c13.x*c22.x*c13y2 - c11y2*c12.x*c12.y*c13.x*c23.x +
            2*c20.x*c12x2*c12.y*c13.y*c23.y + 6*c20.y*c13x2*c21.y*c22.x*c13.y + 2*c11x2*c11.y*c13.x*c13.y*c23.y +
            c11x2*c12.x*c12.y*c13.y*c23.y + 2*c12x2*c20.y*c12.y*c13.y*c23.x + 2*c12x2*c21.x*c12.y*c13.y*c22.y +
            2*c12x2*c12.y*c21.y*c22.x*c13.y + c21x3*c13y3 + 3*c10x2*c13y3*c23.x - 3*c10y2*c13x3*c23.y +
            3*c20x2*c13y3*c23.x + c11y3*c13x2*c23.x - c11x3*c13y2*c23.y - c11.x*c11y2*c13x2*c23.y +
            c11x2*c11.y*c13y2*c23.x - 3*c10x2*c13.x*c13y2*c23.y + 3*c10y2*c13x2*c13.y*c23.x - c11x2*c12y2*c13.x*c23.y +
            c11y2*c12x2*c13.y*c23.x - 3*c21x2*c13.x*c21.y*c13y2 - 3*c20x2*c13.x*c13y2*c23.y + 3*c20y2*c13x2*c13.y*c23.x +
            c11.x*c12.x*c13.x*c13.y*(6*c20.y*c23.y + 6*c21.y*c22.y) + c12x3*c13.y*(-2*c20.y*c23.y - 2*c21.y*c22.y) +
            c10.y*c13x3*(6*c20.y*c23.y + 6*c21.y*c22.y) + c11.y*c12.x*c13x2*(-2*c20.y*c23.y - 2*c21.y*c22.y) +
            c12x2*c12.y*c13.x*(2*c20.y*c23.y + 2*c21.y*c22.y) + c11.x*c12.y*c13x2*(-4*c20.y*c23.y - 4*c21.y*c22.y) +
            c10.x*c13x2*c13.y*(-6*c20.y*c23.y - 6*c21.y*c22.y) + c20.x*c13x2*c13.y*(6*c20.y*c23.y + 6*c21.y*c22.y) +
            c21.x*c13x2*c13.y*(6*c20.y*c22.y + 3*c21y2) + c13x3*(-2*c20.y*c21.y*c22.y - c20y2*c23.y -
            c21.y*(2*c20.y*c22.y + c21y2) - c20.y*(2*c20.y*c23.y + 2*c21.y*c22.y)),
        -c10.x*c11.x*c12.y*c13.x*c13.y*c22.y + c10.x*c11.y*c12.x*c13.x*c13.y*c22.y + 6*c10.x*c11.y*c12.y*c13.x*c22.x*c13.y -
            6*c10.y*c11.x*c12.x*c13.x*c13.y*c22.y - c10.y*c11.x*c12.y*c13.x*c22.x*c13.y + c10.y*c11.y*c12.x*c13.x*c22.x*c13.y +
            c11.x*c11.y*c12.x*c12.y*c13.x*c22.y - c11.x*c11.y*c12.x*c12.y*c22.x*c13.y + c11.x*c20.x*c12.y*c13.x*c13.y*c22.y +
            c11.x*c20.y*c12.y*c13.x*c22.x*c13.y + c11.x*c21.x*c12.y*c13.x*c21.y*c13.y - c20.x*c11.y*c12.x*c13.x*c13.y*c22.y -
            6*c20.x*c11.y*c12.y*c13.x*c22.x*c13.y - c11.y*c12.x*c20.y*c13.x*c22.x*c13.y - c11.y*c12.x*c21.x*c13.x*c21.y*c13.y -
            6*c10.x*c20.x*c22.x*c13y3 - 2*c10.x*c12y3*c13.x*c22.x + 2*c20.x*c12y3*c13.x*c22.x + 2*c10.y*c12x3*c13.y*c22.y -
            6*c10.x*c10.y*c13.x*c22.x*c13y2 + 3*c10.x*c11.x*c12.x*c13y2*c22.y - 2*c10.x*c11.x*c12.y*c22.x*c13y2 -
            4*c10.x*c11.y*c12.x*c22.x*c13y2 + 3*c10.y*c11.x*c12.x*c22.x*c13y2 + 6*c10.x*c10.y*c13x2*c13.y*c22.y +
            6*c10.x*c20.x*c13.x*c13y2*c22.y - 3*c10.x*c11.y*c12.y*c13x2*c22.y + 2*c10.x*c12.x*c12y2*c13.x*c22.y +
            2*c10.x*c12.x*c12y2*c22.x*c13.y + 6*c10.x*c20.y*c13.x*c22.x*c13y2 + 6*c10.x*c21.x*c13.x*c21.y*c13y2 +
            4*c10.y*c11.x*c12.y*c13x2*c22.y + 6*c10.y*c20.x*c13.x*c22.x*c13y2 + 2*c10.y*c11.y*c12.x*c13x2*c22.y -
            3*c10.y*c11.y*c12.y*c13x2*c22.x + 2*c10.y*c12.x*c12y2*c13.x*c22.x - 3*c11.x*c20.x*c12.x*c13y2*c22.y +
            2*c11.x*c20.x*c12.y*c22.x*c13y2 + c11.x*c11.y*c12y2*c13.x*c22.x - 3*c11.x*c12.x*c20.y*c22.x*c13y2 -
            3*c11.x*c12.x*c21.x*c21.y*c13y2 + 4*c20.x*c11.y*c12.x*c22.x*c13y2 - 2*c10.x*c12x2*c12.y*c13.y*c22.y -
            6*c10.y*c20.x*c13x2*c13.y*c22.y - 6*c10.y*c20.y*c13x2*c22.x*c13.y - 6*c10.y*c21.x*c13x2*c21.y*c13.y -
            2*c10.y*c12x2*c12.y*c13.x*c22.y - 2*c10.y*c12x2*c12.y*c22.x*c13.y - c11.x*c11.y*c12x2*c13.y*c22.y -
            2*c11.x*c11y2*c13.x*c22.x*c13.y + 3*c20.x*c11.y*c12.y*c13x2*c22.y - 2*c20.x*c12.x*c12y2*c13.x*c22.y -
            2*c20.x*c12.x*c12y2*c22.x*c13.y - 6*c20.x*c20.y*c13.x*c22.x*c13y2 - 6*c20.x*c21.x*c13.x*c21.y*c13y2 +
            3*c11.y*c20.y*c12.y*c13x2*c22.x + 3*c11.y*c21.x*c12.y*c13x2*c21.y - 2*c12.x*c20.y*c12y2*c13.x*c22.x -
            2*c12.x*c21.x*c12y2*c13.x*c21.y - c11y2*c12.x*c12.y*c13.x*c22.x + 2*c20.x*c12x2*c12.y*c13.y*c22.y -
            3*c11.y*c21x2*c12.y*c13.x*c13.y + 6*c20.y*c21.x*c13x2*c21.y*c13.y + 2*c11x2*c11.y*c13.x*c13.y*c22.y +
            c11x2*c12.x*c12.y*c13.y*c22.y + 2*c12x2*c20.y*c12.y*c22.x*c13.y + 2*c12x2*c21.x*c12.y*c21.y*c13.y -
            3*c10.x*c21x2*c13y3 + 3*c20.x*c21x2*c13y3 + 3*c10x2*c22.x*c13y3 - 3*c10y2*c13x3*c22.y + 3*c20x2*c22.x*c13y3 +
            c21x2*c12y3*c13.x + c11y3*c13x2*c22.x - c11x3*c13y2*c22.y + 3*c10.y*c21x2*c13.x*c13y2 -
            c11.x*c11y2*c13x2*c22.y + c11.x*c21x2*c12.y*c13y2 + 2*c11.y*c12.x*c21x2*c13y2 + c11x2*c11.y*c22.x*c13y2 -
            c12.x*c21x2*c12y2*c13.y - 3*c20.y*c21x2*c13.x*c13y2 - 3*c10x2*c13.x*c13y2*c22.y + 3*c10y2*c13x2*c22.x*c13.y -
            c11x2*c12y2*c13.x*c22.y + c11y2*c12x2*c22.x*c13.y - 3*c20x2*c13.x*c13y2*c22.y + 3*c20y2*c13x2*c22.x*c13.y +
            c12x2*c12.y*c13.x*(2*c20.y*c22.y + c21y2) + c11.x*c12.x*c13.x*c13.y*(6*c20.y*c22.y + 3*c21y2) +
            c12x3*c13.y*(-2*c20.y*c22.y - c21y2) + c10.y*c13x3*(6*c20.y*c22.y + 3*c21y2) +
            c11.y*c12.x*c13x2*(-2*c20.y*c22.y - c21y2) + c11.x*c12.y*c13x2*(-4*c20.y*c22.y - 2*c21y2) +
            c10.x*c13x2*c13.y*(-6*c20.y*c22.y - 3*c21y2) + c20.x*c13x2*c13.y*(6*c20.y*c22.y + 3*c21y2) +
            c13x3*(-2*c20.y*c21y2 - c20y2*c22.y - c20.y*(2*c20.y*c22.y + c21y2)),
        -c10.x*c11.x*c12.y*c13.x*c21.y*c13.y + c10.x*c11.y*c12.x*c13.x*c21.y*c13.y + 6*c10.x*c11.y*c21.x*c12.y*c13.x*c13.y -
            6*c10.y*c11.x*c12.x*c13.x*c21.y*c13.y - c10.y*c11.x*c21.x*c12.y*c13.x*c13.y + c10.y*c11.y*c12.x*c21.x*c13.x*c13.y -
            c11.x*c11.y*c12.x*c21.x*c12.y*c13.y + c11.x*c11.y*c12.x*c12.y*c13.x*c21.y + c11.x*c20.x*c12.y*c13.x*c21.y*c13.y +
            6*c11.x*c12.x*c20.y*c13.x*c21.y*c13.y + c11.x*c20.y*c21.x*c12.y*c13.x*c13.y - c20.x*c11.y*c12.x*c13.x*c21.y*c13.y -
            6*c20.x*c11.y*c21.x*c12.y*c13.x*c13.y - c11.y*c12.x*c20.y*c21.x*c13.x*c13.y - 6*c10.x*c20.x*c21.x*c13y3 -
            2*c10.x*c21.x*c12y3*c13.x + 6*c10.y*c20.y*c13x3*c21.y + 2*c20.x*c21.x*c12y3*c13.x + 2*c10.y*c12x3*c21.y*c13.y -
            2*c12x3*c20.y*c21.y*c13.y - 6*c10.x*c10.y*c21.x*c13.x*c13y2 + 3*c10.x*c11.x*c12.x*c21.y*c13y2 -
            2*c10.x*c11.x*c21.x*c12.y*c13y2 - 4*c10.x*c11.y*c12.x*c21.x*c13y2 + 3*c10.y*c11.x*c12.x*c21.x*c13y2 +
            6*c10.x*c10.y*c13x2*c21.y*c13.y + 6*c10.x*c20.x*c13.x*c21.y*c13y2 - 3*c10.x*c11.y*c12.y*c13x2*c21.y +
            2*c10.x*c12.x*c21.x*c12y2*c13.y + 2*c10.x*c12.x*c12y2*c13.x*c21.y + 6*c10.x*c20.y*c21.x*c13.x*c13y2 +
            4*c10.y*c11.x*c12.y*c13x2*c21.y + 6*c10.y*c20.x*c21.x*c13.x*c13y2 + 2*c10.y*c11.y*c12.x*c13x2*c21.y -
            3*c10.y*c11.y*c21.x*c12.y*c13x2 + 2*c10.y*c12.x*c21.x*c12y2*c13.x - 3*c11.x*c20.x*c12.x*c21.y*c13y2 +
            2*c11.x*c20.x*c21.x*c12.y*c13y2 + c11.x*c11.y*c21.x*c12y2*c13.x - 3*c11.x*c12.x*c20.y*c21.x*c13y2 +
            4*c20.x*c11.y*c12.x*c21.x*c13y2 - 6*c10.x*c20.y*c13x2*c21.y*c13.y - 2*c10.x*c12x2*c12.y*c21.y*c13.y -
            6*c10.y*c20.x*c13x2*c21.y*c13.y - 6*c10.y*c20.y*c21.x*c13x2*c13.y - 2*c10.y*c12x2*c21.x*c12.y*c13.y -
            2*c10.y*c12x2*c12.y*c13.x*c21.y - c11.x*c11.y*c12x2*c21.y*c13.y - 4*c11.x*c20.y*c12.y*c13x2*c21.y -
            2*c11.x*c11y2*c21.x*c13.x*c13.y + 3*c20.x*c11.y*c12.y*c13x2*c21.y - 2*c20.x*c12.x*c21.x*c12y2*c13.y -
            2*c20.x*c12.x*c12y2*c13.x*c21.y - 6*c20.x*c20.y*c21.x*c13.x*c13y2 - 2*c11.y*c12.x*c20.y*c13x2*c21.y +
            3*c11.y*c20.y*c21.x*c12.y*c13x2 - 2*c12.x*c20.y*c21.x*c12y2*c13.x - c11y2*c12.x*c21.x*c12.y*c13.x +
            6*c20.x*c20.y*c13x2*c21.y*c13.y + 2*c20.x*c12x2*c12.y*c21.y*c13.y + 2*c11x2*c11.y*c13.x*c21.y*c13.y +
            c11x2*c12.x*c12.y*c21.y*c13.y + 2*c12x2*c20.y*c21.x*c12.y*c13.y + 2*c12x2*c20.y*c12.y*c13.x*c21.y +
            3*c10x2*c21.x*c13y3 - 3*c10y2*c13x3*c21.y + 3*c20x2*c21.x*c13y3 + c11y3*c21.x*c13x2 - c11x3*c21.y*c13y2 -
            3*c20y2*c13x3*c21.y - c11.x*c11y2*c13x2*c21.y + c11x2*c11.y*c21.x*c13y2 - 3*c10x2*c13.x*c21.y*c13y2 +
            3*c10y2*c21.x*c13x2*c13.y - c11x2*c12y2*c13.x*c21.y + c11y2*c12x2*c21.x*c13.y - 3*c20x2*c13.x*c21.y*c13y2 +
            3*c20y2*c21.x*c13x2*c13.y,
        c10.x*c10.y*c11.x*c12.y*c13.x*c13.y - c10.x*c10.y*c11.y*c12.x*c13.x*c13.y + c10.x*c11.x*c11.y*c12.x*c12.y*c13.y -
            c10.y*c11.x*c11.y*c12.x*c12.y*c13.x - c10.x*c11.x*c20.y*c12.y*c13.x*c13.y + 6*c10.x*c20.x*c11.y*c12.y*c13.x*c13.y +
            c10.x*c11.y*c12.x*c20.y*c13.x*c13.y - c10.y*c11.x*c20.x*c12.y*c13.x*c13.y - 6*c10.y*c11.x*c12.x*c20.y*c13.x*c13.y +
            c10.y*c20.x*c11.y*c12.x*c13.x*c13.y - c11.x*c20.x*c11.y*c12.x*c12.y*c13.y + c11.x*c11.y*c12.x*c20.y*c12.y*c13.x +
            c11.x*c20.x*c20.y*c12.y*c13.x*c13.y - c20.x*c11.y*c12.x*c20.y*c13.x*c13.y - 2*c10.x*c20.x*c12y3*c13.x +
            2*c10.y*c12x3*c20.y*c13.y - 3*c10.x*c10.y*c11.x*c12.x*c13y2 - 6*c10.x*c10.y*c20.x*c13.x*c13y2 +
            3*c10.x*c10.y*c11.y*c12.y*c13x2 - 2*c10.x*c10.y*c12.x*c12y2*c13.x - 2*c10.x*c11.x*c20.x*c12.y*c13y2 -
            c10.x*c11.x*c11.y*c12y2*c13.x + 3*c10.x*c11.x*c12.x*c20.y*c13y2 - 4*c10.x*c20.x*c11.y*c12.x*c13y2 +
            3*c10.y*c11.x*c20.x*c12.x*c13y2 + 6*c10.x*c10.y*c20.y*c13x2*c13.y + 2*c10.x*c10.y*c12x2*c12.y*c13.y +
            2*c10.x*c11.x*c11y2*c13.x*c13.y + 2*c10.x*c20.x*c12.x*c12y2*c13.y + 6*c10.x*c20.x*c20.y*c13.x*c13y2 -
            3*c10.x*c11.y*c20.y*c12.y*c13x2 + 2*c10.x*c12.x*c20.y*c12y2*c13.x + c10.x*c11y2*c12.x*c12.y*c13.x +
            c10.y*c11.x*c11.y*c12x2*c13.y + 4*c10.y*c11.x*c20.y*c12.y*c13x2 - 3*c10.y*c20.x*c11.y*c12.y*c13x2 +
            2*c10.y*c20.x*c12.x*c12y2*c13.x + 2*c10.y*c11.y*c12.x*c20.y*c13x2 + c11.x*c20.x*c11.y*c12y2*c13.x -
            3*c11.x*c20.x*c12.x*c20.y*c13y2 - 2*c10.x*c12x2*c20.y*c12.y*c13.y - 6*c10.y*c20.x*c20.y*c13x2*c13.y -
            2*c10.y*c20.x*c12x2*c12.y*c13.y - 2*c10.y*c11x2*c11.y*c13.x*c13.y - c10.y*c11x2*c12.x*c12.y*c13.y -
            2*c10.y*c12x2*c20.y*c12.y*c13.x - 2*c11.x*c20.x*c11y2*c13.x*c13.y - c11.x*c11.y*c12x2*c20.y*c13.y +
            3*c20.x*c11.y*c20.y*c12.y*c13x2 - 2*c20.x*c12.x*c20.y*c12y2*c13.x - c20.x*c11y2*c12.x*c12.y*c13.x +
            3*c10y2*c11.x*c12.x*c13.x*c13.y + 3*c11.x*c12.x*c20y2*c13.x*c13.y + 2*c20.x*c12x2*c20.y*c12.y*c13.y -
            3*c10x2*c11.y*c12.y*c13.x*c13.y + 2*c11x2*c11.y*c20.y*c13.x*c13.y + c11x2*c12.x*c20.y*c12.y*c13.y -
            3*c20x2*c11.y*c12.y*c13.x*c13.y - c10x3*c13y3 + c10y3*c13x3 + c20x3*c13y3 - c20y3*c13x3 -
            3*c10.x*c20x2*c13y3 - c10.x*c11y3*c13x2 + 3*c10x2*c20.x*c13y3 + c10.y*c11x3*c13y2 +
            3*c10.y*c20y2*c13x3 + c20.x*c11y3*c13x2 + c10x2*c12y3*c13.x - 3*c10y2*c20.y*c13x3 - c10y2*c12x3*c13.y +
            c20x2*c12y3*c13.x - c11x3*c20.y*c13y2 - c12x3*c20y2*c13.y - c10.x*c11x2*c11.y*c13y2 +
            c10.y*c11.x*c11y2*c13x2 - 3*c10.x*c10y2*c13x2*c13.y - c10.x*c11y2*c12x2*c13.y + c10.y*c11x2*c12y2*c13.x -
            c11.x*c11y2*c20.y*c13x2 + 3*c10x2*c10.y*c13.x*c13y2 + c10x2*c11.x*c12.y*c13y2 +
            2*c10x2*c11.y*c12.x*c13y2 - 2*c10y2*c11.x*c12.y*c13x2 - c10y2*c11.y*c12.x*c13x2 + c11x2*c20.x*c11.y*c13y2 -
            3*c10.x*c20y2*c13x2*c13.y + 3*c10.y*c20x2*c13.x*c13y2 + c11.x*c20x2*c12.y*c13y2 - 2*c11.x*c20y2*c12.y*c13x2 +
            c20.x*c11y2*c12x2*c13.y - c11.y*c12.x*c20y2*c13x2 - c10x2*c12.x*c12y2*c13.y - 3*c10x2*c20.y*c13.x*c13y2 +
            3*c10y2*c20.x*c13x2*c13.y + c10y2*c12x2*c12.y*c13.x - c11x2*c20.y*c12y2*c13.x + 2*c20x2*c11.y*c12.x*c13y2 +
            3*c20.x*c20y2*c13x2*c13.y - c20x2*c12.x*c12y2*c13.y - 3*c20x2*c20.y*c13.x*c13y2 + c12x2*c20y2*c12.y*c13.x
    );
    var roots = poly.getRootsInInterval(0,1);

    for ( var i = 0; i < roots.length; i++ ) {
        var s = roots[i];
        var xRoots = new Polynomial(
            c13.x,
            c12.x,
            c11.x,
            c10.x - c20.x - s*c21.x - s*s*c22.x - s*s*s*c23.x
        ).getRoots();
        var yRoots = new Polynomial(
            c13.y,
            c12.y,
            c11.y,
            c10.y - c20.y - s*c21.y - s*s*c22.y - s*s*s*c23.y
        ).getRoots();

        if ( xRoots.length > 0 && yRoots.length > 0 ) {
            var TOLERANCE = 1e-4;

            checkRoots:
            for ( var j = 0; j < xRoots.length; j++ ) {
                var xRoot = xRoots[j];
                
                if ( 0 <= xRoot && xRoot <= 1 ) {
                    for ( var k = 0; k < yRoots.length; k++ ) {
                        if ( Math.abs( xRoot - yRoots[k] ) < TOLERANCE ) {
                            result.points.push(
                                c23.multiply(s*s*s).add(c22.multiply(s*s).add(c21.multiply(s).add(c20)))
                            );
                            break checkRoots;
                        }
                    }
                }
            }
        }
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier3Circle
*
*****/
Intersection.intersectBezier3Circle = function(p1, p2, p3, p4, c, r) {
    return Intersection.intersectBezier3Ellipse(p1, p2, p3, p4, c, r, r);
};


/*****
*
*   intersectBezier3Ellipse
*
*****/
Intersection.intersectBezier3Ellipse = function(p1, p2, p3, p4, ec, rx, ry) {
    var a, b, c, d;       // temporary variables
    var c3, c2, c1, c0;   // coefficients of cubic
    var result = new Intersection("No Intersection");

    // Calculate the coefficients of cubic polynomial
    a = p1.multiply(-1);
    b = p2.multiply(3);
    c = p3.multiply(-3);
    d = a.add(b.add(c.add(p4)));
    c3 = new Vector2D(d.x, d.y);

    a = p1.multiply(3);
    b = p2.multiply(-6);
    c = p3.multiply(3);
    d = a.add(b.add(c));
    c2 = new Vector2D(d.x, d.y);

    a = p1.multiply(-3);
    b = p2.multiply(3);
    c = a.add(b);
    c1 = new Vector2D(c.x, c.y);

    c0 = new Vector2D(p1.x, p1.y);

    var rxrx  = rx*rx;
    var ryry  = ry*ry;
    var poly = new Polynomial(
        c3.x*c3.x*ryry + c3.y*c3.y*rxrx,
        2*(c3.x*c2.x*ryry + c3.y*c2.y*rxrx),
        2*(c3.x*c1.x*ryry + c3.y*c1.y*rxrx) + c2.x*c2.x*ryry + c2.y*c2.y*rxrx,
        2*c3.x*ryry*(c0.x - ec.x) + 2*c3.y*rxrx*(c0.y - ec.y) +
            2*(c2.x*c1.x*ryry + c2.y*c1.y*rxrx),
        2*c2.x*ryry*(c0.x - ec.x) + 2*c2.y*rxrx*(c0.y - ec.y) +
            c1.x*c1.x*ryry + c1.y*c1.y*rxrx,
        2*c1.x*ryry*(c0.x - ec.x) + 2*c1.y*rxrx*(c0.y - ec.y),
        c0.x*c0.x*ryry - 2*c0.y*ec.y*rxrx - 2*c0.x*ec.x*ryry +
            c0.y*c0.y*rxrx + ec.x*ec.x*ryry + ec.y*ec.y*rxrx - rxrx*ryry
    );
    var roots = poly.getRootsInInterval(0,1);

    for ( var i = 0; i < roots.length; i++ ) {
        var t = roots[i];

        result.points.push(
            c3.multiply(t*t*t).add(c2.multiply(t*t).add(c1.multiply(t).add(c0)))
        );
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier3Line
*
*   Many thanks to Dan Sunday at SoftSurfer.com.  He gave me a very thorough
*   sketch of the algorithm used here.  Without his help, I'm not sure when I
*   would have figured out this intersection problem.
*
*****/
Intersection.intersectBezier3Line = function(p1, p2, p3, p4, a1, a2) {
    var a, b, c, d;       // temporary variables
    var c3, c2, c1, c0;   // coefficients of cubic
    var cl;               // c coefficient for normal form of line
    var n;                // normal for normal form of line
    var min = a1.min(a2); // used to determine if point is on line segment
    var max = a1.max(a2); // used to determine if point is on line segment
    var result = new Intersection("No Intersection");
    
    // Start with Bezier using Bernstein polynomials for weighting functions:
    //     (1-t^3)P1 + 3t(1-t)^2P2 + 3t^2(1-t)P3 + t^3P4
    //
    // Expand and collect terms to form linear combinations of original Bezier
    // controls.  This ends up with a vector cubic in t:
    //     (-P1+3P2-3P3+P4)t^3 + (3P1-6P2+3P3)t^2 + (-3P1+3P2)t + P1
    //             /\                  /\                /\       /\
    //             ||                  ||                ||       ||
    //             c3                  c2                c1       c0
    
    // Calculate the coefficients
    a = p1.multiply(-1);
    b = p2.multiply(3);
    c = p3.multiply(-3);
    d = a.add(b.add(c.add(p4)));
    c3 = new Vector2D(d.x, d.y);

    a = p1.multiply(3);
    b = p2.multiply(-6);
    c = p3.multiply(3);
    d = a.add(b.add(c));
    c2 = new Vector2D(d.x, d.y);

    a = p1.multiply(-3);
    b = p2.multiply(3);
    c = a.add(b);
    c1 = new Vector2D(c.x, c.y);

    c0 = new Vector2D(p1.x, p1.y);
    
    // Convert line to normal form: ax + by + c = 0
    // Find normal to line: negative inverse of original line's slope
    n = new Vector2D(a1.y - a2.y, a2.x - a1.x);
    
    // Determine new c coefficient
    cl = a1.x*a2.y - a2.x*a1.y;

    // ?Rotate each cubic coefficient using line for new coordinate system?
    // Find roots of rotated cubic
    roots = new Polynomial(
        n.dot(c3),
        n.dot(c2),
        n.dot(c1),
        n.dot(c0) + cl
    ).getRoots();

    // Any roots in closed interval [0,1] are intersections on Bezier, but
    // might not be on the line segment.
    // Find intersections and calculate point coordinates
    for ( var i = 0; i < roots.length; i++ ) {
        var t = roots[i];

        if ( 0 <= t && t <= 1 ) {
            // We're within the Bezier curve
            // Find point on Bezier
            var p5 = p1.lerp(p2, t);
            var p6 = p2.lerp(p3, t);
            var p7 = p3.lerp(p4, t);

            var p8 = p5.lerp(p6, t);
            var p9 = p6.lerp(p7, t);

            var p10 = p8.lerp(p9, t);

            // See if point is on line segment
            // Had to make special cases for vertical and horizontal lines due
            // to slight errors in calculation of p10
            if ( a1.x == a2.x ) {
                if ( min.y <= p10.y && p10.y <= max.y ) {
                    result.status = "Intersection";
                    result.appendPoint( p10 );
                }
            } else if ( a1.y == a2.y ) {
                if ( min.x <= p10.x && p10.x <= max.x ) {
                    result.status = "Intersection";
                    result.appendPoint( p10 );
                }
            } else if ( p10.gte(min) && p10.lte(max) ) {
                result.status = "Intersection";
                result.appendPoint( p10 );
            }
        }
    }

    return result;
};


/*****
*
*   intersectBezier3Polygon
*
*****/
Intersection.intersectBezier3Polygon = function(p1, p2, p3, p4, points) {
    var result = new Intersection("No Intersection");
    var length = points.length;

    for ( var i = 0; i < length; i++ ) {
        var a1 = points[i];
        var a2 = points[(i+1) % length];
        var inter = Intersection.intersectBezier3Line(p1, p2, p3, p4, a1, a2);

        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier3Rectangle
*
*****/
Intersection.intersectBezier3Rectangle = function(p1, p2, p3, p4, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectBezier3Line(p1, p2, p3, p4, min, topRight);
    var inter2 = Intersection.intersectBezier3Line(p1, p2, p3, p4, topRight, max);
    var inter3 = Intersection.intersectBezier3Line(p1, p2, p3, p4, max, bottomLeft);
    var inter4 = Intersection.intersectBezier3Line(p1, p2, p3, p4, bottomLeft, min);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectCircleCircle
*
*****/
Intersection.intersectCircleCircle = function(c1, r1, c2, r2) {
    var result;
    
    // Determine minimum and maximum radii where circles can intersect
    var r_max = r1 + r2;
    var r_min = Math.abs(r1 - r2);
    
    // Determine actual distance between circle circles
    var c_dist = c1.distanceFrom( c2 );

    if ( c_dist > r_max ) {
        result = new Intersection("Outside");
    } else if ( c_dist < r_min ) {
        result = new Intersection("Inside");
    } else {
        result = new Intersection("Intersection");

        var a = (r1*r1 - r2*r2 + c_dist*c_dist) / ( 2*c_dist );
        var h = Math.sqrt(r1*r1 - a*a);
        var p = c1.lerp(c2, a/c_dist);
        var b = h / c_dist;

        result.points.push(
            new Point2D(
                p.x - b * (c2.y - c1.y),
                p.y + b * (c2.x - c1.x)
            )
        );
        result.points.push(
            new Point2D(
                p.x + b * (c2.y - c1.y),
                p.y - b * (c2.x - c1.x)
            )
        );
    }

    return result;
};


/*****
*
*   intersectCircleEllipse
*
*****/
Intersection.intersectCircleEllipse = function(cc, r, ec, rx, ry) {
    return Intersection.intersectEllipseEllipse(cc, r, r, ec, rx, ry);
};


/*****
*
*   intersectCircleLine
*
*****/
Intersection.intersectCircleLine = function(c, r, a1, a2) {
    var result;
    var a  = (a2.x - a1.x) * (a2.x - a1.x) +
             (a2.y - a1.y) * (a2.y - a1.y);
    var b  = 2 * ( (a2.x - a1.x) * (a1.x - c.x) +
                   (a2.y - a1.y) * (a1.y - c.y)   );
    var cc = c.x*c.x + c.y*c.y + a1.x*a1.x + a1.y*a1.y -
             2 * (c.x * a1.x + c.y * a1.y) - r*r;
    var deter = b*b - 4*a*cc;

    if ( deter < 0 ) {
        result = new Intersection("Outside");
    } else if ( deter == 0 ) {
        result = new Intersection("Tangent");
        // NOTE: should calculate this point
    } else {
        var e  = Math.sqrt(deter);
        var u1 = ( -b + e ) / ( 2*a );
        var u2 = ( -b - e ) / ( 2*a );

        if ( (u1 < 0 || u1 > 1) && (u2 < 0 || u2 > 1) ) {
            if ( (u1 < 0 && u2 < 0) || (u1 > 1 && u2 > 1) ) {
                result = new Intersection("Outside");
            } else {
                result = new Intersection("Inside");
            }
        } else {
            result = new Intersection("Intersection");

            if ( 0 <= u1 && u1 <= 1)
                result.points.push( a1.lerp(a2, u1) );

            if ( 0 <= u2 && u2 <= 1)
                result.points.push( a1.lerp(a2, u2) );
        }
    }
    
    return result;
};


/*****
*
*   intersectCirclePolygon
*
*****/
Intersection.intersectCirclePolygon = function(c, r, points) {
    var result = new Intersection("No Intersection");
    var length = points.length;
    var inter;

    for ( var i = 0; i < length; i++ ) {
        var a1 = points[i];
        var a2 = points[(i+1) % length];

        inter = Intersection.intersectCircleLine(c, r, a1, a2);
        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 )
        result.status = "Intersection";
    else
        result.status = inter.status;

    return result;
};


/*****
*
*   intersectCircleRectangle
*
*****/
Intersection.intersectCircleRectangle = function(c, r, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectCircleLine(c, r, min, topRight);
    var inter2 = Intersection.intersectCircleLine(c, r, topRight, max);
    var inter3 = Intersection.intersectCircleLine(c, r, max, bottomLeft);
    var inter4 = Intersection.intersectCircleLine(c, r, bottomLeft, min);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 )
        result.status = "Intersection";
    else
        result.status = inter1.status;

    return result;
};


/*****
*
*   intersectEllipseEllipse
*   
*   This code is based on MgcIntr2DElpElp.cpp written by David Eberly.  His
*   code along with many other excellent examples are avaiable at his site:
*   http://www.magic-software.com
*
*   NOTE: Rotation will need to be added to this function
*
*****/
Intersection.intersectEllipseEllipse = function(c1, rx1, ry1, c2, rx2, ry2) {
    var a = [
        ry1*ry1, 0, rx1*rx1, -2*ry1*ry1*c1.x, -2*rx1*rx1*c1.y,
        ry1*ry1*c1.x*c1.x + rx1*rx1*c1.y*c1.y - rx1*rx1*ry1*ry1
    ];
    var b = [
        ry2*ry2, 0, rx2*rx2, -2*ry2*ry2*c2.x, -2*rx2*rx2*c2.y,
        ry2*ry2*c2.x*c2.x + rx2*rx2*c2.y*c2.y - rx2*rx2*ry2*ry2
    ];

    var yPoly   = Intersection.bezout(a, b);
    var yRoots  = yPoly.getRoots();
    var epsilon = 1e-3;
    var norm0   = ( a[0]*a[0] + 2*a[1]*a[1] + a[2]*a[2] ) * epsilon;
    var norm1   = ( b[0]*b[0] + 2*b[1]*b[1] + b[2]*b[2] ) * epsilon;
    var result  = new Intersection("No Intersection");

    for ( var y = 0; y < yRoots.length; y++ ) {
        var xPoly = new Polynomial(
            a[0],
            a[3] + yRoots[y] * a[1],
            a[5] + yRoots[y] * (a[4] + yRoots[y]*a[2])
        );
        var xRoots = xPoly.getRoots();

        for ( var x = 0; x < xRoots.length; x++ ) {
            var test =
                ( a[0]*xRoots[x] + a[1]*yRoots[y] + a[3] ) * xRoots[x] + 
                ( a[2]*yRoots[y] + a[4] ) * yRoots[y] + a[5];
            if ( Math.abs(test) < norm0 ) {
                test =
                    ( b[0]*xRoots[x] + b[1]*yRoots[y] + b[3] ) * xRoots[x] +
                    ( b[2]*yRoots[y] + b[4] ) * yRoots[y] + b[5];
                if ( Math.abs(test) < norm1 ) {
                    result.appendPoint( new Point2D( xRoots[x], yRoots[y] ) );
                }
            }
        }
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectEllipseLine
*   
*   NOTE: Rotation will need to be added to this function
*
*****/
Intersection.intersectEllipseLine = function(c, rx, ry, a1, a2) {
    var result;
    var origin = new Vector2D(a1.x, a1.y);
    var dir    = Vector2D.fromPoints(a1, a2);
    var center = new Vector2D(c.x, c.y);
    var diff   = origin.subtract(center);
    var mDir   = new Vector2D( dir.x/(rx*rx),  dir.y/(ry*ry)  );
    var mDiff  = new Vector2D( diff.x/(rx*rx), diff.y/(ry*ry) );

    var a = dir.dot(mDir);
    var b = dir.dot(mDiff);
    var c = diff.dot(mDiff) - 1.0;
    var d = b*b - a*c;

    if ( d < 0 ) {
        result = new Intersection("Outside");
    } else if ( d > 0 ) {
        var root = Math.sqrt(d);
        var t_a  = (-b - root) / a;
        var t_b  = (-b + root) / a;

        if ( (t_a < 0 || 1 < t_a) && (t_b < 0 || 1 < t_b) ) {
            if ( (t_a < 0 && t_b < 0) || (t_a > 1 && t_b > 1) )
                result = new Intersection("Outside");
            else
                result = new Intersection("Inside");
        } else {
            result = new Intersection("Intersection");
            if ( 0 <= t_a && t_a <= 1 )
                result.appendPoint( a1.lerp(a2, t_a) );
            if ( 0 <= t_b && t_b <= 1 )
                result.appendPoint( a1.lerp(a2, t_b) );
        }
    } else {
        var t = -b/a;
        if ( 0 <= t && t <= 1 ) {
            result = new Intersection("Intersection");
            result.appendPoint( a1.lerp(a2, t) );
        } else {
            result = new Intersection("Outside");
        }
    }
    
    return result;
};


/*****
*
*   intersectEllipsePolygon
*
*****/
Intersection.intersectEllipsePolygon = function(c, rx, ry, points) {
    var result = new Intersection("No Intersection");
    var length = points.length;

    for ( var i = 0; i < length; i++ ) {
        var b1 = points[i];
        var b2 = points[(i+1) % length];
        var inter = Intersection.intersectEllipseLine(c, rx, ry, b1, b2);

        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;
};


/*****
*
*   intersectEllipseRectangle
*
*****/
Intersection.intersectEllipseRectangle = function(c, rx, ry, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectEllipseLine(c, rx, ry, min, topRight);
    var inter2 = Intersection.intersectEllipseLine(c, rx, ry, topRight, max);
    var inter3 = Intersection.intersectEllipseLine(c, rx, ry, max, bottomLeft);
    var inter4 = Intersection.intersectEllipseLine(c, rx, ry, bottomLeft, min);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;
};


/*****
*
*   intersectLineLine
*
*****/
Intersection.intersectLineLine = function(a1, a2, b1, b2) {
    var result;
    
    var ua_t = (b2.x - b1.x) * (a1.y - b1.y) - (b2.y - b1.y) * (a1.x - b1.x);
    var ub_t = (a2.x - a1.x) * (a1.y - b1.y) - (a2.y - a1.y) * (a1.x - b1.x);
    var u_b  = (b2.y - b1.y) * (a2.x - a1.x) - (b2.x - b1.x) * (a2.y - a1.y);

    if ( u_b != 0 ) {
        var ua = ua_t / u_b;
        var ub = ub_t / u_b;

        if ( 0 <= ua && ua <= 1 && 0 <= ub && ub <= 1 ) {
            result = new Intersection("Intersection");
            result.points.push(
                new Point2D(
                    a1.x + ua * (a2.x - a1.x),
                    a1.y + ua * (a2.y - a1.y)
                )
            );
        } else {
            result = new Intersection("No Intersection");
        }
    } else {
        if ( ua_t == 0 || ub_t == 0 ) {
            result = new Intersection("Coincident");
        } else {
            result = new Intersection("Parallel");
        }
    }

    return result;
};


/*****
*
*   intersectLinePolygon
*
*****/
Intersection.intersectLinePolygon = function(a1, a2, points) {
    var result = new Intersection("No Intersection");
    var length = points.length;

    for ( var i = 0; i < length; i++ ) {
        var b1 = points[i];
        var b2 = points[(i+1) % length];
        var inter = Intersection.intersectLineLine(a1, a2, b1, b2);

        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectLineRectangle
*
*****/
Intersection.intersectLineRectangle = function(a1, a2, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectLineLine(min, topRight, a1, a2);
    var inter2 = Intersection.intersectLineLine(topRight, max, a1, a2);
    var inter3 = Intersection.intersectLineLine(max, bottomLeft, a1, a2);
    var inter4 = Intersection.intersectLineLine(bottomLeft, min, a1, a2);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;
};


/*****
*
*   intersectPolygonPolygon
*
*****/
Intersection.intersectPolygonPolygon = function(points1, points2) {
    var result = new Intersection("No Intersection");
    var length = points1.length;

    for ( var i = 0; i < length; i++ ) {
        var a1 = points1[i];
        var a2 = points1[(i+1) % length];
        var inter = Intersection.intersectLinePolygon(a1, a2, points2);

        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;

};


/*****
*
*   intersectPolygonRectangle
*
*****/
Intersection.intersectPolygonRectangle = function(points, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectLinePolygon(min, topRight, points);
    var inter2 = Intersection.intersectLinePolygon(topRight, max, points);
    var inter3 = Intersection.intersectLinePolygon(max, bottomLeft, points);
    var inter4 = Intersection.intersectLinePolygon(bottomLeft, min, points);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;
};


/*****
*
*   intersectRayRay
*
*****/
Intersection.intersectRayRay = function(a1, a2, b1, b2) {
    var result;
    
    var ua_t = (b2.x - b1.x) * (a1.y - b1.y) - (b2.y - b1.y) * (a1.x - b1.x);
    var ub_t = (a2.x - a1.x) * (a1.y - b1.y) - (a2.y - a1.y) * (a1.x - b1.x);
    var u_b  = (b2.y - b1.y) * (a2.x - a1.x) - (b2.x - b1.x) * (a2.y - a1.y);

    if ( u_b != 0 ) {
        var ua = ua_t / u_b;

        result = new Intersection("Intersection");
        result.points.push(
            new Point2D(
                a1.x + ua * (a2.x - a1.x),
                a1.y + ua * (a2.y - a1.y)
            )
        );
    } else {
        if ( ua_t == 0 || ub_t == 0 ) {
            result = new Intersection("Coincident");
        } else {
            result = new Intersection("Parallel");
        }
    }

    return result;
};


/*****
*
*   intersectRectangleRectangle
*
*****/
Intersection.intersectRectangleRectangle = function(a1, a2, b1, b2) {
    var min        = a1.min(a2);
    var max        = a1.max(a2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectLineRectangle(min, topRight, b1, b2);
    var inter2 = Intersection.intersectLineRectangle(topRight, max, b1, b2);
    var inter3 = Intersection.intersectLineRectangle(max, bottomLeft, b1, b2);
    var inter4 = Intersection.intersectLineRectangle(bottomLeft, min, b1, b2);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;
};


/*****
*
*   bezout
*
*   This code is based on MgcIntr2DElpElp.cpp written by David Eberly.  His
*   code along with many other excellent examples are avaiable at his site:
*   http://www.magic-software.com
*
*****/
Intersection.bezout = function(e1, e2) {
    var AB    = e1[0]*e2[1] - e2[0]*e1[1];
    var AC    = e1[0]*e2[2] - e2[0]*e1[2];
    var AD    = e1[0]*e2[3] - e2[0]*e1[3];
    var AE    = e1[0]*e2[4] - e2[0]*e1[4];
    var AF    = e1[0]*e2[5] - e2[0]*e1[5];
    var BC    = e1[1]*e2[2] - e2[1]*e1[2];
    var BE    = e1[1]*e2[4] - e2[1]*e1[4];
    var BF    = e1[1]*e2[5] - e2[1]*e1[5];
    var CD    = e1[2]*e2[3] - e2[2]*e1[3];
    var DE    = e1[3]*e2[4] - e2[3]*e1[4];
    var DF    = e1[3]*e2[5] - e2[3]*e1[5];
    var BFpDE = BF + DE;
    var BEmCD = BE - CD;

    return new Polynomial(
        AB*BC - AC*AC,
        AB*BEmCD + AD*BC - 2*AC*AE,
        AB*BFpDE + AD*BEmCD - AE*AE - 2*AC*AF,
        AB*DF + AD*BFpDE - 2*AE*AF,
        AD*DF - AF*AF
    );
};