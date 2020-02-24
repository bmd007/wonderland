FROM adoptopenjdk/openjdk11

MAINTAINER "Mohammad Mahdi Amini 'bmd579@gmail.com'"

ADD build/libs/message-search.jar /message-search.jar

ENV JAVA_OPTS ""

ENTRYPOINT ["sh", "-c", "exec java -jar $JAVA_OPTS /message-search.jar"]

EXPOSE 8081
