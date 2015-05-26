$(function() {
	
	$('.tooltip').not('#welcome .tooltip').tooltipster({
		offsetY: 2,
	});
	$('#welcome .tooltip').tooltipster({
		offsetY: 2,
		theme: 'tooltipster-white'
	});
	$('#demo-default').tooltipster({
		offsetY: 2
	});
	$('#demo-html').tooltipster({
		content: $('<img src="doc/images/spiderman.png" width="50" height="50" /><p style="text-align:left;"><strong>Soufflé chocolate cake powder.</strong> Applicake lollipop oat cake gingerbread.</p>'),
		// setting a same value to minWidth and maxWidth will result in a fixed width
		minWidth: 300,
		maxWidth: 300,
		position: 'right'
	});
	$('#demo-theme').tooltipster({
		animation: 'grow',
		theme: 'tooltipster-pink'
	});
	$('#demo-callback').tooltipster({
		content: 'Loading...',
		updateAnimation: false,
		functionBefore: function(origin, continueTooltip) {
			continueTooltip();
			
			if (origin.data('ajax') !== 'cached') {
				
				$.jGFeed('http://ws.audioscrobbler.com/2.0/user/ce3ge/recenttracks.rss?',
					function(feeds){
						var content = '';
						if(!feeds){
							content = 'Woops - there was an error retrieving my last.fm RSS feed';
							origin.tooltipster('content', content);
						}
						else {
							content = $('<span>I last listened to: <strong>' + feeds.entries[0].title + '</strong></span>');
							origin
								.tooltipster('content', content)
								.data('ajax', 'cached');
						}
				}, 10);
				
				origin.data('ajax', 'cached');
			}
		},
		functionAfter: function(origin) {
			alert('The tooltip has closed!');
		}
	});
	$('#demo-events').tooltipster({
		trigger: 'click'
	});
	$(window).keypress(function() {
		$('#demo-events').tooltipster('hide');
	});
	$('#demo-interact').tooltipster({
		contentAsHTML: true,
		interactive: true
	});
	$('#demo-touch').tooltipster({
		touchDevices: false
	});
	$('#demo-icon').tooltipster({
		iconDesktop: true,
		iconTouch: true
	});
	$('#demo-multiple').tooltipster({
		animation: 'swing',
		content: 'North',
		multiple: true,
		position: 'top'
	});
	$('#demo-multiple').tooltipster({
		content: 'East',
		multiple: true,
		position: 'right',
		theme: 'tooltipster-punk'
	});	
	$('#demo-multiple').tooltipster({
		animation: 'grow',
		content: 'South',
		delay: 200,
		multiple: true,
		position: 'bottom',
		theme: 'tooltipster-light'
	});	
	$('#demo-multiple').tooltipster({
		animation: 'fall',
		content: 'West',
		multiple: true,
		position: 'left',
		theme: 'tooltipster-shadow'
	});	
	$('.tooltipster-light-preview').tooltipster({
		theme: 'tooltipster-light'
	});
	$('.tooltipster-punk-preview').tooltipster({
		theme: 'tooltipster-punk'
	});
	$('.tooltipster-noir-preview').tooltipster({
		theme: 'tooltipster-noir'
	});
	$('.tooltipster-shadow-preview').tooltipster({
		theme: 'tooltipster-shadow'
	});
	
	$('header select').change(function() {
		var goTo = $(this).val();
		var section = $('#'+goTo);
		var offset = section.offset().top;
		$('html, body').scrollTop(offset);
	});
	
	prettyPrint();
	
	
});