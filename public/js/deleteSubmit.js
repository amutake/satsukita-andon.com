$(function() {
	$(".delete-submit").click(function() {
		var ok = window.confirm("削除します。よろしいですか?");
		if (ok) {
			$(this).parent("form").submit();
		}
		return false;
	});
});
