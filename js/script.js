
var VERSION = "v1.3.0"
var FILE_NAME = "Mediator-" + VERSION +  ".apk"

$(document).ready(function() {
	var downloadButton = $(".download-btn")
	downloadButton.html('<span class="glyphicon glyphicon-download-alt"></span> ' + FILE_NAME);
	downloadButton.attr("href", "https://github.com/OriginalNexus/Mediator/releases/download/" + VERSION + "/" + FILE_NAME);

	$(".version").html(VERSION);
});
