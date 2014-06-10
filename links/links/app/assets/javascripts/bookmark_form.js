var ms_tags;
function initMagicSuggest(prepopulatedValues) {
  ms_tags = $('#ms_tags').magicSuggest({
   	minChars: 2,
   	style: 'padding: 0px',
    data: [],
	value: prepopulatedValues,  		
  	highlight: false       
  });
}

$(function() {
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
        if($(window).scrollTop() == $(document).height() - $(window).height())
        {
          // Infinite Timeline for Homepage
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
            },
            error: function(html) {
              
            }
            });
          }
        }
    });
});