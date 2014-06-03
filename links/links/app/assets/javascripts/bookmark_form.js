var ms_tags;
function initMagicSuggest(prepopulatedValues) {
  ms_tags = $('#ms_tags').magicSuggest({
    	minChars: 2,
      data: [],
		  value: prepopulatedValues,  		
  		highlight: false       
  });
}
