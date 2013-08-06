/** 
 * Draws a rounded rectangle using the current state of the canvas.  
 * If you omit the last three params, it will draw a rectangle  
 * outline with a 5 pixel border radius  
 * @param {Number} x The top left x coordinate 
 * @param {Number} y The top left y coordinate  
 * @param {Number} width The width of the rectangle  
 * @param {Number} height The height of the rectangle 
 * @param {Object} radius All corner radii. Defaults to 0,0,0,0; 
 * @param {Boolean} fill Whether to fill the rectangle. Defaults to false. 
 * @param {Boolean} stroke Whether to stroke the rectangle. Defaults to true. 
 */
CanvasRenderingContext2D.prototype.roundRect = function (x, y, width, height, radius, fill, stroke) {
    var cornerRadius = { upperLeft: 0, upperRight: 0, lowerLeft: 0, lowerRight: 0 };
    if (typeof stroke == "undefined") {
        stroke = true;
    }
    if (typeof radius === "object") {
        for (var side in radius) {
            cornerRadius[side] = radius[side];
        }
    }

    this.beginPath();
    this.moveTo(x + cornerRadius.upperLeft, y);
    this.lineTo(x + width - cornerRadius.upperRight, y);
    this.quadraticCurveTo(x + width, y, x + width, y + cornerRadius.upperRight);
    this.lineTo(x + width, y + height - cornerRadius.lowerRight);
    this.quadraticCurveTo(x + width, y + height, x + width - cornerRadius.lowerRight, y + height);
    this.lineTo(x + cornerRadius.lowerLeft, y + height);
    this.quadraticCurveTo(x, y + height, x, y + height - cornerRadius.lowerLeft);
    this.lineTo(x, y + cornerRadius.upperLeft);
    this.quadraticCurveTo(x, y, x + cornerRadius.upperLeft, y);
    this.closePath();
    if (stroke) {
        this.stroke();
    }
    if (fill) {
        this.fill();
    }
} 