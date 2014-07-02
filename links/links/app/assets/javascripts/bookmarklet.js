window.onload = function () {
	initMagicSuggest(document.getElementById('tags_string_list').getAttribute('value').split(', '));
	document.getElementById("cancel_bookmark").onclick = function() {window.close();};
};



