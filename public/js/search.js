(function() {

	var insert = function(data) {

		var $ul = $("<ul>");

		data.map(function(datum) {

			var $img = $("<img>").attr({
				src: "/assets/" + datum.thumbnail,
				width: 280,
				height: 210
			});

			var $detail = $("<div>").addClass("img-detail").text(
				datum.times + " " + datum.grade + "-" + datum.classn + " " + datum.title
			);

			var $a = $("<a>").attr({
				href: "/gallery/" + datum.times + "/" + datum.grade + "/" + datum.classn
			}).append($img).append($detail);

			var $li = $("<li>").addClass("none-img").append($a);

			$ul.append($li);
		});

		$("#search-result").append($ul);
	};

	$("#search-submit").click(function() {

		$("#search-result").empty();

		var times = $("#search-times option:selected").val();
		var prize = $("#search-prize option:selected").val();
		var grade = $("#search-grade option:selected").val();
		var tag = $("#search-tag option:selected").val();

		var data = {
			times: times,
			prize: prize,
			grade: grade,
			tag: tag
		};

		$.ajax({
			type: "GET",
			url: "/api/search",
			data: data,
			success: insert,
			error: function() {
				alert("Error");
			},
		});
	});
})()
