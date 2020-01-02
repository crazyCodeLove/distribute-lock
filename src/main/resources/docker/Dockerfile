FROM java:8

ADD dis-lock.jar ./

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
  && echo 'Asia/Shanghai' > /etc/timezone

ENV LANG en_US.utf8

ENTRYPOINT java $JAVA_OPTS -jar  dis-lock.jar
