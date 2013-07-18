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
