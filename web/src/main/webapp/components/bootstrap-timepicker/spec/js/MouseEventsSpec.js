describe('Mouse events feature', function() {
  'use strict';

  var $input1,
    $input2,
    $input3,
    $input4,
    $input5,
    $timepicker1,
    $timepicker2,
    $timepicker3,
    $timepicker4,
    $timepicker5,
    tp1,
    tp2,
    tp3,
    tp4,
    tp5;

  beforeEach(function () {
    loadFixtures('timepicker.html');

    $input1 = $('#timepicker1');
    $timepicker1 = $input1.timepicker();
    tp1 = $timepicker1.data('timepicker');

    $input5 = $('#timepicker5');
    $timepicker5 = $input1.timepicker({showWidgetOnAddonClick: false});
    tp5 = $timepicker5.data('timepicker');

    $input2 = $('#timepicker2');
    $timepicker2 = $input2.timepicker({
      template: 'modal',
      showSeconds: true,
      minuteStep: 30,
      secondStep: 30,
      defaultTime: false
    });
    tp2 = $timepicker2.data('timepicker');

    $input3 = $('#timepicker3');
    $timepicker3 = $input3.timepicker({
      defaultTime: '23:15:20',
      showMeridian: false,
      showSeconds: true
    });
    tp3 = $timepicker3.data('timepicker');

    $input4 = $('#timepicker4');
    $timepicker4 = $input4.timepicker({
			minuteStep: 5,
			showInputs: false,
			showMeridian: true,
			template: 'modal',
			disableFocus: true
    });
    tp4 = $timepicker4.data('timepicker');
  });

  afterEach(function () {
    $input1.data('timepicker').remove();
    $input2.data('timepicker').remove();
    $input3.data('timepicker').remove();
    $input4.data('timepicker').remove();
    $input1.remove();
    $input2.remove();
    $input3.remove();
    $input4.remove();
  });

  it('should be shown and trigger show events on input click', function() {
    var showEvents = 0;

    $input1.on('show.timepicker', function() {
      showEvents++;
    });

    $input1.parents('div').find('.add-on').trigger('click');

    expect(tp1.isOpen).toBe(true);
    expect(showEvents).toBe(1);
  });

  it('should be hidden and trigger hide events on click outside of widget', function() {
    var hideEvents = 0,
        time;
    tp1.setTime('11:30 AM');
    tp1.update();

    $input1.on('hide.timepicker', function(e) {
      hideEvents++;

      time = e.time.value;
    });

    $input1.parents('div').find('.add-on').trigger('click');
    expect(tp1.isOpen).toBe(true);

    //tp1.$widget.find('.bootstrap-timepicker-hour').trigger('mousedown');
    $('body').trigger('mousedown');

    expect(tp1.isOpen).toBe(false, 'widget is still open');
    expect(hideEvents).toBe(1, 'hide event was not thrown once');
    expect(time).toBe('11:30 AM');

  });

  it('should not show widget when clicking add-on icon if showWidgetOnAddonClick is false', function() {
    expect(tp5.isOpen).toBe(false);
    $input5.parents('div').find('.add-on').trigger('click');
    expect(tp5.isOpen).toBe(false);
  });

  it('should increment hour on button click', function() {
    tp1.setTime('11:30 AM');
    tp1.update();

    var count = 0;
    $input1.on('changeTime.timepicker', function() {
      count++;
    });

    tp1.$widget.find('a[data-action="incrementHour"]').trigger('click');

    expect(tp1.getTime()).toBe('12:30 PM');
    expect(count).toBe(1);

    tp2.$widget.find('a[data-action="incrementHour"]').trigger('click');
    expect(tp2.getTime()).toBe('1:00:00 AM');
  });

  it('should decrement hour on button click and fire 1 changeTime event', function() {
    tp1.setTime('12:30 PM');
    tp1.update();

    var count = 0;
    $input1.on('changeTime.timepicker', function() {
      count++;
    });

    tp1.$widget.find('a[data-action="decrementHour"]').trigger('click');

    expect(tp1.getTime()).toBe('11:30 AM', 'meridian isnt toggling');
    expect(count).toBe(1);

    tp2.$widget.find('a[data-action="incrementHour"]').trigger('click');
    tp2.$widget.find('a[data-action="incrementHour"]').trigger('click');
    tp2.$widget.find('a[data-action="decrementHour"]').trigger('click');
    expect(tp2.getTime()).toBe('1:00:00 AM');
  });

  it('should increment minute on button click and fire 1 changeTime event', function() {
    tp1.setTime('11:30 AM');
    tp1.update();
		tp4.setTime('11:30 AM');
		tp4.update();

    var count = 0;
    $input1.on('changeTime.timepicker', function() {
      count++;
    });

    tp1.$widget.find('a[data-action="incrementMinute"]').trigger('click');
    expect(tp1.getTime()).toBe('11:45 AM');

    tp2.$widget.find('a[data-action="incrementMinute"]').trigger('click');
    expect(tp2.getTime()).toBe('0:30:00 AM');

    expect(count).toBe(1);

		$input4.trigger('click');
    tp4.$widget.find('a[data-action="incrementMinute"]').trigger('click');
    tp4.$widget.find('a[data-action="decrementHour"]').trigger('click');
		$input4.closest('modal').find('.btn-primary').trigger('click');
    expect(tp4.getTime()).toBe('10:35 AM');
		expect($input4.val()).toBe('10:35 AM');
  });

  it('should decrement minute on button click', function() {
    tp1.setTime('12:30 PM');
    tp1.update();
    tp4.setTime('11:30 AM');
    tp4.update();

    tp1.$widget.find('a[data-action="decrementMinute"]').trigger('click');
    expect(tp1.getTime()).toBe('12:15 PM');

    tp4.$widget.find('a[data-action="decrementMinute"]').trigger('click');
    expect(tp4.getTime()).toBe('11:25 AM');
  });

  it('should go from 11:00 AM to 1:00 AM on 2 hour increments and fire 2 change time events', function() {
    tp1.setTime('11:00 AM');
    tp1.update();

    var count = 0;
    $input1.on('changeTime.timepicker', function() {
      count++;
    });

    tp1.$widget.find('a[data-action="incrementHour"]').trigger('click');
    tp1.$widget.find('a[data-action="incrementHour"]').trigger('click');

    expect(tp1.getTime()).toBe('1:00 PM');

    expect(count).toBe(2);
  });

  it('should go from 11:45 AM to 12:00 PM on 4 minute increments and fire 1 change time events', function() {
    tp1.setTime('11:45 AM');
    tp1.update();

    var count = 0;
    $input1.on('changeTime.timepicker', function() {
      count++;
    });

    tp1.$widget.find('a[data-action="incrementMinute"]').trigger('click');

    expect(tp1.getTime()).toBe('12:00 PM');

    expect(count).toBe(1);
  });


  it('should be 11:30:00 PM if minute is decremented on empty input', function() {
    tp2.$widget.find('a[data-action="decrementMinute"]').trigger('click');
    expect(tp2.getTime()).toBe('11:30:00 PM');
  });

  it('should increment second on button click', function() {
    tp2.setTime('11:30:15 AM');
    tp2.update();

    tp2.$widget.find('a[data-action="incrementSecond"]').trigger('click');

    expect(tp2.getTime()).toBe('11:30:30 AM');
  });

  it('should decrement second on button click', function() {
    tp2.setTime('12:30:15 PM');
    tp2.update();

    tp2.$widget.find('a[data-action="decrementSecond"]').trigger('click');

    expect(tp2.getTime()).toBe('12:29:45 PM');
  });

  it('should be 11:30:00 PM if minute is decremented on empty input', function() {
    tp2.$widget.find('a[data-action="decrementMinute"]').trigger('click');
    expect(tp2.getTime()).toBe('11:30:00 PM');
  });

  it('should increment second on button click', function() {
    tp2.setTime('11:30:15 AM');
    tp2.update();

    tp2.$widget.find('a[data-action="incrementSecond"]').trigger('click');

    expect(tp2.getTime()).toBe('11:30:30 AM');
  });

  it('should decrement second on button click', function() {
    tp2.setTime('12:30:15 PM');
    tp2.update();

    tp2.$widget.find('a[data-action="decrementSecond"]').trigger('click');

    expect(tp2.getTime()).toBe('12:29:45 PM');
  });

  it('should toggle meridian on button click', function() {
    tp1.setTime('12:30 PM');
    tp1.update();

    tp1.$widget.find('a[data-action="toggleMeridian"]').first().trigger('click');
    expect(tp1.getTime()).toBe('12:30 AM');
    tp1.$widget.find('a[data-action="toggleMeridian"]').last().trigger('click');
    expect(tp1.getTime()).toBe('12:30 PM');
  });


  it('should trigger changeTime event if time is changed', function() {
    var eventCount = 0,
        time;

    $input1.timepicker().on('changeTime.timepicker', function(e) {
      eventCount++;
      time = e.time.value;
    });

    tp1.setTime('11:30 AM');

    expect(eventCount).toBe(1);
    expect(time).toBe('11:30 AM');

    tp1.$widget.find('a[data-action="incrementHour"]').trigger('click');

    expect(eventCount).toBe(2);
    expect(tp1.getTime()).toBe('12:30 PM');
    expect(time).toBe('12:30 PM');

    tp1.$widget.find('a[data-action="incrementMinute"]').trigger('click');

    expect(eventCount).toBe(3);
    expect(tp1.getTime()).toBe('12:45 PM');
  });

  it('should highlight widget inputs on click', function() {
      //TODO;
      //tp1.setTime('11:55 AM');
      //tp1.update();

      //$input1.parents('.bootstrap-timepicker').find('.add-on').trigger('click');
      //expect(tp1.isOpen).toBe(true);
      //expect(tp1.$widget.find('.bootstrap-timepicker-hour').val()).toBe('11');
      //tp1.$widget.find('.bootstrap-timepicker-hour').trigger('click');
      //var hour1 = window.getSelection().toString();
////var range = window.getSelection().getRangeAt(0);
////var hour1 = range.extractContents();

      //expect(hour1).toBe('11', 'hour input not being highlighted');

      //tp1.$widget.find('.bootstrap-timepicker-minute').trigger('click');
      //var minute1 = window.getSelection().toString();
      //expect(minute1).toBe('55', 'minute input not being highlighted');

      //tp1.$widget.find('.bootstrap-timepicker-meridian').trigger('click');
      //var meridian1 = window.getSelection().toString();
      //expect(meridian1).toBe('AM', 'meridian input not being highlighted');
  });
});

