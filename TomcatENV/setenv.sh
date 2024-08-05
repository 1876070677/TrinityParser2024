# JMX 설정 추가
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote=true"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.port=9000"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.rmi.port=9000"
export CATALINA_OPTS="$CATALINA_OPTS -Djava.rmi.server.hostname=localhost"