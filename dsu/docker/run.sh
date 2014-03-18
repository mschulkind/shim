#!/bin/bash

/etc/init.d/tomcat7 start
mkdir /data/db
mongod --fork --logpath /var/log/mongodb.log
tail -f /var/log/tomcat7/* /var/log/mongodb.log
