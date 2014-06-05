$(function() {
    $(document).on('click', '.tagname', function(event) {
        var searchKey = $(event.target).text();
		$('#keyword').val(searchKey);
		$('#search-form').submit();
    })
});