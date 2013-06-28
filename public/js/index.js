$(function() {
	var $logo = $("#index-logo").hide();
	$logo.css({
		marginTop: $("#browser").height() / 2 - 160
	});
	var id = setInterval(function() {
		$logo.fadeIn(3000);
		clearInterval(id);
	}, 1500);
});
