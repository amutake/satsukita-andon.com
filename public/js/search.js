(function() {

	var insert = function(data) {

		var $ul = $("<ul>").addClass("thumbnails");

		data.map(function(datum) {

			var $img = $("<img>").attr({
				src: "/assets/" + datum.thumbnail
			});

			var $detail = $("<div>").text(
				datum.times + " " + datum.grade + "-" + datum.classn + " " + datum.title
			).addClass("img-detail");

			var $a = $("<a>").attr({
				href: "/gallery/" + datum.times + "/" + datum.grade + "/" + datum.classn
			}).append($img).append($detail);

			var $li = $("<li>").append($a).addClass("span4");

			$ul.append($li);
		});

		$("#search-result").empty().append($ul);
	};

	$("#search-submit").click(function() {

		$loading = $("<img>").attr({
			src: "/assets/img/loading.gif",
			width: 64,
			height: 64
		}).css({
			position: "fixed",
			top: $("#browser").height() / 2 - 32,
			left: $("#browser").width() / 2 - 32
		});

		$("#search-result").empty().append($loading);

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
