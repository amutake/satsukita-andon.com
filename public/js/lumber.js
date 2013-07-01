//
// 2011年に書いたのでカスコードです
//

function numberOfLumber() {
	/*変数宣言*/
	var num = document.getElementById('num').value;
	var cut = document.getElementById('cut');
	/*numをnumLumberに*/
	cut.innerHTML = '<p>カット後の木材 : <span id="numLumber">' + num + '</span>本';
	/*初期化*/
	var fld = document.getElementById('inputField');
	fld.innerHTML = '';
	initialize();
	/*numの数だけinput要素を描きだす*/
	makeInputField(num);
}

/*numの数だけinput要素を描きだす関数*/
function makeInputField(num) {
	var fld = document.getElementById('inputField');
	for (var i = 0; i < num; i++) {
		fld.innerHTML += '名前:<input type="text" id="name_' + i + '" size="10"> 長さ:<input type="number" min="1" max="3650" id="length_' + i + '" size="10">mm<br>';
		// 本数:<input type="text" id="num_' + i + '" value="1" size="2">
	}
	fld.innerHTML += '<input type="button" value="送信" onClick="checkResult()">';
}
function initialize() {
	var result = document.getElementById('result');
	result.innerHTML = '<p id="numOrder"></p>';
	var ord = document.getElementById('numOrder');
	ord.innerHTML = '';
}

/*入力された長さをチェックする関数*/
function check() {
	var num = document.getElementById('numLumber').innerHTML;
	var chk = 1;
	for (var i = 0; i < num; i++) {
		var value = document.getElementById('length_' + i).value;
		if (value < 1 || 3650 < value) {
			chk = 0;
		}
	}
	return chk;
}
function checkResult() {
	var chk = check();
	if (chk == 0) {
		var ord = document.getElementById('numOrder');
		ord.innerHTML = '長さは1mm以上3650mm以下で入力してください';
	} else {
		main();
	}
}


function bubbleSort(target) {
	var temp;
	for (i = 0; i < target.length - 1; i++) {
		for (j = target.length - 1; j > i; j--) {
			if (target[j][1] > target[j - 1][1]) {
				temp = target[j - 1];
				target[j - 1] = target[j];
				target[j] = temp;
			}
		}
	}
	return target;
}

function strage(target) {
	for (var i = 0; i < target.length; i++) {
		target[i][0] = document.getElementById('name_' + i).value;
		target[i][1] = document.getElementById('length_' + i).value;
		target[i][1] -= 0;
	}
	return target;
}
function makeCopy(num, target) {
	/*lumberのコピーを作る*/
	var copy = new Array(num);
	for (i = 0; i < num; i++) {
		copy[i] = new Array(2);
	}
	for (i = 0; i < target.length; i++) {
		copy[i][0] = target[i][0];
		copy[i][1] = target[i][1];
	}
	return copy;
}
function main() {
	/*準備*/
	var num = document.getElementById('numLumber').innerHTML;

	initialize();
	var ord = document.getElementById('numOrder');
	/*配列を作る*/
	var lumber = new Array(num);
	for (var i = 0; i < num; i++) {
		lumber[i] = new Array(2);
	}
	/*配列に値を格納*/
	lumber = strage(lumber);
	/*値をソート*/
	bubbleSort(lumber);
	/*コピーを作る*/
	//var copy = makeCopy(num, lumber);
	/*countに発注量を格納*/
	var count = order(num, lumber);
	ord.innerHTML += '発注量は' + count + '本です<br>';

}

function order(num, lumber) {
	/*カット前の木材を初期化*/
	var materials = new Array(num);
	for (var i = 0; i < num; i++) {
		materials[i] = 3650;
	}

	var count = 0;
	var times = 0;
	for (var i = 0; i < materials.length; i++) {

		var matBox = document.createElement('div');
		times = 0;

		for (var j = 0; j < lumber.length; j++) {
			if (materials[i] >= lumber[j][1] && lumber[j][1] != 0) {
				materials[i] -= lumber[j][1];


				var cutBox = document.createElement('div');
				var cutBoxName = document.createTextNode(' ' + lumber[j][0] + ' : ');
				var cutBoxLength = document.createTextNode(lumber[j][1] + 'mm');
				cutBox.style.height = '18px';
				cutBox.style.margin = '0';
				cutBox.style.width = ((lumber[j][1] / 4) - 2) + 'px';
				cutBox.style.cssFloat = 'left';
				cutBox.style.backgroundColor = '#996600';
				cutBox.style.border = '1px #000000 solid';
				matBox.appendChild(cutBox);
				cutBox.appendChild(cutBoxName);
				cutBox.appendChild(cutBoxLength);

				times++;

				lumber[j][1] = 0;
			}
		}
		if (materials[i] != 3650) {
			count++;

			matBox.style.clear = 'both';
			matBox.style.height = '20px';
			matBox.style.width = '912px';
			matBox.style.margin = '30px';
			matBox.style.border = '1px #ff3333 solid';
			result.appendChild(matBox);

		} else {
			break;
		}
	}
	return count;
}

/*カット方法を描きだす関数*/
function cutMethod() {

}
