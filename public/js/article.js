$(function() {

	$.each($(".article"), function(i, article) {
		var $article = $(article);

		$article.find("table").addClass("table table-bordered table-striped");

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

	var insert = function(path, thumbnail) {
		$img = $("<img>").attr({
			src: thumbnail
		});
		$button = $("<button>").text("この画像を挿入").click(function() {
			var anchor = "[![" + path + "](" + thumbnail + ")](" + path + ")\n";
			$("textarea").insertAtCaret(anchor);
		});
		$caption = $("<li>").text(path).append($img).append($button).addClass("span3");

		$(".fileupload-wrapper").append($caption);
	};

	var $input = $(".fileupload-input");

	var $submit = $(".fileupload-submit");

	$submit.click(function() {

		$.each($input[0].files, function(i, file) {
			$loading = $("<img>").attr({
				src: "/assets/img/loading.gif",
				width: 64,
				height: 64,
				id: "loading-" + i
			});
			$(".fileupload-wrapper").append($loading);

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
					$("#loading-" + i).remove();
					if (data.status === "success") {
						insert(data.path, data.thumbnail);
					} else {
						window.alert("エラー。" + data.message);
					}
				},
				error: function() {
					$("#loading-" + i).remove();
					window.alert("エラー。もう一度送信してください。");
				}
			});
		});
	});
});
