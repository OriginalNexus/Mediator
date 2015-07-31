
var VERSION = "1.2.0"
var VERSION_DISPLAY = "Mediator v1.2.0"
var FILE_PATH = "./download/Mediator_v1.2.0.apk"

function update() {
	var btns = document.getElementsByClassName("dl-btn")
	for (var i = 0; i < btns.length; i++) {
		btns[i].innerHTML = '<span class="glyphicon glyphicon-download-alt"></span> ' + VERSION_DISPLAY;
		btns[i].href = FILE_PATH;
	};
	btns = document.getElementsByClassName("dl-btn-nav")
	for (var i = 0; i < btns.length; i++) {
		btns[i].innerHTML = '<span class="glyphicon glyphicon-download-alt"></span> Download ' + VERSION_DISPLAY;
		btns[i].href = FILE_PATH;
	};
}