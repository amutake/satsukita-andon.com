$(function() {
	var imgNum = 17;
	var mkImg = function($img, index, speed, opacity) {
		return {
			img: $img,
			index: index,
			zIndex: -index * 100,
			speed: speed,
			opacity: opacity
		};
	};
	var mkSpeed = function(n) {
		return ((imgNum - n + 1) * 0.1) * 0.8 + 0.2;
	};
	var mkOpacity = function(n) {
		return (-0.03 * n) + 1;
	};

	$imgs = [];
	for (var i = 1; i <= imgNum; i++) {
		$imgs.push(mkImg($("#background-" + i), i, mkSpeed(i), mkOpacity(i)));
	}

	var $browser = $("#browser");
	var width = $browser.width();
	var height = $browser.height();

	var random = function(n) {
		return Math.floor(Math.random() * n + 1);
	};

	$imgs.map(function($img) {
		$img.img.css({
			position: "fixed",
			left: random(width - 200),
			top: random(height * 2),
			opacity: $img.opacity,
			zIndex: $img.zIndex
		});
	});

	var go = function($imgs) {
		var next = function(top, speed) {
			var n = parseInt(top);
			if (n < -300) {
				if (Math.random() < 0.01) {
					return {
						left: random(width - 200),
						top: height
					};
				} else {
					return {};
				}
			} else {
				return {
					top: "-=" + speed + "px"
				};
			}
		};

		setInterval(function() {
			$imgs.map(function($img) {
				$img.img.css(next($img.img.css("top"), $img.speed));
			})
		}, 100);
	};

	go($imgs);
});
