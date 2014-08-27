###
Source, bug reports, examples: https://github.com/tamc/Sankey
Copyright: Thomas Counsell 2010, 2011
Licence: MIT Open Source licence http://www.opensource.org/licenses/mit-license.php
###
class Sankey

  constructor: () ->
    # The id of the element to draw the sankey in, defaults to sankey
    @display_in_element = 'sankey' 
    # The width of the sankey diagram, defaults to the width of the element
    @display_width = $('#sankey').width() # pixels
    # The height of the sankey diagram, defaults to the height of the element
    @display_height = $('#sankey').height() # pixels
    
    # And here is the Raphael drawing element filling that space
    @r = Raphael(@display_in_element,@display_width,@display_height)
    
    # Margin to left most box
    @left_margin = 100
    # Margin to right most box
    @right_margin = 100
    # Vertical spacing between transformation boxes in the same column
    @y_space = 10
    # Don't bother drawing a line or a box if its size is less than this in pixels
    @threshold_for_drawing = 0 
    # Width of transformation boxes in pixels
    @box_width = 50
    # Width of the border line on flow lines in pixels
    @flow_edge_width = 2
    # On flow lines, proportion of the horizontal distance to position the control point at
    @flow_curve = 0.25 
    # Stores all the transformation boxes
    @boxes = {}
    @box_array = []
    # Stores all the transformation lines
    @lines = {}
    @line_array = []
    # Stores the organisation of the diagram
    @stacks = []
    # Stores the bubbles that can be drawn on transformation boxes
    @bubbles = []
    @bubbleColor = '#000'
    @bubbleLabelColor = '#fff'
  
  find_or_create_transformation_box: (name) ->
    unless @boxes[name]?
      new_box = new TransformationBox(this,name)
      @boxes[name] = new_box
      @box_array.push(new_box)
    return @boxes[name]
  
  lineName: (start,end) ->
    "#{start}-#{end}"
  
  createLine: (datum) ->
    return if datum[0] == 0
    new_line = new FlowLine(this,datum[0],datum[1],datum[2])
    @lines[@lineName(datum[0],datum[2])] = new_line
    @line_array.push(new_line)
  
  setData: (data) ->
    for datum in data
      @createLine(datum)
    
  setBubbles: (data) ->
    @bubbles = data
      
  updateData: (data) ->
    for datum in data
      line = @lines[@lineName(datum[0],datum[2])]
      if line
        line.setFlow(datum[1])
      else
        @createLine(datum)
  
  # Called to turn whatever unit the data is in into pixels for the flow lines
  convert_flow_values_callback: (flow) ->
    flow

  # Called to turn whatever unit the data is in into a string for the flow lines labels
  convert_flow_labels_callback: (flow) ->
    flow
  
  # Called to turn whatever unit the data is in into a string for the transformation box labels
  convert_box_value_labels_callback: (flow) ->
    @convert_flow_labels_callback(flow)
  
  # Called to turn the names of transformation boxes into labels
  convert_box_description_labels_callback: (name) ->
    name
  
  # Called to turn whatever unit the bubble values are in into pixels
  convert_bubble_values_callback: (size) ->
    size
  
  # Called to turn the bubble value into a label for the bubble
  convert_bubble_labels_callback: (size) ->
    size
  
  # This callback can be used to tweak the layout of the boxes
  nudge_boxes_callback: () ->
    undefined
  
  # This callback can be used to tweak the colouring of lines
  nudge_colours_callback: () ->
    undefined
  
  stack: (x,box_names,y_box) ->
    @stacks.push { x: x, box_names: box_names, y_box: y_box }
    
  setColors: (colors) ->
    for own box_name, color of colors
      box = @find_or_create_transformation_box(box_name)
      box.line_colour = colors[box.name] || box.line_colour
  
  recolour: (lines,new_colour) ->
    for line in lines
      line.colour = new_colour    
  
  calculateXStep: () ->
    maximum_x = 0
    for stack in @stacks
      maximum_x = stack.x if stack.x > maximum_x
    (@display_width - @left_margin - @right_margin) / maximum_x
  
  # Work out where everything should go
  position_boxes_and_lines: () ->
    # Horizontal spacing between stacks of transformation boxes
    x_step = @calculateXStep()
    for stack in @stacks
      x = stack.x
      if stack.y_box?
        y = @boxes[stack.y_box]?.y || 10
      else
        y = 10
      for name in stack.box_names
        box = @boxes[name]
        unless box?
          # alert "Can't find transformation called #{name}"
        else
          box.y = y
          box.x = @left_margin + (x * x_step)
          y = box.b() + @y_space
    
    @nudge_boxes_callback()
            
    for box in @box_array
      box.position_and_colour_lines()
    
    for bubble in @bubbles
      @boxes[bubble[0]]?.bubbleValue = bubble[1]
      
    @nudge_colours_callback()
    
    @line_array.sort( (a,b) -> 
       b.size - a.size
    )
  
  draw: () ->
    @position_boxes_and_lines()
      
    #  Draw the lines
    for line in @line_array
      if line.size > @threshold_for_drawing
        line.draw(@r)

    #  Draw the boxes over the top
    for box in @box_array
      if box.size() > @threshold_for_drawing
        box.draw(@r)
  
  redraw: () ->
    @position_boxes_and_lines()

    for line in @line_array
      line.redraw(@r)
      
    for box in @box_array
      box.redraw(@r)
  
  # Used for the mouseovers
  fade_unless_highlighted: () ->
    for line in @line_array
      line.fade_unless_highlighted()
      undefined
    for box in @box_array
      box.fade_unless_highlighted()
      undefined
  
  un_fade: () ->
    for line in @line_array
      line.un_fade()
      undefined
    for box in @box_array
      box.un_fade()
      undefined
            
class FlowLine 
  constructor: (@sankey,left_box_name,flow,right_box_name) ->
    @setFlow flow
    @colour = undefined
    @ox = 0
    @oy = 0
    @dx = 0
    @dy = 0
    @left_box = @sankey.find_or_create_transformation_box(left_box_name)
    @right_box = @sankey.find_or_create_transformation_box(right_box_name)
    @left_box.right_lines.push(this)
    @right_box.left_lines.push(this)

  setFlow: (flow) ->
    @flow = flow
    @size = @sankey.convert_flow_values_callback(@flow)
  
  labelText: () ->
    @sankey.convert_flow_labels_callback(@flow)
  
  path: () ->
    curve = ((@dx-@ox) * @sankey.flow_curve)
    "M "+@ox+","+@oy+" Q "+(@ox+curve)+","+@oy+" "+((@ox+@dx)/2)+","+((@oy+@dy)/2)+" Q "+(@dx-curve)+","+@dy+" "+@dx+","+@dy
  
  innerWidth: () ->
    return (@size - @sankey.flow_edge_width) if @size > @sankey.flow_edge_width
    @size
  
  innerColor: () ->
    c = Raphael.rgb2hsb(@colour)
    if c.h != 0 && c.s != 0
      if c.b > 0.5
        c.b = c.b - 0.15
      else
        c.b = c.b + 0.15
    Raphael.hsb2rgb(c.h,c.s,c.b)
  
  draw: (r) ->
    @outer_line = r.path(@path()).attr({'stroke-width':@size, 'stroke':@colour})
    @inner_line = r.path(@path()).attr({'stroke-width':@innerWidth(), 'stroke':@innerColor()})
    r.set().push(@inner_line,@outer_line).hover(@hover_start,@hover_stop)
    @left_label = r.text((@ox+1),(@oy-(@size/2)-5),@labelText()).attr({'text-anchor':'start'})
    @right_label = r.text((@dx-1),(@dy-(@size/2)-5),@labelText()).attr({'text-anchor':'end'})
    @left_label.hide()
    @right_label.hide()
    
  hover_start: (event) =>
    @highlight(true,true)
    @sankey.fade_unless_highlighted()

  hover_stop: (event) =>
    @un_highlight(true,true)
    @sankey.un_fade()

  redraw: (r) ->
    @draw(r) unless @outer_line? 
    @outer_line.attr({path:@path(),'stroke-width':@size})
    @inner_line.attr({path:@path(), 'stroke-width':@innerWidth()})
    @left_label.attr({text:@labelText(), x:(@ox+1), y: (@oy-(@size/2)-5) })
    @right_label.attr({text:@labelText(), x:(@dx-1), y: (@dy-(@size/2)-5) })

  fade_unless_highlighted: () ->
    return false unless @outer_line?
    return false unless @inner_line?
    return false if @highlighed is true
    @outer_line.attr({'opacity':'0.1'})
    @inner_line.attr({'opacity':'0.1'})

  un_fade: () ->
    return false unless @outer_line?
    return false unless @inner_line?
    return false if @highlighed is true
    @outer_line.attr({'opacity':'1.0'})
    @inner_line.attr({'opacity':'1.0'})

  highlight: (left,right) ->
    return false unless @outer_line?
    return false unless @inner_line?

    @highlighed = true

    if left
      @left_label.toFront()
      @left_label.show()
      @left_box.highlight()  

    if right
      @right_label.toFront()
      @right_label.show()
      @right_box.highlight() 

  un_highlight: (left,right) ->
    return false unless @outer_line?
    @highlighed = false
    if left
      @left_label.hide()
      @left_box.un_highlight()   

    if right
      @right_label.hide()
      @right_box.un_highlight()

class TransformationBox
  
  constructor: (@sankey,@name) ->
    @label_text = @sankey.convert_box_description_labels_callback(name)
    @line_colour = "orange"
    @left_lines = []
    @right_lines = []
    @x = 0
    @y = 0
    @bubbleValue = null

  b: () -> 
    @y + @size()
    
  is_left_box: () -> 
    @left_lines.length == 0
    
  is_right_box: () -> 
    @right_lines.length == 0
    
  size: () ->
    s = 0
    if @is_left_box()
      lines = @right_lines
    else
      lines = @left_lines
    for line in lines
      if line.size > @sankey.threshold_for_drawing
        s = s + line.size
    return s
  
  flow: () ->
    s = 0
    if @is_left_box()
      lines = @right_lines
    else
      lines = @left_lines
    for line in lines
      if line.size > @sankey.threshold_for_drawing
        s = s + line.flow
    return s    

  position_and_colour_lines: () ->
    ly = @y
    left_lines = @left_lines
    left_lines.sort( (a,b) -> 
      a.left_box.y - b.left_box.y
    )
    for line in left_lines
      line.dx = @x
      line.dy = ly + (line.size/2)
      ly = ly + (line.size)
      
    ry = @y
    right_lines = @right_lines
    right_lines.sort( (a,b) -> 
      a.right_box.y - b.right_box.y
    )
    box_width = @sankey.box_width
    for line in right_lines
      line.colour?= @line_colour
      line.ox = @x + box_width
      line.oy = ry + (line.size/2)
      ry = ry + (line.size)

  valueLabelText: () ->
    @sankey.convert_box_value_labels_callback(@flow())
  
  descriptionLabelText: () ->
    @label_text
  
  labelPositionX: () ->
    return @x-3.0 if @is_left_box()
    return @x+@sankey.box_width+3.0 if @is_right_box()
    @x+(@sankey.box_width/2)
  
  labelPositionY: () ->
    @y+(@size()/2)
    
  labelAttributes: () ->
    return {'text-anchor':'end'} if @is_left_box()
    return {'text-anchor':'start'} if @is_right_box()
    {}
  
  numberLabelPositionX: () ->
    @x+(@sankey.box_width/2)
    
  numberLabelPositionY: () ->
    @y-5
    
  bubbleSize: () ->
    Math.sqrt(@sankey.convert_bubble_values_callback(Math.abs(@bubbleValue)))
    
  bubbleLabel: () ->
    @sankey.convert_bubble_labels_callback(@bubbleValue)
  
  bubbleColourForValue: () ->
    return @sankey.bubbleColor if @bubbleValue > 0
    return @sankey.bubbleColor unless @sankey.negativeBubbleColor?
    @sankey.negativeBubbleColor
  
  bubbleLabelColourForValue: () ->
    return @sankey.bubbleLabelColor if @bubbleValue > 0
    return @sankey.bubbleLabelColor unless @sankey.negativeBubbleLabelColor?
    @sankey.negativeBubbleLabelColor
  
  draw: (r) ->
    return false unless @size() > @sankey.threshold_for_drawing
    box_width = @sankey.box_width
    @box = r.rect(@x,@y,box_width,@size()).attr({'fill':"#E8E2FF","stroke":"#D4CBF2"})
    @label = r.text(@labelPositionX(),@labelPositionY(),@descriptionLabelText()).attr(@labelAttributes())

    if @bubbleValue?
      @bubble_circle = r.circle(@x+box_width,@y,@bubbleSize()).attr({'fill': @bubbleColourForValue(),'stroke-width':0})
      @bubble_label = r.text(@x+box_width,@y,@bubbleLabel()).attr({'stroke': @bubbleLabelColourForValue(),'text-anchor':'middle'})
  
    @number_label = r.text(@numberLabelPositionX(),@numberLabelPositionY(),@valueLabelText())
    @number_label.hide()

    r.set().push(@number_label,@label,@box,@bubble_circle,@bubble_label).hover(@hover_start,@hover_end)
  
  redraw: (r) ->
    @draw(r) unless @box?
    return unless @box?
    @box.attr({y: @y, height:@size()}) 
    @label.attr({y: @labelPositionY()})
    @number_label.attr({y: @numberLabelPositionY(),text:@valueLabelText()})
    if @bubbleValue?
      if @bubble_circle?
        @bubble_circle.attr(cy:@y,r:@bubbleSize(),fill:@bubbleColourForValue())
        @bubble_label.attr(y:@y,text:@bubbleLabel(),'stroke':@bubbleLabelColourForValue())
      else
        @draw(r)
    if @size() <= @sankey.threshold_for_drawing
      @box.hide()
      @label.hide()
      @bubble_circle.hide() if @bubble_circle?
    else
      @box.show()
      @label.show()
      @bubble_circle.show() if @bubble_circle?
    
  hover_start: () =>
    @highlight()
    @number_label.toFront()
    @number_label.show()
    
    for line in @left_lines
      line.highlight(true,false)

    for line in @right_lines
      line.highlight(false,true)

    @sankey.fade_unless_highlighted()
  
  hover_end: () =>
    @un_highlight()
    @number_label.hide()

    for line in @left_lines
      line.un_highlight(true,false)

    for line in @right_lines
      line.un_highlight(false,true)

    @sankey.un_fade()
  
  highlight: () ->
    return false unless @box?
    @highlighed = true    

  un_highlight: () ->
    return false unless @box?
    @highlighed = false    

  fade_unless_highlighted: () ->
    return false unless @box?
    return false if @highlighed is true
    @box.attr({'opacity':'0.1'})
    @label.attr({'opacity':'0.1'})
    @bubble_circle.attr({'opacity':'0.1'}) if @bubble_circle?
    @bubble_label.attr({'opacity':'0.1'}) if @bubble_label?

  un_fade: () ->
    return false unless @box?
    return false if @highlighed is true
    @box.attr({'opacity':'1.0'})
    @label.attr({'opacity':'1.0'})
    @bubble_circle.attr({'opacity':'1.0'}) if @bubble_circle?
    @bubble_label.attr({'opacity':'1.0'}) if @bubble_label?
    

window.Sankey = Sankey
  
