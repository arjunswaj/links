var ms1;
    $(document).ready(function() {
            ms1 = $('#ms').magicSuggest({
            	minChars: 2,
   				data: '/searches/search_user.json',   		
   				displayField: 'email',
   				valueField: 'email',
   				name: 'users',
   				highlight: false,
   				allowFreeEntries: false,
   				required: true
        });
    });
