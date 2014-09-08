#!/bin/bash

echo "Enter the complete url at which links is going to be hosted"
read url

TMPFILE="`mktemp`"
TOOLSFILE="app/views/pages/tools.html.erb" # Assuming that we are in the 'links' app directory
sed "s@http://w.fieldsofview.in:3000@$url@g" $TOOLSFILE > $TMPFILE
mv -f $TMPFILE $TOOLSFILE

echo "Installed succesfully"
