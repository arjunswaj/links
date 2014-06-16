$(function() {
    $(document).on('click', '.tagname', function(event) {
        var searchKey = $(event.target).text();
		$('#keyword').val(searchKey);
		$('#search-form').submit();
 });
});


$(function() {
  var infiniteScrollRequestMade = false;
    $(document).on('click', '#cancel_bookmark', function(event) {
      $("#overlay").remove();
		  $('.timeline').prepend('<div id="overlay" class="overlay-invisible"></div>');
		  $("#url").val("");
    });

    $(document).on('click', '#cancel_share_bookmark', function(event) {
      $("#overlay").remove();
      $('.timeline').prepend('<div id="overlay" class="overlay-invisible"></div>');
      $("#url").val("");
    });

    $(window).scroll(function()
    {      
        if(($(window).scrollTop() == $(document).height() - $(window).height()) && !infiniteScrollRequestMade) {
          // Infinite Timeline for Homepage
          infiniteScrollRequestMade = true;
          if ($(location).attr('pathname') == "" 
            || $(location).attr('pathname') == "/timeline" 
            || $(location).attr('pathname') == "/" ) {            

            var resourceURL = "/loadmore"
            if ($('#keyword').val()) {
              resourceURL = "/searches/searchmore"  
            }
            $('#loadmoreajaxloader').show();
            $.ajax({
            url: resourceURL,
            success: function(html)
            {                
                $('#loadmoreajaxloader').hide();
                infiniteScrollRequestMade = false;
            },
            error: function(html) {
              
            }
            });
          }
        }
    });
});