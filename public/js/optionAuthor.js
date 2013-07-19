$(function() {
	var $checkbox = $("#option");
	var $author = $("#option-author");
	var $date = $("#option-date");

	var change = function() {
		check = $checkbox.is(":checked");
		if (check) {
			$author.removeAttr("disabled");
			$date.removeAttr("disabled");
		} else {
			$author.attr("disabled", "disabled");
			$date.attr("disabled", "disabled");
		}
	};

	$checkbox.change(change);
	change();
});
