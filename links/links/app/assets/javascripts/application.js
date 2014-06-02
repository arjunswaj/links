// This is a manifest file that'll be compiled into application.js, which will include all the files
// listed below.
//
// Any JavaScript/Coffee file within this directory, lib/assets/javascripts, vendor/assets/javascripts,
// or vendor/assets/javascripts of plugins, if any, can be referenced here using a relative path.
//
// It's not advisable to add code directly here, but if you do, it'll appear at the bottom of the
// compiled file.
//
// Read Sprockets README (https://github.com/sstephenson/sprockets#sprockets-directives) for details
// about supported directives.
//
//= require jquery
//= require jquery_ujs
//= require twitter/bootstrap
//= require magicsuggest
//= require turbolinks
//= require_tree .
$(document).ready(function() {

	$("#bookmark_url_attributes_url").val('');
	$("#bookmark_title").val('');
	$("#bookmark_description").val('');

	$("#bookmark_url_attributes_url").focus();

	$("#bookmark_url_attributes_url").focusout(function() {
		$.get('http://localhost:3000/annotations?url=' + $(this).val(), function(data) {
			$("#bookmark_title").val(data);
		});
		$("#bookmark_description").val("Extracted description ");
	});
});
