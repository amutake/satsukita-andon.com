$("#search-submit").click(function() {
	var times = $("#search-times option:selected").val();
	var prize = $("#search-prize option:selected").val();
	var grade = $("#search-grade option:selected").val();
	var data = {
		times: times,
		prize: prize,
		grade: grade
	};
	console.log(data);
	$.ajax({
		type: "GET",
		url: "/api/search",
		data: data,
		success: function(data) {
			console.log(data);
			alert(data);
		},
		error: function() {
			alert("error");
		},
	});
});
