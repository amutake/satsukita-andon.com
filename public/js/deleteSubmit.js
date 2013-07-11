$(function() {
	$(".delete-submit").click(function() {
		var ok = window.confirm("アカウントを削除します。よろしいですか?");
		if (ok) {
			$(".delete-form").submit();
		}
	});
});
