FROM java:8

# Note:
# This image requires that the mesos libraries are mounted under /usr/local when run
# AND that logstash is available (somehow)


ADD ./build/docker/logstash-executor-dependencies.jar /tmp/logstash-executor-dependencies.jar
ADD ./build/docker/logstash-executor.jar /tmp/logstash-executor.jar
ADD ./build/resources/main/run_logstash.sh /tmp/run_logstash.sh

ENTRYPOINT ["java", "-Djava.library.path=/usr/local/lib","-jar", "/tmp/logstash-executor.jar"]
