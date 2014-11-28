#!/bin/bash
cd /var/log/nginx;
while true
do 
    X=$(grep '() {' access.log)
	clear
	D=date
	echo $X | mail -s $D "sakagemann@gmail.com"    
	sleep 1200
done

