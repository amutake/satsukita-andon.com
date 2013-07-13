$(function() {

	$.each($(".article"), function(i, article) {
		var $article = $(article);

		$article.find("table").addClass("table table-bordered table-striped");

		$article.find("code").css({
			backgroundColor: "transparent",
			color: "white",
			border: "none"
		});
		$article.find("pre").css({
			backgroundColor: "rgba(255, 255, 255, 0.1)",
			border: "none"
		});

		$article.find("ul li").css({
			listStyleType: "disc"
		});
		$article.find("ol li").css({
			listStyleType: "decimal"
		});

		$article.find("a:has(img)").addClass("fresco").attr("data-fresco-group", "article-" + i);
		$article.find("a img").attr("width", "210px");
		$article.find("ul:has(img)").addClass("thumbnails");
		$article.find("ul li:has(img)").addClass("span3").css({
			listStyleType: "none"
		});
	});
});

$(function() {

	$.fn.extend({
		insertAtCaret: function(text) {
			$.each(this, function(i, textarea) {
				textarea.focus();
				if ($.support.msie) {
					var range = document.selection.createRange();
					range.text = text;
					range.select();
				} else {
					var s = textarea.value;
					var p = textarea.selectionStart;
					var np = p + text.length;
					textarea.value = s.substr(0, p) + text + s.substr(p);
					textarea.setSelectionRange(np, np);
				}
			});
		}
	});

	var insert = function(path) {
		$img = $("<img>").attr({
			src: path,
			width: "210px"
		});
		$button = $("<button>").text("この画像を挿入").click(function() {
			var anchor = "[![" + path + "](" + path + ")](" + path + ")\n";
			$("textarea").insertAtCaret(anchor);
		});
		$caption = $("<p>").text(path).append($img).append($button).css({
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
						insert(data.path);
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
