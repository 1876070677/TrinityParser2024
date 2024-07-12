FROM tomcat:9.0.89-jdk17

RUN ["rm", "/etc/localtime"]
RUN ["ln", "-sf", "/usr/share/zoneinfo/Asia/Seoul", "/etc/localtime"]

COPY ./target/gajang-backend-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

#start tomcat
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]