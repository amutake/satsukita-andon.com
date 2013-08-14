$(function() {

	var acc = []; // acc: List[(Long, Boolean, String)]

	var deleteTag = function() {
		var id = $(this).attr("tag-id") * 1;
		var name = $(this).parent().text().substring(2);
		if (isNaN(id)) {
			// search and delete
			var n = acc.map(function(tag) {
				return tag.name;
			}).indexOf(name);

			acc.splice(n, 1);
		} else {
			acc.push({
				id: id,
				type: "delete",
				name: name
			});
		}

		$(this).parent().fadeOut(400, function() {
			this.remove();
		});
	};

	var addTag = function(tag) {
		var $del = $("<span>").addClass("delete-tag").attr("href", "#").text("×").click(deleteTag);
		var $li = $("<li>").addClass("flat-btn").append($del).append(" " + tag);

		acc.push({
			id: 0,
			type: "add",
			name: tag
		});

		$(".current-tags").append($li);
	};

	$(".input-tag").keypress(function(e) {
		if ($(this).val() !== "" && e.which === 13) { // if (KEY === ENTER) {
			var value = $(this).val();
			addTag(value);
			$(this).val("");
		}
	});

	$(".delete-tag").click(deleteTag);

	$(".other-tags li").click(function() {
		var value = $(this).text();
		addTag(value);
		return false;
	});

	$("#tags-form .btn").click(function(e) {
		e.preventDefault();

		$form = $("#tags-form");

		var input = function(name, value) {
			return $("<input>").attr({
				type: "hidden",
				name: name,
				value: value
			});
		};

		$.each(acc, function(i, tag) {
			$form.append(input("tags[" + i + "].id", tag.id))
				.append(input("tags[" + i + "].type", tag.type))
				.append(input("tags[" + i + "].name", tag.name))
		});

		$form.submit();
	});
});
