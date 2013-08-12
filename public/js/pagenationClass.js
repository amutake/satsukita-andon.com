$(function() {
	var change = function() {
		var times = $("#select-times option:selected").val();
		location.href = "?times=" + times;
	}
	$("#select-times").change(change);
});
