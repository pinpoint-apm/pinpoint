module('Options', {
    setup: function(){},
    teardown: function(){
        $('#qunit-fixture *').each(function(){
            var t = $(this);
            if ('datepicker' in t.data())
                t.datepicker('remove');
        });
    }
});

test('Autoclose', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    autoclose: true
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;


    input.focus();
    ok(picker.is(':visible'), 'Picker is visible');
    target = picker.find('.datepicker-days tbody td:nth(7)');
    equal(target.text(), '4'); // Mar 4

    target.click();
    ok(picker.is(':not(:visible)'), 'Picker is hidden');
    datesEqual(dp.dates[0], UTCDate(2012, 2, 4));
    datesEqual(dp.viewDate, UTCDate(2012, 2, 4));
});

test('Startview: year view (integer)', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    startView: 1
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':not(:visible)'), 'Days view hidden');
        ok(picker.find('.datepicker-months').is(':visible'), 'Months view visible');
        ok(picker.find('.datepicker-years').is(':not(:visible)'), 'Years view hidden');
});

test('Startview: year view (string)', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    startView: 'year'
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':not(:visible)'), 'Days view hidden');
        ok(picker.find('.datepicker-months').is(':visible'), 'Months view visible');
        ok(picker.find('.datepicker-years').is(':not(:visible)'), 'Years view hidden');
});

test('Startview: decade view (integer)', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    startView: 2
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':not(:visible)'), 'Days view hidden');
        ok(picker.find('.datepicker-months').is(':not(:visible)'), 'Months view hidden');
        ok(picker.find('.datepicker-years').is(':visible'), 'Years view visible');
});

test('Startview: decade view (string)', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    startView: 'decade'
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':not(:visible)'), 'Days view hidden');
        ok(picker.find('.datepicker-months').is(':not(:visible)'), 'Months view hidden');
        ok(picker.find('.datepicker-years').is(':visible'), 'Years view visible');
});

test('Today Button: today button not default', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd'
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':visible'), 'Days view visible');
        ok(picker.find('.datepicker-days tfoot .today').is(':not(:visible)'), 'Today button not visible');
});

test('Today Button: today visibility when enabled', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    todayBtn: true
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':visible'), 'Days view visible');
        ok(picker.find('.datepicker-days tfoot .today').is(':visible'), 'Today button visible');

        picker.find('.datepicker-days thead th.datepicker-switch').click();
        ok(picker.find('.datepicker-months').is(':visible'), 'Months view visible');
        ok(picker.find('.datepicker-months tfoot .today').is(':visible'), 'Today button visible');

        picker.find('.datepicker-months thead th.datepicker-switch').click();
        ok(picker.find('.datepicker-years').is(':visible'), 'Years view visible');
        ok(picker.find('.datepicker-years tfoot .today').is(':visible'), 'Today button visible');
});

test('Today Button: data-api', function(){
    var input = $('<input data-date-today-btn="true" />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd'
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':visible'), 'Days view visible');
        ok(picker.find('.datepicker-days tfoot .today').is(':visible'), 'Today button visible');
});

test('Today Button: moves to today\'s date', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    todayBtn: true
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':visible'), 'Days view visible');
        ok(picker.find('.datepicker-days tfoot .today').is(':visible'), 'Today button visible');

        target = picker.find('.datepicker-days tfoot .today');
        target.click();

        var d = new Date(),
            today = UTCDate(d.getFullYear(), d.getMonth(), d.getDate());
        datesEqual(dp.viewDate, today);
        datesEqual(dp.dates[0], UTCDate(2012, 2, 5));
});

test('Today Button: "linked" selects today\'s date', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    todayBtn: "linked"
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':visible'), 'Days view visible');
        ok(picker.find('.datepicker-days tfoot .today').is(':visible'), 'Today button visible');

        target = picker.find('.datepicker-days tfoot .today');
        target.click();

        var d = new Date(),
            today = UTCDate(d.getFullYear(), d.getMonth(), d.getDate());
        datesEqual(dp.viewDate, today);
        datesEqual(dp.dates[0], today);
});

test('Today Highlight: today\'s date is not highlighted by default', patch_date(function(Date){
    Date.now = UTCDate(2012, 2, 15);
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd'
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':visible'), 'Days view visible');
        equal(picker.find('.datepicker-days thead .datepicker-switch').text(), 'March 2012', 'Title is "March 2012"');

        target = picker.find('.datepicker-days tbody td:contains(15)');
        ok(!target.hasClass('today'), 'Today is not marked with "today" class');
        target = picker.find('.datepicker-days tbody td:contains(14)');
        ok(!target.hasClass('today'), 'Yesterday is not marked with "today" class');
        target = picker.find('.datepicker-days tbody td:contains(16)');
        ok(!target.hasClass('today'), 'Tomorrow is not marked with "today" class');
}));

test('Today Highlight: today\'s date is highlighted when not active', patch_date(function(Date){
    Date.now = new Date(2012, 2, 15);
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    todayHighlight: true
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':visible'), 'Days view visible');
        equal(picker.find('.datepicker-days thead .datepicker-switch').text(), 'March 2012', 'Title is "March 2012"');

        target = picker.find('.datepicker-days tbody td:contains(15)');
        ok(target.hasClass('today'), 'Today is marked with "today" class');
        target = picker.find('.datepicker-days tbody td:contains(14)');
        ok(!target.hasClass('today'), 'Yesterday is not marked with "today" class');
        target = picker.find('.datepicker-days tbody td:contains(16)');
        ok(!target.hasClass('today'), 'Tomorrow is not marked with "today" class');
}));

test('Clear Button: clear visibility when enabled', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    clearBtn: true
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':visible'), 'Days view visible');
        ok(picker.find('.datepicker-days tfoot .clear').is(':visible'), 'Clear button visible');

        picker.find('.datepicker-days thead th.datepicker-switch').click();
        ok(picker.find('.datepicker-months').is(':visible'), 'Months view visible');
        ok(picker.find('.datepicker-months tfoot .clear').is(':visible'), 'Clear button visible');

        picker.find('.datepicker-months thead th.datepicker-switch').click();
        ok(picker.find('.datepicker-years').is(':visible'), 'Years view visible');
        ok(picker.find('.datepicker-years tfoot .clear').is(':visible'), 'Clear button visible');
});

test('Clear Button: clears input value', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    clearBtn: true
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':visible'), 'Days view visible');
        ok(picker.find('.datepicker-days tfoot .clear').is(':visible'), 'Today button visible');

        target = picker.find('.datepicker-days tfoot .clear');
        target.click();

        equal(input.val(),'',"Input value has been cleared.")
        ok(picker.is(':visible'), 'Picker is visible');
});

test('Clear Button: hides datepicker if autoclose is on', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    clearBtn: true,
                    autoclose: true
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

        input.focus();
        ok(picker.find('.datepicker-days').is(':visible'), 'Days view visible');
        ok(picker.find('.datepicker-days tfoot .clear').is(':visible'), 'Today button visible');

        target = picker.find('.datepicker-days tfoot .clear');
        target.click();

        equal(input.val(),'',"Input value has been cleared.");
        ok(picker.is(':not(:visible)'), 'Picker is hidden');

});

test('DaysOfWeekDisabled', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-10-26')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    daysOfWeekDisabled: '1,5'
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;


    input.focus();
    target = picker.find('.datepicker-days tbody td:nth(22)');
    ok(target.hasClass('disabled'), 'Day of week is disabled');
    target = picker.find('.datepicker-days tbody td:nth(24)');
    ok(!target.hasClass('disabled'), 'Day of week is enabled');
    target = picker.find('.datepicker-days tbody td:nth(26)');
    ok(target.hasClass('disabled'), 'Day of week is disabled');
});

test('BeforeShowDay', function(){

    var beforeShowDay = function(date) {
        var dateTime = UTCDate(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate()).getTime();
        var dateTime25th = UTCDate(2012, 9, 25).getTime();
        var dateTime26th = UTCDate(2012, 9, 26).getTime();
        var dateTime27th = UTCDate(2012, 9, 27).getTime();
        var dateTime28th = UTCDate(2012, 9, 28).getTime();

        if (dateTime == dateTime25th) {
            return {tooltip: 'A tooltip'};
        }
        else if (dateTime == dateTime26th) {
            return 'test26';
        }
        else if (dateTime == dateTime27th) {
            return {enabled: false, classes:'test27'};
        }
        else if (dateTime == dateTime28th) {
            return false;
        }
    };

    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-10-26')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    beforeShowDay: beforeShowDay
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

    input.focus();
    target = picker.find('.datepicker-days tbody td:nth(25)');
    equal(target.attr('title'), 'A tooltip', '25th has tooltip');
    ok(!target.hasClass('disabled'), '25th is enabled');
    target = picker.find('.datepicker-days tbody td:nth(26)');
    ok(target.hasClass('test26'), '26th has test26 class');
    ok(!target.hasClass('disabled'), '26th is enabled');
    target = picker.find('.datepicker-days tbody td:nth(27)');
    ok(target.hasClass('test27'), '27th has test27 class');
    ok(target.hasClass('disabled'), '27th is disabled');
    target = picker.find('.datepicker-days tbody td:nth(28)');
    ok(target.hasClass('disabled'), '28th is disabled');
    target = picker.find('.datepicker-days tbody td:nth(29)');
    ok(!target.hasClass('disabled'), '29th is enabled');
});

test('Orientation: values are parsed correctly', function(){

    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-10-26')
                .datepicker({
                    format: 'yyyy-mm-dd'
                }),
        dp = input.data('datepicker');

    equal(dp.o.orientation.x, 'auto');
    equal(dp.o.orientation.y, 'auto');

    dp._process_options({orientation: ''});
    equal(dp.o.orientation.x, 'auto', 'Empty value');
    equal(dp.o.orientation.y, 'auto', 'Empty value');

    dp._process_options({orientation: 'left'});
    equal(dp.o.orientation.x, 'left', '"left"');
    equal(dp.o.orientation.y, 'auto', '"left"');

    dp._process_options({orientation: 'right'});
    equal(dp.o.orientation.x, 'right', '"right"');
    equal(dp.o.orientation.y, 'auto', '"right"');

    dp._process_options({orientation: 'top'});
    equal(dp.o.orientation.x, 'auto', '"top"');
    equal(dp.o.orientation.y, 'top', '"top"');

    dp._process_options({orientation: 'bottom'});
    equal(dp.o.orientation.x, 'auto', '"bottom"');
    equal(dp.o.orientation.y, 'bottom', '"bottom"');

    dp._process_options({orientation: 'left top'});
    equal(dp.o.orientation.x, 'left', '"left top"');
    equal(dp.o.orientation.y, 'top', '"left top"');

    dp._process_options({orientation: 'left bottom'});
    equal(dp.o.orientation.x, 'left', '"left bottom"');
    equal(dp.o.orientation.y, 'bottom', '"left bottom"');

    dp._process_options({orientation: 'right top'});
    equal(dp.o.orientation.x, 'right', '"right top"');
    equal(dp.o.orientation.y, 'top', '"right top"');

    dp._process_options({orientation: 'right bottom'});
    equal(dp.o.orientation.x, 'right', '"right bottom"');
    equal(dp.o.orientation.y, 'bottom', '"right bottom"');

    dp._process_options({orientation: 'left right'});
    equal(dp.o.orientation.x, 'left', '"left right"');
    equal(dp.o.orientation.y, 'auto', '"left right"');

    dp._process_options({orientation: 'right left'});
    equal(dp.o.orientation.x, 'right', '"right left"');
    equal(dp.o.orientation.y, 'auto', '"right left"');

    dp._process_options({orientation: 'top bottom'});
    equal(dp.o.orientation.x, 'auto', '"top bottom"');
    equal(dp.o.orientation.y, 'top', '"top bottom"');

    dp._process_options({orientation: 'bottom top'});
    equal(dp.o.orientation.x, 'auto', '"bottom top"');
    equal(dp.o.orientation.y, 'bottom', '"bottom top"');

    dp._process_options({orientation: 'foo bar'});
    equal(dp.o.orientation.x, 'auto', '"foo bar"');
    equal(dp.o.orientation.y, 'auto', '"foo bar"');

    dp._process_options({orientation: 'foo left'});
    equal(dp.o.orientation.x, 'left', '"foo left"');
    equal(dp.o.orientation.y, 'auto', '"foo left"');

    dp._process_options({orientation: 'top bar'});
    equal(dp.o.orientation.x, 'auto', '"top bar"');
    equal(dp.o.orientation.y, 'top', '"top bar"');
});

test('startDate', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-10-26')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    startDate: new Date(2012, 9, 26)
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

    input.focus();
    target = picker.find('.datepicker-days tbody td:nth(25)');
    ok(target.hasClass('disabled'), 'Previous day is disabled');
    target = picker.find('.datepicker-days tbody td:nth(26)');
    ok(!target.hasClass('disabled'), 'Specified date is enabled');
    target = picker.find('.datepicker-days tbody td:nth(27)');
    ok(!target.hasClass('disabled'), 'Next day is enabled');
});

test('endDate', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-10-26')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    endDate: new Date(2012, 9, 26)
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

    input.focus();
    target = picker.find('.datepicker-days tbody td:nth(25)');
    ok(!target.hasClass('disabled'), 'Previous day is enabled');
    target = picker.find('.datepicker-days tbody td:nth(26)');
    ok(!target.hasClass('disabled'), 'Specified date is enabled');
    target = picker.find('.datepicker-days tbody td:nth(27)');
    ok(target.hasClass('disabled'), 'Next day is disabled');
});

test('Multidate', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    multidate: true
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

    input.focus();

    // Initial value is selected
    ok(dp.dates.contains(UTCDate(2012, 2, 5)) !== -1, '2012-03-05 (initial date) in dates');

    // Select first
    target = picker.find('.datepicker-days tbody td:nth(7)');
    equal(target.text(), '4'); // Mar 4

    target.click();
    datesEqual(dp.dates.get(-1), UTCDate(2012, 2, 4), '2012-03-04 in dates');
    datesEqual(dp.viewDate, UTCDate(2012, 2, 4));
    equal(input.val(), '2012-03-05,2012-03-04');

    // Select second
    target = picker.find('.datepicker-days tbody td:nth(15)');
    equal(target.text(), '12'); // Mar 12

    target.click();
    datesEqual(dp.dates.get(-1), UTCDate(2012, 2, 12), '2012-03-12 in dates');
    datesEqual(dp.viewDate, UTCDate(2012, 2, 12));
    equal(input.val(), '2012-03-05,2012-03-04,2012-03-12');

    // Deselect first
    target = picker.find('.datepicker-days tbody td:nth(7)');
    equal(target.text(), '4'); // Mar 4

    target.click();
    ok(dp.dates.contains(UTCDate(2012, 2, 4)) === -1, '2012-03-04 no longer in dates');
    datesEqual(dp.viewDate, UTCDate(2012, 2, 4));
    equal(input.val(), '2012-03-05,2012-03-12');
});

test('Multidate with limit', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    multidate: 2
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

    input.focus();

    // Initial value is selected
    ok(dp.dates.contains(UTCDate(2012, 2, 5)) !== -1, '2012-03-05 (initial date) in dates');

    // Select first
    target = picker.find('.datepicker-days tbody td:nth(7)');
    equal(target.text(), '4'); // Mar 4

    target.click();
    datesEqual(dp.dates.get(-1), UTCDate(2012, 2, 4), '2012-03-04 in dates');
    datesEqual(dp.viewDate, UTCDate(2012, 2, 4));
    equal(input.val(), '2012-03-05,2012-03-04');

    // Select second
    target = picker.find('.datepicker-days tbody td:nth(15)');
    equal(target.text(), '12'); // Mar 12

    target.click();
    datesEqual(dp.dates.get(-1), UTCDate(2012, 2, 12), '2012-03-12 in dates');
    datesEqual(dp.viewDate, UTCDate(2012, 2, 12));
    equal(input.val(), '2012-03-04,2012-03-12');

    // Select third
    target = picker.find('.datepicker-days tbody td:nth(20)');
    equal(target.text(), '17'); // Mar 17

    target.click();
    datesEqual(dp.dates.get(-1), UTCDate(2012, 2, 17), '2012-03-17 in dates');
    ok(dp.dates.contains(UTCDate(2012, 2, 4)) === -1, '2012-03-04 no longer in dates');
    datesEqual(dp.viewDate, UTCDate(2012, 2, 17));
    equal(input.val(), '2012-03-12,2012-03-17');
});

test('Multidate Separator', function(){
    var input = $('<input />')
                .appendTo('#qunit-fixture')
                .val('2012-03-05')
                .datepicker({
                    format: 'yyyy-mm-dd',
                    multidate: true,
                    multidateSeparator: ' '
                }),
        dp = input.data('datepicker'),
        picker = dp.picker,
        target;

    input.focus();

    // Select first
    target = picker.find('.datepicker-days tbody td:nth(7)');
    equal(target.text(), '4'); // Mar 4

    target.click();
    equal(input.val(), '2012-03-05 2012-03-04');

    // Select second
    target = picker.find('.datepicker-days tbody td:nth(15)');
    equal(target.text(), '12'); // Mar 12

    target.click();
    equal(input.val(), '2012-03-05 2012-03-04 2012-03-12');
});
