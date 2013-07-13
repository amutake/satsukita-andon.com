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

$(function() {

	var insert = function(path) {
		$img = $("<img>").attr({
			src: path,
			width: "210px"
		});
		$caption = $("<p>").text(path).append($img).css({
			padding: "10px"
		});

		$(".fileupload-wrapper").append($caption);
	};

	var $input = $(".fileupload-input");

	var $submit = $(".fileupload-submit");

	$submit.click(function() {

		$.each($input[0].files, function(_, file) {
			var fd = new FormData();
			fd.append("file", file);
			$.ajax({
				type: "POST",
				url: "/api/upload",
				data: fd,
				dataType: "json",
				contentType: false,
				processData: false,
				success: function(data) {
					if (data.status === "success") {
						setTimeout(function() {
							insert(data.path);
						}, 1000);
					} else {
						window.alert("エラー。" + data.message);
					}
				},
				error: function() {
					window.alert("エラー。もう一度送信してください。");
				}
			});
		});
	});
});
