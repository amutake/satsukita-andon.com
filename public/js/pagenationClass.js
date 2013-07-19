$(function() {

	var insert = function(data) {
		var $table = $(".table");
		$table.find("tr:gt(0)").remove();

		$.each(data, function(i, c) {
			var $tr = $("<tr>");
			var td = function(t) {
				return $("<td>").text(t);
			};
			var $img = $("<img>").attr({
				src: c.thumbnail,
				width: "200px"
			});
			var $a = $("<a>").addClass("fresco").attr({
				href: c.fullsize,
				"data-fresco-group": c.times
			}).append($img);

			var $btn = function(href, text) {
				return $("<a>").addClass("btn").attr("href", href).text(text);
			};
			$tr.append($("<td>").append($a))
				.append(td(c.times))
				.append(td(c.grade))
				.append(td(c.classn))
				.append(td(c.title))
				.append(td(c.prize))
				.append($("<td>").append(
					$btn("/artisan/gallery/upload?id=" + c.id, "画像のアップロード")
				).append(
					$btn("/artisan/gallery/select?id=" + c.id, "トップ画像の選択")
				).append(
					$btn("/artisan/gallery/delete?id=" + c.id, "画像の削除")
				).append(
					$btn("/artisan/classdata/edit?id=" + c.id, "クラス情報の編集")
				));
			$table.append($tr);
		});
	};

	var change = function() {

		var times = $("#select-times option:selected").val();

		var data = {
			times: times
		}

		$.ajax({
			type: "GET",
			url: "/api/search/times",
			data: data,
			success: insert,
			error: function() {
				alert("Error");
			}
		});
	}

	$("#select-times").change(change);
	change();
});
