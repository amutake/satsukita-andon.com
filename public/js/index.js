$(function() {
    var $logo = $("#index-logo").hide();
    var margin = $("#browser").height() / 2 - 160;
	$logo.css({
	    marginTop: margin,
        marginBottom: margin
	});
	$logo.fadeIn(5000);
});
