describe('Timepicker feature', function() {
  'use strict';

  var $input1,
    $input2,
    $input3,
    $timepicker1,
    $timepicker2,
    $timepicker3,
    tp1,
    tp2,
    tp3;

  beforeEach(function () {
    loadFixtures('timepicker.html');

    $input1 = $('#timepicker1');
    $timepicker1 = $input1.timepicker();
    tp1 = $timepicker1.data('timepicker');

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
      showMeridian: false,
      showSeconds: true,
      defaultTime: '13:25:15'
    });
    tp3 = $timepicker3.data('timepicker');
  });

  afterEach(function () {
    if ($input1.data('timepicker') !== undefined) {
      $input1.data('timepicker').remove();
    }
    if ($input2.data('timepicker') !== undefined) {
      $input2.data('timepicker').remove();
    }
    if ($input3.data('timepicker') !== undefined) {
      $input3.data('timepicker').remove();
    }
    $input1.remove();
    $input2.remove();
    $input3.remove();
  });

  it('should be available on the jquery object', function() {
    expect($.fn.timepicker).toBeDefined();
  });

  it('should be chainable', function() {
    expect($timepicker1).toBe($input1);
  });

  it('should have sensible defaults', function() {
    expect(tp1.defaultTime).toBeTruthy();
    expect(tp1.minuteStep).toBe(15);
    expect(tp1.secondStep).toBe(15);
    expect(tp1.disableFocus).toBe(false);
    expect(tp1.showSeconds).toBe(false);
    expect(tp1.showInputs).toBe(true);
    expect(tp1.showMeridian).toBe(true);
    expect(tp1.template).toBe('dropdown');
    expect(tp1.modalBackdrop).toBe(false);
    expect(tp1.modalBackdrop).toBe(false);
    expect(tp1.isOpen).toBe(false);
    expect(tp1.showWidgetOnAddonClick).toBe(true);
  });

  it('should allow user to configure defaults', function() {
    expect(tp2.template).toBe('modal');
    expect(tp2.minuteStep).toBe(30);
  });

  it('should be configurable with data attributes', function() {
    $('body').append('<div id="hi" class="bootstrap-timepicker"><input id="customTimepicker" data-template="modal" data-minute-step="30" data-modal-backdrop="true" data-show-meridian="true" type="text"/></div');

    var $customInput = $('body').find('#customTimepicker'),
        tpCustom = $customInput.timepicker().data('timepicker');

    expect($('body').find('#customTimepicker').length).toBe(1);
    expect(tpCustom.template).toBe('modal');
    expect(tpCustom.minuteStep).toBe(30, 'data-minute-step not working');
    expect(tpCustom.modalBackdrop).toBe(true, 'data-modal-backdrop not working');
    expect(tpCustom.showMeridian).toBe(true, 'data-show-meridian not working');

    tpCustom.remove();
  });

  it('should have current time by default', function() {
    var dTime = new Date(),
      hour = dTime.getHours(),
      minutes = dTime.getMinutes(),
      meridian;

    if (minutes !== 0) {
      minutes = Math.ceil(minutes / tp1.minuteStep) * tp1.minuteStep;
    }

    if (minutes === 60) {
      hour += 1;
      minutes = 0;
    }

    if (hour < 13) {
      meridian = 'AM';
    } else {
      meridian = 'PM';
    }

    if (hour > 12) {
      hour = hour - 12;
    }
    if (hour === 0) {
      hour = 12;
    }

    expect(tp1.hour).toBe(hour);
    expect(tp1.minute).toBe(minutes);
    expect(tp1.meridian).toBe(meridian);
  });

  it('should not override time with current time if value is already set', function() {
    $('body').append('<div id="timepickerCustom"><input id="timepickerCustomInput" type="text" value="12:15 AM" /></div>');
    var $customInput = $('#timepickerCustomInput').timepicker(),
      tpCustom = $customInput.data('timepicker');

    expect($customInput.val()).toBe('12:15 AM');

    tpCustom.remove();
    $('#timepickerCustom').remove();
  });

  it('should have no value if defaultTime is set to false', function() {
    expect($input2.val()).toBe('');
  });

  it('should be able to set default time with config option', function() {
    expect(tp3.getTime()).toBe('13:25:15');
  });

  it('should update the element and widget with the setTime method', function() {
    tp2.setTime('9:15:20 AM');

    expect(tp2.hour).toBe(9);
    expect(tp2.minute).toBe(15);
    expect(tp2.second).toBe(20);
    expect(tp2.meridian).toBe('AM');
    expect($input2.val()).toBe('9:15:20 AM');
    expect(tp2.$widget.find('.bootstrap-timepicker-hour').val()).toBe('9');
    expect(tp2.$widget.find('.bootstrap-timepicker-minute').val()).toBe('15');
    expect(tp2.$widget.find('.bootstrap-timepicker-second').val()).toBe('20');
    expect(tp2.$widget.find('.bootstrap-timepicker-meridian').val()).toBe('AM');
  });

  it('should be able get & set the pickers time', function() {
    tp1.setTime('11:15 PM');
    expect(tp1.getTime()).toBe('11:15 PM');
    tp3.setTime('23:15:20');
    expect(tp3.getTime()).toBe('23:15:20');

    tp1.setTime('11pm');
    expect(tp1.getTime()).toBe('11:00 PM');
    tp3.setTime('11pm');
    expect(tp3.getTime()).toBe('23:00:00');

    tp1.setTime('11a');
    expect(tp1.getTime()).toBe('11:00 AM');
    tp3.setTime('11a');
    expect(tp3.getTime()).toBe('11:00:00');

    tp1.setTime('1');
    expect(tp1.getTime()).toBe('1:00 AM');
    tp3.setTime('1');
    expect(tp3.getTime()).toBe('1:00:00');

    tp1.setTime('13');
    expect(tp1.getTime()).toBe('12:00 AM');
    tp3.setTime('13');
    expect(tp3.getTime()).toBe('13:00:00');

    tp1.setTime('10:20p');
    expect(tp1.getTime()).toBe('10:20 PM');
    tp3.setTime('10:20p');
    expect(tp3.getTime()).toBe('22:20:00');

    tp1.setTime('10:20 p.m.');
    expect(tp1.getTime()).toBe('10:20 PM');
    tp3.setTime('10:20 p.m.');
    expect(tp3.getTime()).toBe('22:20:00');

    tp1.setTime('10:20a');
    expect(tp1.getTime()).toBe('10:20 AM');
    tp3.setTime('10:20a');
    expect(tp3.getTime()).toBe('10:20:00');

    tp1.setTime('10:2010');
    expect(tp1.getTime()).toBe('10:20 AM', 'setTime with 10:2010 on tp1');
    tp3.setTime('10:2010');
    expect(tp3.getTime()).toBe('10:20:10', 'setTime with 10:2010 on tp3');

    tp1.setTime('102010');
    expect(tp1.getTime()).toBe('10:20 AM', 'setTime with 102010 on tp1');
    tp3.setTime('102010');
    expect(tp3.getTime()).toBe('10:20:10', 'setTime with 102010 on tp3');

    tp1.setTime('2320');
    expect(tp1.getTime()).toBe('12:20 AM', 'setTime with 2320 on tp1');
    tp3.setTime('2320');
    expect(tp3.getTime()).toBe('23:20:00', 'setTime with 2320 on tp3');

    tp3.setTime('0:00');
    expect(tp3.getTime()).toBe('0:00:00', 'setTime with 0:00 on tp3');
  });

  it('should update picker on blur', function() {
    $input1.val('10:25 AM');
    expect(tp1.getTime()).not.toBe('10:25 AM');
    $input1.trigger('blur');
    expect(tp1.getTime()).toBe('10:25 AM');
  });

  it('should update element with updateElement method', function() {
    tp1.hour = 10;
    tp1.minute = 30;
    tp1.meridian = 'PM';
    tp1.updateElement();
    expect($input1.val()).toBe('10:30 PM');
  });

  it('should update widget with updateWidget method', function() {
    tp2.hour = 10;
    tp2.minute = 30;
    tp2.second = 15;

    expect(tp2.$widget.find('.bootstrap-timepicker-hour').val()).not.toBe('10');
    expect(tp2.$widget.find('.bootstrap-timepicker-minute').val()).not.toBe('30');
    expect(tp2.$widget.find('.bootstrap-timepicker-second').val()).not.toBe('15');

    tp2.updateWidget();

    expect(tp2.$widget.find('.bootstrap-timepicker-hour').val()).toBe('10');
    expect(tp2.$widget.find('.bootstrap-timepicker-minute').val()).toBe('30');
    expect(tp2.$widget.find('.bootstrap-timepicker-second').val()).toBe('15');
  });

  it('should update picker with updateFromElementVal method', function() {
    tp1.hour = 12;
    tp1.minute = 12;
    tp1.meridian = 'PM';
    tp1.update();

    $input1.val('10:30 AM');

    expect(tp1.$widget.find('.bootstrap-timepicker-hour').val()).not.toBe('10');
    expect(tp1.$widget.find('.bootstrap-timepicker-minute').val()).not.toBe('30');
    expect(tp1.$widget.find('.bootstrap-timepicker-meridian').val()).not.toBe('AM');
    expect(tp1.hour).not.toBe(10);
    expect(tp1.minute).not.toBe(30);
    expect(tp1.meridian).not.toBe('AM');

    tp1.updateFromElementVal();

    expect(tp1.$widget.find('.bootstrap-timepicker-hour').val()).toBe('10');
    expect(tp1.$widget.find('.bootstrap-timepicker-minute').val()).toBe('30');
    expect(tp1.$widget.find('.bootstrap-timepicker-meridian').val()).toBe('AM');
    expect(tp1.hour).toBe(10);
    expect(tp1.minute).toBe(30);
    expect(tp1.meridian).toBe('AM');
  });

  it('should update picker with updateFromWidgetInputs method', function() {
    tp1.hour = 12;
    tp1.minute = 12;
    tp1.meridian = 'PM';
    tp1.update();

    tp1.$widget.find('.bootstrap-timepicker-hour').val(10);
    tp1.$widget.find('.bootstrap-timepicker-minute').val(30);
    tp1.$widget.find('.bootstrap-timepicker-meridian').val('AM');

    expect(tp1.hour).not.toBe(10);
    expect(tp1.minute).not.toBe(30);
    expect(tp1.meridian).not.toBe('AM');
    expect($input1.val()).not.toBe('10:30 AM');

    tp1.updateFromWidgetInputs();

    expect(tp1.hour).toBe(10);
    expect(tp1.minute).toBe(30);
    expect(tp1.meridian).toBe('AM');
    expect($input1.val()).toBe('10:30 AM');
  });

  it('should increment hours with incrementHour method', function() {
    tp1.hour = 9;
    tp1.incrementHour();
    expect(tp1.hour).toBe(10);
  });

  it('should decrement hours with decrementHour method', function() {
    tp1.hour = 9;
    tp1.decrementHour();
    expect(tp1.hour).toBe(8);
  });

  it('should toggle meridian if hour goes past 12', function() {
    $input1.val('11:00 AM');
    tp1.updateFromElementVal();
    tp1.incrementHour();

    expect(tp1.hour).toBe(12);
    expect(tp1.minute).toBe(0);
    expect(tp1.meridian).toBe('PM');
  });

  it('should toggle meridian if hour goes below 1', function() {
    $input1.val('11:00 AM');
    tp1.updateFromElementVal();
    tp1.incrementHour();

    expect(tp1.hour).toBe(12);
    expect(tp1.minute).toBe(0);
    expect(tp1.meridian).toBe('PM');
  });

  it('should set hour to 1 if hour increments on 12 for 12h clock', function() {
    $input1.val('11:15 PM');
    tp1.updateFromElementVal();
    tp1.incrementHour();
    tp1.incrementHour();

    expect(tp1.getTime()).toBe('1:15 AM');
  });

  it('should set hour to 0 if hour increments on 23 for 24h clock', function() {
    $input3.val('22:15:30');
    tp3.updateFromElementVal();
    tp3.incrementHour();
    tp3.incrementHour();

    expect(tp3.hour).toBe(0);
    expect(tp3.minute).toBe(15);
    expect(tp3.second).toBe(30);
  });

  it('should increment minutes with incrementMinute method', function() {
    tp1.minute = 10;
    tp1.incrementMinute();

    expect(tp1.minute).toBe(15);

    tp2.minute = 0;
    tp2.incrementMinute();

    expect(tp2.minute).toBe(30);
  });

  it('should decrement minutes with decrementMinute method', function() {
    tp1.hour = 11;
    tp1.minute = 0;
    tp1.decrementMinute();

    expect(tp1.hour).toBe(10);
    expect(tp1.minute).toBe(45);

    tp2.hour = 11;
    tp2.minute = 0;
    tp2.decrementMinute();

    expect(tp2.hour).toBe(10);
    expect(tp2.minute).toBe(30);
  });


  it('should increment hour if minutes increment past 59', function() {
    $input1.val('11:55 AM');
    tp1.updateFromElementVal();
    tp1.incrementMinute();
    tp1.update();

    expect(tp1.getTime()).toBe('12:00 PM');
  });

  it('should toggle meridian with toggleMeridian method', function() {
    tp1.meridian = 'PM';
    tp1.toggleMeridian();

    expect(tp1.meridian).toBe('AM');
  });

  it('should increment seconds with incrementSecond method', function() {
    tp1.second = 0;
    tp1.incrementSecond();

    expect(tp1.second).toBe(15);

    tp2.second = 0;
    tp2.incrementSecond();

    expect(tp2.second).toBe(30);
  });

  it('should decrement seconds with decrementSecond method', function() {
    tp2.hour = 11;
    tp2.minute = 0;
    tp2.second = 0;
    tp2.decrementSecond();

    expect(tp2.minute).toBe(59);
    expect(tp2.second).toBe(30);
  });


  it('should increment minute by 1 if seconds increment past 59', function() {
    $input2.val('11:55:30 AM');
    tp2.updateFromElementVal();
    tp2.incrementSecond();
    tp2.update();

    expect(tp2.getTime()).toBe('11:56:00 AM');
  });

  it('should not have any remaining events if remove is called', function() {
    var hideEvents = 0;

    $input1.on('hide.timepicker', function() {
      hideEvents++;
    });

    $input1.parents('div').find('.add-on').trigger('click');
    $('body').trigger('mousedown');

    expect(hideEvents).toBe(1);

    tp1.remove();
    tp2.remove();
    tp3.remove();

    $('body').trigger('click');
    expect(hideEvents).toBe(1);
  });

  it('should be able to reset time by using setTime 0/null', function() {
    tp1.hour = 10;
    tp1.minute = 30;
    tp1.meridian = 'PM';
    tp1.updateElement();

    $input1.timepicker('setTime', null);
//    tp1.update();
    expect(tp1.getTime()).toBe('');
  });


  it('should not have the widget in the DOM if remove method is called', function() {
    tp1.showWidget();
    tp2.showWidget();
    tp3.showWidget();
    expect($('body')).toContain('.bootstrap-timepicker-widget');
    tp1.remove();
    tp2.remove();
    tp3.remove();
    expect($('body')).not.toContain('.bootstrap-timepicker-widget');
  });

  it('should be able to set time from a script', function() {
    $input1.timepicker('setTime', '12:35 PM');
    tp1.update();
    expect(tp1.getTime()).toBe('12:35 PM');
  });

  it('should be able to opened from script', function() {
    expect(tp1.isOpen).toBe(false);
    $input1.timepicker('showWidget');
    expect(tp1.isOpen).toBe(true);
  });

});
