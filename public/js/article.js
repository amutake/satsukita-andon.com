$(function() {
	$(".article table").addClass("table table-bordered table-striped");

	$(".article code").css({
		backgroundColor: "transparent",
		color: "white",
		border: "none"
	});
	$(".article pre").css({
		backgroundColor: "rgba(255, 255, 255, 0.1)",
		border: "none"
	});

	$(".article ul li").css({
		listStyleType: "disc"
	});
	$(".article ol li").css({
		listStyleType: "decimal"
	});
});
