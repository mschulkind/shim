FROM ubuntu:13.04

RUN apt-get update
RUN apt-get -y install tomcat7 tomcat7-admin mongodb
ADD docker/run.sh /root/
ADD docker/tomcat-users.xml /etc/tomcat7/
ADD docker/server.xml /etc/tomcat7/
ADD docker/logging.properties /etc/tomcat7/
RUN ln -s /data/omh.war /var/lib/tomcat7/webapps/
RUN ln -s /data/omh.conf /etc/omh.conf

CMD trap exit TERM; /root/run.sh & wait
