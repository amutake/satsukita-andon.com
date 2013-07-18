$(function() {
	$(".delete-submit").click(function() {
		var ok = window.confirm("削除すると復元出来ません。よろしいですか?");
		if (ok) {
			$(this).parent("form").submit();
		}
		return false;
	});
});
